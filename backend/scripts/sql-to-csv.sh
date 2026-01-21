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

# Fonction pour nettoyer les \N dans un fichier CSV
clean_null_values() {
    local file=$1
    local temp_file="${file}.tmp"
    
    # Remplacer \N par une chaîne vide en gardant les séparateurs
    sed 's/|\\N|/||/g; s/|\\N$/|/g; s/^\\N|/|/g; s/|\\N|/||/g' "$file" > "$temp_file"
    mv "$temp_file" "$file"
}

# Movies (nodes)
extract_table_to_csv "movies" \
    "$OUTPUT_DIR/movies_base.csv" \
    "id:int|genres|id_imdb:ID(Movie)|image|location_search_count|locations_checked:boolean|movie_search_count|release_year|runtime_minutes|title|backdrop_path|overview|poster_path|tmdb_info_checked:boolean"

# Nettoyer les valeurs NULL dans movies_base.csv
echo -e "${YELLOW}  → Nettoyage des valeurs NULL dans movies_base.csv...${NC}"
clean_null_values "$OUTPUT_DIR/movies_base.csv"
echo -e "${GREEN}    ✓ Nettoyage terminé${NC}"

# Extraire les ratings
echo -e "${YELLOW}  → Extraction des ratings...${NC}"
echo "id:int|average_rating:float|id_imdb|num_votes:int" > "$OUTPUT_DIR/ratings_temp.csv"
awk '
    /INSERT INTO `ratings` VALUES/ {in_block=1; next}
    in_block && /^INSERT INTO|^/\*|^--|^$|^LOCK|^UNLOCK|^ALTER|^CREATE|^DROP/ {in_block=0}
    in_block && /^\(/ {print}
' "$TEMP_SQL" | \
    sed -E 's/\),$/|NEXT_ROW|/g' | \
    tr '\n' ' ' | \
    sed -E 's/\|NEXT_ROW\| /\n/g' | \
    sed -E 's/^\(//g' | \
    sed -E 's/\),?;?\s*$//g' | \
    sed -E "s/'([^']*)'/'\1/g" | \
    awk -F',' '{
        id = $1
        rating = $2
        id_imdb = $3
        votes = $4
        if (rating == "NULL" || rating == "\\\\N") rating = ""
        if (votes == "NULL" || votes == "\\\\N") votes = ""
        print id "|" rating "|" id_imdb "|" votes
    }' >> "$OUTPUT_DIR/ratings_temp.csv"
rating_count=$(wc -l < "$OUTPUT_DIR/ratings_temp.csv")
echo -e "${GREEN}    ✓ $((rating_count - 1)) ratings extraits${NC}"

# Fusionner movies avec ratings
echo -e "${YELLOW}  → Fusion movies + ratings...${NC}"
python3 <<PYTHON_MERGE_RATINGS
import csv
from collections import defaultdict

# Lire les ratings et créer un mapping id_imdb -> (rating, votes)
ratings_map = {}
try:
    with open("$OUTPUT_DIR/ratings_temp.csv", "r") as f:
        reader = csv.DictReader(f, delimiter='|')
        for row in reader:
            id_imdb = row['id_imdb']
            if id_imdb:
                ratings_map[id_imdb] = {
                    'average_rating': row['average_rating'],
                    'num_votes': row['num_votes']
                }
except Exception as e:
    print(f"Avertissement: Erreur lors de la lecture des ratings: {e}")

# Fusionner avec movies
with open("$OUTPUT_DIR/movies.csv", "w") as out:
    # Nouveau header avec ratings
    out.write("id:int|genres|id_imdb:ID(Movie)|image|location_search_count|locations_checked:boolean|movie_search_count|release_year|runtime_minutes|title|backdrop_path|overview|poster_path|tmdb_info_checked:boolean|average_rating:float|num_votes:int\\n")
    
    with open("$OUTPUT_DIR/movies_base.csv", "r") as f:
        next(f)  # Skip header
        for line in f:
            line = line.rstrip('\\n')
            parts = line.split('|')
            if len(parts) >= 3:
                id_imdb = parts[2]
                
                # Récupérer les ratings si disponibles
                rating_info = ratings_map.get(id_imdb, {})
                average_rating = rating_info.get('average_rating', '')
                num_votes = rating_info.get('num_votes', '')
                
                # Écrire la ligne complète
                out.write(f"{line}|{average_rating}|{num_votes}\\n")

print("✓ Fusion terminée")
PYTHON_MERGE_RATINGS
echo -e "${GREEN}    ✓ Movies enrichis avec ratings${NC}"

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

# Persons (nodes) - extraction de base
extract_table_to_csv "persons" \
    "$OUTPUT_DIR/persons_base.csv" \
    "id:int|birth_year:int|death_year:int|nconst:ID(Person)|primary_name"

# Extraction des professions primaires
echo -e "${YELLOW}  → Extraction des professions primaires...${NC}"
awk '
    /INSERT INTO `person_primary_professions` VALUES/ {in_block=1; next}
    in_block && /^INSERT INTO `[^p]|^\/\*|^--|^LOCK|^UNLOCK|^ALTER|^CREATE|^DROP|^commit|^set autocommit/ {in_block=0}
    in_block && /^\(/ {print}
    in_block && /^[0-9]/ {print}
' "$TEMP_SQL" | \
    sed -E 's/\),$/|NEXT_ROW|/g' | \
    tr '\n' ' ' | \
    sed -E 's/\|NEXT_ROW\| /\n/g' | \
    sed -E 's/^\(//g' | \
    sed -E 's/\),?;?\s*$//g' | \
    sed -E "s/'([^']*)'/\1/g" | \
    awk -F',' '{print $1 "|" $2}' \
    > "$OUTPUT_DIR/person_professions_raw.csv"

# Extraction des titres connus
echo -e "${YELLOW}  → Extraction des titres connus...${NC}"
awk '
    /INSERT INTO `person_known_for_titles` VALUES/ {in_block=1; next}
    in_block && /^INSERT INTO `[^p]|^\/\*|^--|^LOCK|^UNLOCK|^ALTER|^CREATE|^DROP|^commit|^set autocommit/ {in_block=0}
    in_block && /^\(/ {print}
    in_block && /^[0-9]/ {print}
' "$TEMP_SQL" | \
    sed -E 's/\),$/|NEXT_ROW|/g' | \
    tr '\n' ' ' | \
    sed -E 's/\|NEXT_ROW\| /\n/g' | \
    sed -E 's/^\(//g' | \
    sed -E 's/\),?;?\s*$//g' | \
    sed -E "s/'([^']*)'/\1/g" | \
    awk -F',' '{print $1 "|" $2}' \
    > "$OUTPUT_DIR/person_titles_raw.csv"

# Fusionner les données avec Python
echo -e "${YELLOW}  → Fusion des données persons...${NC}"
python3 <<PYTHON_SCRIPT
import sys
from collections import defaultdict

# Lire les professions
professions = defaultdict(list)
try:
    with open("$OUTPUT_DIR/person_professions_raw.csv", "r") as f:
        for line in f:
            parts = line.strip().split("|")
            if len(parts) == 2:
                professions[parts[0]].append(parts[1])
except:
    pass

# Lire les titres
titles = defaultdict(list)
try:
    with open("$OUTPUT_DIR/person_titles_raw.csv", "r") as f:
        for line in f:
            parts = line.strip().split("|")
            if len(parts) == 2:
                titles[parts[0]].append(parts[1])
except:
    pass

# Écrire le résultat
with open("$OUTPUT_DIR/persons.csv", "w") as out:
    out.write("id:int|birth_year:int|death_year:int|nconst:ID(Person)|primary_name|primary_professions:string[]|known_for_titles:string[]\n")
    
    with open("$OUTPUT_DIR/persons_base.csv", "r") as f:
        next(f)  # Skip header
        for line in f:
            parts = line.strip().split("|")
            if len(parts) >= 5:
                person_id = parts[0]
                # Remplacer \\N et NULL par des chaînes vides pour les colonnes nullables
                birth_year = "" if parts[1] in [r"\N", "NULL", ""] else parts[1]
                death_year = "" if parts[2] in [r"\N", "NULL", ""] else parts[2]
                nconst = parts[3]
                primary_name = parts[4]
                
                prof_list = ",".join(professions.get(person_id, []))
                title_list = ",".join(titles.get(person_id, []))
                
                out.write(f"{person_id}|{birth_year}|{death_year}|{nconst}|{primary_name}|{prof_list}|{title_list}\n")

print("✓ Fusion terminée")
PYTHON_SCRIPT
echo -e "${GREEN}    ✓ Fusion terminée${NC}"

# Directors mapping (pour créer les relations Person->Movie via DIRECTED)
echo -e "${YELLOW}  → Extraction des directors...${NC}"

# Créer un mapping temporaire director_id -> id_imdb (du film)
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

# Director relations (Person->Movie via DIRECTED)
echo -e "${YELLOW}  → Création des relations DIRECTED (Person->Movie)...${NC}"
echo ":START_ID(Person)|:END_ID(Movie)" > "$OUTPUT_DIR/directed_by.csv"

# Utiliser le mapping pour créer les relations Person->Movie
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
    awk -F',' 'NR==FNR {split($0,a,"|"); mapping[a[1]]=a[2]; next} {if (mapping[$1] != "" && $2 != "") print $2 "|" mapping[$1]}' \
        "$OUTPUT_DIR/directors_mapping.csv" - >> "$OUTPUT_DIR/directed_by.csv"

echo -e "${GREEN}    ✓ Relations créées${NC}"

# Locations (nodes)
extract_table_to_csv "locations" \
    "$OUTPUT_DIR/locations.csv" \
    "id:ID(Location)|country_code|description|display_name|geocoding_failed:boolean|id_imdb|latitude:float|location_string|longitude:float|location_checked:boolean"

# Nettoyer les valeurs NULL dans locations.csv
echo -e "${YELLOW}  → Nettoyage des valeurs NULL dans locations.csv...${NC}"
clean_null_values "$OUTPUT_DIR/locations.csv"
echo -e "${GREEN}    ✓ Nettoyage terminé${NC}"

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
echo ":START_ID(Person)|:END_ID(Movie)|:TYPE|category|characters|job|ordering:int" > "$OUTPUT_DIR/principals.csv"
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
        # Colonnes SQL: id, category, characters, id_imdb, job, nconst, ordering
        id = $1
        category = $2
        characters = $3
        tconst = $4
        job = $5
        nconst = $6
        ordering = $7
        
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
        
        # Nettoyer les valeurs NULL
        if (ordering == "NULL" || ordering == "\\\\N") ordering = ""
        if (characters == "NULL" || characters == "\\\\N") characters = ""
        if (job == "NULL" || job == "\\\\N") job = ""
        
        print nconst "|" tconst "|" rel_type "|" category "|" characters "|" job "|" ordering
    }' >> "$OUTPUT_DIR/principals.csv"
echo -e "${GREEN}    ✓ Relations créées${NC}"

# Nettoyage
rm -f "$TEMP_SQL" "$OUTPUT_DIR/directors_mapping.csv" "$OUTPUT_DIR/persons_base.csv" "$OUTPUT_DIR/person_professions_raw.csv" "$OUTPUT_DIR/person_titles_raw.csv" "$OUTPUT_DIR/movies_base.csv" "$OUTPUT_DIR/ratings_temp.csv"

echo ""
echo -e "${GREEN}✅ Conversion terminée!${NC}"
echo -e "${YELLOW}Fichiers créés dans:${NC} $OUTPUT_DIR"
echo ""
echo -e "${BLUE}Fichiers de nœuds:${NC}"
echo "  - movies.csv ($(wc -l < "$OUTPUT_DIR/movies.csv") lignes) [avec ratings intégrés]"
echo "  - persons.csv ($(wc -l < "$OUTPUT_DIR/persons.csv") lignes)"
echo "  - genres.csv ($(wc -l < "$OUTPUT_DIR/genres.csv") lignes)"
echo "  - locations.csv ($(wc -l < "$OUTPUT_DIR/locations.csv") lignes)"
echo ""
echo -e "${BLUE}Fichiers de relations:${NC}"
echo "  - has_genre.csv ($(wc -l < "$OUTPUT_DIR/has_genre.csv") lignes)"
echo "  - directed_by.csv (Person→Movie) ($(wc -l < "$OUTPUT_DIR/directed_by.csv") lignes)"
echo "  - filmed_at.csv ($(wc -l < "$OUTPUT_DIR/filmed_at.csv") lignes)"
echo "  - principals.csv (Person→Movie avec rôles) ($(wc -l < "$OUTPUT_DIR/principals.csv") lignes)"
echo ""
echo -e "${YELLOW}Prochaine étape:${NC}"
echo "  sudo ./scripts/neo4j-bulk-import.sh $OUTPUT_DIR"
