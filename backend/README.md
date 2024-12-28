# Backend

## Sommaire

- [Backend](#backend)
  - [Sommaire](#sommaire)
  - [Commandes utiles (Cheatsheet)](#commandes-utiles-cheatsheet)
    - [Gradle](#gradle)
    - [Docker](#docker)
  - [Procedure de lancement en local](#procedure-de-lancement-en-local)
    - [Etapes](#etapes)


<br>

## Commandes utiles (Cheatsheet)
Ces commandes sont utiles pour des opérations courantes liées au backend. Elles doivent être exécutées depuis la racine du backend.

### Gradle

- **Supprimer le répertoire `build`**  
  Utile lorsque les changements ne semblent pas être reconnus.
  ```sh
  ./gradlew clean
  ```

- **Nettoyer et construire le backend en ignorant les tests**  
  ```sh
  ./gradlew clean build -x test
  ```

- **Construire le backend**  
  ```sh
  ./gradlew build
  ```

- **Lancer le backend**  
  ```sh
  ./gradlew bootRun
  ```

- **Lancer les tests**  
  ```sh
  ./gradlew test
  ```

### Docker

- **Lister les conteneurs Docker Compose**  
  ```sh
  docker compose ps
  ```

- **Supprimer une image Docker**  
  Exemple :
  ```sh
  docker image rm -f image-cinetour-backend
  ```

- **Arrêter et supprimer les conteneurs, réseaux, volumes et images**  
  Exemple :
  ```sh
  docker compose -p cinetour down --volumes --rmi all
  ```

- **Voir les logs d'un conteneur**  
  ```sh
  docker compose logs cinetour-mysql-1
  ```

- **Démarrer et arrêter les conteneurs tout en conservant l'état de la base de données**  
  ```sh
  docker compose -p cinetour start
  docker compose -p cinetour stop
  ```

<br>

## Procedure de lancement en local

Suivez les étapes numérotées ci-dessous pour lancer le backend localement avec Docker.

### Etapes

1. **Nettoyer et construire le backend**  
   ```sh
   ./gradlew clean build
   ```

2. **Construire l'image Docker**  
   Exemple :
   ```sh
   docker build -t image-cinetour-backend .
   ```

3. **Créer les conteneurs et construire les images à partir de Docker Compose**  
   Exemple :
   ```sh
   docker compose -p cinetour up --build
   ```

   > Note : La base de données peut planter la première fois. Si c'est le cas, passez à l'étape 4, sinon passez directement à l'étape 5.

4. **Redémarrer le conteneur MySQL (facultatif)**  
   ```sh
   docker compose -p cinetour up --force-recreate mysql
   ```

   > Note : Si vous souhaitez accéder à la base de données avec un client plutôt qu'en lignes de commandes, suivez les étapes suivantes.

5. **Récupérer l'IP du conteneur MySQL**  
   ```sh
   docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' cinetour-mysql-1
   ```

6. **Configurer la connexion avec un client lourd**  
    Utilisez les informations d'utilisateur et de mot de passe définies dans le `docker-compose.yml`.

    ![configbd](readme_files/db_config.png)

---