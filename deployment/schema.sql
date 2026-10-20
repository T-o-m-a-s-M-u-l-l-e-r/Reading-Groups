CREATE TABLE USERS (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(25) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    email_verified_at TIMESTAMPTZ
);

CREATE TABLE EMAIL_VERIFICATIONS (
    user_id INT PRIMARY KEY REFERENCES USERS(user_id) ON DELETE CASCADE,
    token_hash CHAR(64) UNIQUE NOT NULL,
    requested_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL,
    CHECK (expires_at > requested_at)
);

CREATE TABLE REFRESH_SESSIONS (
    session_id BIGSERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES USERS(user_id) ON DELETE CASCADE,
    token_hash CHAR(64) UNIQUE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    CHECK (expires_at > created_at)
);

CREATE INDEX idx_refresh_sessions_user_id
    ON REFRESH_SESSIONS(user_id);

CREATE INDEX idx_refresh_sessions_expires_at
    ON REFRESH_SESSIONS(expires_at);

CREATE TABLE GROUPS (
    group_id SERIAL PRIMARY KEY,
    reading_text_path TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    administrator_user_id INT NOT NULL,
    group_name VARCHAR(100) NOT NULL,
    FOREIGN KEY (administrator_user_id) REFERENCES USERS(user_id)
);

CREATE TABLE GROUP_JOIN_LINKS (
    group_id INT PRIMARY KEY REFERENCES GROUPS(group_id) ON DELETE CASCADE,
    token_hash CHAR(64) UNIQUE NOT NULL,
    issued_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL,
    CHECK (expires_at > issued_at)
);

CREATE TABLE CATEGORIES (
    category_id SERIAL PRIMARY KEY,
    category_label VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE COMMENTS (
    comment_id SERIAL PRIMARY KEY,
    category_id INT,
    FOREIGN KEY (category_id) REFERENCES CATEGORIES(category_id),
    text_section TEXT NOT NULL,
    comment_content TEXT NOT NULL,
    user_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES USERS(user_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    comment_page_number INT NOT NULL,
    comment_page_occurrence INT NOT NULL,
    group_id INT NOT NULL,
    FOREIGN KEY (group_id) REFERENCES GROUPS(group_id)
);

CREATE TABLE USERS_GROUPS (
    user_id INT NOT NULL,
    group_id INT NOT NULL,
    PRIMARY KEY (user_id, group_id),
    FOREIGN KEY (user_id) REFERENCES USERS(user_id),
    FOREIGN KEY (group_id) REFERENCES GROUPS(group_id)
);

CREATE TABLE INVITES (
    invite_id SERIAL PRIMARY KEY,
    group_id INT NOT NULL,
    FOREIGN KEY (group_id)
        REFERENCES GROUPS(group_id)
        ON DELETE CASCADE,
    user_id INT NOT NULL,
    FOREIGN KEY (user_id)
        REFERENCES USERS(user_id)
        ON DELETE CASCADE,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (group_id, user_id)
);

CREATE INDEX idx_comments_group_id
    ON COMMENTS(group_id);

CREATE INDEX idx_users_groups_group_id
    ON USERS_GROUPS(group_id);

CREATE INDEX idx_invites_user_id
    ON INVITES(user_id);
