ALTER TABLE memberships
    ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',

    ADD CONSTRAINT ck_memberships_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'));