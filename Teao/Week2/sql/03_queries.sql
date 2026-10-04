USE library_week1;

/* ================================================================
   미션 1. 문학 카테고리의 대여 가능한 도서 최신순 10개

   기준 테이블: book
   JOIN 이유: 책의 category_id만으로는 카테고리 이름을 보여줄 수 없으므로 category를 JOIN합니다.
   WHERE: 문학 카테고리이고(카테고리 이름), 대여 가능한 수량이 1권 이상인 책만 조회합니다.
   정렬/목록: 출판연도 최신순, 같은 연도는 등록일 최신순으로 정렬하고 10개로 제한합니다.
================================================================ */
SELECT
    b.title AS book_title,
    b.description,
    c.name AS category_name
FROM book AS b
JOIN category AS c ON c.category_id = b.category_id
WHERE c.name = '문학'
  AND b.available_copies > 0
ORDER BY b.published_year DESC, b.created_at DESC
LIMIT 10;

/* ================================================================
   미션 2. 특정 사용자의 미반납 도서

   기준 테이블: rental
   JOIN 이유: 대여 기록의 user_id/book_id를 이용해 user와 book의 표시 정보를 결합합니다.
   WHERE: 대상 사용자이며 returned_at이 없고 상태가 RENTED 또는 OVERDUE인 기록만 남깁니다.
   정렬/목록: 반납 예정일이 빠른 순서로 정렬합니다.
================================================================ */
SET @target_user_id = 1;

SELECT
    b.title AS book_title,
    r.rented_at AS rental_date,
    r.due_at AS due_date
FROM rental AS r
JOIN book AS b ON b.book_id = r.book_id
JOIN `user` AS u ON u.user_id = r.user_id
WHERE r.user_id = @target_user_id
  AND r.returned_at IS NULL
  AND r.status IN ('RENTED', 'OVERDUE')
ORDER BY r.due_at ASC;

/* ================================================================
   미션 3. 특정 책의 태그 목록과 특정 사용자의 좋아요 여부

   기준 테이블: book
   JOIN 이유: book_tag와 tag로 책의 태그를 연결하고, book_like를 대상 사용자 조건으로 LEFT JOIN합니다.
   WHERE: 조회할 책 하나만 선택합니다. 태그가 없어도 책은 보여야 하므로 태그 JOIN은 LEFT JOIN입니다.
   정렬/목록: 태그 이름순으로 정렬하며, 좋아요 행이 있으면 TRUE로 표시합니다.
================================================================ */
SET @target_book_id = 1;
SET @target_user_id = 1;

SELECT
    b.title AS book_title,
    t.name AS tag_name,
    CASE WHEN bl.user_id IS NULL THEN FALSE ELSE TRUE END AS is_liked
FROM book AS b
LEFT JOIN book_tag AS bt ON bt.book_id = b.book_id
LEFT JOIN tag AS t ON t.tag_id = bt.tag_id
LEFT JOIN book_like AS bl
    ON bl.book_id = b.book_id
   AND bl.user_id = @target_user_id
WHERE b.book_id = @target_book_id
ORDER BY t.name;

/* ================================================================
   확장. 1주차 ERD(맛집 미션)의 회원별 완료 미션 화면

   기준 테이블: member_mission
   JOIN 이유: 회원·미션·가게·지역 이름을 화면에 함께 표시하기 위해 연결합니다.
   WHERE: 특정 회원의 COMPLETED 미션만 조회합니다.
   정렬/목록: 완료 시각이 최신인 미션부터 보여줍니다.

   아래 쿼리는 Teao/Week1/ERD의 member, member_mission, mission, store, region을
   사용하는 확장 예시이며 library_week1 공통 DB에서는 실행하지 않습니다.
================================================================ */
SET @target_member_id = 1;

SELECT
    m.nickname,
    mi.title AS mission_title,
    s.name AS store_name,
    r.name AS region_name,
    mm.completed_at
FROM member_mission AS mm
JOIN member AS m ON m.member_id = mm.member_id
JOIN mission AS mi ON mi.mission_id = mm.mission_id
JOIN store AS s ON s.store_id = mi.store_id
JOIN region AS r ON r.region_id = s.region_id
WHERE mm.member_id = @target_member_id
  AND mm.status = 'COMPLETED'
ORDER BY mm.completed_at DESC;
