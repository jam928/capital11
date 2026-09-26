# Capital 11 Bank

Spring Boot 4 / Thymeleaf / Spring Data JPA / MySQL.

## Run locally

Requires Java 21+ and Docker.

```sh
docker compose up -d
./mvnw spring-boot:run
```

`docker compose up -d` starts the `mysql` service; the datasource defaults in `application.yml` point at
it. On first start MySQL runs `docker/mysql/init/01-schema.sql` to create the tables.

Open http://localhost:8080/capital11

To reset the database: `docker compose down -v`.

## JSON API and Swagger

Alongside the pages there is a JSON API under `/api`, documented with springdoc:

- Swagger UI: http://localhost:8080/capital11/swagger-ui.html
- OpenAPI spec: http://localhost:8080/capital11/v3/api-docs

Auth uses the same session as the pages: call `POST /api/session` with a username and password, and
the `JSESSIONID` cookie it sets authenticates the `/api/me/**` endpoints (401 without it). In Swagger
UI the browser sends the cookie for you, so log in there first and then try the other endpoints.
Errors come back as RFC 9457 problem details.

## Run everything in Docker

The `app` service in `docker-compose.yml` runs `mvn spring-boot:run` in a Maven container against the
checked-out source, connected to the `mysql` service. It sits behind the `app` profile so
`docker compose up -d` only starts MySQL.

```sh
docker compose --profile app up
```

Restart the service (`docker compose --profile app restart app`) to pick up code changes. Set
`APP_PORT` to publish on a port other than 8080. The first start is slow while Maven downloads
dependencies; they are cached in the `maven-repo` volume after that.

## Run the tests in Docker

The `tests` service runs `mvn verify` (unit tests, integration tests and the coverage check) in a Maven
container, sharing the `maven-repo` cache with `app`, and ends by printing unit and integration test
coverage:

```sh
docker compose run --rm tests
```

The integration tests start their own MySQL through Testcontainers, so the service mounts the host's
Docker socket and reaches that MySQL via `host.docker.internal`; it does not use the `mysql` service.
Build output, including the JaCoCo reports, goes to the `test-target` volume rather than `./target`.

## Running the packaged jar

```sh
docker compose up -d
./mvnw package
DB_URL=jdbc:mysql://localhost:3306/capital_11 DB_USERNAME=capital11 DB_PASSWORD=capital11 \
  java -jar target/capital11-1.0.0-SNAPSHOT.jar
```

## Legacy URL mapping

| Legacy (`*.do` servlet / JSP)      | Now                          |
|------------------------------------|------------------------------|
| `login.jsp`, `login.do`            | `GET/POST /login`            |
| `register.jsp`, `register.do`      | `GET/POST /register`         |
| `exist.do`                         | `GET /register/username-check` |
| `default.do`, `main.jsp`           | `GET /main`                  |
| `deposit.jsp`, `deposit.do`        | `GET/POST /deposit`          |
| `withdrawl.jsp`, `withdrawl.do`    | `GET/POST /withdraw`         |
| `viewinfo.jsp`                     | `GET /profile`               |
| `update.jsp`, `update.do`          | `GET/POST /profile/edit`     |
| `verifyReset.jsp`, `verify.do`     | `GET/POST /forgot-password`  |
| `reset.jsp`, `reset.do`            | `POST /reset-password`       |
| `logout.do`                        | `GET /logout`                |
