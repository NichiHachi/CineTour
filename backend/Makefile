all: clean-all build run

all-import: clean-all build run-import

all-without-tests: clean-all build-without-tests run

all-import-without-tests: clean-all build-without-tests run-import

clean:
	docker rm cinetour-backend-1

clean-all:
	docker compose -p cinetour down --volumes --rmi all || true
	./gradlew clean

build:
	./gradlew clean build

build-without-tests:
	./gradlew clean build -x test

rebuild-backend: clean build
	docker compose -p cinetour up --build backend

run:
	docker compose -p cinetour up --build

run-import:
	SPRING_PROFILES_ACTIVE=import docker compose -p cinetour up --build

start:
	docker compose -p cinetour up -d

stop:
	docker compose -p cinetour stop

logs:
	docker compose -p cinetour logs -f