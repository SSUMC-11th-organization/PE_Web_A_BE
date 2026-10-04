CREATE DATABASE IF NOT EXISTS library_week1
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE library_week1;

CREATE TABLE IF NOT EXISTS `user` (
    user_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_user_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS category (
    category_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NULL,
    PRIMARY KEY (category_id),
    UNIQUE KEY uk_category_name (name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS book (
    book_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    category_id BIGINT UNSIGNED NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000) NULL,
    author VARCHAR(100) NOT NULL,
    isbn VARCHAR(20) NULL,
    published_year SMALLINT UNSIGNED NULL,
    total_copies INT UNSIGNED NOT NULL DEFAULT 1,
    available_copies INT UNSIGNED NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (book_id),
    UNIQUE KEY uk_book_isbn (isbn),
    CONSTRAINT fk_book_category FOREIGN KEY (category_id) REFERENCES category (category_id),
    CONSTRAINT ck_book_copies CHECK (available_copies <= total_copies)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS rental (
    rental_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    book_id BIGINT UNSIGNED NOT NULL,
    rented_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_at DATETIME NOT NULL,
    returned_at DATETIME NULL,
    status ENUM('RENTED', 'RETURNED', 'OVERDUE') NOT NULL DEFAULT 'RENTED',
    PRIMARY KEY (rental_id),
    CONSTRAINT fk_rental_user FOREIGN KEY (user_id) REFERENCES `user` (user_id),
    CONSTRAINT fk_rental_book FOREIGN KEY (book_id) REFERENCES book (book_id),
    INDEX idx_rental_user_status (user_id, status),
    INDEX idx_rental_book_status (book_id, status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tag (
    tag_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (tag_id),
    UNIQUE KEY uk_tag_name (name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS book_tag (
    book_id BIGINT UNSIGNED NOT NULL,
    tag_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (book_id, tag_id),
    CONSTRAINT fk_book_tag_book FOREIGN KEY (book_id) REFERENCES book (book_id),
    CONSTRAINT fk_book_tag_tag FOREIGN KEY (tag_id) REFERENCES tag (tag_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS book_like (
    user_id BIGINT UNSIGNED NOT NULL,
    book_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, book_id),
    CONSTRAINT fk_book_like_user FOREIGN KEY (user_id) REFERENCES `user` (user_id),
    CONSTRAINT fk_book_like_book FOREIGN KEY (book_id) REFERENCES book (book_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS notification (
    notification_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(150) NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (notification_id),
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES `user` (user_id),
    INDEX idx_notification_user_read (user_id, is_read, created_at)
) ENGINE=InnoDB;
