#!/bin/bash

# Script de migration CineTour - MySQL dump vers Neo4j

set -e

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Fonction d'aide
show_help() {
    cat << EOF
Usage: ./run-migration.sh [OPTIONS]

Options:
    -f, --file FILE         Fichier dump SQL (requis)
    -p, --password PASS     Mot de passe Neo4j (requis)
    -u, --uri URI           URI Neo4j (défaut: bolt://localhost:7474)
    --user USER             Utilisateur Neo4j (défaut: neo4j)
    --clear                 Nettoyer la base Neo4j avant import
    -h, --help              Afficher cette aide

Exemples:
    # Import simple
    ./run-migration.sh -f dump.sql -p mypassword

    # Import avec nettoyage
    ./run-migration.sh -f dump.sql -p mypassword --clear

    # Import personnalisé
    ./run-migration.sh -f dump.sql -p mypassword --uri bolt://192.168.1.100:7474 --user admin --clear
EOF
}

# Valeurs par défaut
DUMP_FILE=""
NEO4J_PASSWORD=""
NEO4J_URI="neo4j://localhost:7687"
NEO4J_USER="neo4j"
CLEAR_FLAG=""

# Parser les arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -f|--file)
            DUMP_FILE="$2"
            shift 2
            ;;
        -p|--password)
            NEO4J_PASSWORD="$2"
            shift 2
            ;;
        -u|--uri)
            NEO4J_URI="$2"
            shift 2
            ;;
        --user)
            NEO4J_USER="$2"
            shift 2
            ;;
        --clear)
            CLEAR_FLAG="--clear"
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            echo -e "${RED}Option inconnue: $1${NC}"
            show_help
            exit 1
            ;;
    esac
done

# Vérifier les paramètres requis
if [ -z "$DUMP_FILE" ]; then
    echo -e "${RED}❌ Erreur: Le fichier dump est requis (-f)${NC}"
    show_help
    exit 1
fi

if [ -z "$NEO4J_PASSWORD" ]; then
    echo -e "${RED}❌ Erreur: Le mot de passe Neo4j est requis (-p)${NC}"
    show_help
    exit 1
fi

# Vérifier que le fichier existe
if [ ! -f "$DUMP_FILE" ]; then
    echo -e "${RED}❌ Erreur: Fichier non trouvé: $DUMP_FILE${NC}"
    exit 1
fi

# Afficher la configuration
echo -e "${GREEN}=== Configuration ===${NC}"
echo "Fichier dump  : $DUMP_FILE"
echo "URI Neo4j     : $NEO4J_URI"
echo "Utilisateur   : $NEO4J_USER"
echo "Nettoyer DB   : $([ -n "$CLEAR_FLAG" ] && echo 'Oui' || echo 'Non')"
echo ""

# Vérifier si le projet est compilé
if [ ! -f "build/libs/neo4j-migration-1.0.0.jar" ]; then
    echo -e "${YELLOW}⚙️  Compilation du projet...${NC}"
    ./gradlew build
    echo ""
fi

# Exécuter l'import
echo -e "${GREEN}🚀 Démarrage de l'import...${NC}"
echo ""

java -jar build/libs/neo4j-migration-1.0.0.jar \
    "$DUMP_FILE" \
    "$NEO4J_PASSWORD" \
    $CLEAR_FLAG \
    --uri "$NEO4J_URI" \
    --user "$NEO4J_USER"

exit_code=$?

if [ $exit_code -eq 0 ]; then
    echo ""
    echo -e "${GREEN}✅ Import terminé avec succès !${NC}"
else
    echo ""
    echo -e "${RED}❌ L'import a échoué avec le code $exit_code${NC}"
    exit $exit_code
fi
