all: clean build run

all-import: clean build run-import

clean:
	docker compose -p cinetour down --volumes --rmi all || true
	./gradlew clean

build:
	./gradlew clean build

run:
	docker compose -p cinetour up --build

run-import:
	SPRING_PROFILES_ACTIVE=import docker compose -p cinetour up --build