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
