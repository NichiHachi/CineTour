all: clean test run

allImport: clean test runImport

allWithoutTests: clean buildWithoutTests run

allImportWithoutTests: clean buildWithoutTests runImport

restart: stopOtherServices cleanOnlyCinetour run

restartWithImport: stopOtherServices cleanOnlyCinetour runImport

clean:
	docker compose -p cinetour down --volumes --rmi all || true
	./gradlew clean

cleanOnlyCinetour:
	docker compose -p cinetour down --volumes cinetour-backend
	./gradlew clean

stopOtherServices:
	docker compose -p cinetour stop cinetour-mysql selenium-firefox

test:
	./gradlew clean build

buildWithoutTests:
	./gradlew clean build -x test

run:
	docker compose -p cinetour up --build -d

runImport:
	SPRING_PROFILES_ACTIVE=import docker compose -p cinetour up --build -d

start:
	docker compose -p cinetour up -d

stop:
	docker compose -p cinetour stop

logs:
	docker compose -p cinetour logs -f