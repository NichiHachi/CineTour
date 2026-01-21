#!/bin/bash

# Script pour importer des CSV dans Neo4j via neo4j-admin import
# Usage: ./neo4j-bulk-import.sh <csv_dir>

set -e

CSV_DIR="${1:-./csv-export}"
CONTAINER_NAME="${NEO4J_CONTAINER:-neo4j}"
DB_NAME="${NEO4J_DATABASE:-neo4j}"
BACKUP_DIR="./neo4j-backups"

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

if [[ -z "$CSV_DIR" ]] || [[ "$CSV_DIR" == "-h" ]] || [[ "$CSV_DIR" == "--help" ]]; then
    echo "Usage: $0 <csv_dir>"
    echo ""
    echo "Importe des fichiers CSV dans Neo4j via neo4j-admin import (ULTRA-RAPIDE)"
    echo ""
    echo "Arguments:"
    echo "  csv_dir    Dossier contenant les fichiers CSV"
    echo ""
    echo "Variables d'environnement:"
    echo "  NEO4J_CONTAINER    Nom du conteneur Docker (défaut: neo4j)"
    echo "  NEO4J_DATABASE     Nom de la base (défaut: neo4j)"
    echo ""
    echo "ATTENTION: Cette commande va:"
    echo "  1. Arrêter Neo4j"
    echo "  2. Supprimer la base de données existante"
    echo "  3. Importer les données"
    echo "  4. Redémarrer Neo4j"
    exit 0
fi

if [[ ! -d "$CSV_DIR" ]]; then
    echo -e "${RED}Erreur: Dossier $CSV_DIR introuvable${NC}"
    exit 1
fi

echo -e "${GREEN}=== Import Neo4j Ultra-Rapide ===${NC}"
echo -e "${YELLOW}Dossier CSV:${NC} $CSV_DIR"
echo -e "${YELLOW}Conteneur:${NC} $CONTAINER_NAME"
echo -e "${YELLOW}Base de données:${NC} $DB_NAME"
echo ""

# Vérifier que les fichiers CSV existent
required_files=("movies.csv" "persons.csv")
for file in "${required_files[@]}"; do
    if [[ ! -f "$CSV_DIR/$file" ]]; then
        echo -e "${RED}Erreur: Fichier $CSV_DIR/$file manquant${NC}"
        echo "Exécutez d'abord: ./scripts/sql-to-csv.sh"
        exit 1
    fi
done

# Vérifier que Docker est disponible
if ! docker ps &> /dev/null; then
    echo -e "${RED}Erreur: Docker n'est pas accessible${NC}"
    echo "Exécutez avec sudo si nécessaire"
    exit 1
fi

# Vérifier que le conteneur existe
if ! docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo -e "${RED}Erreur: Conteneur $CONTAINER_NAME introuvable${NC}"
    exit 1
fi

# Confirmation
echo -e "${RED}⚠️  ATTENTION: Cette opération va:${NC}"
echo "  1. Arrêter Neo4j"
echo "  2. Supprimer TOUTES les données de la base '$DB_NAME'"
echo "  3. Importer les données depuis les CSV"
echo "  4. Redémarrer Neo4j"
echo ""
read -p "Continuer? (oui/non): " -r
if [[ ! $REPLY =~ ^[Oo][Uu][Ii]$ ]]; then
    echo "Annulé."
    exit 0
fi

# Backup optionnel
echo ""
read -p "Voulez-vous faire un backup avant? (oui/non): " -r
if [[ $REPLY =~ ^[Oo][Uu][Ii]$ ]]; then
    mkdir -p "$BACKUP_DIR"
    BACKUP_FILE="$BACKUP_DIR/neo4j-backup-$(date +%Y%m%d-%H%M%S).tar.gz"
    echo -e "${BLUE}📦 Création du backup...${NC}"
    docker exec $CONTAINER_NAME tar czf /tmp/backup.tar.gz /data 2>/dev/null || true
    docker cp $CONTAINER_NAME:/tmp/backup.tar.gz "$BACKUP_FILE"
    docker exec $CONTAINER_NAME rm /tmp/backup.tar.gz
    echo -e "${GREEN}✓ Backup créé: $BACKUP_FILE${NC}"
fi

echo ""
echo -e "${BLUE}1️⃣  Arrêt de Neo4j (mais pas du conteneur)...${NC}"
# Arrêter Neo4j à l'intérieur du conteneur sans arrêter le conteneur
docker exec $CONTAINER_NAME bash -c "neo4j stop" 2>/dev/null || true
sleep 3

echo -e "${BLUE}2️⃣  Copie des CSV dans le conteneur...${NC}"
docker cp "$CSV_DIR" $CONTAINER_NAME:/var/lib/neo4j/import/

echo -e "${BLUE}3️⃣  Suppression de la base existante...${NC}"
# Supprimer directement les fichiers de la base
docker exec $CONTAINER_NAME bash -c "rm -rf /data/databases/$DB_NAME /data/transactions/$DB_NAME" || true

echo -e "${BLUE}4️⃣  Import des données (RAPIDE!)...${NC}"
echo -e "${YELLOW}Ceci peut prendre quelques minutes pour des millions d'enregistrements...${NC}"

# Construire la commande d'import
IMPORT_CMD="neo4j-admin database import full $DB_NAME \
  --delimiter='|' \
  --array-delimiter=',' \
  --nodes=Movie=/var/lib/neo4j/import/$(basename $CSV_DIR)/movies.csv \
  --nodes=Person=/var/lib/neo4j/import/$(basename $CSV_DIR)/persons.csv"

# Ajouter genres si le fichier existe
if [[ -f "$CSV_DIR/genres.csv" ]]; then
    IMPORT_CMD="$IMPORT_CMD --nodes=Genre=/var/lib/neo4j/import/$(basename $CSV_DIR)/genres.csv"
fi

# Ajouter locations si le fichier existe
if [[ -f "$CSV_DIR/locations.csv" ]]; then
    IMPORT_CMD="$IMPORT_CMD --nodes=Location=/var/lib/neo4j/import/$(basename $CSV_DIR)/locations.csv"
fi

# Ajouter les relations
if [[ -f "$CSV_DIR/has_genre.csv" ]]; then
    IMPORT_CMD="$IMPORT_CMD --relationships=HAS_GENRE=/var/lib/neo4j/import/$(basename $CSV_DIR)/has_genre.csv"
fi

if [[ -f "$CSV_DIR/directed_by.csv" ]]; then
    IMPORT_CMD="$IMPORT_CMD --relationships=DIRECTED=/var/lib/neo4j/import/$(basename $CSV_DIR)/directed_by.csv"
fi

if [[ -f "$CSV_DIR/filmed_at.csv" ]]; then
    IMPORT_CMD="$IMPORT_CMD --relationships=FILMED_AT=/var/lib/neo4j/import/$(basename $CSV_DIR)/filmed_at.csv"
fi

# Ajouter principals avec tous les types de relations (le :TYPE dans le CSV détermine le type)
if [[ -f "$CSV_DIR/principals.csv" ]]; then
    IMPORT_CMD="$IMPORT_CMD --relationships=/var/lib/neo4j/import/$(basename $CSV_DIR)/principals.csv"
fi

# Ajouter les options d'optimisation
IMPORT_CMD="$IMPORT_CMD \
  --skip-duplicate-nodes \
  --skip-bad-relationships \
  --bad-tolerance=10000000 \
  --high-parallel-io=on \
  --verbose"

# Exécuter l'import
echo ""
echo -e "${YELLOW}Commande d'import:${NC}"
echo "$IMPORT_CMD"
echo ""

echo -e "${BLUE}4️⃣  Import en cours (conteneur arrêté)...${NC}"
START_TIME=$(date +%s)
# L'import doit se faire avec Neo4j arrêté
docker exec $CONTAINER_NAME bash -c "$IMPORT_CMD"
END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

echo ""
echo -e "${GREEN}✓ Import terminé en ${DURATION}s!${NC}"

echo -e "${BLUE}5️⃣  Recréation des dossiers de transaction...${NC}"
# Recréer les dossiers nécessaires pour éviter les erreurs au démarrage
docker exec $CONTAINER_NAME bash -c "mkdir -p /data/transactions/$DB_NAME" || true

echo -e "${BLUE}6️⃣  Démarrage de Neo4j...${NC}"
# Essayer de démarrer Neo4j dans le conteneur
docker exec $CONTAINER_NAME bash -c "neo4j start" 2>/dev/null || {
    echo -e "${YELLOW}Le démarrage classique a échoué, redémarrage du conteneur...${NC}"
    docker restart $CONTAINER_NAME
    sleep 5
}

# Attendre que Neo4j soit prêt
echo -e "${YELLOW}Attente du démarrage de Neo4j (peut prendre jusqu'à 60s)...${NC}"
READY=false
for i in {1..30}; do
    if docker exec $CONTAINER_NAME cypher-shell -u neo4j -p cinetour_neo4j_password "RETURN 1" &>/dev/null; then
        echo -e "${GREEN}✓ Neo4j est prêt!${NC}"
        READY=true
        break
    fi
    echo -n "."
    sleep 2
done
echo ""

if [[ "$READY" == "false" ]]; then
    echo -e "${RED}⚠️  Neo4j n'a pas répondu dans les délais. Vérifiez les logs avec:${NC}"
    echo "  docker logs $CONTAINER_NAME"
    echo -e "${YELLOW}La base a peut-être besoin de plus de temps pour démarrer.${NC}"
fi

echo -e "${BLUE}7️⃣  Création des index pour les performances...${NC}"
if [[ "$READY" == "true" ]]; then
    docker exec $CONTAINER_NAME cypher-shell -u neo4j -p cinetour_neo4j_password << 'EOF'
CREATE INDEX IF NOT EXISTS FOR (m:Movie) ON (m.id_imdb);
CREATE INDEX IF NOT EXISTS FOR (p:Person) ON (p.nconst);
CREATE INDEX IF NOT EXISTS FOR (g:Genre) ON (g.name);
CREATE INDEX IF NOT EXISTS FOR (l:Location) ON (l.id);
CREATE CONSTRAINT IF NOT EXISTS FOR (m:Movie) REQUIRE m.id_imdb IS UNIQUE;
CREATE CONSTRAINT IF NOT EXISTS FOR (p:Person) REQUIRE p.nconst IS UNIQUE;
CREATE CONSTRAINT IF NOT EXISTS FOR (g:Genre) REQUIRE g.name IS UNIQUE;
EOF

    echo -e "${GREEN}✓ Index créés${NC}"
else
    echo -e "${YELLOW}⚠️  Impossible de créer les index car Neo4j n'est pas prêt${NC}"
    echo -e "${YELLOW}Vous pourrez les créer manuellement plus tard avec:${NC}"
    echo "  CREATE INDEX FOR (m:Movie) ON (m.id_imdb);"
    echo "  CREATE INDEX FOR (p:Person) ON (p.nconst);"
    echo "  CREATE CONSTRAINT FOR (m:Movie) REQUIRE m.id_imdb IS UNIQUE;"
    echo "  CREATE CONSTRAINT FOR (p:Person) REQUIRE p.nconst IS UNIQUE;"
fi

# Statistiques
echo ""
echo -e "${BLUE}8️⃣  Statistiques de la base:${NC}"
if [[ "$READY" == "true" ]]; then
docker exec $CONTAINER_NAME cypher-shell -u neo4j -p cinetour_neo4j_password << 'EOF'
MATCH (m:Movie) RETURN 'Movies' as Type, count(m) as Count
UNION
MATCH (p:Person) RETURN 'Persons' as Type, count(p) as Count
UNION
MATCH (g:Genre) RETURN 'Genres' as Type, count(g) as Count
UNION
MATCH (l:Location) RETURN 'Locations' as Type, count(l) as Count
UNION
MATCH ()-[r]->() RETURN 'Relations' as Type, count(r) as Count;
EOF
else
    echo -e "${YELLOW}⚠️  Impossible d'afficher les statistiques car Neo4j n'est pas prêt${NC}"
fi

echo ""
echo -e "${GREEN}✅ Import terminé avec succès!${NC}"
echo -e "${YELLOW}Durée totale:${NC} ${DURATION}s"
echo ""
echo -e "${BLUE}Neo4j Browser:${NC} http://localhost:7474"
echo -e "${BLUE}Credentials:${NC} neo4j / cinetour_neo4j_password"
