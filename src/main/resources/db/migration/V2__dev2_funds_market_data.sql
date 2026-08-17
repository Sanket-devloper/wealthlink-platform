-- =====================================================================
-- Wealth Link Platform
-- Dev 2 — Funds & Market Data module
-- Funds | Share Classes | Providers | FX Rates | Fund Prices | Imports
--
-- Depends on V1__dev1_foundation.sql for CURRENCY / COUNTRY. Real FKs are
-- used directly (no UUID stubs) because Dev 1's foundation module is
-- already merged into this codebase.
--
-- Everything Dev 3 (Portfolio/Trading/Ledger) needs from this module is
-- FUND_SHARE_CLASS and PROVIDER — see README.local.md handoff notes.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Funds
-- ---------------------------------------------------------------------

create table fund (
    id                  uuid primary key default gen_random_uuid(),
    isin                varchar(12) not null unique,
    name                text not null,
    base_currency_id    uuid not null references currency (id) on delete restrict,
    domicile_country_id uuid not null references country (id) on delete restrict,
    status              text not null check (status in ('ACTIVE', 'SUSPENDED', 'CLOSED')),
    inception_date      date,
    created_at          timestamptz not null default now(),
    updated_at          timestamptz not null default now()
);

create table fund_share_class (
    id          uuid primary key default gen_random_uuid(),
    fund_id     uuid not null references fund (id) on delete restrict,
    class_code  varchar(30) not null,
    name        text not null,
    currency_id uuid not null references currency (id) on delete restrict,
    status      text not null check (status in ('ACTIVE', 'CLOSED')),
    constraint uq_fund_share_class_fund_code unique (fund_id, class_code)
);

-- ---------------------------------------------------------------------
-- Providers
-- ---------------------------------------------------------------------

create table provider (
    id     uuid primary key default gen_random_uuid(),
    code   varchar(30) not null unique,
    name   text not null,
    status text not null check (status in ('ACTIVE', 'DISABLED'))
);

create table fund_provider_mapping (
    id                  uuid primary key default gen_random_uuid(),
    fund_share_class_id uuid not null references fund_share_class (id) on delete restrict,
    provider_id         uuid not null references provider (id) on delete restrict,
    external_fund_id    text not null,
    constraint uq_fund_provider_mapping_provider_external_id unique (provider_id, external_fund_id)
);

-- ---------------------------------------------------------------------
-- FX Rates
-- ---------------------------------------------------------------------

create table fx_rate_source (
    id   uuid primary key default gen_random_uuid(),
    code varchar(30) not null unique,
    name text not null
);

create table fx_rate (
    id                 uuid primary key default gen_random_uuid(),
    base_currency_id   uuid not null references currency (id) on delete restrict,
    quote_currency_id  uuid not null references currency (id) on delete restrict,
    rate_date          date not null,
    rate_type          text not null check (rate_type in ('SPOT', 'CLOSE')),
    source_id          uuid not null references fx_rate_source (id) on delete restrict,
    rate               numeric(24, 8) not null check (rate > 0),
    constraint uq_fx_rate_business_key
        unique (base_currency_id, quote_currency_id, rate_date, rate_type, source_id)
);

-- ---------------------------------------------------------------------
-- Imports (job -> batch -> item), created before fund_price because
-- fund_price.import_batch_id references import_batch.
-- ---------------------------------------------------------------------

create table import_job (
    id          uuid primary key default gen_random_uuid(),
    name        text not null,
    provider_id uuid not null references provider (id) on delete restrict,
    job_type    text not null check (job_type in ('FUND_PRICE_IMPORT', 'FX_RATE_IMPORT')),
    status      text not null check (status in ('ACTIVE', 'DISABLED')),
    created_at  timestamptz not null default now()
);

create table import_batch (
    id              uuid primary key default gen_random_uuid(),
    import_job_id   uuid not null references import_job (id) on delete restrict,
    idempotency_key text not null unique,
    status          text not null check (status in ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED')),
    started_at      timestamptz not null default now(),
    completed_at    timestamptz,
    total_items     integer not null default 0,
    success_count   integer not null default 0,
    failure_count   integer not null default 0,
    version         integer not null default 0
);

-- ---------------------------------------------------------------------
-- Fund prices
-- ---------------------------------------------------------------------

create table fund_price (
    id                   uuid primary key default gen_random_uuid(),
    fund_share_class_id  uuid not null references fund_share_class (id) on delete restrict,
    price_date           date not null,
    price_type           text not null check (price_type in ('NAV', 'BID', 'ASK')),
    provider_id          uuid not null references provider (id) on delete restrict,
    currency_id          uuid not null references currency (id) on delete restrict,
    price                numeric(24, 8) not null check (price > 0),
    import_batch_id      uuid references import_batch (id) on delete set null,
    -- Critical business rule (DEV2-D1 acceptance criteria): uniqueness is
    -- anchored to the internal fund_share_class_id, not any provider's
    -- external identifier, per the architecture doc.
    constraint uq_fund_price_business_key
        unique (fund_share_class_id, price_date, price_type, provider_id)
);

-- import_item comes after fund_price because it optionally points at the
-- fund_price row it produced.
create table import_item (
    id               uuid primary key default gen_random_uuid(),
    import_batch_id  uuid not null references import_batch (id) on delete restrict,
    raw_payload      jsonb not null,
    status           text not null check (status in ('PENDING', 'SUCCESS', 'FAILED')),
    error_details    text,
    fund_price_id    uuid references fund_price (id) on delete set null,
    processed_at     timestamptz
);

-- ---------------------------------------------------------------------
-- Indexes (per Section 31 of the architecture doc, Dev 2 subset)
-- ---------------------------------------------------------------------

create index ix_fund_base_currency on fund (base_currency_id);
create index ix_fund_domicile_country on fund (domicile_country_id);
create index ix_fund_share_class_fund on fund_share_class (fund_id);
create index ix_fund_share_class_currency on fund_share_class (currency_id);
create index ix_fund_provider_mapping_share_class on fund_provider_mapping (fund_share_class_id);
create index ix_fund_provider_mapping_provider on fund_provider_mapping (provider_id);

create index ix_fx_rate_base_quote_date on fx_rate (base_currency_id, quote_currency_id, rate_date desc);

create index ix_fund_price_share_class_date on fund_price (fund_share_class_id, price_date desc);
create index ix_fund_price_import_batch on fund_price (import_batch_id);

create index ix_import_batch_job on import_batch (import_job_id);
create index ix_import_item_batch on import_item (import_batch_id);
create index ix_import_item_fund_price on import_item (fund_price_id);

-- code/isin/class_code/idempotency_key already have unique indexes via
-- their UNIQUE constraints above.

-- ---------------------------------------------------------------------
-- Seed reference data — per Day 4 handoff notes (DEV2-D4)
-- ---------------------------------------------------------------------

insert into provider (code, name, status) values
    ('MORNINGSTAR', 'Morningstar', 'ACTIVE'),
    ('BLOOMBERG', 'Bloomberg', 'ACTIVE'),
    ('MANUAL', 'Manual Entry', 'ACTIVE');

insert into fx_rate_source (code, name) values
    ('ECB', 'European Central Bank'),
    ('MANUAL', 'Manual Entry');
