all: clean build run

all-import: clean build run-import

all-without-tests: clean build-without-tests run

all-import-without-tests: clean build-without-tests run-import

clean:
	docker compose -p cinetour down --volumes --rmi all || true
	./gradlew clean

build:
	./gradlew clean build

build-without-tests:
	./gradlew clean build -x test

run:
	docker compose -p cinetour up --build

run-import:
	SPRING_PROFILES_ACTIVE=import docker compose -p cinetour up --build

start:
	docker compose -p cinetour start

stop:
	docker compose -p cinetour stop