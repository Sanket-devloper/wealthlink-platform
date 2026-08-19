-- =====================================================================
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
