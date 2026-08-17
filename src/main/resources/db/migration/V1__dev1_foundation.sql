-- =====================================================================
-- Wealth Link Platform
-- Dev 1 — Foundation module
-- Identity & Access | Country / Market Config | Customer Management | Accounts
--
-- This is the dependency root: every other developer's module FKs into
-- CURRENCY / COUNTRY at minimum, and Trading/Ledger + Dividends/Reconciliation
-- FK into CUSTOMER / ACCOUNT. Per the architecture doc, this migration must
-- land first (Day 1) so the other three devs can wire real FKs on Day 2.
-- =====================================================================

create extension if not exists pgcrypto; -- gen_random_uuid()

-- ---------------------------------------------------------------------
-- Identity & Access
-- ---------------------------------------------------------------------

create table app_user (
    id            uuid primary key default gen_random_uuid(),
    username      text not null unique,
    email         text not null unique,
    password_hash text not null,
    status        text not null check (status in ('ACTIVE', 'DISABLED', 'LOCKED')),
    created_at    timestamptz not null default now(),
    updated_at    timestamptz not null default now()
);

create table role (
    id   uuid primary key default gen_random_uuid(),
    name text not null unique
);

create table user_role (
    user_id uuid not null references app_user (id) on delete cascade,
    role_id uuid not null references role (id) on delete cascade,
    primary key (user_id, role_id)
);

-- ---------------------------------------------------------------------
-- Country / Market Configuration
-- ---------------------------------------------------------------------

create table currency (
    id                uuid primary key default gen_random_uuid(),
    iso_code            varchar(3) not null unique,
    name              text not null,
    minor_unit_digits smallint not null check (minor_unit_digits >= 0)
);

create table country (
    id                  uuid primary key default gen_random_uuid(),
    iso_code            varchar(2) not null unique,
    name                text not null,
    default_currency_id uuid not null references currency (id) on delete restrict,
    timezone            text not null
);

create table market (
    id         uuid primary key default gen_random_uuid(),
    country_id uuid not null references country (id) on delete restrict,
    name       text not null,
    mic_code   text not null,
    timezone   text not null,
    status     text not null check (status in ('ACTIVE', 'SUSPENDED', 'CLOSED'))
);

-- ---------------------------------------------------------------------
-- Customer Management
-- ---------------------------------------------------------------------

create table customer (
    id                        uuid primary key default gen_random_uuid(),
    customer_number           text not null unique,
    customer_type             text not null check (customer_type in ('INDIVIDUAL', 'CORPORATE')),
    status                    text not null check (status in ('ACTIVE', 'PENDING_KYC', 'SUSPENDED', 'CLOSED')),
    country_id                uuid not null references country (id) on delete restrict,
    tax_residency_country_id  uuid not null references country (id) on delete restrict,
    created_at                timestamptz not null default now(),
    updated_at                timestamptz not null default now()
);

create table customer_contact (
    id          uuid primary key default gen_random_uuid(),
    customer_id uuid not null references customer (id) on delete restrict,
    contact_type text not null check (contact_type in ('EMAIL', 'PHONE', 'ADDRESS')),
    value       text not null,
    is_primary  boolean not null default false
);

-- Only one primary contact per (customer, contact_type)
create unique index uq_customer_contact_primary
    on customer_contact (customer_id, contact_type)
    where is_primary = true;

create table customer_identifier (
    id                 uuid primary key default gen_random_uuid(),
    customer_id        uuid not null references customer (id) on delete restrict,
    id_type            text not null,
    id_value           text not null,
    issuing_country_id uuid not null references country (id) on delete restrict
);

-- ---------------------------------------------------------------------
-- Accounts
-- ---------------------------------------------------------------------

create table account (
    id             uuid primary key default gen_random_uuid(),
    account_number text not null unique,
    account_type   text not null check (account_type in ('CASH', 'INVESTMENT')),
    currency_id    uuid not null references currency (id) on delete restrict,
    country_id     uuid not null references country (id) on delete restrict,
    status         text not null check (status in ('ACTIVE', 'DORMANT', 'CLOSED')),
    opened_at      timestamptz not null default now()
);

create table account_owner (
    id             uuid primary key default gen_random_uuid(),
    account_id     uuid not null references account (id) on delete restrict,
    customer_id    uuid not null references customer (id) on delete restrict,
    ownership_role text not null check (ownership_role in ('PRIMARY', 'JOINT'))
);

-- ---------------------------------------------------------------------
-- Indexes (per Section 6 of the architecture doc, foundation subset)
-- ---------------------------------------------------------------------

create index ix_market_country on market (country_id);
create index ix_customer_country on customer (country_id);
create index ix_customer_contact_customer on customer_contact (customer_id);
create index ix_customer_identifier_customer on customer_identifier (customer_id);
create index ix_account_owner_account on account_owner (account_id);
create index ix_account_owner_customer on account_owner (customer_id);

-- customer_number / account_number / currency.iso_code / country.iso_code
-- already have unique indexes via their UNIQUE constraints above.

-- ---------------------------------------------------------------------
-- Seed reference data (NOK/SEK/DKK, NO/SE/DK) — per Day 4 handoff notes
-- ---------------------------------------------------------------------

insert into currency (iso_code, name, minor_unit_digits) values
    ('NOK', 'Norwegian Krone', 2),
    ('SEK', 'Swedish Krona', 2),
    ('DKK', 'Danish Krone', 2),
    ('EUR', 'Euro', 2);

insert into country (iso_code, name, default_currency_id, timezone)
select 'NO', 'Norway', id, 'Europe/Oslo' from currency where iso_code = 'NOK';

insert into country (iso_code, name, default_currency_id, timezone)
select 'SE', 'Sweden', id, 'Europe/Stockholm' from currency where iso_code = 'SEK';

insert into country (iso_code, name, default_currency_id, timezone)
select 'DK', 'Denmark', id, 'Europe/Copenhagen' from currency where iso_code = 'DKK';
