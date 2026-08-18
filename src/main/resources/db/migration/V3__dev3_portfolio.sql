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
    );