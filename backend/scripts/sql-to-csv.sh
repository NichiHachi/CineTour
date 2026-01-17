#!/bin/bash

# Script pour convertir un dump SQL en fichiers CSV pour neo4j-admin import
# Usage: ./sql-to-csv.sh <dump.sql.gz> [output_dir]

set -e

SQL_DUMP="${1:-}"
OUTPUT_DIR="${2:-./csv-export}"

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

if [[ -z "$SQL_DUMP" ]] || [[ "$SQL_DUMP" == "-h" ]] || [[ "$SQL_DUMP" == "--help" ]]; then
    echo "Usage: $0 <dump.sql.gz|dump.sql> [output_dir]"
    echo ""
    echo "Convertit un dump SQL MySQL en fichiers CSV pour neo4j-admin import"
    echo ""
    echo "Arguments:"
    echo "  dump.sql.gz    Fichier dump SQL (compressé ou non)"
    echo "  output_dir     Dossier de sortie (défaut: ./csv-export)"
    exit 0
fi

if [[ ! -f "$SQL_DUMP" ]]; then
    echo -e "${RED}Erreur: Fichier $SQL_DUMP introuvable${NC}"
    exit 1
fi

echo -e "${GREEN}=== Conversion SQL vers CSV pour Neo4j ===${NC}"
echo -e "${YELLOW}Dump SQL:${NC} $SQL_DUMP"
echo -e "${YELLOW}Dossier de sortie:${NC} $OUTPUT_DIR"
echo ""

# Créer le dossier de sortie
mkdir -p "$OUTPUT_DIR"

# Décompresser si nécessaire
TEMP_SQL="/tmp/cinetour-csv-conversion.sql"
if [[ "$SQL_DUMP" == *.gz ]]; then
    echo -e "${BLUE}📦 Décompression...${NC}"
    gunzip -c "$SQL_DUMP" > "$TEMP_SQL"
else
    cp "$SQL_DUMP" "$TEMP_SQL"
fi

echo -e "${BLUE}🔄 Conversion en cours...${NC}"

# Fonction pour extraire les données d'une table et les convertir en CSV
extract_table_to_csv() {
    local table_name=$1
    local output_file=$2
    local header=$3
    
    echo -e "${YELLOW}  → Extraction de ${table_name}...${NC}"
    
    # Écrire le header
    echo "$header" > "$output_file"
    
    # Extraire tout le bloc entre INSERT INTO et la prochaine commande SQL
    # Les données sont toutes les lignes qui commencent par (
    awk -v table="$table_name" '
        /INSERT INTO `'"$table_name"'` VALUES/ {in_block=1; next}
        in_block && /^INSERT INTO|^\/\*|^--|^$|^LOCK|^UNLOCK|^ALTER|^CREATE|^DROP/ {in_block=0}
        in_block && /^\(/ {print}
    ' "$TEMP_SQL" | \
    sed -E 's/\),\s*\(/|NEXT_ROW|(/g' | \
    sed -E 's/\);\s*\(/|NEXT_ROW|(/g' | \
    sed -E 's/\),$/|NEXT_ROW|/g' | \
    sed -E 's/\);$/|NEXT_ROW|/g' | \
    tr '\n' ' ' | \
    sed -E 's/\|NEXT_ROW\| /\n/g' | \
    sed -E 's/^\(//g' | \
    sed -E 's/\),?;?\s*$//g' | \
    sed -E "s/'NULL'/\\\\N/g" | \
    sed -E "s/,NULL,/,\\\\N,/g" | \
    sed -E "s/,NULL$/,\\\\N/g" | \
    sed -E "s/^NULL,/\\\\N,/g" | \
    sed -E 's/,0x[0-9A-Fa-f]+,/,\\N,/g' | \
    sed -E 's/,0x[0-9A-Fa-f]+$/,\\N/g' | \
    python3 -c "
import sys
import re

for line in sys.stdin:
    line = line.rstrip('\n')
    if not line:
        continue
        
    # Protéger les virgules dans les chaînes entre quotes
    parts = []
    pos = 0
    in_quote = False
    current = ''
    
    i = 0
    while i < len(line):
        if line[i] == \"'\" and (i == 0 or line[i-1] != '\\\\'):
            in_quote = not in_quote
            i += 1
            continue
        elif line[i] == ',' and not in_quote:
            parts.append(current)
            current = ''
            i += 1
        else:
            if line[i] == '\\\\' and i+1 < len(line) and line[i+1] == \"'\": 
                current += \"'\"
                i += 2
            else:
                current += line[i]
                i += 1
    
    if current:
        parts.append(current)
    
    if parts:
        # Remplacer les pipes dans les valeurs par des tirets pour éviter les conflits avec le séparateur CSV
        cleaned_parts = [p.replace('|', '-') if p else p for p in parts]
        print('|'.join(cleaned_parts))
" >> "$output_file"
    
    local count=$(wc -l < "$output_file")
    echo -e "${GREEN}    ✓ $((count - 1)) lignes${NC}"
}

# Movies (nodes)
extract_table_to_csv "movies" \
    "$OUTPUT_DIR/movies.csv" \
    "id:int|genres|id_imdb:ID(Movie)|image|location_search_count|locations_checked:boolean|movie_search_count|release_year|runtime_minutes|title"

# Genres (nodes) - Extraire tous les genres uniques
echo -e "${YELLOW}  → Extraction des genres...${NC}"
echo "name:ID(Genre)" > "$OUTPUT_DIR/genres.csv"
tail -n +2 "$OUTPUT_DIR/movies.csv" | \
    cut -d'|' -f2 | \
    grep -v "^$" | \
    grep -v "NULL" | \
    grep -v "\\\\N" | \
    tr ',' '\n' | \
    sort -u >> "$OUTPUT_DIR/genres.csv"
genre_count=$(wc -l < "$OUTPUT_DIR/genres.csv")
echo -e "${GREEN}    ✓ $((genre_count - 1)) genres uniques${NC}"

# Genre relations (HAS_GENRE)
echo -e "${YELLOW}  → Création des relations HAS_GENRE...${NC}"
echo ":START_ID(Movie)|:END_ID(Genre)" > "$OUTPUT_DIR/has_genre.csv"
tail -n +2 "$OUTPUT_DIR/movies.csv" | \
    awk -F'|' '{
        id_imdb = $3
        genres = $2
        if (genres != "" && genres != "NULL" && genres != "\\N") {
            n = split(genres, genre_array, ",")
            for (i = 1; i <= n; i++) {
                genre = genre_array[i]
                if (genre != "" && genre != "NULL" && genre != "\\N") {
                    print id_imdb "|" genre
                }
            }
        }
    }' >> "$OUTPUT_DIR/has_genre.csv"
genre_rel_count=$(wc -l < "$OUTPUT_DIR/has_genre.csv")
echo -e "${GREEN}    ✓ $((genre_rel_count - 1)) relations créées${NC}"

# Persons (nodes)
extract_table_to_csv "persons" \
    "$OUTPUT_DIR/persons.csv" \
    "id:int|birth_year|death_year|nconst:ID(Person)|primary_name"

# Directors (table qui lie movies à leurs directors)
extract_table_to_csv "directors" \
    "$OUTPUT_DIR/directors.csv" \
    "id:int|id_imdb"

# Directors mapping (pour créer les relations)
echo -e "${YELLOW}  → Extraction des directors...${NC}"

# Créer un mapping temporaire director_id -> id_imdb
awk '
    /INSERT INTO `directors` VALUES/ {in_block=1; next}
    in_block && /^INSERT INTO `[^d]|^\/\*|^--|^LOCK|^UNLOCK|^ALTER|^CREATE|^DROP|^commit|^set autocommit/ {in_block=0}
    in_block && /^\(/ {print}
    in_block && /^[0-9]/ {print}
' "$TEMP_SQL" | \
    sed -E 's/\),$/|NEXT_ROW|/g' | \
    tr '\n' ' ' | \
    sed -E 's/\|NEXT_ROW\| /\n/g' | \
    sed -E 's/^\(//g' | \
    sed -E 's/\),?;?\s*$//g' | \
    sed -E "s/'([^']*)'/\1/g" | \
    awk -F',' '{if (NF >= 2) print $1 "|" $2}' > "$OUTPUT_DIR/directors_mapping.csv"

echo -e "${GREEN}    ✓ $(wc -l < "$OUTPUT_DIR/directors_mapping.csv") lignes${NC}"

# Director relations
echo -e "${YELLOW}  → Création des relations DIRECTED_BY...${NC}"
echo ":START_ID(Movie)|:END_ID(Person)" > "$OUTPUT_DIR/directed_by.csv"

# Utiliser le mapping pour créer les relations
awk '
    /INSERT INTO `director_directors` VALUES/ {in_block=1; next}
    in_block && /^INSERT INTO `[^d]|^\/\*|^--|^LOCK|^UNLOCK|^ALTER|^CREATE|^DROP|^commit|^set autocommit/ {in_block=0}
    in_block && /^\(/ {print}
    in_block && /^[0-9]/ {print}
' "$TEMP_SQL" | \
    sed -E 's/\),$/|NEXT_ROW|/g' | \
    tr '\n' ' ' | \
    sed -E 's/\|NEXT_ROW\| /\n/g' | \
    sed -E 's/^\(//g' | \
    sed -E 's/\),?;?\s*$//g' | \
    sed -E "s/'([^']*)'/\1/g" | \
    awk -F',' 'NR==FNR {split($0,a,"|"); mapping[a[1]]=a[2]; next} {if (mapping[$1] != "") print mapping[$1] "|" $2}' \
        "$OUTPUT_DIR/directors_mapping.csv" - >> "$OUTPUT_DIR/directed_by.csv"

echo -e "${GREEN}    ✓ Relations créées${NC}"

# Locations (nodes)
extract_table_to_csv "locations" \
    "$OUTPUT_DIR/locations.csv" \
    "id:ID(Location)|country_code|description|display_name|geocoding_failed:boolean|id_imdb|latitude:float|location_string|longitude:float"

# Location relations
echo -e "${YELLOW}  → Création des relations FILMED_AT...${NC}"
echo ":START_ID(Movie)|:END_ID(Location)" > "$OUTPUT_DIR/filmed_at.csv"
awk '
    /INSERT INTO `locations` VALUES/ {in_block=1; next}
    in_block && /^INSERT INTO|^\/\*|^--|^$|^LOCK|^UNLOCK|^ALTER|^CREATE|^DROP/ {in_block=0}
    in_block && /^\(/ {print}
' "$TEMP_SQL" | \
    sed -E 's/\),\s*\(/),\n(/g' | \
    sed -E 's/\);\s*$/);/g' | \
    sed -E 's/^\(//g' | \
    sed -E 's/\),?;?\s*$//g' | \
    python3 -c "
import sys
import csv
import io

for line in sys.stdin:
    line = line.strip()
    if not line:
        continue
    
    # Parse comme une ligne CSV
    reader = csv.reader(io.StringIO(line), quotechar=\"'\")
    row = next(reader, None)
    if not row or len(row) < 6:
        continue
    
    # Colonnes: id, country_code, description, display_name, geocoding_failed, id_imdb, latitude, location_string, longitude
    id_location = row[0].strip()
    id_imdb = row[5].strip() if len(row) > 5 else ''
    
    # Ne créer la relation que si id_imdb existe
    if id_imdb and id_imdb not in ('NULL', r'\N', ''):
        print(f'{id_imdb}|{id_location}')
" >> "$OUTPUT_DIR/filmed_at.csv"
echo -e "${GREEN}    ✓ Relations créées${NC}"

# Principals (relations avec rôles)
echo -e "${YELLOW}  → Création des relations principales...${NC}"
echo ":START_ID(Person)|:END_ID(Movie)|:TYPE|category|characters|job" > "$OUTPUT_DIR/principals.csv"
awk '
    /INSERT INTO `principals` VALUES/ {in_block=1; next}
    in_block && /^INSERT INTO|^\/\*|^--|^$|^LOCK|^UNLOCK|^ALTER|^CREATE|^DROP/ {in_block=0}
    in_block && /^\(/ {print}
' "$TEMP_SQL" | \
    sed -E 's/\),$/|NEXT_ROW|/g' | \
    tr '\n' ' ' | \
    sed -E 's/\|NEXT_ROW\| /\n/g' | \
    sed -E 's/^\(//g' | \
    sed -E 's/\),?;?\s*$//g' | \
    sed -E "s/'([^']*)'/\1/g" | \
    awk -F',' '{
        tconst = $1
        nconst = $3
        category = $4
        characters = $5
        job = $6
        
        # Déterminer le type de relation
        rel_type = "WORKED_ON"
        if (category == "actor" || category == "actress") {
            rel_type = "ACTED_IN"
        } else if (category == "director") {
            rel_type = "DIRECTED"
        } else if (category == "writer") {
            rel_type = "WROTE"
        } else if (category == "producer") {
            rel_type = "PRODUCED"
        } else if (category == "cinematographer") {
            rel_type = "CINEMATOGRAPHY"
        } else if (category == "composer") {
            rel_type = "COMPOSED"
        } else if (category == "editor") {
            rel_type = "EDITED"
        }
        
        print nconst "|" tconst "|" rel_type "|" category "|" characters "|" job
    }' >> "$OUTPUT_DIR/principals.csv"
echo -e "${GREEN}    ✓ Relations créées${NC}"

# Ratings (propriétés à merger dans Movie)
echo -e "${YELLOW}  → Extraction des ratings...${NC}"
echo "id:int|average_rating:float|id_imdb|num_votes:int" > "$OUTPUT_DIR/ratings.csv"
awk '
    /INSERT INTO `ratings` VALUES/ {in_block=1; next}
    in_block && /^INSERT INTO|^\/\*|^--|^$|^LOCK|^UNLOCK|^ALTER|^CREATE|^DROP/ {in_block=0}
    in_block && /^\(/ {print}
' "$TEMP_SQL" | \
    sed -E 's/\),$/|NEXT_ROW|/g' | \
    tr '\n' ' ' | \
    sed -E 's/\|NEXT_ROW\| /\n/g' | \
    sed -E 's/^\(//g' | \
    sed -E 's/\),?;?\s*$//g' | \
    sed -E "s/'([^']*)'/\1/g" | \
    sed -E 's/,/|/g' >> "$OUTPUT_DIR/ratings.csv"
count=$(wc -l < "$OUTPUT_DIR/ratings.csv")
echo -e "${GREEN}    ✓ $((count - 1)) lignes${NC}"

# Nettoyage
rm -f "$TEMP_SQL" "$OUTPUT_DIR/directors_mapping.csv"

echo ""
echo -e "${GREEN}✅ Conversion terminée!${NC}"
echo -e "${YELLOW}Fichiers créés dans:${NC} $OUTPUT_DIR"
echo ""
echo -e "${BLUE}Fichiers de nœuds:${NC}"
echo "  - movies.csv ($(wc -l < "$OUTPUT_DIR/movies.csv") lignes)"
echo "  - persons.csv ($(wc -l < "$OUTPUT_DIR/persons.csv") lignes)"
echo "  - locations.csv ($(wc -l < "$OUTPUT_DIR/locations.csv") lignes)"
echo ""
echo -e "${BLUE}Fichiers de relations:${NC}"
echo "  - directed_by.csv ($(wc -l < "$OUTPUT_DIR/directed_by.csv") lignes)"
echo "  - filmed_at.csv ($(wc -l < "$OUTPUT_DIR/filmed_at.csv") lignes)"
echo "  - principals.csv ($(wc -l < "$OUTPUT_DIR/principals.csv") lignes)"
echo ""
echo -e "${YELLOW}Prochaine étape:${NC}"
echo "  sudo ./scripts/neo4j-bulk-import.sh $OUTPUT_DIR"
