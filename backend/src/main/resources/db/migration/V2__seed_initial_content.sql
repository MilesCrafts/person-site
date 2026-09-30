INSERT INTO content_category (code, display_name, content_type, sort_order) VALUES
('FOOTBALL', 'Football', 'ARTICLE', 10),
('CINEMA', 'Cinema', 'ARTICLE', 20),
('BOOKS', 'Books', 'ARTICLE', 30),
('NOTES', 'Notes', 'ARTICLE', 40),
('ALBUM', 'Album', 'ALBUM', 50);

INSERT INTO topic (category_id, code, display_name, slug, sort_order)
SELECT c.id, v.code, v.display_name, v.slug, v.sort_order
FROM content_category c
JOIN (VALUES
    ('FOOTBALL', 'MESSI', 'Messi', 'messi', 10),
    ('FOOTBALL', 'TACTICS', 'Tactics', 'tactics', 20),
    ('FOOTBALL', 'ARGENTINA', 'Argentina', 'argentina', 30),
    ('CINEMA', 'WAR_FILM', 'War Film', 'war-film', 10),
    ('CINEMA', 'TV_SERIES', 'TV Series', 'tv-series', 20),
    ('CINEMA', 'FILM_NOTES', 'Film Notes', 'film-notes', 30),
    ('BOOKS', 'LATIN_AMERICA', 'Latin America', 'latin-america', 10),
    ('BOOKS', 'BORGES', 'Borges', 'borges', 20),
    ('BOOKS', 'SOLITUDE', 'Solitude', 'solitude', 30),
    ('NOTES', 'SHANGHAI', 'Shanghai', 'shanghai', 10),
    ('NOTES', 'BUILDING', 'Building', 'building', 20),
    ('NOTES', 'FRAGMENTS', 'Fragments', 'fragments', 30),
    ('ALBUM', 'CITY', 'City', 'city', 10),
    ('ALBUM', 'DAILY_LIFE', 'Daily Life', 'daily-life', 20),
    ('ALBUM', 'FOOTBALL', 'Football', 'football', 30),
    ('ALBUM', 'CINEMA', 'Cinema', 'cinema', 40)
) AS v(category_code, code, display_name, slug, sort_order)
ON c.code = v.category_code;

INSERT INTO media_asset (source_type, external_url, original_name, content_type, alt_text)
VALUES
('LOCAL_STATIC', '/images/journal/argentina-champions-wide.jpg', 'argentina-champions-wide.jpg', 'image/jpeg', '阿根廷队捧起世界杯'),
('LOCAL_STATIC', '/images/journal/tactics.jpg', 'tactics.jpg', 'image/jpeg', '足球战术'),
('LOCAL_STATIC', '/images/journal/argentina.jpg', 'argentina.jpg', 'image/jpeg', '阿根廷足球'),
('LOCAL_STATIC', '/images/journal/dunkirk.jpg', 'dunkirk.jpg', 'image/jpeg', '敦刻尔克'),
('LOCAL_STATIC', '/images/journal/soldier.jpg', 'soldier.jpg', 'image/jpeg', '战争电影中的士兵'),
('LOCAL_STATIC', '/images/journal/shameless.jpg', 'shameless.jpg', 'image/jpeg', '无耻之徒'),
('LOCAL_STATIC', '/images/journal/rain.jpg', 'rain.jpg', 'image/jpeg', '马孔多的雨'),
('LOCAL_STATIC', '/images/journal/library.jpg', 'library.jpg', 'image/jpeg', '图书馆'),
('LOCAL_STATIC', '/images/journal/solitude.jpg', 'solitude.jpg', 'image/jpeg', '百年孤独'),
('LOCAL_STATIC', '/images/journal/shanghai.jpg', 'shanghai.jpg', 'image/jpeg', '上海'),
('LOCAL_STATIC', '/images/journal/website.jpg', 'website.jpg', 'image/jpeg', '个人网站'),
('LOCAL_STATIC', '/images/journal/fragments.jpg', 'fragments.jpg', 'image/jpeg', '生活片段');

INSERT INTO article (
    slug, title, excerpt, body_markdown, body_html, category_id, topic_id,
    cover_asset_id, status, published_at, read_minutes
)
SELECT v.slug, v.title, v.excerpt, v.body_markdown, v.body_html, c.id, t.id,
       m.id, 'PUBLISHED', v.published_at, v.read_minutes
FROM (VALUES
    ('messi-world-cup', '2022，梅西终于捧起世界杯', '从罗萨里奥到卢赛尔，一段持续近二十年的等待，终于在卡塔尔的夜晚迎来了结局。', '## 记忆如何成为故事\n\n我们记住的从来不只是结果。更长久地停留在脑海中的，是事情发生时的光线、身边人的神情，以及那一刻我们相信的东西。\n\n> 真正重要的故事，往往在结束之后才开始生长。', '<h2>记忆如何成为故事</h2><p>我们记住的从来不只是结果。更长久地停留在脑海中的，是事情发生时的光线、身边人的神情，以及那一刻我们相信的东西。</p><blockquote>真正重要的故事，往往在结束之后才开始生长。</blockquote>', 'FOOTBALL', 'MESSI', 'argentina-champions-wide.jpg', TIMESTAMPTZ '2026-07-19 00:00:00+08', 8),
    ('parking-the-bus', '什么是足球比赛中的摆大巴', '防守并非消极的同义词。理解空间、耐心与风险，才看得见低位防守的全部。', '## 时间的另一面\n\n细节让宏大的叙事重新变得具体。一个动作、一句没有说完的话、一段沉默，都可能比结论更接近真实。', '<h2>时间的另一面</h2><p>细节让宏大的叙事重新变得具体。一个动作、一句没有说完的话、一段沉默，都可能比结论更接近真实。</p>', 'FOOTBALL', 'TACTICS', 'tactics.jpg', TIMESTAMPTZ '2026-07-15 00:00:00+08', 6),
    ('argentina-football', '阿根廷足球为什么如此迷人', '街头、探戈、天才与悲剧，共同塑造了一种难以复制的足球气质。', '## 记忆如何成为故事\n\n重新讲述一段往事，是试着理解它为何仍能改变我们观看世界的方式。', '<h2>记忆如何成为故事</h2><p>重新讲述一段往事，是试着理解它为何仍能改变我们观看世界的方式。</p>', 'FOOTBALL', 'ARGENTINA', 'argentina.jpg', TIMESTAMPTZ '2026-07-08 00:00:00+08', 7),
    ('dunkirk-myth', '敦刻尔克：撤退如何成为神话', '诺兰没有拍摄一场传统胜利，而是让时间、海洋与沉默成为战争的主角。', '## 时间的另一面\n\n写作是一种缓慢的观看。我们不急着抵达答案，而是在过程中保留复杂与犹疑。', '<h2>时间的另一面</h2><p>写作是一种缓慢的观看。我们不急着抵达答案，而是在过程中保留复杂与犹疑。</p>', 'CINEMA', 'WAR_FILM', 'dunkirk.jpg', TIMESTAMPTZ '2026-07-17 00:00:00+08', 9),
    ('saving-private-ryan', '拯救大兵瑞恩中的士兵与武器', '穿过奥马哈海滩的噪声，重新审视一部战争电影里的身体、器械与伦理。', '## 记忆如何成为故事\n\n细节让宏大的叙事重新变得具体。', '<h2>记忆如何成为故事</h2><p>细节让宏大的叙事重新变得具体。</p>', 'CINEMA', 'WAR_FILM', 'soldier.jpg', TIMESTAMPTZ '2026-07-11 00:00:00+08', 10),
    ('shameless-family', '无耻之徒：混乱家庭里的真实生活', '它粗粝、喧闹而不体面，却比许多精致故事更接近生活的纹理。', '## 时间的另一面\n\n允许一个故事保留开放的结尾。', '<h2>时间的另一面</h2><p>允许一个故事保留开放的结尾。</p>', 'CINEMA', 'TV_SERIES', 'shameless.jpg', TIMESTAMPTZ '2026-07-04 00:00:00+08', 7),
    ('macondo-rain', '马孔多为什么一直在下雨', '雨在马孔多不是天气，而是时间、遗忘与孤独共同写下的一种语言。', '## 记忆如何成为故事\n\n我们记住的从来不只是结果。', '<h2>记忆如何成为故事</h2><p>我们记住的从来不只是结果。</p>', 'BOOKS', 'LATIN_AMERICA', 'rain.jpg', TIMESTAMPTZ '2026-07-13 00:00:00+08', 8),
    ('borges-library', '博尔赫斯与无限图书馆', '当所有书都已存在，人还要如何阅读、选择，并为意义负责？', '## 时间的另一面\n\n观看，而不是急于判断。', '<h2>时间的另一面</h2><p>观看，而不是急于判断。</p>', 'BOOKS', 'BORGES', 'library.jpg', TIMESTAMPTZ '2026-06-28 00:00:00+08', 6),
    ('one-hundred-years', '百年孤独中的孤独究竟是什么', '布恩迪亚家族反复经历的，也许不是命运，而是无法彼此理解的漫长回声。', '## 记忆如何成为故事\n\n记录具体的人与瞬间。', '<h2>记忆如何成为故事</h2><p>记录具体的人与瞬间。</p>', 'BOOKS', 'SOLITUDE', 'solitude.jpg', TIMESTAMPTZ '2026-06-20 00:00:00+08', 9),
    ('shanghai-internship', '在上海实习的普通一天', '早高峰、写字楼、便利店晚饭，以及一座城市在日常缝隙里露出的表情。', '## 时间的另一面\n\n写作因此成为一种缓慢的观看。', '<h2>时间的另一面</h2><p>写作因此成为一种缓慢的观看。</p>', 'NOTES', 'SHANGHAI', 'shanghai.jpg', TIMESTAMPTZ '2026-07-06 00:00:00+08', 5),
    ('why-personal-site', '为什么我想做一个个人网站', '在算法的时间线之外，给自己的文字留下一间可以慢慢整理的房间。', '## 记忆如何成为故事\n\n真正重要的故事，往往在结束之后才开始生长。', '<h2>记忆如何成为故事</h2><p>真正重要的故事，往往在结束之后才开始生长。</p>', 'NOTES', 'BUILDING', 'website.jpg', TIMESTAMPTZ '2026-06-16 00:00:00+08', 4),
    ('recent-fragments', '最近值得记住的一些片段', '六月末的风、一场没有看完的电影，以及朋友随口说出的一句话。', '## 时间的另一面\n\n我们在过程中保留那些尚未命名的感受。', '<h2>时间的另一面</h2><p>我们在过程中保留那些尚未命名的感受。</p>', 'NOTES', 'FRAGMENTS', 'fragments.jpg', TIMESTAMPTZ '2026-06-08 00:00:00+08', 3)
) AS v(slug, title, excerpt, body_markdown, body_html, category_code, topic_code, image_name, published_at, read_minutes)
JOIN content_category c ON c.code = v.category_code
JOIN topic t ON t.category_id = c.id AND t.code = v.topic_code
JOIN media_asset m ON m.original_name = v.image_name;

INSERT INTO site_profile (id, display_name, bio, manifesto, email, now_watching_title, now_watching_detail)
VALUES (1, 'LEKANG', '记录足球、电影、书与日常生活。', '一份持续生长的个人数字文化档案。', NULL, 'Now Watching', 'Cinema, football and the ordinary days between them.');

INSERT INTO homepage_feature (id, article_id)
SELECT 1, id FROM article WHERE slug = 'messi-world-cup';

INSERT INTO album (slug, title, description, status, published_at)
VALUES ('personal-album', 'Personal Album', '城市、日常、足球与电影的私人影像档案。', 'PUBLISHED', TIMESTAMPTZ '2026-07-01 00:00:00+08');
