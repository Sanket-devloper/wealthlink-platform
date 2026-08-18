CREATE TABLE IF NOT EXISTS dividend_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fund_share_class_id UUID NOT NULL REFERENCES fund_share_class (id) ON DELETE RESTRICT,
    currency_id UUID NOT NULL REFERENCES currency (id) ON DELETE RESTRICT,
    ex_date DATE NOT NULL,
    record_date DATE NOT NULL,
    payment_date DATE NOT NULL,
    dividend_per_unit NUMERIC(18, 8) NOT NULL CHECK (dividend_per_unit >= 0),
    status VARCHAR(50) NOT NULL,
    source VARCHAR(100) NOT NULL,
    corrected_from_event_id UUID REFERENCES dividend_event(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dividend_allocation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dividend_event_id UUID NOT NULL REFERENCES dividend_event(id) ON DELETE RESTRICT,
    portfolio_id UUID NOT NULL REFERENCES portfolio(id) ON DELETE RESTRICT,
    position_quantity NUMERIC(24, 8) NOT NULL CHECK (position_quantity >= 0),
    gross_amount NUMERIC(24, 6) NOT NULL CHECK (gross_amount >= 0),
    tax_amount NUMERIC(24, 6) NOT NULL DEFAULT 0 CHECK (tax_amount >= 0),
    net_amount NUMERIC(24, 6) NOT NULL CHECK (net_amount >= 0),
    currency_id UUID NOT NULL REFERENCES currency (id) ON DELETE RESTRICT,
    journal_id UUID,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_div_alloc_event_portfolio UNIQUE (dividend_event_id, portfolio_id)
);

CREATE INDEX idx_div_event_dates ON dividend_event (ex_date, record_date, payment_date);
CREATE INDEX idx_div_alloc_portfolio ON dividend_allocation (portfolio_id);

-- Reconciliation
CREATE TABLE IF NOT EXISTS reconciliation_run (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    run_type VARCHAR(100) NOT NULL,
    business_date DATE NOT NULL,
    started_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMPTZ,
    status VARCHAR(50) NOT NULL,
    initiated_by UUID REFERENCES app_user(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS external_record (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reconciliation_run_id UUID NOT NULL REFERENCES reconciliation_run(id) ON DELETE RESTRICT,
    provider_id UUID NOT NULL REFERENCES provider(id) ON DELETE RESTRICT,
    external_reference VARCHAR(255) NOT NULL,
    raw_payload JSONB NOT NULL,
    received_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reconciliation_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reconciliation_run_id UUID NOT NULL REFERENCES reconciliation_run(id) ON DELETE RESTRICT,
    external_record_id UUID REFERENCES external_record(id) ON DELETE RESTRICT,
    internal_reference_type VARCHAR(100),
    internal_reference_id UUID,
    match_status VARCHAR(50) NOT NULL,
    difference_details JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_recon_match_status CHECK (match_status IN (
        'MATCHED', 'MISSING_INTERNAL', 'MISSING_EXTERNAL', 'AMOUNT_MISMATCH',
        'QUANTITY_MISMATCH', 'PRICE_MISMATCH', 'DATE_MISMATCH', 'DUPLICATE', 'ERROR'
    ))
);

CREATE TABLE IF NOT EXISTS reconciliation_resolution (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reconciliation_item_id UUID NOT NULL UNIQUE REFERENCES reconciliation_item(id) ON DELETE RESTRICT,
    resolved_by UUID NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
    resolution_type VARCHAR(100) NOT NULL,
    resolution_notes TEXT,
    resolved_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_recon_item_run_status ON reconciliation_item (reconciliation_run_id, match_status);

-- Audit
CREATE TABLE IF NOT EXISTS audit_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES app_user(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    old_value JSONB,
    new_value JSONB,
    correlation_id VARCHAR(255),
    ip_address VARCHAR(45),
    request_id VARCHAR(255),
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_event_lookup ON audit_event (entity_type, entity_id, occurred_at DESC);
CREATE INDEX idx_audit_event_corr ON audit_event (correlation_id);