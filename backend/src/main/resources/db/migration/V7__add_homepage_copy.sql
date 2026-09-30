CREATE TABLE homepage_copy (
    id SMALLINT PRIMARY KEY CHECK (id = 1),
    eyebrow VARCHAR(120) NOT NULL,
    headline_primary VARCHAR(80) NOT NULL,
    headline_emphasis VARCHAR(80) NOT NULL,
    headline_accent VARCHAR(80) NOT NULL,
    description VARCHAR(500) NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO homepage_copy (
    id, eyebrow, headline_primary, headline_emphasis, headline_accent, description
) VALUES (
    1,
    'LEKANG · 产品、开发与记录',
    '把想法',
    '理清楚，',
    '再做出来。',
    '关注产品体验、前端开发和内容系统，也持续记录阅读、电影与工作之外的日常。'
);
