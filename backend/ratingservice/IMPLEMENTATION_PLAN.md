# Rating Microservice Implementation Plan

## Overview
The Rating microservice handles all operations related to movie ratings, including creating, updating, deleting, and retrieving ratings. It uses Neo4j's graph database to model the `User -[RATED]-> Movie` relationship and emits Kafka events for integration with other services.

## High-Level Features

### 1. **Rate a Movie**
- User submits a rating (1-10) with an optional comment
- Creates or updates the `User -[RATED]-> Movie` relationship
- Validates that both user and movie exist
- Emits a Kafka event (RATING_CREATED or RATING_UPDATED)

### 2. **View User's Ratings**
- Retrieve all ratings for a specific user
- Include movie details (title, genre, year)
- Sort by most recent timestamp
- Support pagination for large datasets

### 3. **View Movie's Ratings**
- Retrieve all ratings for a specific movie
- Include user details (username)
- Calculate and return average rating
- Sort by rating value or date

### 4. **Update Rating**
- User modifies their existing rating
- Automatically updates the timestamp
- Emits a Kafka event (RATING_UPDATED)

### 5. **Delete Rating**
- User removes their rating for a movie
- Deletes the RATED relationship
- Emits a Kafka event (RATING_DELETED)

### 6. **Get Average Rating**
- Calculate the average rating for a movie
- Return both the average and the total count
- Cache results for performance optimization

---

## Implementation Phases

### Phase 1: Data Model & Repository Layer

#### 1.1 Update Models with Neo4j Annotations

**Rating Model** (`@RelationshipEntity`)
```java
@RelationshipEntity(type = "RATED")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {
    @Id
    @GeneratedValue
    private Long id;
    
    @Property("rating")
    @Min(1) @Max(10)
    private Integer rating;
    
    @Property("comment")
    @Size(max = 500)
    private String comment;
    
    @Property("timestamp")
    private LocalDateTime timestamp;
    
    @StartNode
    private User user;
    
    @EndNode
    private Movie movie;
}
```

**User Model** (`@Node`)
```java
@Node("User")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue
    private String id;
    
    @Property("username")
    private String username;
    
    @Property("email")
    private String email;
}
```

**Movie Model** (`@Node`)
```java
@Node("Movie")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {
    @Id
    @GeneratedValue
    private String id;
    
    @Property("title")
    private String title;
    
    @Property("releaseDate")
    private LocalDate releaseDate;
    
    @Property("genre")
    private String genre;
}
```

#### 1.2 RatingRepository
```java
public interface RatingRepository extends Neo4jRepository<Rating, Long> {
    
    // Find all ratings by a user
    List<Rating> findByUserId(String userId);
    
    // Find all ratings for a movie
    List<Rating> findByMovieId(String movieId);
    
    // Find specific user's rating for a movie
    Optional<Rating> findByUserIdAndMovieId(String userId, String movieId);
    
    // Count ratings for a movie
    long countByMovieId(String movieId);
    
    // Calculate average rating for a movie
    @Query("MATCH (:User {id: $userId})-[r:RATED]->(:Movie {id: $movieId}) RETURN avg(r.rating)")
    Optional<Double> findAverageRatingByMovieId(String movieId);
}
```

---

### Phase 2: Business Logic (Service Layer)

**RatingService**
```java
@Service
public class RatingService {
    
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final RatingEventProducer eventProducer;
    
    public Rating createRating(String userId, String movieId, Integer rating, String comment) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        Movie movie = movieRepository.findById(movieId)
            .orElseThrow(() -> new MovieNotFoundException(movieId));
        
        Rating newRating = Rating.builder()
            .user(user)
            .movie(movie)
            .rating(rating)
            .comment(comment)
            .timestamp(LocalDateTime.now())
            .build();
        
        Rating saved = ratingRepository.save(newRating);
        eventProducer.publishRatingCreatedEvent(saved);
        return saved;
    }
    
    public Rating updateRating(Long ratingId, Integer rating, String comment) {
        Rating existingRating = ratingRepository.findById(ratingId)
            .orElseThrow(() -> new RatingNotFoundException(ratingId));
        
        existingRating.setRating(rating);
        existingRating.setComment(comment);
        existingRating.setTimestamp(LocalDateTime.now());
        
        Rating updated = ratingRepository.save(existingRating);
        eventProducer.publishRatingUpdatedEvent(updated);
        return updated;
    }
    
    public void deleteRating(Long ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
            .orElseThrow(() -> new RatingNotFoundException(ratingId));
        ratingRepository.delete(rating);
        eventProducer.publishRatingDeletedEvent(rating);
    }
    
    public Page<RatingDTO> getUserRatings(String userId, Pageable pageable) {
        Page<Rating> ratings = ratingRepository.findByUserId(userId, pageable);
        return ratings.map(this::toDTO);
    }
    
    public Page<RatingDTO> getMovieRatings(String movieId, Pageable pageable) {
        Page<Rating> ratings = ratingRepository.findByMovieId(movieId, pageable);
        return ratings.map(this::toDTO);
    }
    
    public AverageRatingDTO getAverageRating(String movieId) {
        List<Rating> ratings = ratingRepository.findByMovieId(movieId);
        if (ratings.isEmpty()) {
            return AverageRatingDTO.builder()
                .movieId(movieId)
                .average(0.0)
                .count(0)
                .build();
        }
        
        double average = ratings.stream()
            .mapToInt(Rating::getRating)
            .average()
            .orElse(0.0);
        
        return AverageRatingDTO.builder()
            .movieId(movieId)
            .average(average)
            .count(ratings.size())
            .build();
    }
    
    private RatingDTO toDTO(Rating rating) {
        return RatingDTO.builder()
            .id(rating.getId())
            .rating(rating.getRating())
            .comment(rating.getComment())
            .timestamp(rating.getTimestamp())
            .userId(rating.getUser().getId())
            .username(rating.getUser().getUsername())
            .movieId(rating.getMovie().getId())
            .movieTitle(rating.getMovie().getTitle())
            .build();
    }
}
```

---

### Phase 3: REST API (Controller Layer)

**RatingController**
```java
@RestController
@RequestMapping("/api/ratings")
public class RatingController {
    
    @PostMapping
    public ResponseEntity<RatingDTO> createRating(
        @RequestBody CreateRatingRequest request,
        @AuthenticationPrincipal Jwt jwt) {
        
        String userId = jwt.getSubject();
        Rating rating = ratingService.createRating(
            userId, 
            request.getMovieId(), 
            request.getRating(), 
            request.getComment()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(toDTO(rating));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<RatingDTO> updateRating(
        @PathVariable Long id,
        @RequestBody UpdateRatingRequest request) {
        
        Rating rating = ratingService.updateRating(
            id, 
            request.getRating(), 
            request.getComment()
        );
        
        return ResponseEntity.ok(toDTO(rating));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRating(@PathVariable Long id) {
        ratingService.deleteRating(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<RatingDTO>> getUserRatings(
        @PathVariable String userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "timestamp") String sortBy) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<RatingDTO> ratings = ratingService.getUserRatings(userId, pageable);
        return ResponseEntity.ok(ratings);
    }
    
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<Page<RatingDTO>> getMovieRatings(
        @PathVariable String movieId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "rating") String sortBy) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<RatingDTO> ratings = ratingService.getMovieRatings(movieId, pageable);
        return ResponseEntity.ok(ratings);
    }
    
    @GetMapping("/movie/{movieId}/average")
    public ResponseEntity<AverageRatingDTO> getAverageRating(@PathVariable String movieId) {
        AverageRatingDTO averageRating = ratingService.getAverageRating(movieId);
        return ResponseEntity.ok(averageRating);
    }
}
```

**DTOs**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRatingRequest {
    @NotBlank
    private String movieId;
    
    @Min(1) @Max(10)
    private Integer rating;
    
    @Size(max = 500)
    private String comment;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRatingRequest {
    @Min(1) @Max(10)
    private Integer rating;
    
    @Size(max = 500)
    private String comment;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingDTO {
    private Long id;
    private Integer rating;
    private String comment;
    private LocalDateTime timestamp;
    private String userId;
    private String username;
    private String movieId;
    private String movieTitle;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AverageRatingDTO {
    private String movieId;
    private Double average;
    private Long count;
}
```

---

### Phase 4: Event Publishing (Kafka)

**RatingEventProducer**
```java
@Service
public class RatingEventProducer {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${kafka.topic.rating-created}")
    private String ratingCreatedTopic;
    
    @Value("${kafka.topic.rating-updated}")
    private String ratingUpdatedTopic;
    
    @Value("${kafka.topic.rating-deleted}")
    private String ratingDeletedTopic;
    
    public void publishRatingCreatedEvent(Rating rating) {
        RatingEvent event = RatingEvent.builder()
            .eventType("RATING_CREATED")
            .ratingId(rating.getId())
            .userId(rating.getUser().getId())
            .movieId(rating.getMovie().getId())
            .rating(rating.getRating())
            .comment(rating.getComment())
            .timestamp(rating.getTimestamp())
            .build();
        
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(ratingCreatedTopic, event.getMovieId(), json);
        } catch (JsonProcessingException e) {
            throw new EventPublishingException("Failed to publish rating created event", e);
        }
    }
    
    public void publishRatingUpdatedEvent(Rating rating) {
        RatingEvent event = RatingEvent.builder()
            .eventType("RATING_UPDATED")
            .ratingId(rating.getId())
            .userId(rating.getUser().getId())
            .movieId(rating.getMovie().getId())
            .rating(rating.getRating())
            .comment(rating.getComment())
            .timestamp(rating.getTimestamp())
            .build();
        
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(ratingUpdatedTopic, event.getMovieId(), json);
        } catch (JsonProcessingException e) {
            throw new EventPublishingException("Failed to publish rating updated event", e);
        }
    }
    
    public void publishRatingDeletedEvent(Rating rating) {
        RatingEvent event = RatingEvent.builder()
            .eventType("RATING_DELETED")
            .ratingId(rating.getId())
            .userId(rating.getUser().getId())
            .movieId(rating.getMovie().getId())
            .rating(rating.getRating())
            .comment(rating.getComment())
            .timestamp(rating.getTimestamp())
            .build();
        
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(ratingDeletedTopic, event.getMovieId(), json);
        } catch (JsonProcessingException e) {
            throw new EventPublishingException("Failed to publish rating deleted event", e);
        }
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingEvent {
    private String eventType;
    private Long ratingId;
    private String userId;
    private String movieId;
    private Integer rating;
    private String comment;
    private LocalDateTime timestamp;
}
```

---

### Phase 5: Configuration & Integration

#### 5.1 Update application.properties
```properties
spring.application.name=ratingservice
server.port=0

# Eureka Configuration
eureka.client.serviceUrl.defaultZone=${EUREKA_URI:http://localhost:8761/eureka}
eureka.instance.preferIpAddress=true

# Neo4j Aura Cloud Configuration
spring.neo4j.uri=${NEO4J_URI:}
spring.neo4j.authentication.username=${NEO4J_USERNAME:neo4j}
spring.neo4j.authentication.password=${NEO4J_PASSWORD:}

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer

# Kafka Topics
kafka.topic.rating-created=rating-created
kafka.topic.rating-updated=rating-updated
kafka.topic.rating-deleted=rating-deleted

# JWT Configuration
jwt.secret=${JWT_SECRET:}

# Logging
org.neo4j.driver.level=FINE
org.neo4j.driver.logging.slf4j.Slf4jBridge=enabled
```

#### 5.2 Update SecurityConfig
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http, 
        JwtAuthenticationFilter jwtAuthFilter) throws Exception {
        
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/greeting").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(sesh -> sesh
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

---

### Phase 6: Testing

#### 6.1 Unit Tests
- **RatingServiceTests**: Test all service methods with mock repositories
- **RatingEventProducerTests**: Verify Kafka events are published correctly
- **DTO Validation Tests**: Test request validation (rating range, comment length)

#### 6.2 Integration Tests
- **RatingRepositoryTests**: Test repository operations with test Neo4j instance
- **RatingControllerTests**: Test REST endpoints with Spring MockMvc
- **SecurityTests**: Verify authentication and authorization

#### 6.3 Event Publishing Tests
- Verify events are published to correct topics
- Test event payload structure
- Handle and test failure scenarios

---

## API Endpoints Summary

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/ratings` | Create a rating | Yes |
| PUT | `/api/ratings/{id}` | Update a rating | Yes |
| DELETE | `/api/ratings/{id}` | Delete a rating | Yes |
| GET | `/api/ratings/user/{userId}` | Get user's ratings (paginated) | Yes |
| GET | `/api/ratings/movie/{movieId}` | Get movie's ratings (paginated) | Yes |
| GET | `/api/ratings/movie/{movieId}/average` | Get average rating for movie | No |

---

## Kafka Events

### Topics
1. `rating-created` - Published when a new rating is created
2. `rating-updated` - Published when an existing rating is updated
3. `rating-deleted` - Published when a rating is deleted

### Event Payload
```json
{
  "eventType": "RATING_CREATED | RATING_UPDATED | RATING_DELETED",
  "ratingId": 123,
  "userId": "user-123",
  "movieId": "movie-456",
  "rating": 8,
  "comment": "Great movie!",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Dependencies Required
- Already included in `pom.xml`:
  - Spring Boot Starter Data Neo4j
  - Spring Boot Starter Security
  - Spring Cloud Netflix Eureka Client
  - Spring Kafka
  - JWT (jjwt-api, jjwt-impl, jjwt-jackson)
  - Lombok
  - Jackson (databind)
  - Jakarta Validation

---

## Notes
- All rating operations require JWT authentication
- JWT token's subject field is used as userId
- Rating must be between 1 and 10
- Comment is optional but limited to 500 characters
- Timestamp is automatically set on creation/update
- All CRUD operations are idempotent (safe to retry)
- Kafka events use movieId as the key for partitioning
