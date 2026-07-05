--------------v2--------------
-- Comment
-- ├── TrustOps ID
-- ├── organisation ID
-- ├── source
-- ├── external ID
-- ├── text
-- ├── status
-- └── received time

-- One row represents one company/customer using TrustOps.
CREATE TABLE organizations (
        id UUID PRIMARY KEY,
        name VARCHAR(200) NOT NULL,
        api_key_hash VARCHAR(64) NOT NULL UNIQUE,
        created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Local development organisation.
-- Raw local key: trustops-dev-key
-- Only its SHA-256 hash is stored.
INSERT INTO organizations (
    id,
    name,
    api_key_hash,
    created_at
)
VALUES (
        '00000000-0000-0000-0000-000000000001',
        'TrustOps Local Demo',
        '85860a909898a76de6e5cc70b35833cab7804bd8e215cee1c23a851c296d18a8',
        CURRENT_TIMESTAMP
    );

-- Add event ownership and identity to the existing table.
-- begin nullable because old rows already exist.

ALTER TABLE comments --changes an existing table.
    ADD COLUMN organization_id UUID, --which company owns this comment.
    ADD COLUMN source VARCHAR(50), -- which system sent it.
    ADD COLUMN external_id VARCHAR(255); --sender’s own ID for the comment.

-- Give existing comments valid local-demo values.
-- Each existing TrustOps UUID becomes its external ID.
UPDATE comments
SET
    organization_id = '00000000-0000-0000-0000-000000000001',
    source = 'TRUSTOPS_DEMO',
    external_id = id::text;

-- Now every old row has values, so future rows can require them.
ALTER TABLE comments
    ALTER COLUMN organization_id SET NOT NULL,
ALTER COLUMN source SET NOT NULL,
    ALTER COLUMN external_id SET NOT NULL;

-- A comment cannot claim ownership by an organisation that does not exist.
ALTER TABLE comments
    ADD CONSTRAINT fk_comments_organization
        FOREIGN KEY (organization_id)
            REFERENCES organizations(id);

-- PostgreSQL provides the final protection against duplicated delivery.
ALTER TABLE comments
    ADD CONSTRAINT uq_comments_external_event
        UNIQUE (organization_id, source, external_id);

-- PostgreSQL only accepts source values understood by our current Java enum.
ALTER TABLE comments
    ADD CONSTRAINT valid_comment_source
        CHECK (
            source IN (
                       'TRUSTOPS_DEMO',
                       'GENERIC_WEBHOOK',
                       'COMPANY_FORUM'
                )
            );

-- Future organisation queues will normally ask for newest comments
-- belonging to one organisation. This index makes that lookup faster.
CREATE INDEX idx_comments_organization_received_at
    ON comments (organization_id, received_at DESC);