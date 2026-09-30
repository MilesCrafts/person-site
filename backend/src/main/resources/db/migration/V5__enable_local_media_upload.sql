ALTER TABLE media_asset DROP CONSTRAINT ck_media_source;

ALTER TABLE media_asset ADD CONSTRAINT ck_media_source CHECK (
    (source_type = 'LOCAL_STATIC' AND external_url IS NOT NULL AND storage_key IS NULL)
    OR (source_type = 'LOCAL_UPLOAD' AND storage_key IS NOT NULL AND external_url IS NOT NULL)
    OR (source_type = 'S3' AND storage_key IS NOT NULL)
);

CREATE INDEX idx_media_asset_ready_created
    ON media_asset(status, created_at DESC, id DESC);
