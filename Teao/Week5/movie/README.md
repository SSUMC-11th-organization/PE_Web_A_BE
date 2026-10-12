# 5주차 영화 평점 API

## API

| 기능 | Method | URL |
| --- | --- | --- |
| 닉네임 중복 확인 | GET | `/members/nickname/{nickname}` |
| 이메일 중복 확인 | GET | `/members/email/{email}` |
| 회원별 평점 조회 | GET | `/members/{memberId}/ratings` |

## 실행

1. `db/01_schema.sql`, `db/02_seed.sql` 실행
2. `DB_PASSWORD` 등 DB 환경 변수 설정
3. `gradlew.bat bootRun` 실행
4. `python -m http.server 5173 --directory mock-frontend` 실행

Postman에서는 `postman/week5-movie-api.postman_collection.json` 가져오기 후 요청 실행
