CREATE TABLE user_profiles (
    id UUID PRIMARY KEY,
    issuer TEXT NOT NULL,
    subject TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_user_profiles_issuer_not_blank
        CHECK (btrim(issuer) <> ''),

    CONSTRAINT ck_user_profiles_subject_not_blank
        CHECK (btrim(subject) <> ''),

    CONSTRAINT uq_user_profiles_external_identity
        UNIQUE (issuer, subject)
);


CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_organizations_name_not_blank
        CHECK (btrim(name) <> '')
);

CREATE TABLE memberships (
    id UUID PRIMARY KEY,
    user_profile_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    role VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_memberships_user_profile
        FOREIGN KEY (user_profile_id)
        REFERENCES user_profiles (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_memberships_organization
        FOREIGN KEY (organization_id)
        REFERENCES organizations (id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_memberships_role
        CHECK (role IN ('OWNER')),

    CONSTRAINT uq_memberships_user_organization
        UNIQUE (user_profile_id, organization_id)
);

CREATE INDEX ix_memberships_organization_id ON memberships (organization_id);