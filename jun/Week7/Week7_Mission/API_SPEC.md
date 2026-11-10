# 영화 평점 서비스 API 명세 (7주차)

- Base URL: `http://localhost:8080`
- 요청/응답 형식: `application/json`
- 시간 형식: ISO-8601 (`yyyy-MM-ddTHH:mm:ss`)
- 로그인이 필요한 API는 헤더에 `Authorization: Bearer {accessToken}`을 함께 보냅니다.
- 요청한 회원은 요청에 적지 않고 **토큰**으로 서버가 판단합니다. (`memberId`를 보내는 칸이 없습니다)

## 공통 응답 형식

모든 API는 성공과 실패 모두 같은 틀로 응답합니다.

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

| API | Method | URL | 로그인 | 성공 |
| --- | --- | --- | --- | --- |
| 닉네임 중복 체크 | GET | `/members/nickname/{nickname}` | X | `200` |
| 이메일 중복 체크 | GET | `/members/email/{email}` | X | `200` |
| 회원가입 | POST | `/members` | X | `201` |
| 로그인 | POST | `/auth/login` | X | `200` |
| 내 정보 조회 | GET | `/members/me` | O | `200` |
| 내 정보 수정 | PATCH | `/members/me` | O | `200` |
| 유저별 평점 조회 | GET | `/members/{memberId}/ratings` | X | `200` |
| 평점 등록 | POST | `/ratings` | O | `201` |
| 평점 수정 | PATCH | `/ratings/{ratingId}` | O | `200` |
| 평점 삭제 | DELETE | `/ratings/{ratingId}` | O | `200` |

> 6주차에서 `/members/{memberId}/ratings` 아래에 있던 평점 등록/수정/삭제는 `memberId`가 사라지고 `/ratings`로 옮겨졌습니다.
> 평점 삭제는 공통 응답 형식을 지키기 위해 `204`에서 `200`(`result: null`)으로 바뀌었습니다.

## 에러 코드

| HTTP | code | message | 발생 상황 |
| --- | --- | --- | --- |
| 400 | COMMON4001 | (필드별 검증 메시지) | DTO 검증 실패 |
| 400 | COMMON4002 | 요청 본문의 형식이 올바르지 않습니다. | JSON 문법 오류, 타입 불일치 (`"score": "abc"`, `"score": 3.5`) |
| 400 | COMMON4003 | 요청 경로의 값이 올바르지 않습니다. | `/members/abc/ratings` |
| 401 | AUTH401_1 | 이메일 또는 비밀번호가 올바르지 않습니다. | 로그인 정보가 틀림 (없는 이메일도 같은 응답) |
| 401 | AUTH401_2 | 로그인이 필요한 요청입니다. | Authorization 헤더가 없거나 `Bearer ` 형식이 아님 |
| 401 | AUTH401_3 | 유효하지 않은 토큰입니다. | 서명이 맞지 않거나 형식이 깨진 토큰 |
| 401 | AUTH401_4 | 토큰이 만료되었습니다. 다시 로그인해주세요. | 유효시간이 지난 토큰 |
| 403 | RATING403_1 | 본인이 작성한 평점만 수정하거나 삭제할 수 있습니다. | 다른 회원의 평점 수정/삭제 |
| 404 | MEMBER404_1 | 해당 회원을 찾을 수 없습니다. | 없는 `memberId` (토큰의 회원이 삭제된 경우 포함) |
| 404 | RATING404_1 | 해당 평점을 찾을 수 없습니다. | 없는 `ratingId` |
| 404 | COMMON4041 | 존재하지 않는 API입니다. | 없는 URL |
| 405 | COMMON4051 | 지원하지 않는 HTTP 메서드입니다. | 예: `PUT /members` |
| 409 | MEMBER4091 | 이미 사용 중인 이메일입니다. | 이메일 중복 |
| 409 | MEMBER4092 | 이미 사용 중인 닉네임입니다. | 회원가입 시 닉네임 중복 |
| 409 | MEMBER409_1 | 이미 사용 중인 닉네임입니다. | 내 정보 수정 시 다른 회원과 닉네임 중복 |
| 409 | RATING409_1 | 이미 이 영화에 평점을 남기셨습니다. | 같은 회원이 같은 영화에 중복 등록 |
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

## 4. 로그인

`POST /auth/login`

### Request Body

| 필드 | 타입 | 필수 | 규칙 |
| --- | --- | --- | --- |
| email | string | O | 이메일 형식 |
| password | string | O | 비어 있지 않은 값 (규칙 검사는 가입 때만 합니다) |

```json
{ "email": "umc@example.com", "password": "Umc!1234" }
```

### Response `200 OK`

```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "로그인에 성공했습니다.",
  "result": { "nickname": "moviefan", "accessToken": "eyJhbGciOi…" }
}
```

없는 이메일과 틀린 비밀번호는 **같은 응답**(`AUTH401_1`)입니다. 가입 여부를 알아내지 못하게 하기 위해서입니다.

### Errors

`400 COMMON4001` / `401 AUTH401_1`

---

## 5. 내 정보 조회 / 수정

두 API 모두 `Authorization: Bearer {accessToken}` 헤더가 필요합니다.

### 5-1. 내 정보 조회

`GET /members/me`

```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "회원 정보를 조회했습니다.",
  "result": { "email": "umc@example.com", "nickname": "moviefan", "createdAt": "2026-11-09T21:14:03" }
}
```

Errors: `401 AUTH401_2~4` / `404 MEMBER404_1`

### 5-2. 내 정보 수정

`PATCH /members/me`

| 필드 | 타입 | 필수 | 규칙 |
| --- | --- | --- | --- |
| nickname | string | O | 2~12자. 내가 쓰던 닉네임은 중복으로 보지 않음 |

```json
{ "nickname": "newnickname" }
```

`200 OK` — `result`는 내 정보 조회와 같은 형태, `message`는 `회원 정보를 수정했습니다.`

Errors: `400 COMMON4001` / `401 AUTH401_2~4` / `404 MEMBER404_1` / `409 MEMBER409_1`

---

## 6. 유저별 평점 조회

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

`404 MEMBER404_1`

---

## 7. 평점 등록

`POST /ratings`

| 위치 | 이름 | 값 |
| --- | --- | --- |
| Header | Authorization | `Bearer {accessToken}` |

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

`400 COMMON4001` / `400 COMMON4002` / `401 AUTH401_2~4` / `404 MEMBER404_1` / `409 RATING409_1` / `409 COMMON4091`

---

## 8. 평점 수정

`PATCH /ratings/{ratingId}`

| 위치 | 이름 | 값 |
| --- | --- | --- |
| Header | Authorization | `Bearer {accessToken}` |
| Path | ratingId | 수정할 평점 ID |

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

`400 COMMON4001` / `401 AUTH401_2~4` / `403 RATING403_1` / `404 RATING404_1`

---

## 9. 평점 삭제

`DELETE /ratings/{ratingId}`

| 위치 | 이름 | 값 |
| --- | --- | --- |
| Header | Authorization | `Bearer {accessToken}` |
| Path | ratingId | 삭제할 평점 ID |

### Response `200 OK`

공통 응답 형식을 지키기 위해 `204`가 아닌 `200`으로 응답하고 `result`는 `null`입니다.

```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "평점 삭제에 성공했습니다.",
  "result": null
}
```

### Errors

`401 AUTH401_2~4` / `403 RATING403_1` / `404 RATING404_1`
