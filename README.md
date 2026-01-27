# game-borrower
## Running the application in dev mode

Because we are connecting to a `Firebase Emulator` we need to run the following command to start the `firebase-local` container.

```shell script
# Position yourself in the firebase-env folder
cd firebase-env

# Start the compose
docker compose --env-file ../.env up --build
```

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/game-borrower-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Provided Code

### REST
You can access endpoints with [curl](https://curl.se/docs/tutorial.html).

##### Simulating a web login mechanism
*NB. Create a `login.json` file with the following content to simplify the commands.*
*{"email": "test@example.com","password": "password123","returnSecureToken": true}*
```shell
curl -v -X POST 'http://localhost:9099/identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=fake-api-key' \
  -H "Content-Type: application/json" \
  --data-binary "@login.json" \
  > token.json
```

##### Logging in
```shell
curl -v -X POST 'http://localhost:8080/auth/login' \
  -H "Content-Type: application/json" \
  --data-binary "@token.json" \
  -c cookie-jar.txt
```

##### Logging out
```shell
curl -v -X POST 'http://localhost:8080/auth/logout' \
  -H "Content-Type: application/json" \
  -b cookie-jar.txt
```

##### General route calling
`curl [OPTIONS] [URL]`
```shell
# Here is a simple request to the personal profile of a user
# (requires to be logged in hence the cookie)
curl -v -X GET http://localhost:8080/user/me -b cookie-jar.txt
```