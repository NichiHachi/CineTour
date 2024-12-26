### Backend

## Commandes utiles
*Pour les commandes Gradle et Docker, il faut être dans la racine du backend.*

<br>

**Supprimer le répertoire build**
```sh
./gradlew clean
```
*Peut-être utile lorsque les changements ne semblent pas être reconnus.*

**Nettoyer et construire l'application en ignorant les tests**
```sh
./gradlew clean build -x test
```

**Build l'application**
```sh
./gradlew build
```

**Lancer l'application**
```sh
./gradlew bootRun
```

**Lancer les tests**
```sh
./gradlew test
```
<br>

## Commandes Docker & Procédure de lancement en local
Vous pourrez vous référez aux étapes numérotées ci-dessous.

<br>

**Construire l'image Docker**
```sh
docker build -t image-name .
```
1. Étape :
```sh
docker build -t image-cinetour-backend .
```
<br>

**Créer les conteneurs et construire les images à partir de Docker Compose**
```sh
docker compose up --build
```

 2. Étape :
```sh
docker compose -p cinetour up --build
```
<br>

**Lister les conteneurs Docker Compose**
```sh
docker compose ps
```

 3. Étape :
```sh
docker compose -p cinetour ps
```
<br>

**Note : La base de données plante souvent la première fois, je ne sais pas encore pourquoi, il faut donc forcer le redémarrage :**

**Redémarrer le conteneur MySQL**
 
 4. Étape :
```sh
docker compose -p cinetour up --force-recreate mysql
```

<br>

**Entrer dans le conteneur MySQL**
```sh
docker exec -it container_name bash
```
 5. Étape
```sh
> Récupérer le nom du conteneur MySQL
docker compose -p cinetour ps

> Rentrer dans le conteneur MySQL
docker exec -it cinetour-mysql-1 bash
```

<br>

*Autres commandes Docker*

**Supprimer l'image Docker**
```sh
docker image rm -f image-name .
```
Exemple :
```shd
docker image rm -f image-cinetour-backend
```

<br>

**Arrêter et supprimer les conteneurs, réseaux, volumes et images**
```sh
docker compose down --volumes --rmi all
```

Exemple :
```sh
docker compose -p cinetour down --volumes --rmi all
```

<br>

**Voir logs d'un conteneur**
```sh
docker compose logs cinetour-mysql-1 
```

<br>

**Démarrer et arrêter les conteneurs (conserve l'état de la BD)**
```sh
docker compose -p cinetour start
docker compose -p cinetour stop
```

<br>

## Commandes MySQL

**Créer un utilisateur dans la base de données**

 6. Étape :
```sql
CREATE USER 'dbuser'@'%' IDENTIFIED BY 'dbpassword';
GRANT ALL PRIVILEGES ON *.* TO 'dbuser'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;
```

<br>

**Redémarrer les services Docker Compose**

 7. Étape :
```sh
docker compose restart
```

<br>

**Lister les utilisateurs MySQL**

 8. Étape :
```sql
SELECT User FROM mysql.user;
```

<br>

**Note : La base de données peut prendre du temps à démarrer. Si vous voyez cette erreur, ne vous inquiétez pas :**
```
ERROR 2002 (HY000): Can't connect to local MySQL server through socket '/var/run/mysqld/mysqld.sock' (2)
```

<br>

# Se connecter avec un client lourd
**Récupérer l'ip du conteneur MySQL**

 9. Étape :
```sh
docker inspect \\n  -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' backend-mysql-1
```

<br>

**Configurer la connexion**

*User et password dans le docker-compose*

 10. Étape :

![configbd](readme_files/db_config.png)