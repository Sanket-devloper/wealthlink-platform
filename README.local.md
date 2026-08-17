# Wealth Link Platform — Dev 1 (Foundation) + Dev 2 (Funds & Market Data)

Java 17 / Spring Boot / PostgreSQL / Modular monolith.

This repo now contains **Dev 1's scope** (merged, per the architecture
document's team split, Section 15: Identity & Access, Country / Market
Config, Customer Management, Accounts) plus **Dev 2's scope** built on top
of it: **Funds, Share Classes, Providers, FX Rates, Fund Prices, and the
Import pipeline** (Section 11 / DEV2-D1..D4 in the Jira doc).

Because Dev 1's foundation module is already merged into this codebase,
Dev 2's entities wire **real `@ManyToOne` foreign keys directly into
`Currency`/`Country`** rather than the temporary UUID-stub pattern the
architecture doc describes for when Dev 1 hasn't landed yet — Day 1 and
Day 2 of the Dev 2 plan are effectively collapsed into one step here.

## Dev 2 modules included

| Module | Entities |
|---|---|
| Funds | `Fund`, `FundShareClass`, `Provider`, `FundProviderMapping` |
| Market Data | `FxRateSource`, `FxRate`, `FundPrice` |
| Imports | `ImportJob`, `ImportBatch`, `ImportItem` |

Schema is owned by `src/main/resources/db/migration/V2__dev2_funds_market_data.sql`,
which runs after Dev 1's `V1__dev1_foundation.sql` and references its
`currency` / `country` tables with real foreign keys.

### Critical business rules implemented

- **`FUND_PRICE` uniqueness** — `UNIQUE(fund_share_class_id, price_date, price_type, provider_id)`,
  anchored to the *internal* share class, not a provider's external fund id.
  Covered by `MarketDataRepositoryIT#duplicateFundPriceSameShareClassDatePriceTypeAndProviderIsRejected`.
- **`FX_RATE` uniqueness** — `UNIQUE(base_currency_id, quote_currency_id, rate_date, rate_type, source_id)`.
- **`FUND_PROVIDER_MAPPING` uniqueness** — `UNIQUE(provider_id, external_fund_id)`.
- **`IMPORT_BATCH.idempotency_key`** is unique, so retrying a batch with the
  same key cannot create a duplicate completed batch (Required Cross-Module
  Test 3). Covered by `ImportRepositoryIT#importBatchIdempotencyKeyPreventsADuplicateCompletedBatch`.
- **Per-item import transaction boundary** — `IMPORT_ITEM` carries its own
  `status`/`error_details` so one bad row does not roll back the rest of the
  batch (no batch-wide `@Transactional` wrapping all items).
- **Optimistic locking** on `ImportBatch` via `@Version`, per the
  architecture doc's Section 32 candidate list.

## Dev 1 modules included (unchanged, merged upstream)

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

   On startup, Flyway automatically applies `V1__dev1_foundation.sql` then
   `V2__dev2_funds_market_data.sql` in order, creating all foundation +
   funds/market-data tables and seeding NOK/SEK/DKK/EUR + NO/SE/DK
   (Dev 1) and providers/FX sources (Dev 2) reference data.

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
`AppUserTest`, `CustomerTest`, `AccountTest` (Dev 1), `FundTest`,
`ImportBatchTest` (Dev 2) — verify the `@PrePersist` defaulting logic
(status defaults, timestamps) directly on the entity.

**Repository-layer integration tests** (Day 2, real Postgres via
Testcontainers — not H2, per the doc's explicit call-out that H2 doesn't
give real NUMERIC precision / constraint behavior):
- `CurrencyRepositoryIT` / `MarketRepositoryIT` — Country / Market Config (Dev 1)
- `IdentityRepositoryIT` — App user, role, and the user_role RBAC junction (Dev 1)
- `CustomerRepositoryIT` — Customer, contacts, identifiers, including the
  partial-unique-index rule ("one primary contact per type") (Dev 1)
- `AccountRepositoryIT` — Account, joint account ownership (Dev 1)
- `FundRepositoryIT` — Fund, FundShareClass, Provider, FundProviderMapping,
  including per-fund (not global) share-class code uniqueness (Dev 2)
- `MarketDataRepositoryIT` — FxRate business key, and the **critical**
  FundPrice deduplication test (Dev 2)
- `ImportRepositoryIT` — ImportJob defaulting, ImportBatch idempotency-key
  retry rejection, per-item failure persistence (Dev 2)

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

## Handing off to Dev 3 / Dev 4

Per Section 44 of the architecture doc, Dev 2 → Dev 3/4 should communicate:

```text
FUND_SHARE_CLASS
PROVIDER
FUND_PRICE
FX_RATE
```

Once this module is merged into `develop`, Dev 3 (Portfolio, Trading &
Ledger) replaces any stubbed `UUID` foreign-key columns with real
`@ManyToOne` references into `FundShareClass` and `Provider`, exactly as
described in Section 12 / Day 2 of the architecture doc. Dev 3's own Flyway
migration should be added as `V3__dev3_portfolio_trading_ledger.sql`,
referencing the tables created here (and Dev 1's). Dev 4 depends further on
`FUND_SHARE_CLASS` / `PROVIDER` (and, from Dev 3, `PORTFOLIO`/`JOURNAL`) and
should use `V4__dev4_dividend_reconciliation_audit.sql`.

Key facts Dev 3/4 need about this module:
- `fund_share_class.id` is the PK they FK into for pricing/positions — not `fund.id`.
- `fund_price` uniqueness is `(fund_share_class_id, price_date, price_type, provider_id)`.
- `fx_rate` uniqueness is `(base_currency_id, quote_currency_id, rate_date, rate_type, source_id)`.
- All money/rate columns use `numeric(24,8)`, per the architecture doc's data-type table.
- Seeded reference rows exist for providers (`MORNINGSTAR`, `BLOOMBERG`, `MANUAL`) and FX sources (`ECB`, `MANUAL`).

## What's *not* in this repo yet

Per scope, this is schema + entities + tests only — no REST controllers, no
business logic (matching engines, reconciliation algorithms, reporting),
and no Dev 3/Dev 4 tables (Portfolio/Trading/Ledger, Dividends/Reconciliation/Audit).
Those are Devs 3–4's deliverables, built on top of this module.
