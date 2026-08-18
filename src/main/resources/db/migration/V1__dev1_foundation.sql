CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Identity & Access
CREATE TABLE app_user (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username      VARCHAR(255) NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status        VARCHAR(255) NOT NULL CHECK (status IN ('ACTIVE', 'DISABLED', 'LOCKED')),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE role (
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE user_role (
    user_id UUID NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES role (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Country / Market Configuration
CREATE TABLE currency (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    iso_code          VARCHAR(3) NOT NULL UNIQUE,
    name              VARCHAR(255) NOT NULL,
    minor_unit_digits SMALLINT NOT NULL CHECK (minor_unit_digits >= 0)
);

CREATE TABLE country (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    iso_code            VARCHAR(2) NOT NULL UNIQUE,
    name                VARCHAR(255) NOT NULL,
    default_currency_id UUID NOT NULL REFERENCES currency (id) ON DELETE RESTRICT,
    timezone            VARCHAR(255) NOT NULL
);

CREATE TABLE market (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country_id UUID NOT NULL REFERENCES country (id) ON DELETE RESTRICT,
    name       VARCHAR(255) NOT NULL,
    mic_code   VARCHAR(255) NOT NULL,
    timezone   VARCHAR(255) NOT NULL,
    status     VARCHAR(255) NOT NULL CHECK (status IN ('ACTIVE', 'SUSPENDED', 'CLOSED'))
);

-- Customer Management
CREATE TABLE customer (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_number           VARCHAR(255) NOT NULL UNIQUE,
    customer_type             VARCHAR(255) NOT NULL CHECK (customer_type IN ('INDIVIDUAL', 'CORPORATE')),
    status                    VARCHAR(255) NOT NULL CHECK (status IN ('ACTIVE', 'PENDING_KYC', 'SUSPENDED', 'CLOSED')),
    country_id                UUID NOT NULL REFERENCES country (id) ON DELETE RESTRICT,
    tax_residency_country_id  UUID NOT NULL REFERENCES country (id) ON DELETE RESTRICT,
    created_at                TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE customer_contact (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customer (id) ON DELETE RESTRICT,
    contact_type VARCHAR(255) NOT NULL CHECK (contact_type IN ('EMAIL', 'PHONE', 'ADDRESS')),
    value       VARCHAR(255) NOT NULL,
    is_primary  BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX uq_customer_contact_primary
    ON customer_contact (customer_id, contact_type)
    WHERE is_primary = true;

CREATE TABLE customer_identifier (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id        UUID NOT NULL REFERENCES customer (id) ON DELETE RESTRICT,
    id_type            VARCHAR(255) NOT NULL,
    id_value           VARCHAR(255) NOT NULL,
    issuing_country_id UUID NOT NULL REFERENCES country (id) ON DELETE RESTRICT
);

-- Accounts
CREATE TABLE account (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_number VARCHAR(255) NOT NULL UNIQUE,
    account_type   VARCHAR(255) NOT NULL CHECK (account_type IN ('CASH', 'INVESTMENT')),
    currency_id    UUID NOT NULL REFERENCES currency (id) ON DELETE RESTRICT,
    country_id     UUID NOT NULL REFERENCES country (id) ON DELETE RESTRICT,
    status         VARCHAR(255) NOT NULL CHECK (status IN ('ACTIVE', 'DORMANT', 'CLOSED')),
    opened_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE account_owner (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id     UUID NOT NULL REFERENCES account (id) ON DELETE RESTRICT,
    customer_id    UUID NOT NULL REFERENCES customer (id) ON DELETE RESTRICT,
    ownership_role VARCHAR(255) NOT NULL CHECK (ownership_role IN ('PRIMARY', 'JOINT'))
);

-- Indexes
CREATE INDEX ix_market_country ON market (country_id);
CREATE INDEX ix_customer_country ON customer (country_id);
CREATE INDEX ix_customer_contact_customer ON customer_contact (customer_id);
CREATE INDEX ix_customer_identifier_customer ON customer_identifier (customer_id);
CREATE INDEX ix_account_owner_account ON account_owner (account_id);
CREATE INDEX ix_account_owner_customer ON account_owner (customer_id);

-- Seed reference data
INSERT INTO currency (iso_code, name, minor_unit_digits) VALUES
    ('NOK', 'Norwegian Krone', 2),
    ('SEK', 'Swedish Krona', 2),
    ('DKK', 'Danish Krone', 2),
    ('EUR', 'Euro', 2);

INSERT INTO country (iso_code, name, default_currency_id, timezone)
SELECT 'NO', 'Norway', id, 'Europe/Oslo' FROM currency WHERE iso_code = 'NOK';

INSERT INTO country (iso_code, name, default_currency_id, timezone)
SELECT 'SE', 'Sweden', id, 'Europe/Stockholm' FROM currency WHERE iso_code = 'SEK';

INSERT INTO country (iso_code, name, default_currency_id, timezone)
SELECT 'DK', 'Denmark', id, 'Europe/Copenhagen' FROM currency WHERE iso_code = 'DKK';