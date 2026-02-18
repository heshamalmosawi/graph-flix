# Frontend Integration Plan - Rating Microservice

## Overview
This document describes how the Angular frontend will integrate with the Rating microservice for managing movie ratings.

---

## API Endpoints Reference

### Base URL
```
http://localhost:8080/api/ratings
```
Note: Actual port will be dynamically assigned by Eureka service discovery.

### Available Endpoints

| Method | Endpoint | Description | Auth Required | Request Body | Response |
|--------|----------|-------------|---------------|--------------|
| POST | `/api/ratings` | Create a new rating | Yes | `CreateRatingRequest` | `RatingDTO` (201 Created) |
| PUT | `/api/ratings/{id}` | Update existing rating | Yes | `UpdateRatingRequest` | `RatingDTO` (200 OK) |
| DELETE | `/api/ratings/{id}` | Delete a rating | Yes | - | 204 No Content |
| GET | `/api/ratings/user/{userId}` | Get user's ratings | Yes | - | `Page<RatingDTO>` (200 OK) |
| GET | `/api/ratings/movie/{movieId}` | Get movie's ratings | No | - | `Page<RatingDTO>` (200 OK) |
| GET | `/api/ratings/movie/{movieId}/average` | Get average rating | No | - | `AverageRatingDTO` (200 OK) |

---

## Request/Response Models

### CreateRatingRequest
```typescript
interface CreateRatingRequest {
  movieId: string;        // Required
  rating: number;         // Required, min: 1, max: 10
  comment?: string;        // Optional, max: 500 chars
}
```

### UpdateRatingRequest
```typescript
interface UpdateRatingRequest {
  rating: number;         // Optional, min: 1, max: 10
  comment?: string;        // Optional, max: 500 chars
}
```

### RatingDTO
```typescript
interface RatingDTO {
  id: number;
  rating: number;          // 1-10
  comment: string;
  timestamp: string;       // ISO 8601 datetime
  userId: string;
  userName: string;
  movieId: string;
  movieTitle: string;
}
```

### AverageRatingDTO
```typescript
interface AverageRatingDTO {
  movieId: string;
  average: number;          // Decimal with 2+ places
  count: number;
}
```

---

## Angular Components Required

### 1. Rating Service
**File**: `src/app/features/ratings/rating.service.ts`

```typescript
@Injectable({ providedIn: 'root' })
export class RatingService {
  private apiUrl = 'http://localhost:8080/api/ratings';

  constructor(private http: HttpClient, private authService: AuthService) {}

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  // Create a new rating
  createRating(request: CreateRatingRequest): Observable<RatingDTO> {
    return this.http.post<RatingDTO>(
      `${this.apiUrl}`,
      request,
      { headers: this.getAuthHeaders() }
    );
  }

  // Update existing rating
  updateRating(ratingId: number, request: UpdateRatingRequest): Observable<RatingDTO> {
    return this.http.put<RatingDTO>(
      `${this.apiUrl}/${ratingId}`,
      request,
      { headers: this.getAuthHeaders() }
    );
  }

  // Delete a rating
  deleteRating(ratingId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${ratingId}`,
      { headers: this.getAuthHeaders() }
    );
  }

  // Get user's ratings
  getUserRatings(userId: string, page = 0, size = 20): Observable<Page<RatingDTO>> {
    return this.http.get<Page<RatingDTO>>(
      `${this.apiUrl}/user/${userId}?page=${page}&size=${size}&sortBy=timestamp`,
      { headers: this.getAuthHeaders() }
    );
  }

  // Get movie's ratings
  getMovieRatings(movieId: string, page = 0, size = 20): Observable<Page<RatingDTO>> {
    return this.http.get<Page<RatingDTO>>(
      `${this.apiUrl}/movie/${movieId}?page=${page}&size=${size}&sortBy=rating`,
      { headers: this.getAuthHeaders() }
    );
  }

  // Get average rating for movie
  getAverageRating(movieId: string): Observable<AverageRatingDTO> {
    return this.http.get<AverageRatingDTO>(
      `${this.apiUrl}/movie/${movieId}/average`
    );
  }
}
```

### 2. Rating Component (Rate a Movie)
**File**: `src/app/features/ratings/rate-movie/rate-movie.component.ts`

Features:
- Display movie information (title, poster, genre)
- Star rating input (1-10 stars)
- Optional comment textarea
- Submit button
- Update existing rating if user has already rated

### 3. Rating List Component
**File**: `src/app/features/ratings/rating-list/rating-list.component.ts`

Features:
- Display list of ratings for a user or movie
- Show rating stars, comment, and timestamp
- Pagination controls (page size, navigation)
- Sort options (most recent, highest rated)
- Delete rating button (for user's own ratings)

### 4. Average Rating Display Component
**File**: `src/app/features/ratings/average-rating/average-rating.component.ts`

Features:
- Display average rating for a movie
- Show total number of ratings
- Visual rating bar/chart (optional)

---

## Integration Points

### Movie Detail Page
When viewing a movie detail page:
1. Call `getMovieRatings(movieId)` to display all ratings
2. Call `getAverageRating(movieId)` to show average rating
3. If user has already rated the movie, display their rating with edit option
4. If user hasn't rated, show "Rate this Movie" button/section

### User Profile Page
When viewing a user profile:
1. Call `getUserRatings(userId)` to display user's rating history
2. Show pagination controls for large rating lists
3. Allow sorting by timestamp or rating value

### Rating Flow

**Creating a New Rating:**
1. User clicks "Rate this Movie" on movie detail page
2. Opens rating component with star selector and comment field
3. User selects rating (1-10) and optionally adds comment
4. User clicks "Submit"
5. Frontend calls `POST /api/ratings` with `CreateRatingRequest`
6. On success (201), display success message and update UI
7. Optionally: Show updated average rating for the movie

**Updating an Existing Rating:**
1. User clicks "Edit" on their existing rating
2. Opens rating component with pre-filled values
3. User modifies rating and/or comment
4. User clicks "Update"
5. Frontend calls `PUT /api/ratings/{id}` with `UpdateRatingRequest`
6. On success (200), display success message and update UI

**Deleting a Rating:**
1. User clicks "Delete" on their existing rating
2. Show confirmation dialog
3. User confirms deletion
4. Frontend calls `DELETE /api/ratings/{id}`
5. On success (204), remove rating from UI and refresh list

---

## Error Handling

### Common Error Responses

| Status Code | Scenario | Frontend Action |
|-------------|-----------|-----------------|
| 400 | Validation error (rating out of range, comment too long) | Show inline error message |
| 401 | Unauthorized (invalid/expired JWT) | Redirect to login page |
| 403 | Forbidden (trying to delete another user's rating) | Show error message |
| 404 | Rating/Movie/User not found | Show "Not found" error |
| 500 | Server error | Show generic error message with retry option |

---

## UI/UX Considerations

### Star Rating Input
- Interactive star component (clickable stars)
- Hover tooltips to show rating values (1-10)
- Visual feedback for selected rating
- Clear labels: "Poor", "Fair", "Good", "Excellent", etc.

### Comment Section
- Textarea with character counter (0/500)
- Optional field, clearly marked as such
- Support for line breaks (preserve on submission)

### Loading States
- Show skeleton/loading state while API call is in progress
- Disable submit button during API call
- Optimistic UI updates (show new rating immediately, rollback on error)

### Pagination
- Default page size: 20
- Page navigation buttons (Previous, Next)
- "Load more" button for infinite scroll option
- Total count display ("Showing 1-20 of 156 ratings")

### Sorting Options
- User ratings: Sort by timestamp (most recent), rating value (highest first)
- Movie ratings: Sort by rating value (highest first), timestamp (most recent)

---

## Authentication Integration

The Rating microservice expects JWT tokens in the `Authorization` header:

```typescript
headers: {
  'Authorization': `Bearer ${token}`
}
```

Token is stored in frontend from user-service login and retrieved via AuthService.

---

## Development Order

1. ✅ Implement Rating Service (TypeScript)
2. ✅ Implement Rate Movie Component
3. ✅ Implement Rating List Component
4. ✅ Implement Average Rating Display Component
5. ✅ Add to movie detail page
6. ✅ Add to user profile page
7. ✅ Add error handling and loading states
8. ✅ Test with real backend
9. ⏳ Add visual polish and animations
10. ⏳ Write unit tests

---

## Testing Checklist

### API Testing
- [ ] Create rating succeeds (201)
- [ ] Create rating with validation errors (400)
- [ ] Update rating succeeds (200)
- [ ] Delete rating succeeds (204)
- [ ] Get user ratings paginated
- [ ] Get movie ratings paginated
- [ ] Get average rating returns correct value

### Frontend Testing
- [ ] Can rate a movie
- [ ] Can update own rating
- [ ] Can delete own rating
- [ ] Cannot delete another user's rating (403)
- [ ] Pagination works correctly
- [ ] Sorting works correctly
- [ ] Average rating displays correctly
- [ ] Error messages display correctly
- [ ] Loading states work properly
- [ ] JWT authentication works
- [ ] Token expiry redirects to login

---

## Notes

- All POST and PUT endpoints require authentication
- GET endpoints for `/user/{userId}` require authentication (to view own ratings)
- GET endpoints for `/movie/{movieId}` and `/movie/{movieId}/average` are public (no auth required)
- API returns paginated results for rating lists (default 20 per page)
- JWT token subject field is used as userId
- All timestamps are in ISO 8601 format
- Rating scale: 1 (worst) to 10 (best)
