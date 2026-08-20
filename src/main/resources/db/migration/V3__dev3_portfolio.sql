-- =====================================================================
-- Wealth Link Platform
-- Dev 3 — Portfolio & Positions module
-- PostgreSQL Version
--
-- Depends on:
--   Dev 1 — Foundation module
--   Dev 2 — Funds & Market Data module
--
-- Required tables:
--   account
--   currency
--   fund_share_class
-- =====================================================================


-- ---------------------------------------------------------------------
-- UUID Support
-- ---------------------------------------------------------------------

CREATE EXTENSION IF NOT EXISTS pgcrypto;


-- ---------------------------------------------------------------------
-- Portfolio
-- ---------------------------------------------------------------------

CREATE TABLE portfolio (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    account_id       UUID NOT NULL
                     REFERENCES account(id)
                     ON DELETE RESTRICT,

    portfolio_number VARCHAR(255) NOT NULL UNIQUE,

    portfolio_type   VARCHAR(255) NOT NULL
                     CHECK (
                         portfolio_type IN (
                             'STANDARD',
                             'RETIREMENT',
                             'MARGIN'
                         )
                     ),

    base_currency_id UUID NOT NULL
                     REFERENCES currency(id)
                     ON DELETE RESTRICT,

    status           VARCHAR(255) NOT NULL
                     CHECK (
                         status IN (
                             'ACTIVE',
                             'CLOSED',
                             'SUSPENDED'
                         )
                     ),

    opened_at        TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE INDEX ix_portfolio_account
    ON portfolio(account_id);


-- ---------------------------------------------------------------------
-- Portfolio Valuation Snapshot
-- ---------------------------------------------------------------------

CREATE TABLE portfolio_valuation_snapshot (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    portfolio_id   UUID NOT NULL
                   REFERENCES portfolio(id)
                   ON DELETE RESTRICT,

    valuation_date DATE NOT NULL,

    total_value    DECIMAL(24,6) NOT NULL,

    currency_id    UUID NOT NULL
                   REFERENCES currency(id)
                   ON DELETE RESTRICT,

    created_at     TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_portfolio_valuation_date
        UNIQUE (
            portfolio_id,
            valuation_date
        )
);


-- ---------------------------------------------------------------------
-- Position
-- ---------------------------------------------------------------------

CREATE TABLE position (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    portfolio_id           UUID NOT NULL
                           REFERENCES portfolio(id)
                           ON DELETE RESTRICT,

    fund_share_class_id    UUID NOT NULL
                           REFERENCES fund_share_class(id)
                           ON DELETE RESTRICT,

    position_date          DATE NOT NULL,

    quantity               DECIMAL(24,8) NOT NULL
                           CHECK (quantity >= 0),

    average_cost           DECIMAL(24,8) NOT NULL,

    cost_basis_currency_id UUID NOT NULL
                           REFERENCES currency(id)
                           ON DELETE RESTRICT,

    market_value           DECIMAL(24,6) NOT NULL,

    currency_id            UUID NOT NULL
                           REFERENCES currency(id)
                           ON DELETE RESTRICT,

    status                 VARCHAR(255) NOT NULL
                           CHECK (
                               status IN (
                                   'OPEN',
                                   'CLOSED',
                                   'RECONCILED'
                               )
                           ),

    computed_at            TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_position_portfolio_fund_date
        UNIQUE (
            portfolio_id,
            fund_share_class_id,
            position_date
        )
);


-- ---------------------------------------------------------------------
-- Indexes
-- ---------------------------------------------------------------------

CREATE INDEX ix_position_portfolio_date
    ON position (
        portfolio_id,
        position_date DESC
    );-- =====================================================================
-- Wealth Link Platform
-- Dev 3 - Ledger module (double-entry bookkeeping)
-- LedgerAccount | Journal | JournalEntry
--
-- Depends on V1__dev1_foundation.sql for currency.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Ledger Account
-- Financial bookkeeping account (e.g. Cash, Investments, Fees Payable).
-- ---------------------------------------------------------------------

create table ledger_account (
    id            UUID primary key default (gen_random_uuid()),
    account_code  varchar(30) not null unique,
    account_name  varchar(255) not null,
    account_type  varchar(20) not null check (account_type in ('ASSET', 'LIABILITY', 'EQUITY', 'REVENUE', 'EXPENSE')),
    status        varchar(10) not null check (status in ('ACTIVE', 'CLOSED')),
    currency_id   UUID not null references currency (id) on delete restrict,
    balance       decimal(24, 6) not null default 0,
    description   varchar(500),
    created_at    timestamp(6) not null default current_timestamp(6),
    updated_at    timestamp(6) not null default current_timestamp(6)
);

create index ix_ledger_account_account_type on ledger_account (account_type);
create index ix_ledger_account_status on ledger_account (status);

-- ---------------------------------------------------------------------
-- Journal
-- Records a financial transaction using double-entry bookkeeping.
-- A posted journal must have balanced entries (debits == credits).
-- ---------------------------------------------------------------------

create table journal (
    id                UUID primary key default (gen_random_uuid()),
    journal_reference varchar(50) not null unique,
    description       varchar(500) not null,
    status            varchar(20) not null check (status in ('DRAFT', 'POSTED', 'REVERSED')),
    journal_date      date not null,
    source_reference  varchar(100),
    created_at        timestamp(6) not null default current_timestamp(6),
    updated_at        timestamp(6) not null default current_timestamp(6)
);

create index ix_journal_status on journal (status);
create index ix_journal_journal_date on journal (journal_date desc);
create index ix_journal_source_reference on journal (source_reference);

-- ---------------------------------------------------------------------
-- Journal Entry
-- A single debit or credit line within a Journal.
-- Total debits must equal total credits per Journal (enforced at app layer).
-- ---------------------------------------------------------------------

create table journal_entry (
    id                UUID primary key default (gen_random_uuid()),
    journal_id        UUID not null references journal (id) on delete restrict,
    ledger_account_id UUID not null references ledger_account (id) on delete restrict,
    entry_type        varchar(10) not null check (entry_type in ('DEBIT', 'CREDIT')),
    amount            decimal(24, 6) not null check (amount > 0),
    currency_id       UUID not null references currency (id) on delete restrict,
    description       varchar(500),
    created_at        timestamp(6) not null default current_timestamp(6)
);

create index ix_journal_entry_journal on journal_entry (journal_id);
create index ix_journal_entry_ledger_account on journal_entry (ledger_account_id);
create index ix_journal_entry_entry_type on journal_entry (entry_type);
-- =====================================================================
-- Wealth Link Platform
-- Dev 6 — Schema Alignment
-- Adds missing columns and constraints to align with the architecture document.
-- =====================================================================

-- 1. trade_order
ALTER TABLE trade_order RENAME COLUMN side TO order_type;
ALTER TABLE trade_order DROP CONSTRAINT IF EXISTS trade_order_side_check;
ALTER TABLE trade_order ADD CONSTRAINT trade_order_order_type_check CHECK (order_type IN ('BUY','SELL','SUBSCRIBE','REDEEM','TRANSFER','CANCEL'));
ALTER TABLE trade_order ADD COLUMN idempotency_key varchar(255);
ALTER TABLE trade_order ADD CONSTRAINT uq_trade_order_idempotency_key UNIQUE (idempotency_key);
ALTER TABLE trade_order ADD COLUMN limit_price decimal(24, 8);
ALTER TABLE trade_order ADD COLUMN version integer not null default 0;

-- 2. trade_execution
ALTER TABLE trade_execution ADD COLUMN trade_date date;
ALTER TABLE trade_execution ADD COLUMN external_reference varchar(255);
ALTER TABLE trade_execution ADD COLUMN version integer not null default 0;

-- 3. settlement
ALTER TABLE settlement ADD COLUMN version integer not null default 0;

-- 4. journal
ALTER TABLE journal ADD COLUMN journal_type varchar(20) not null default 'TRADE' check (journal_type in ('TRADE','DIVIDEND','DEPOSIT','WITHDRAWAL','FX','FEE','ADJUSTMENT','REVERSAL'));
ALTER TABLE journal DROP COLUMN IF EXISTS source_reference;
ALTER TABLE journal ADD COLUMN reference_type varchar(50);
ALTER TABLE journal ADD COLUMN reference_id UUID;
ALTER TABLE journal ADD COLUMN posting_date date;
ALTER TABLE journal ADD COLUMN value_date date;
ALTER TABLE journal ADD COLUMN idempotency_key varchar(255);
ALTER TABLE journal ADD CONSTRAINT uq_journal_idempotency_key UNIQUE (idempotency_key);
ALTER TABLE journal ADD COLUMN reversed_journal_id UUID references journal (id) on delete restrict;

-- 5. journal_entry
ALTER TABLE journal_entry RENAME COLUMN entry_type TO direction;
ALTER TABLE journal_entry DROP CONSTRAINT IF EXISTS journal_entry_entry_type_check;
ALTER TABLE journal_entry ADD CONSTRAINT journal_entry_direction_check CHECK (direction IN ('DEBIT','CREDIT'));

-- 6. ledger_account
ALTER TABLE ledger_account ADD COLUMN account_id UUID references account (id) on delete restrict;
ALTER TABLE ledger_account ADD COLUMN portfolio_id UUID references portfolio (id) on delete restrict;
ALTER TABLE ledger_account RENAME COLUMN account_type TO ledger_account_type;
ALTER TABLE ledger_account DROP CONSTRAINT IF EXISTS ledger_account_account_type_check;
ALTER TABLE ledger_account ADD CONSTRAINT ledger_account_type_check CHECK (ledger_account_type IN ('CASH','POSITION','FEE','TAX','SUSPENSE'));
ALTER TABLE ledger_account ADD CONSTRAINT chk_ledger_account_owner CHECK (account_id IS NOT NULL OR portfolio_id IS NOT NULL);

-- 7. import_batch
ALTER TABLE import_batch ADD COLUMN records_received integer not null default 0;
ALTER TABLE import_batch ADD COLUMN records_failed integer not null default 0;

-- 8. reconciliation_item
ALTER TABLE reconciliation_item ADD COLUMN version integer not null default 0;
