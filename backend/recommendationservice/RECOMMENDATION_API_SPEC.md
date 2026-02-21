# GraphFlix Recommendation API Specification

## Overview

The Recommendation Service provides personalized movie recommendations using content-based filtering with Neo4j graph traversal. It analyzes movies users have rated highly and finds similar movies based on shared actors and directors.

**Base URL:** `/api/recommendations`

**Authentication:** JWT Bearer token required for all endpoints.

---

## Endpoints

### GET /api/recommendations/personalized

Get personalized movie recommendations for the authenticated user based on their rating history.

#### Request

**Parameters:**

| Name | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| limit | integer | No | 10 | Number of recommendations to return (1-50) |

**Headers:**

| Name | Required | Description |
|------|----------|-------------|
| Authorization | Yes | Bearer {jwt_token} |

**Example Request:**

```http
GET /api/recommendations/personalized?limit=10 HTTP/1.1
Host: api.graphflix.com
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Note:** The user's email is extracted from the JWT token's `sub` claim. No userId parameter is required.

#### Response

**Success (200 OK):**

```json
{
  "movies": [
    {
      "id": "movie-uuid-1",
      "title": "John Wick",
      "released": 2014,
      "tagline": "Ex-hitman comes out of retirement",
      "reason": "Because you liked movies with these actors",
      "score": 0.8
    },
    {
      "id": "movie-uuid-2",
      "title": "The Matrix Reloaded",
      "released": 2003,
      "tagline": "Free your mind",
      "reason": "Because you liked movies with these actors and directors",
      "score": 1.0
    },
    {
      "id": "movie-uuid-3",
      "title": "Inception",
      "released": 2010,
      "tagline": "Your mind is the scene of the crime",
      "reason": "Because you liked movies directed by these directors",
      "score": 0.7
    }
  ]
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| movies | array | List of recommended movies |
| movies[].id | string | Unique movie identifier |
| movies[].title | string | Movie title |
| movies[].released | integer | Release year |
| movies[].tagline | string | Movie tagline |
| movies[].reason | string | Explanation for why this movie was recommended |
| movies[].score | number | Recommendation confidence score (0.0-1.0) |

**Score Values:**

| Score | Reason |
|-------|--------|
| 1.0 | Actor AND Director match |
| 0.8 | Actor match only |
| 0.7 | Director match only |
| 0.5 | Trending fallback (cold start) |

---

### GET /api/recommendations/trending

Get trending movies based on overall rating activity. Used as fallback for users with insufficient ratings.

#### Request

**Parameters:**

| Name | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| limit | integer | No | 10 | Number of trending movies to return (1-50) |

**Headers:**

| Name | Required | Description |
|------|----------|-------------|
| Authorization | Yes | Bearer {jwt_token} |

**Example Request:**

```http
GET /api/recommendations/trending?limit=5 HTTP/1.1
Host: api.graphflix.com
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response

**Success (200 OK):**

```json
{
  "movies": [
    {
      "id": "movie-uuid-1",
      "title": "The Matrix",
      "released": 1999,
      "tagline": "Welcome to the Real World",
      "reason": "Trending now",
      "score": 0.5
    }
  ]
}
```

---

## Error Responses

### 401 Unauthorized

Returned when JWT token is missing, invalid, or expired.

```json
{
  "error": "Unauthorized",
  "message": "Token has expired. Please login again."
}
```

### 403 Forbidden

Returned when the user does not have permission to access the resource.

```json
{
  "error": "Forbidden",
  "message": "Access denied"
}
```

### 429 Too Many Requests

Returned when rate limit is exceeded (15 requests per minute per IP).

```text
Too many requests. Please try again later.
```

### 500 Internal Server Error

Returned when an unexpected error occurs.

```json
{
  "status": 500,
  "message": "An unexpected error occurred: [error details]"
}
```

---

## Recommendation Algorithm

### Content-Based Filtering

The recommendation engine uses Neo4j graph traversal to find movies with similar content:

1. **User Profile Analysis**: Identify movies rated >= 7/10 by the user
2. **Content Extraction**: Extract actors (via `ACTED_IN`) and directors (via `DIRECTED`) from liked movies
3. **Candidate Discovery**: Find other movies with the same actors/directors
4. **Filtering**: Exclude movies already rated by the user
5. **Scoring**: Rank by number of matching attributes

### Cold Start Handling

For users with fewer than 3 ratings, the system returns trending movies instead:

- Movies with at least 5 ratings
- Sorted by rating count (descending)
- Secondary sort by average rating (descending)

---

## Cypher Queries

### Movies by Liked Actors

```cypher
MATCH (user:User {email: $email})-[r:RATED]->(likedMovie:Movie)
WHERE r.rating >= 7
WITH user, likedMovie
MATCH (likedMovie)<-[:ACTED_IN]-(actor:Person)-[:ACTED_IN]->(candidateMovie:Movie)
WHERE NOT (user)-[:RATED]->(candidateMovie)
AND candidateMovie <> likedMovie
RETURN candidateMovie, count(actor) as actorMatches
ORDER BY actorMatches DESC, candidateMovie.released DESC
LIMIT $limit
```

### Movies by Liked Directors

```cypher
MATCH (user:User {email: $email})-[r:RATED]->(likedMovie:Movie)
WHERE r.rating >= 7
MATCH (likedMovie)<-[:DIRECTED]-(director:Person)-[:DIRECTED]->(candidateMovie:Movie)
WHERE NOT (user)-[:RATED]->(candidateMovie)
AND candidateMovie <> likedMovie
RETURN candidateMovie, count(director) as directorMatches
ORDER BY directorMatches DESC, candidateMovie.released DESC
LIMIT $limit
```

### Trending Movies

```cypher
MATCH (m:Movie)<-[r:RATED]-(u:User)
WITH m, count(r) as ratingCount, avg(r.rating) as avgRating
WHERE ratingCount >= 5
RETURN m, avgRating
ORDER BY ratingCount DESC, avgRating DESC
LIMIT $limit
```

---

## Performance Targets

| Metric | Target |
|--------|--------|
| Recommendation query time | < 100ms |
| API response time | < 200ms |
| Cold start coverage | 100% of users |

---

## Rate Limiting

- **Limit**: 15 requests per minute per IP address
- **Window**: Rolling 60-second window
- **Response**: 429 status with plain text message when exceeded

---

## Security

- All endpoints require JWT authentication
- User ID must be present in JWT claims
- Rate limiting applied per IP address
- No sensitive data exposed in responses
