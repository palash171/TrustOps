CREATE TABLE comments( --create the storage table.
    id UUID PRIMARY KEY, --every key must be unique
    text VARCHAR(5000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    received_at TIMESTAMP with time zone NOT NULL,

    CONSTRAINT valid_comment_status
        CHECK( -- assert PostgreSQL only accepts our three statuses.
            status IN (
            'PENDING',
            'APPROVED',
            'REJECTED')
            )

    );