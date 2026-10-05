# Week 4 - Spring Data JPA 도서 API

이슈: https://github.com/SSUMC-11th-organization/PE_Web_A_BE/issues/7

## 구현 범위

- `Book`, `Category` JPA 엔티티와 다대일 관계
- 요청·응답 DTO 분리 및 요청 검증
- `GET /books`: 최신 도서부터 응답
- `POST /books`: 도서 등록 후 201 응답
- 없는 카테고리 404, 잘못된 요청 400 응답

선택 심화 기능은 구현하지 않았습니다.

## 실행 전 준비

2주차 SQL의 `01_schema.sql`, `02_seed.sql`을 실행해 `library_week1` DB를 준비합니다.

환경 변수에는 로컬 MySQL 정보를 입력합니다.

```powershell
$env:DB_HOST="127.0.0.1"
$env:DB_PORT="3306"
$env:DB_NAME="library_week1"
$env:DB_USER="root"
$env:DB_PASSWORD="본인 비밀번호"
```

## 실행

저장소 루트에서 PowerShell로 실행합니다.

```powershell
cd Teao/Week4/orm-api
.\mvnw.cmd spring-boot:run
```

## 요청

Postman에서 메서드와 URL을 각각 입력합니다.

```text
GET http://localhost:8080/books
```

```text
POST http://localhost:8080/books
Content-Type: application/json
```

```json
{
  "categoryId": 2,
  "title": "JPA 입문",
  "description": "Spring Data JPA 실습"
}
```

## 검증

```powershell
.\mvnw.cmd test
```

Controller 테스트에서 GET 성공, POST 201, 빈 제목 400, 없는 카테고리 404를 확인합니다.

## Raw SQL과 달라진 점

3주차에는 SQL 문자열과 `?` 파라미터 순서를 Repository에서 직접 관리했습니다. 4주차에는 `Book`, `Category` 엔티티로 테이블과 관계를 표현하고 `JpaRepository`가 기본 조회·저장 SQL을 생성합니다. 또한 Entity를 API에 직접 노출하지 않고 요청·응답 DTO를 사용해 DB 구조와 API 계약을 분리했습니다.

## 요구사항 검증

`GET /books`와 `POST /books`가 JPA Repository와 DTO를 통해 동작하고, 정상 요청과 예외 요청에 요구된 상태 코드를 반환합니다.
