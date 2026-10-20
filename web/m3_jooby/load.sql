-- Step 1 create the database
drop database if exists twitter_db;
create database twitter_db;
ALTER DATABASE twitter_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER DATABASE test_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
use twitter_db;

-- Step 2 create the tweets table
drop table if exists `tweets`;
create table `tweets` (
    `id` bigint not null,
    `text` LONGTEXT not null,
    `is_reply` boolean,
    `is_retweet` boolean,
    `user_id` bigint not null,
    `user_description` LONGTEXT not null,
    `user_screen_name` LONGTEXT,
    `in_reply_to_user_id` bigint,
    `retweeted_status_user_id` bigint,
    `created_at` varchar(100),
    `hashtags` varchar(100),
    primary key (id)
);

-- Step 3 create indices
create index id_index ON tweets(id);
create index user_id_index ON tweets(user_id);
create index in_reply_to_user_id_index ON tweets(in_reply_to_user_id);
create index retweeted_status_user_id_index ON tweets(retweeted_status_user_id);

create index user_id_index ON users(user_id);
create index rt_user_id_index ON users(rt_user_id);

create index user_index ON interaction_score(user1, user2);
create index user_id_index ON userId_HashtagStr(user_id);

-- Step 4 load data to the users table
ALTER DATABASE twitter_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
load data local infile 'tweets2.csv' into table tweets columns terminated by ',' LINES TERMINATED BY '\n' IGNORE 1 LINES;