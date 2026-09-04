-- Isolated experiment: not part of the application's migration location.
CREATE TABLE experiment_user_profiles (
    id UUID PRIMARY KEY,
    issuer VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    CONSTRAINT uq_experiment_external_identity UNIQUE (issuer, subject)
);
