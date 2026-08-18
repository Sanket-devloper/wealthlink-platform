-- =====================================================================
-- Wealth Link Platform
-- Dev 2 — Funds & Market Data module
-- PostgreSQL Version
--
-- Depends on V1__dev1_foundation.sql
-- Required tables from Dev 1:
--   currency
--   country
-- =====================================================================


-- ---------------------------------------------------------------------
-- UUID Support
-- ---------------------------------------------------------------------

CREATE EXTENSION IF NOT EXISTS pgcrypto;


-- ---------------------------------------------------------------------
-- Funds
-- ---------------------------------------------------------------------

CREATE TABLE fund (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    isin                VARCHAR(12) NOT NULL UNIQUE,
    name                VARCHAR(255) NOT NULL,

    base_currency_id    UUID NOT NULL
                        REFERENCES currency(id)
                        ON DELETE RESTRICT,

    domicile_country_id UUID NOT NULL
                        REFERENCES country(id)
                        ON DELETE RESTRICT,

    status              VARCHAR(255) NOT NULL
                        CHECK (
                            status IN (
                                'ACTIVE',
                                'SUSPENDED',
                                'CLOSED'
                            )
                        ),

    inception_date      DATE,

    created_at          TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE fund_share_class (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    fund_id     UUID NOT NULL
                REFERENCES fund(id)
                ON DELETE RESTRICT,

    class_code  VARCHAR(30) NOT NULL,

    name        VARCHAR(255) NOT NULL,

    currency_id UUID NOT NULL
                REFERENCES currency(id)
                ON DELETE RESTRICT,

    status      VARCHAR(255) NOT NULL
                CHECK (
                    status IN (
                        'ACTIVE',
                        'CLOSED'
                    )
                ),

    CONSTRAINT uq_fund_share_class_fund_code
        UNIQUE (fund_id, class_code)
);


-- ---------------------------------------------------------------------
-- Providers
-- ---------------------------------------------------------------------

CREATE TABLE provider (
    id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code   VARCHAR(30) NOT NULL UNIQUE,
    name   VARCHAR(255) NOT NULL,

    status VARCHAR(255) NOT NULL
           CHECK (
               status IN (
                   'ACTIVE',
                   'DISABLED'
               )
           )
);


CREATE TABLE fund_provider_mapping (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    fund_share_class_id UUID NOT NULL
                        REFERENCES fund_share_class(id)
                        ON DELETE RESTRICT,

    provider_id         UUID NOT NULL
                        REFERENCES provider(id)
                        ON DELETE RESTRICT,

    external_fund_id    VARCHAR(255) NOT NULL,

    CONSTRAINT uq_fund_provider_mapping_provider_external_id
        UNIQUE (provider_id, external_fund_id)
);


-- ---------------------------------------------------------------------
-- FX Rates
-- ---------------------------------------------------------------------

CREATE TABLE fx_rate_source (
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL
);


CREATE TABLE fx_rate (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    base_currency_id  UUID NOT NULL
                      REFERENCES currency(id)
                      ON DELETE RESTRICT,

    quote_currency_id UUID NOT NULL
                      REFERENCES currency(id)
                      ON DELETE RESTRICT,

    rate_date         DATE NOT NULL,

    rate_type         VARCHAR(255) NOT NULL
                      CHECK (
                          rate_type IN (
                              'SPOT',
                              'CLOSE'
                          )
                      ),

    source_id         UUID NOT NULL
                      REFERENCES fx_rate_source(id)
                      ON DELETE RESTRICT,

    rate              DECIMAL(24, 8) NOT NULL
                      CHECK (rate > 0),

    CONSTRAINT uq_fx_rate_business_key
        UNIQUE (
            base_currency_id,
            quote_currency_id,
            rate_date,
            rate_type,
            source_id
        )
);


-- ---------------------------------------------------------------------
-- Imports
-- Job -> Batch -> Item
--
-- import_batch must be created before fund_price because
-- fund_price.import_batch_id references import_batch.
-- ---------------------------------------------------------------------

CREATE TABLE import_job (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name        VARCHAR(255) NOT NULL,

    provider_id UUID NOT NULL
                REFERENCES provider(id)
                ON DELETE RESTRICT,

    job_type    VARCHAR(255) NOT NULL
                CHECK (
                    job_type IN (
                        'FUND_PRICE_IMPORT',
                        'FX_RATE_IMPORT'
                    )
                ),

    status      VARCHAR(255) NOT NULL
                CHECK (
                    status IN (
                        'ACTIVE',
                        'DISABLED'
                    )
                ),

    created_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE import_batch (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    import_job_id   UUID NOT NULL
                    REFERENCES import_job(id)
                    ON DELETE RESTRICT,

    idempotency_key VARCHAR(255) NOT NULL UNIQUE,

    status          VARCHAR(255) NOT NULL
                    CHECK (
                        status IN (
                            'PENDING',
                            'RUNNING',
                            'COMPLETED',
                            'FAILED'
                        )
                    ),

    started_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    completed_at    TIMESTAMP(6),

    total_items     INTEGER NOT NULL DEFAULT 0,

    success_count   INTEGER NOT NULL DEFAULT 0,

    failure_count   INTEGER NOT NULL DEFAULT 0,

    version         INTEGER NOT NULL DEFAULT 0
);


-- ---------------------------------------------------------------------
-- Fund Prices
-- ---------------------------------------------------------------------

CREATE TABLE fund_price (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    fund_share_class_id UUID NOT NULL
                        REFERENCES fund_share_class(id)
                        ON DELETE RESTRICT,

    price_date          DATE NOT NULL,

    price_type          VARCHAR(255) NOT NULL
                        CHECK (
                            price_type IN (
                                'NAV',
                                'BID',
                                'ASK'
                            )
                        ),

    provider_id         UUID NOT NULL
                        REFERENCES provider(id)
                        ON DELETE RESTRICT,

    currency_id         UUID NOT NULL
                        REFERENCES currency(id)
                        ON DELETE RESTRICT,

    price               DECIMAL(24, 8) NOT NULL
                        CHECK (price > 0),

    import_batch_id     UUID
                        REFERENCES import_batch(id)
                        ON DELETE SET NULL,

    -- Business rule:
    -- Uniqueness is based on the internal fund_share_class_id,
    -- not the provider's external identifier.

    CONSTRAINT uq_fund_price_business_key
        UNIQUE (
            fund_share_class_id,
            price_date,
            price_type,
            provider_id
        )
);


-- ---------------------------------------------------------------------
-- Import Items
--
-- Created after fund_price because it optionally references fund_price.
-- ---------------------------------------------------------------------

CREATE TABLE import_item (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    import_batch_id UUID NOT NULL
                    REFERENCES import_batch(id)
                    ON DELETE RESTRICT,

    raw_payload     JSONB NOT NULL,

    status          VARCHAR(255) NOT NULL
                    CHECK (
                        status IN (
                            'PENDING',
                            'SUCCESS',
                            'FAILED'
                        )
                    ),

    error_details   VARCHAR(255),

    fund_price_id   UUID
                    REFERENCES fund_price(id)
                    ON DELETE SET NULL,

    processed_at    TIMESTAMP(6)
);


-- ---------------------------------------------------------------------
-- Indexes
-- ---------------------------------------------------------------------

CREATE INDEX ix_fund_base_currency
    ON fund(base_currency_id);

CREATE INDEX ix_fund_domicile_country
    ON fund(domicile_country_id);

CREATE INDEX ix_fund_share_class_fund
    ON fund_share_class(fund_id);

CREATE INDEX ix_fund_share_class_currency
    ON fund_share_class(currency_id);

CREATE INDEX ix_fund_provider_mapping_share_class
    ON fund_provider_mapping(fund_share_class_id);

CREATE INDEX ix_fund_provider_mapping_provider
    ON fund_provider_mapping(provider_id);


CREATE INDEX ix_fx_rate_base_quote_date
    ON fx_rate (
        base_currency_id,
        quote_currency_id,
        rate_date DESC
    );


CREATE INDEX ix_fund_price_share_class_date
    ON fund_price (
        fund_share_class_id,
        price_date DESC
    );

CREATE INDEX ix_fund_price_import_batch
    ON fund_price(import_batch_id);


CREATE INDEX ix_import_batch_job
    ON import_batch(import_job_id);

CREATE INDEX ix_import_item_batch
    ON import_item(import_batch_id);

CREATE INDEX ix_import_item_fund_price
    ON import_item(fund_price_id);


-- ---------------------------------------------------------------------
-- Seed Reference Data
-- ---------------------------------------------------------------------

INSERT INTO provider (
    code,
    name,
    status
)
VALUES
    ('MORNINGSTAR', 'Morningstar', 'ACTIVE'),
    ('BLOOMBERG', 'Bloomberg', 'ACTIVE'),
    ('MANUAL', 'Manual Entry', 'ACTIVE');


INSERT INTO fx_rate_source (
    code,
    name
)
VALUES
    ('ECB', 'European Central Bank'),
    ('MANUAL', 'Manual Entry');