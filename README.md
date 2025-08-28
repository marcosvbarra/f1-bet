# F1 Betting Service

Backend REST API for listing Formula 1 events, placing bets, and processing event outcomes.

This README provides simple, copy‑pasteable ways to run and use the app even without an IDE.

## What you get
- List F1 events (filter by sessionType, year, country) using openf1.org; returns a driver market with random odds in {2,3,4}.
- Place a bet against an event/driver; user balance is updated.
- Process an event outcome; winners get credited (amount * odds), losers are marked as LOST.
- Clean layering and provider interface `OpenF1Client`.

Tech stack: Java 21, Spring Boot 3, Spring Web, Spring Data JPA, H2 (in‑memory)

Default port: 8080

## Quick start (no IDE)
Option A. If you have Java 21 and Maven installed
1) Start the API
   - Windows PowerShell:
     mvn spring-boot:run
   - macOS/Linux:
     mvn spring-boot:run

2) Open another terminal and try the API (see curl examples below)

Option B. If you have Docker installed (no Java/Maven required)
1) Build the image (first time only):
   docker build -t f1-bet:latest .
2) Run the container:
   docker run -p 8080:8080 --name f1-bet f1-bet:latest

Stop the container with:
   docker stop f1-bet && docker rm f1-bet

## Run with Java + Maven
- Build jar: mvn -q -DskipTests package
- Run jar:  java -jar target/f1-bet-1.0-SNAPSHOT.jar
- Or run directly: mvn spring-boot:run

Requirements: Java 21, Maven 3.9+

## Run with Docker (no Java/Maven needed on host)
This repository includes a Dockerfile. Build and run:
- Build: docker build -t f1-bet:latest .
- Run:   docker run -p 8080:8080 --name f1-bet f1-bet:latest

The app will be reachable at http://localhost:8080

## API usage examples (curl)
Note: To successfully place a bet, use a real event sessionKey and a driver number that belongs to that session. The easiest flow is:
1) List events, pick sessionKey and a driverNumber from driverMarket
2) Place bet using userId=1 (pre‑seeded) and the values from step 1
3) (Optional) Process outcome for that event

- List events (all query params optional)
  GET /events
  Example: latest races, first page of 5 results
    curl "http://localhost:8080/events?sessionType=R&size=5"

  Example: filter by year and country
    curl "http://localhost:8080/events?year=2024&country=Italy&size=5"

  Response (example):
    [
      {
        "sessionKey": 123456,
        "sessionName": "Italian Grand Prix - Race",
        "sessionType": "R",
        "year": 2024,
        "country": "Italy",
        "driverMarket": [
          { "fullName": "Max Verstappen", "driverNumber": 1, "odds": 3 },
          { "fullName": "Charles Leclerc", "driverNumber": 16, "odds": 2 }
        ]
      }
    ]

- Place bet
  POST /bets
  Use a sessionKey from the previous call as eventId, and a driverNumber from driverMarket as driverId.

  Example body:
    {
      "userId": 1,
      "eventId": "123456",
      "driverId": 16,
      "amount": 10.00
    }

  curl example:
    curl -X POST http://localhost:8080/bets \
      -H "Content-Type: application/json" \
      -d "{\"userId\":1,\"eventId\":\"123456\",\"driverId\":16,\"amount\":10.00}"

  Successful response example:
    {
      "userId": 1,
      "betId": 7,
      "status": "PENDING",
      "betAmount": 10.00,
      "odds": 3,
      "totalAwarded": null
    }

- List bets by event
  GET /bets?event_id=123456

  curl example:
    curl "http://localhost:8080/bets?event_id=123456"

- Process event outcome (mark winners/losers for an event)
  POST /event-outcomes

  Example body:
    {
      "eventId": "123456",
      "winningDriverId": 16
    }

  curl example:
    curl -X POST http://localhost:8080/event-outcomes \
      -H "Content-Type: application/json" \
      -d "{\"eventId\":\"123456\",\"winningDriverId\":16}"

Expected effect: pending bets on the winningDriverId become WON and add prize to the user balance; others become LOST.

## Database, seed users, and H2 Console
- DB: In‑memory H2 (auto‑created). JDBC URL: jdbc:h2:mem:f1db, username: sa, password: password
- Seed users: src/main/resources/data.sql creates users with ids 1..5 and balance 100.00 each
- H2 web console: http://localhost:8080/h2-console (JDBC URL jdbc:h2:mem:f1db, user sa, password password)

## Run tests
- Run all tests: mvn test
- Build without tests: mvn -DskipTests package

