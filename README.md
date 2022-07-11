# Modality

Modality is a booking system for a wide area of events, from small to complex events over several days or weeks including meals, onsite/offsite accommodation, transport, translation, etc...



## Installation
### 1/ Create the Modality root
```sh
mkdir -vp modality
export MODALTY_ROOT=${PWD}/modality
```


### 2/ Clone the codebase
Git clone the Modality codebase via the terminal (or IntelliJ etc):

```sh
cd $MODALTY_ROOT
git clone https://github.com/mongoose-project/modality.git .
```


### 3/ Install Docker
Modality uses Docker for all external services, including the database and the in-memory datastore for sessions, etc.

Please install Docker on your local machine if you do not have it already. If using a Mac, the easiest way is to install using `brew`. 
Please provide Docker with a minimum of 8GB of RAM, ideally more. Insufficient RAM may result in `java.lang.OutOfMemoryError` errors
when importing the [modality-dev-db](https://github.com/mongoose-project/modality-dev-db).


### 4/ Prepare Docker environment variables
Environment variables store the Postgres database name, username and password. Defaults 
are provided in the `.env-template`. Use this template file as the basis for
your Docker-based configuration, by creating an `.env` file from it. You may leave
the defaults, or provide new values accordingly:

```sh
cd $MODALTY_ROOT/docker
cp .env-template .env
source .env # make the environment variables available to the shell
```


### 5/ Build the Docker containers
```sh
cd $MODALTY_ROOT/docker
docker-compose build --no-cache
```


### 6/ Start the containers and build the database
```sh
cd $MODALTY_ROOT/docker
docker-compose up
```

The database scripts are stored in the `modality-base/modality-base-server-datasource/src/main/resources/db/` folder, and are executed sequentially 
by the [Flyway](https://flywaydb.org/) database version control container. Please allow several minutes for Flyway to complete. Once finished, you 
are now up and running with all the external services that Modality depends on.


### 7/ Compile Modality codebase
@TODO IntelliJ-based procedures to be added later. Once finished, you are now ready to run Modality locally.



## Run Modality locally
@TODO IntelliJ-based procedures to be added later



## Using Docker
### Connect to the Docker database container
Connection is easily made via any Postgres client (e.g. DBeaver). Use the following credentials (contained within the `docker/.env-template` file):

* Server: 127.0.0.1
* Port: 5432
* Database: modality
* User: modality
* Password: modality


### Connect to the Docker session container
Connection can be made through the Docker terminal:
```sh
cd $MODALTY_ROOT/docker
docker exec -ti session /bin/sh
redis-cli
keys *
```


### Shut down Docker
```sh
cd $MODALTY_ROOT/docker
docker-compose down
```


### Destroy & rebuild the Docker containers
Sometimes you will want a fresh set of containers. The simplest way to do this is:
```sh
cd $MODALTY_ROOT/docker
docker-compose down
docker ps -a # Lists all Docker containers
docker rm <container-id> # Remove any docker containers listed
docker images # Lists all Docker images
docker image rm <image-id> # Remove any docker images listed
docker volume ls # Lists all Docker volumes
docker volume rm <volume-id> # Remove all docker volumes listed
docker system prune # Removes build cache, networks and dangling images
rm -rf data # Removes locally stored database tables
```

You can now rebuild the Docker containers:
```sh
docker-compose build --no-cache
docker-compose up
```



## Modality Database
All database setup scripts are stored in the `modality-base/modality-base-server-datasource/src/main/resources/db/` folder, and are numbered in order of execution. Execution of the database scripts is performed automatically by the Flyway container, which runs on startup. All the data is stored on the host, in directory:

`$MODALTY_ROOT/docker/data/postgres/*`

This provides persistence, and the container can be safely shut down and restarted without losing data.

Any new database scripts must be:

1. added to the same `modality-base/modality-base-server-datasource/src/main/resources/db/` folder
2. named according to the convention used in the folder: `V{number}__{desc}.sql`

Once a new script has been added to the folder, the Flyway container should be restarted, in order to apply the change. The easiest way to do this is to simply restart docker-compose:

```sh
cd $MODALTY_ROOT/docker
docker-compose down
docker-compose up
```



## Modality Development Database
The Modality project additionally provides a development database that is pre-populated with test data, available from the 
[modality-dev-db](https://github.com/mongoose-project/modality-dev-db) repository.

If you wish to import this database, you will need to:

1. shut down the Modality server
2. shut down the docker containers
3. delete the `docker/data/` folder
4. download the [modality-dev-db](https://github.com/mongoose-project/modality-dev-db) repository
5. decompress the `V0001__modality_dev_db.sql.zip` file in the [modality-dev-db](https://github.com/mongoose-project/modality-dev-db) repository
6. move the unzipped `V0001__modality_dev_db.sql` to the `modality-base/modality-base-server-datasource/src/main/resources/db/` folder
7. move all the other scripts temporarily out of the folder
8. restart the docker containers - this will auto-import the development database
9. wait until the import is complete. Due to the size of the development database, it can take 20+ minutes to import. Modality will not be usable during this time.



## Modality Session
The session data is controlled by the docker-based Redis container and is not persisted locally. The data persists only as long as the container is running.



## Deploy to Heroku

@TODO IntelliJ-based procedures to be added later
