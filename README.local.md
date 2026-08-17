# Wealth Link Platform — Dev 1 (Foundation)

Java 17 / Spring Boot / PostgreSQL / Modular monolith.

This repo currently contains **Dev 1's scope** only, per the architecture
document's team split (Section 15): **Identity & Access, Country / Market
Config, Customer Management, Accounts** — the tables every other module
(Funds/FX, Portfolio/Trading/Ledger, Dividends/Reconciliation/Audit) foreign
keys into. It is the piece that has to land first so Devs 2–4 can wire real
FKs on Day 2.

## Modules included

| Module | Entities |
|---|---|
| Identity & Access | `AppUser`, `Role`, `UserRole` |
| Country / Market Config | `Currency`, `Country`, `Market` |
| Customer Management | `Customer`, `CustomerContact`, `CustomerIdentifier` |
| Accounts | `Account`, `AccountOwner` |

Schema is owned by the Flyway migration at
`src/main/resources/db/migration/V1__dev1_foundation.sql` — this is the
single source of truth for DDL (Hibernate is set to `ddl-auto: validate`,
it will never auto-generate or alter the schema).

## Prerequisites

- Java 17+
- Maven 3.9+ (or use the included wrapper if you add one)
- Docker (for local Postgres via docker-compose) — or your own Postgres 14+

## Run it locally

1. **Start Postgres:**

   ```bash
   docker compose up -d
   ```

   This starts Postgres 16 on `localhost:5432` with db `wealth_link`,
   user/password `wealth_link` / `wealth_link` (see `docker-compose.yml`,
   matches `application.yml`).

2. **Build and run the app:**

   ```bash
   mvn clean spring-boot:run
   ```

   On startup, Flyway automatically applies `V1__dev1_foundation.sql`,
   which creates all foundation tables and seeds NOK/SEK/DKK/EUR + NO/SE/DK
   reference data (per the Day 4 handoff notes in the architecture doc).

3. The app starts on `http://localhost:8080` (no REST controllers are
   wired yet — this milestone is schema + JPA entities + tests, per the
   Day 1–4 scope: "schema DDL + JPA entities + unit & integration tests,
   excludes business logic").

## Run the tests

```bash
mvn test
```

Two kinds of tests are included, matching the doc's Day 1 / Day 2 split:

**Entity-level unit tests** (Day 1, no database):
`AppUserTest`, `CustomerTest`, `AccountTest` — verify the `@PrePersist`
defaulting logic (status defaults, timestamps) directly on the entity.

**Repository-layer integration tests** (Day 2, real Postgres via
Testcontainers — not H2, per the doc's explicit call-out that H2 doesn't
give real NUMERIC precision / constraint behavior):
- `CurrencyRepositoryIT` / `MarketRepositoryIT` — Country / Market Config
- `IdentityRepositoryIT` — App user, role, and the user_role RBAC junction
- `CustomerRepositoryIT` — Customer, contacts, identifiers, including the
  partial-unique-index rule ("one primary contact per type")
- `AccountRepositoryIT` — Account, joint account ownership

All integration tests extend `AbstractIntegrationTest`, which declares a
single shared Postgres container (the Testcontainers "singleton container"
pattern) so the whole suite doesn't spin up a new container per test class.

## Connecting your own Postgres instead of Docker

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://<host>:<port>/<db>
    username: <user>
    password: <password>
```

Then create an empty database with that name — Flyway will build the schema
for you on first run.

## Handing off to Dev 2 / Dev 3 / Dev 4

Once this module is merged, the other devs replace their stubbed `UUID`
foreign-key columns with real `@ManyToOne` references into these entities
(`Currency`, `Country`, `Customer`, `Account`), exactly as described in
Section 15 / Day 2 of the architecture doc. Their own Flyway migrations
should be added as `V2__...`, `V3__...`, etc., each `references` — ing the
tables created here.

## What's *not* in this repo yet

Per scope, this is schema + entities + tests only — no REST controllers, no
business logic (matching engines, reconciliation algorithms, reporting),
and no other developer's tables (Funds/FX/Pricing, Portfolio/Trading/Ledger,
Dividends/Reconciliation/Audit). Those are Devs 2–4's deliverables, built on
top of this foundation.
