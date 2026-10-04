USE library_week1;

INSERT INTO `user` (user_id, email, nickname) VALUES
    (1, 'jun@example.com', '준'),
    (2, 'teao@example.com', '테오'),
    (3, 'booklover@example.com', '책벌레')
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname);

INSERT INTO category (category_id, name, description) VALUES
    (1, '문학', '문학과 장편 소설'),
    (2, '컴퓨터', '개발과 컴퓨터 과학'),
    (3, '자기계발', '습관과 성장')
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO book (book_id, category_id, title, description, author, isbn, published_year, total_copies, available_copies) VALUES
    (1, 1, '달러구트 꿈 백화점', '잠들어야만 입장할 수 있는 꿈 백화점에서 벌어지는 따뜻한 이야기.', '이미예', '9791165341909', 2020, 3, 2),
    (2, 2, 'Clean Code', '읽기 쉽고 유지보수하기 좋은 코드를 작성하는 원칙을 다룹니다.', 'Robert C. Martin', '9780132350884', 2008, 2, 2),
    (3, 3, '아주 작은 습관의 힘', '작은 습관을 설계해 지속적인 변화를 만드는 방법을 소개합니다.', 'James Clear', '9791162540640', 2019, 4, 4)
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), available_copies = VALUES(available_copies);

INSERT INTO tag (tag_id, name) VALUES
    (1, '베스트셀러'),
    (2, '입문'),
    (3, '추천')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT IGNORE INTO book_tag (book_id, tag_id) VALUES
    (1, 1), (1, 3), (2, 2), (2, 3), (3, 1), (3, 3);

INSERT IGNORE INTO rental (rental_id, user_id, book_id, rented_at, due_at, returned_at, status) VALUES
    (1, 1, 1, '2026-09-20 10:00:00', '2026-10-04 23:59:59', NULL, 'RENTED'),
    (2, 2, 2, '2026-09-01 14:00:00', '2026-09-15 23:59:59', '2026-09-14 18:20:00', 'RETURNED');

INSERT IGNORE INTO book_like (user_id, book_id) VALUES
    (1, 1), (1, 2), (2, 1), (3, 3);

INSERT INTO notification (notification_id, user_id, type, title, message, is_read) VALUES
    (1, 1, 'RENTAL_DUE', '대여 도서 반납 예정', '달러구트 꿈 백화점의 반납일이 다가옵니다.', FALSE),
    (2, 2, 'LIKE', '좋아요 알림', '내가 좋아요한 책이 업데이트되었습니다.', TRUE),
    (3, 3, 'WELCOME', '환영합니다', '도서관 서비스 이용을 시작해 보세요.', FALSE)
ON DUPLICATE KEY UPDATE message = VALUES(message), is_read = VALUES(is_read);
