# League of Legends Tracker API

**Status: Early development / Work in progress**

A personal project built with **Java and Spring Boot**. The goal is to create a League of Legends statistics tracker with a backend REST API and an [Electron/React desktop client](https://github.com/WiktorJakiela/lol-tracker-desktop).

The project is still at an early stage. **It is not a finished application ready for everyday use**, and many more features are planned.

## Currently implemented

- Player lookup and ranked information using the Riot Games API.
- Retrieval and processing of match history and match details.
- Calculation of statistics such as KDA, kill participation and CS per minute.
- Basic request validation, error handling and automated tests.

## Tech stack

Java 25 · Spring Boot 4 · REST API · Maven · JUnit · Mockito

## Local development

Requires **JDK 25** and a **Riot Games API key**. Set the `RIOT_API_KEY` environment variable and run:

```bash
./mvnw spring-boot:run
```

The API starts at `http://localhost:8080`. The desktop client is developed separately.

## What's next?

Development will continue with more detailed analytics, additional features, improved data handling and a more complete desktop experience. The scope may change as the project evolves.

---

*Unofficial personal project; not affiliated with Riot Games.*
