# 영화 평점 서비스 API 명세 (5주차)

- Base URL: `http://localhost:8080`
- 응답 형식: `application/json`
- 시간 형식: ISO-8601 (`yyyy-MM-ddTHH:mm:ss`)

## 요약

| API | Method | URL | Request | Success Response (200) |
| --- | --- | --- | --- | --- |
| 닉네임 중복 체크 | GET | `/members/nickname/{nickname}` | Path: `nickname` (string) | `{ "available": boolean }` |
| 이메일 중복 체크 | GET | `/members/email/{email}` | Path: `email` (string) | `{ "available": boolean }` |
| 유저별 평점 조회 | GET | `/members/{memberId}/ratings` | Path: `memberId` (number) | `RatingResponse[]` |

---

## 1. 닉네임 중복 체크

`GET /members/nickname/{nickname}`

### Request

| 위치 | 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- | --- |
| Path | nickname | string | O | 확인할 닉네임 (한글은 URL 인코딩해서 전송) |

### Response `200 OK`

```json
{
  "available": true
}
```

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| available | boolean | `true`: 사용 가능 / `false`: 이미 사용 중 |

### 예시

- `GET /members/nickname/수현` → `{ "available": false }`
- `GET /members/nickname/새닉네임` → `{ "available": true }`

---

## 2. 이메일 중복 체크

`GET /members/email/{email}`

### Request

| 위치 | 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- | --- |
| Path | email | string | O | 확인할 이메일 |

### Response `200 OK`

```json
{
  "available": true
}
```

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| available | boolean | `true`: 사용 가능 / `false`: 이미 사용 중 |

### 예시

- `GET /members/email/noco@umc.com` → `{ "available": false }`
- `GET /members/email/new@umc.com` → `{ "available": true }`

---

## 3. 유저별 평점 조회

`GET /members/{memberId}/ratings`

### Request

| 위치 | 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- | --- |
| Path | memberId | number | O | 평점을 조회할 회원 ID |

### Response `200 OK`

최신순(`ratingId` 내림차순)으로 정렬된 배열입니다. 평점이 없으면 빈 배열 `[]`을 반환합니다.

```json
[
  {
    "ratingId": 2,
    "movieId": 155,
    "score": 4,
    "comment": "다시 봐도 명작",
    "createdAt": "2026-11-01T16:08:35",
    "updatedAt": "2026-11-01T16:08:35"
  },
  {
    "ratingId": 1,
    "movieId": 550,
    "score": 5,
    "comment": "최고의 영화!",
    "createdAt": "2026-11-01T16:08:35",
    "updatedAt": "2026-11-01T16:08:35"
  }
]
```

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| ratingId | number | 평점 ID |
| movieId | number | TMDB 영화 ID |
| score | number | 별점 (1~5) |
| comment | string \| null | 코멘트 (없으면 `null`) |
| createdAt | string | 작성 시각 |
| updatedAt | string | 수정 시각 |

### Error `404 Not Found`

존재하지 않는 `memberId`로 요청한 경우입니다. (예: `GET /members/999/ratings`)
