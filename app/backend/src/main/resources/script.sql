-- Drop tables if they already exist (optional, for re-run safety)
DROP TABLE IF EXISTS user_infos;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users
(
    user_id  SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email    VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(150) NOT NULL UNIQUE
);

-- Create user_infos table
CREATE TABLE user_infos
(
    user_info_id  SERIAL PRIMARY KEY,
    firstname     VARCHAR(100) NOT NULL,
    lastname      VARCHAR(100) NOT NULL,
    profile_image TEXT,
    bio           TEXT,
    user_id       INTEGER      NOT NULL,
    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
            REFERENCES users (user_id)
            ON DELETE CASCADE
);