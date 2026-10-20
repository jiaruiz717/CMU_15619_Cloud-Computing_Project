-- Minimal serving schema inferred from m3_jooby/Data.java.
-- Existing ETL must populate contact_tweet; this does not recreate the ETL pipeline.
CREATE DATABASE IF NOT EXISTS twitter_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE twitter_db;

CREATE TABLE IF NOT EXISTS contact_tweets (
    user_id BIGINT NOT NULL,
    is_reply BOOLEAN NOT NULL,
    is_retweet BOOLEAN NOT NULL,
    contact_tweet LONGTEXT NOT NULL,
    PRIMARY KEY (user_id, is_reply, is_retweet)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
