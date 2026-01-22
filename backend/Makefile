all: clean test run

allImport: clean test runImport

allWithoutTests: clean buildWithoutTests run

allImportWithoutTests: clean buildWithoutTests runImport

restart: stopOtherServices cleanOnlyCinetour run

restartWithImport: stopOtherServices cleanOnlyCinetour runImport

clean:
	docker compose -p cinetour down --volumes --rmi all || true
	sudo rm -rf /mnt/cinetour/data/*
	./gradlew clean

cleanOnlyCinetour:
	docker compose -p cinetour down --volumes cinetour-backend
	./gradlew clean

stopOtherServices:
	docker compose -p cinetour stop cinetour-backend

test:
	./gradlew clean build

buildWithoutTests:
	./gradlew clean build -x test

run:
	docker compose -p cinetour up --build -d

runImport:
	SPRING_PROFILES_ACTIVE=import docker compose -p cinetour up --build -d

runNetworkDocker:
	docker compose -p cinetour -f docker-compose.network.yml up --build -d

stopNetworkDocker:
	docker compose -p cinetour -f docker-compose.network.yml down

start:
	docker compose -p cinetour up -d

stop:
	docker compose -p cinetour stop

logs:
	docker compose -p cinetour logs -f

dumpSSH:
	@if [ -f .env ]; then \
		export $$(cat .env | grep NETWORK_DB_PASSWORD | xargs) && ./scripts/db-dump-ssh.sh -e -L 3307; \
	else \
		echo "Erreur: fichier .env non trouvé"; \
		exit 1; \
	fi

startNeo4j:
	docker compose -p cinetour up -d neo4j

stopNeo4j:
	docker compose -p cinetour stop neo4j

restartNeo4j:
	docker compose -p cinetour restart neo4j

logsNeo4j:
	docker compose -p cinetour logs -f neo4j

cleanNeo4j:
	docker compose -p cinetour down --volumes neo4j

neo4jShell:
	docker exec -it cinetour-neo4j cypher-shell -u neo4j -p cinetour_neo4j_password

sqlToCsv:
	@if [ -z "$(DUMP)" ]; then \
		echo "Usage: make sqlToCsv DUMP=<fichier.sql.gz>"; \
		echo "Exemple: make sqlToCsv DUMP=dumps/cinetour.sql.gz"; \
		exit 1; \
	fi
	./scripts/sql-to-csv.sh $(DUMP) ./csv-export
	sudo python3 ./scripts/clean-csv-data.py
	@echo ""
	@echo "✓ Export SQL → CSV terminé et nettoyé"
	@echo "Vous pouvez maintenant lancer: make neo4jImport"

neo4jImport:
	sudo NEO4J_CONTAINER=cinetour-neo4j ./scripts/neo4j-bulk-import.sh ./csv-export
