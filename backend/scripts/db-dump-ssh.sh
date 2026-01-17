#!/bin/bash

# Script de dump de la base de données CineTour via tunnel SSH
# Usage: ./db-dump-ssh.sh [options]

set -e

# Configuration par défaut
SSH_HOST="${SSH_HOST:-}"
SSH_PORT="${SSH_PORT:-22}"
SSH_USER="${SSH_USER:-}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3307}"
DB_NAME="${DB_NAME:-cinetour}"
DB_USER="${DB_USER:-dbuser}"
DB_PASSWORD="${DB_PASSWORD:-dbpassword}"
DUMP_DIR="${DUMP_DIR:-./dumps}"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
DUMP_FILE="${DUMP_DIR}/cinetour_dump_${TIMESTAMP}.sql"
USE_EXISTING_TUNNEL="${USE_EXISTING_TUNNEL:-false}"
LOCAL_PORT="${LOCAL_PORT:-}"

# Couleurs pour les messages
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Fonction d'aide
show_help() {
    cat << EOF
Usage: ${0##*/} [OPTIONS]

Dump de la base de données CineTour via tunnel SSH

OPTIONS:
    -h, --help              Afficher cette aide
    -H, --ssh-host HOST     Hôte SSH (non requis si -e est utilisé)
    -P, --ssh-port PORT     Port SSH (défaut: 22)
    -u, --ssh-user USER     Utilisateur SSH (non requis si -e est utilisé)
    -d, --db-host HOST      Hôte de la base de données (défaut: localhost)
    -p, --db-port PORT      Port de la base de données (défaut: 3307)
    -n, --db-name NAME      Nom de la base de données (défaut: cinetour)
    -U, --db-user USER      Utilisateur de la base de données (défaut: dbuser)
    -W, --db-password PASS  Mot de passe de la base de données (défaut: dbpassword)
    -o, --output DIR        Répertoire de sortie pour le dump (défaut: ./dumps)
    -e, --existing-tunnel   Utiliser un tunnel SSH existant
    -L, --local-port PORT   Port local du tunnel existant (requis avec -e)

EXEMPLES:
    # Créer un nouveau tunnel
    ${0##*/} -H example.com -u root
    
    # Utiliser un tunnel existant
    ${0##*/} -e -L 3307
    ${0##*/} --existing-tunnel --local-port 3307
    
VARIABLES D'ENVIRONNEMENT:
    Vous pouvez également configurer via des variables d'environnement:
    SSH_HOST, SSH_PORT, SSH_USER, DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD, 
    DUMP_DIR, USE_EXISTING_TUNNEL, LOCAL_PORT

EOF
}

# Parse des arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -H|--ssh-host)
            SSH_HOST="$2"
            shift 2
            ;;
        -P|--ssh-port)
            SSH_PORT="$2"
            shift 2
            ;;
        -u|--ssh-user)
            SSH_USER="$2"
            shift 2
            ;;
        -d|--db-host)
            DB_HOST="$2"
            shift 2
            ;;
        -p|--db-port)
            DB_PORT="$2"
            shift 2
            ;;
        -n|--db-name)
            DB_NAME="$2"
            shift 2
            ;;
        -U|--db-user)
            DB_USER="$2"
            shift 2
            ;;
        -W|--db-password)
            DB_PASSWORD="$2"
            shift 2
            ;;
        -e|--existing-tunnel)
            USE_EXISTING_TUNNEL=true
            shift
            ;;
        -L|--local-port)
            LOCAL_PORT="$2"
            shift 2
            ;;
        -o|--output)
            DUMP_DIR="$2"
            DUMP_FILE="${DUMP_DIR}/cinetour_dump_${TIMESTAMP}.sql"
            shift 2
            ;;
        *)
            echo -e "${RED}Option inconnue: $1${NC}"
            show_help
            exit 1
            ;;
    esac
done

# Vérification des paramètres requis
if [[ "$USE_EXISTING_TUNNEL" == "true" ]]; then
    if [[ -z "$LOCAL_PORT" ]]; then
        echo -e "${RED}Erreur: LOCAL_PORT est requis avec --existing-tunnel${NC}"
        show_help
        exit 1
    fi
else
    if [[ -z "$SSH_HOST" ]]; then
        echo -e "${RED}Erreur: SSH_HOST est requis (ou utilisez --existing-tunnel)${NC}"
        show_help
        exit 1
    fi

    if [[ -z "$SSH_USER" ]]; then
        echo -e "${RED}Erreur: SSH_USER est requis (ou utilisez --existing-tunnel)${NC}"
        show_help
        exit 1
    fi
fi

# Création du répertoire de dump si nécessaire
mkdir -p "$DUMP_DIR"

echo -e "${GREEN}=== Dump de la base de données CineTour ===${NC}"
echo -e "${YELLOW}Configuration:${NC}"

if [[ "$USE_EXISTING_TUNNEL" == "true" ]]; then
    echo "  Mode: Utilisation d'un tunnel SSH existant"
    echo "  Port local: ${LOCAL_PORT}"
else
    echo "  SSH: ${SSH_USER}@${SSH_HOST}:${SSH_PORT}"
    # Port local pour le tunnel (on utilise un port aléatoire disponible)
    LOCAL_PORT=$(python3 -c 'import socket; s=socket.socket(); s.bind(("", 0)); print(s.getsockname()[1]); s.close()')
fi

echo "  Base de données: ${DB_NAME} (${DB_HOST}:${DB_PORT})"
echo "  Fichier de sortie: ${DUMP_FILE}"
echo ""

# Fonction de nettoyage
cleanup() {
    if [[ "$USE_EXISTING_TUNNEL" == "false" ]]; then
        echo -e "\n${YELLOW}Fermeture du tunnel SSH...${NC}"
        if [[ ! -z "$SSH_PID" ]] && kill -0 "$SSH_PID" 2>/dev/null; then
            kill "$SSH_PID"
        fi
    fi
}

# S'assurer que le tunnel est fermé à la sortie (seulement si on l'a créé)
trap cleanup EXIT INT TERM

if [[ "$USE_EXISTING_TUNNEL" == "false" ]]; then
    echo -e "${YELLOW}Établissement du tunnel SSH sur le port local ${LOCAL_PORT}...${NC}"
    
    # Établir le tunnel SSH en arrière-plan
    ssh -f -N -L "${LOCAL_PORT}:${DB_HOST}:${DB_PORT}" -p "${SSH_PORT}" "${SSH_USER}@${SSH_HOST}"
    SSH_PID=$!

    # Attendre que le tunnel soit établi
    echo -e "${YELLOW}Attente de l'établissement du tunnel...${NC}"
    sleep 2

    # Vérifier que le tunnel est actif
    if ! kill -0 "$SSH_PID" 2>/dev/null; then
        echo -e "${RED}Erreur: Le tunnel SSH n'a pas pu être établi${NC}"
        exit 1
    fi

    echo -e "${GREEN}Tunnel SSH établi avec succès!${NC}"
else
    echo -e "${GREEN}Utilisation du tunnel SSH existant sur le port ${LOCAL_PORT}${NC}"
    
    # Vérifier que le port est bien en écoute
    if ! nc -z 127.0.0.1 "${LOCAL_PORT}" 2>/dev/null; then
        echo -e "${YELLOW}⚠ Attention: Aucun service ne semble écouter sur le port ${LOCAL_PORT}${NC}"
        echo -e "${YELLOW}Le dump va quand même être tenté...${NC}"
    fi
fi

echo -e "${YELLOW}Création du dump de la base de données...${NC}"

# Faire le dump via le tunnel
if mysqldump -h 127.0.0.1 -P "${LOCAL_PORT}" -u "${DB_USER}" -p"${DB_PASSWORD}" \
    --single-transaction \
    --routines \
    --triggers \
    --events \
    "${DB_NAME}" > "${DUMP_FILE}"; then
    
    # Compression du dump
    echo -e "${YELLOW}Compression du dump...${NC}"
    gzip "${DUMP_FILE}"
    DUMP_FILE="${DUMP_FILE}.gz"
    
    # Affichage de la taille du fichier
    DUMP_SIZE=$(du -h "${DUMP_FILE}" | cut -f1)
    
    echo -e "${GREEN}✓ Dump créé avec succès!${NC}"
    echo -e "  Fichier: ${DUMP_FILE}"
    echo -e "  Taille: ${DUMP_SIZE}"
else
    echo -e "${RED}✗ Erreur lors de la création du dump${NC}"
    exit 1
fi

echo -e "${GREEN}=== Terminé ===${NC}"
