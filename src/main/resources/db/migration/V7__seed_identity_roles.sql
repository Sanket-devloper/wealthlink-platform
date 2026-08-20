-- Seed RBAC roles used by the identity/security layer.
INSERT INTO role (name)
VALUES
    ('ADMIN'),
    ('TRADER'),
    ('RECONCILER'),
    ('COMPLIANCE_OFFICER')
ON CONFLICT (name) DO NOTHING;
