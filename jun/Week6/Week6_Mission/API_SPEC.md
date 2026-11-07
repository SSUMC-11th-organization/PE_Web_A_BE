# 영화 평점 서비스 API 명세 (6주차)

- Base URL: `http://localhost:8080`
- 요청/응답 형식: `application/json`
- 시간 형식: ISO-8601 (`yyyy-MM-ddTHH:mm:ss`)
- 로그인 기능 전이므로 `memberId`를 경로로 받아 요청한 회원을 구분합니다. (7주차에 토큰 방식으로 변경 예정)

## 공통 응답 형식

모든 API는 성공과 실패 모두 같은 틀로 응답합니다. (`204 No Content`만 본문 없음)

```json
{
  "isSuccess": true,
  "code": "COMMON201",
  "message": "회원가입에 성공했습니다.",
  "result": { "memberId": 1, "email": "umc@example.com", "nickname": "moviefan" }
}
```

```json
{
  "isSuccess": false,
  "code": "MEMBER4091",
  "message": "이미 사용 중인 이메일입니다.",
  "result": null
}
```

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| isSuccess | boolean | 요청 성공 여부 |
| code | string | 결과 코드 (`도메인 + HTTP 상태 + 순번`) |
| message | string | 화면에 그대로 보여줄 수 있는 메시지 |
| result | any \| null | 성공 시 데이터. 실패 시 `null` (입력값 검증 실패만 필드별 오류 객체) |

### 입력값 검증 실패 (`400`, `COMMON4001`)

`message`에는 필드명 순으로 첫 번째 오류가, `result`에는 필드별 오류가 담깁니다.

```json
{
  "isSuccess": false,
  "code": "COMMON4001",
  "message": "올바른 이메일 형식이 아닙니다.",
  "result": {
    "email": "올바른 이메일 형식이 아닙니다.",
    "nickname": "닉네임은 2자 이상 12자 이하로 입력해주세요.",
    "password": "비밀번호는 대문자/소문자/숫자/특수문자를 포함한 8자 이상이어야 합니다."
  }
}
```

## 요약

| API | Method | URL | 성공 |
| --- | --- | --- | --- |
| 닉네임 중복 체크 | GET | `/members/nickname/{nickname}` | `200` |
| 이메일 중복 체크 | GET | `/members/email/{email}` | `200` |
| 회원가입 | POST | `/members` | `201` |
| 유저별 평점 조회 | GET | `/members/{memberId}/ratings` | `200` |
| 평점 등록 | POST | `/members/{memberId}/ratings` | `201` |
| 평점 수정 | PATCH | `/members/{memberId}/ratings/{ratingId}` | `200` |
| 평점 삭제 | DELETE | `/members/{memberId}/ratings/{ratingId}` | `204` |

## 에러 코드

| HTTP | code | message | 발생 상황 |
| --- | --- | --- | --- |
| 400 | COMMON4001 | (필드별 검증 메시지) | DTO 검증 실패 |
| 400 | COMMON4002 | 요청 본문의 형식이 올바르지 않습니다. | JSON 문법 오류, 타입 불일치 (`"score": "abc"`, `"score": 3.5`) |
| 400 | COMMON4003 | 요청 경로의 값이 올바르지 않습니다. | `/members/abc/ratings` |
| 403 | RATING4031 | 본인이 작성한 평점만 수정하거나 삭제할 수 있습니다. | 다른 회원의 평점 수정/삭제 |
| 404 | MEMBER4041 | 존재하지 않는 유저입니다. | 없는 `memberId` |
| 404 | RATING4041 | 존재하지 않는 평점입니다. | 없는 `ratingId` |
| 404 | COMMON4041 | 존재하지 않는 API입니다. | 없는 URL |
| 405 | COMMON4051 | 지원하지 않는 HTTP 메서드입니다. | 예: `PUT /members` |
| 409 | MEMBER4091 | 이미 사용 중인 이메일입니다. | 이메일 중복 |
| 409 | MEMBER4092 | 이미 사용 중인 닉네임입니다. | 닉네임 중복 |
| 409 | RATING4091 | 이미 이 영화에 평점을 남기셨습니다. | 같은 회원이 같은 영화에 중복 등록 |
| 409 | COMMON4091 | 이미 존재하는 데이터입니다. | 사전 중복 체크를 통과했지만 DB UNIQUE 제약에 걸린 경우 (동시 요청) |
| 500 | COMMON5001 | 서버 내부 오류가 발생했습니다. | 예상하지 못한 오류 |

---

## 1. 닉네임 중복 체크

`GET /members/nickname/{nickname}`

| 위치 | 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- | --- |
| Path | nickname | string | O | 확인할 닉네임 (한글은 URL 인코딩해서 전송) |

### Response `200 OK`

```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "닉네임 중복 확인에 성공했습니다.",
  "result": { "available": true }
}
```

`available` — `true`: 사용 가능 / `false`: 이미 사용 중

---

## 2. 이메일 중복 체크

`GET /members/email/{email}`

| 위치 | 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- | --- |
| Path | email | string | O | 확인할 이메일 |

### Response `200 OK`

```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "이메일 중복 확인에 성공했습니다.",
  "result": { "available": false }
}
```

---

## 3. 회원가입

`POST /members`

### Request Body

| 필드 | 타입 | 필수 | 규칙 |
| --- | --- | --- | --- |
| email | string | O | 이메일 형식 |
| password | string | O | 대문자·소문자·숫자·특수문자(`!@#$%^&*`) 각 1개 이상, 8~64자 |
| nickname | string | O | 2~12자 |

```json
{
  "email": "umc@example.com",
  "password": "Umc!1234",
  "nickname": "moviefan"
}
```

비밀번호는 bcrypt 해시(60자)로 저장되며 **응답에 포함되지 않습니다.**

### Response `201 Created`

```json
{
  "isSuccess": true,
  "code": "COMMON201",
  "message": "회원가입에 성공했습니다.",
  "result": { "memberId": 1, "email": "umc@example.com", "nickname": "moviefan" }
}
```

### Errors

`400 COMMON4001` / `409 MEMBER4091` / `409 MEMBER4092` / `409 COMMON4091`

---

## 4. 유저별 평점 조회

`GET /members/{memberId}/ratings`

최신순(`ratingId` 내림차순)으로 정렬된 배열입니다. 평점이 없으면 `result`가 빈 배열 `[]`입니다.

### Response `200 OK`

```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "평점 조회에 성공했습니다.",
  "result": [
    {
      "ratingId": 2,
      "movieId": 155,
      "score": 4,
      "comment": "다시 봐도 명작",
      "createdAt": "2026-11-01T16:08:35",
      "updatedAt": "2026-11-01T16:08:35"
    }
  ]
}
```

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| ratingId | number | 평점 ID |
| movieId | number | TMDB 영화 ID |
| score | number | 별점 (1~5) |
| comment | string \| null | 후기 (없으면 `null`) |
| createdAt | string | 작성 시각 |
| updatedAt | string | 수정 시각 |

### Errors

`404 MEMBER4041`

---

## 5. 평점 등록

`POST /members/{memberId}/ratings`

### Request Body

| 필드 | 타입 | 필수 | 규칙 |
| --- | --- | --- | --- |
| movieId | number | O | TMDB 영화 ID, 양의 정수 |
| score | number | O | 1~5 정수 (소수 불가) |
| comment | string | X | 500자 이하 |

```json
{ "movieId": 550, "score": 5, "comment": "최고의 영화!" }
```

한 회원은 같은 영화에 평점을 한 번만 등록할 수 있습니다.

### Response `201 Created`

```json
{
  "isSuccess": true,
  "code": "COMMON201",
  "message": "평점 등록에 성공했습니다.",
  "result": {
    "ratingId": 3,
    "movieId": 550,
    "score": 5,
    "comment": "최고의 영화!",
    "createdAt": "2026-11-07T14:00:00",
    "updatedAt": "2026-11-07T14:00:00"
  }
}
```

### Errors

`400 COMMON4001` / `400 COMMON4002` / `404 MEMBER4041` / `409 RATING4091` / `409 COMMON4091`

---

## 6. 평점 수정

`PATCH /members/{memberId}/ratings/{ratingId}`

### Request Body

| 필드 | 타입 | 필수 | 규칙 |
| --- | --- | --- | --- |
| score | number | O | 1~5 정수 |
| comment | string | X | 500자 이하 (보내지 않으면 `null`로 바뀜) |

```json
{ "score": 3, "comment": "다시 보니 평범" }
```

### Response `200 OK`

`result`는 수정된 평점 (`평점 등록` 응답과 같은 형태)

### Errors

`400 COMMON4001` / `403 RATING4031` / `404 RATING4041`

---

## 7. 평점 삭제

`DELETE /members/{memberId}/ratings/{ratingId}`

### Response `204 No Content`

본문 없음

### Errors

`403 RATING4031` / `404 RATING4041`
