ALTER TABLE homepage_copy
    ADD COLUMN paper_label VARCHAR(40) NOT NULL DEFAULT '正在搭建',
    ADD COLUMN paper_line_one VARCHAR(120) NOT NULL DEFAULT '让文章有地方住，',
    ADD COLUMN paper_line_two VARCHAR(120) NOT NULL DEFAULT '让作品慢慢长，',
    ADD COLUMN paper_line_three VARCHAR(120) NOT NULL DEFAULT '也让我持续更新。',
    ADD COLUMN paper_footer VARCHAR(80) NOT NULL DEFAULT 'BUILDING IN PUBLIC';
