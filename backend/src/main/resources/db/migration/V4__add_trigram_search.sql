-- pg_trgm is a PostgreSQL database extension. Production installation must
-- grant this once to the migration role or have a DBA install it beforehand.
CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;

CREATE INDEX idx_article_title_trgm
    ON article USING gin (lower(title) public.gin_trgm_ops);

CREATE INDEX idx_article_excerpt_trgm
    ON article USING gin (lower(excerpt) public.gin_trgm_ops);

CREATE INDEX idx_article_body_markdown_trgm
    ON article USING gin (lower(body_markdown) public.gin_trgm_ops);
