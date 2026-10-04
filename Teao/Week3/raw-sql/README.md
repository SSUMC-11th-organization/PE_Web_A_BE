# Week 3 - No DTO, Only Raw SQL

워크북의 핵심만 따라 만든 NestJS 실습입니다.

## 구현한 구조

`Controller → Service → Repository → MySQL` 3계층으로 나누고, Repository에서만 Raw SQL을 실행합니다.

- `src/database.provider.ts`: `mysql2` 커넥션 풀 생성
- `src/book.repository.ts`: `SELECT * FROM book`, 파라미터 바인딩 `INSERT`
- `src/book.service.ts`: Repository 호출
- `src/book.controller.ts`: `GET /books`, `GET /books/category/:categoryId`, `POST /books`
- `src/rental.repository.ts`: `POST /rentals` 대여 기록 생성

## 실행

먼저 2주차 SQL 실습(`Teao/#16`)의 `01_schema.sql`, `02_seed.sql`을 실행해 `library_week1` DB를 준비합니다. 두 PR은 독립 브랜치이므로 2주차 파일은 2주차 PR에서 확인합니다.

Windows PowerShell에서 저장소 루트를 기준으로 실행합니다.

```powershell
cd Teao/Week3/raw-sql
npm ci
Copy-Item .env.example .env
npm run start:dev
```

`.env`에는 로컬 MySQL 접속 정보를 입력합니다. 기본 DB 이름은 `library_week1`입니다.

## Postman 확인

아래의 GET/POST는 터미널 명령이 아니라 Postman의 요청 방식입니다. 방식은 왼쪽 드롭다운에서 선택하고, URL 칸에는 `http://...` 주소만 입력합니다. POST 요청의 Body는 `raw` → `JSON`으로 설정합니다.

### 전체 도서 조회

`GET http://localhost:3000/books`

### 도서 등록

`POST http://localhost:3000/books`

```json
{
  "categoryId": 1,
  "title": "클린 코드",
  "description": "애자일 소프트웨어 장인 정신",
  "author": "로버트 C. 마틴"
}
```

응답으로 `도서 등록이 완료되었습니다!`가 나오면 다시 `GET /books`를 호출해 등록된 책을 확인합니다.

## 추가 API

### 카테고리별 도서 조회

`GET http://localhost:3000/books/category/1`

`category_id = ?` 조건으로 해당 카테고리의 책만 조회합니다.

### 대여 기록 생성

`POST http://localhost:3000/rentals`

```json
{
  "userId": 1,
  "bookId": 1
}
```

`rented_at`은 현재 시간, `due_at`은 현재 시간에서 7일 뒤로 저장됩니다.

현재 1주차 스키마에서 `author`가 필수이므로, 요청에서 생략하면 Repository가 `미상`을 넣습니다. DTO·ORM·상세 유효성 검사는 이번 실습 범위에서 제외했습니다.

## 제출 범위

원본 작업 이슈: https://github.com/SSUMC-11th-organization/PE_Web_A_FE/issues/22

이 코드는 기존 NestJS 실습을 백엔드 저장소로 옮긴 것입니다. Spring Boot로 변경한 코드는 아닙니다. 자동 테스트·lint·format 스크립트와 실행 결과 캡처는 포함되어 있지 않습니다.
