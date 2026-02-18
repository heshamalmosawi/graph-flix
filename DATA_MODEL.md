# Neo4j Data Model for Neo4flix

## Nodes

### User Node
```cypher
CREATE (u:User {
  id: String,
  username: String,
  email: String,
  password: String,
  createdAt: DateTime,
  twoFactorEnabled: Boolean
})
```

### Movie Node
```cypher
CREATE (m:Movie {
  id: String,
  title: String,
  releaseDate: Date,
  genre: String,
  description: String,
  duration: Integer,
  posterUrl: String OPTIONAL,
  imdbRating: Float OPTIONAL
})
```

### Genre Node
```cypher
CREATE (g:Genre {
  name: String
})
```

### Actor Node
```cypher
CREATE (a:Actor {
  id: String,
  name: String,
  birthDate: Date OPTIONAL
})
```

### Director Node
```cypher
CREATE (d:Director {
  id: String,
  name: String,
  birthDate: Date OPTIONAL
})
```

## Relationships

### User-Rated-Movie Relationship
```cypher
CREATE (u:User)-[r:RATED {
  rating: Integer,
  comment: String OPTIONAL,
  timestamp: DateTime
}]->(m:Movie)
```

### User-Watchlist-Movie Relationship
```cypher
CREATE (u:User)-[r:IN_WATCHLIST {
  addedAt: DateTime
}]->(m:Movie)
```

### User-Recommended-Movie Relationship
```cypher
CREATE (u:User)-[r:RECOMMENDED {
  score: Float,
  reason: String,
  generatedAt: DateTime
}]->(m:Movie)
```

### Movie-BelongsTo-Genre Relationship
```cypher
CREATE (m:Movie)-[r:BELONGS_TO_GENRE]->(g:Genre)
```

### Actor-PlayedIn-Movie Relationship
```cypher
CREATE (a:Actor)-[r:ACTED_IN {
  role: String OPTIONAL
}]->(m:Movie)
```

### Director-Directed-Movie Relationship
```cypher
CREATE (d:Director)-[r:DIRECTED]->(m:Movie)
```

### Movie-SimilarTo-Movie Relationship
```cypher
CREATE (m1:Movie)-[r:SIMILAR_TO {
  similarityScore: Float,
  commonGenres: Integer,
  commonRaters: Integer
}]->(m2:Movie)
```

## Indexes and Constraints

### Constraints
```cypher
CREATE CONSTRAINT user_id_unique IF NOT EXISTS FOR (u:User) REQUIRE u.id IS UNIQUE;
CREATE CONSTRAINT user_email_unique IF NOT EXISTS FOR (u:User) REQUIRE u.email IS UNIQUE;
CREATE CONSTRAINT user_username_unique IF NOT EXISTS FOR (u:User) REQUIRE u.username IS UNIQUE;
CREATE CONSTRAINT movie_id_unique IF NOT EXISTS FOR (m:Movie) REQUIRE m.id IS UNIQUE;
CREATE CONSTRAINT genre_name_unique IF NOT EXISTS FOR (g:Genre) REQUIRE g.name IS UNIQUE;
CREATE CONSTRAINT actor_id_unique IF NOT EXISTS FOR (a:Actor) REQUIRE a.id IS UNIQUE;
CREATE CONSTRAINT director_id_unique IF NOT EXISTS FOR (d:Director) REQUIRE d.id IS UNIQUE;
```

### Indexes
```cypher
CREATE INDEX movie_title_index IF NOT EXISTS FOR (m:Movie) ON (m.title);
CREATE INDEX movie_release_date_index IF NOT EXISTS FOR (m:Movie) ON (m.releaseDate);
CREATE INDEX movie_genre_index IF NOT EXISTS FOR (m:Movie) ON (m.genre);
CREATE INDEX movie_imdb_rating_index IF NOT EXISTS FOR (m:Movie) ON (m.imdbRating);
CREATE INDEX user_username_index IF NOT EXISTS FOR (u:User) ON (u.username);
CREATE INDEX user_email_index IF NOT EXISTS FOR (u:User) ON (u.email);
CREATE INDEX rating_rating_index IF NOT EXISTS FOR ()-[r:RATED]-() ON (r.rating);
CREATE INDEX rating_timestamp_index IF NOT EXISTS FOR ()-[r:RATED]-() ON (r.timestamp);
```

## Sample Cypher Queries

### Create a New User
```cypher
MERGE (u:User {id: 'user-123'})
SET u.username = 'john_doe',
    u.email = 'john@example.com',
    u.password = '$2a$10$encrypted_password_hash',
    u.createdAt = datetime(),
    u.twoFactorEnabled = false
```

### Create a New Movie with Genres and Cast
```cypher
MERGE (m:Movie {id: 'movie-456'})
SET m.title = 'Inception',
    m.releaseDate = date('2010-07-16'),
    m.description = 'A thief who steals corporate secrets...',
    m.duration = 148,
    m.imdbRating = 8.8

MERGE (g1:Genre {name: 'Sci-Fi'})
MERGE (g2:Genre {name: 'Action'})
MERGE (g3:Genre {name: 'Thriller'})

MERGE (m)-[:BELONGS_TO_GENRE]->(g1)
MERGE (m)-[:BELONGS_TO_GENRE]->(g2)
MERGE (m)-[:BELONGS_TO_GENRE]->(g3)
```

### Rate a Movie
```cypher
MATCH (u:User {id: 'user-123'})
MATCH (m:Movie {id: 'movie-456'})
MERGE (u)-[r:RATED]->(m)
SET r.rating = 9,
    r.comment = 'Amazing movie!',
    r.timestamp = datetime()
```

### Add to Watchlist
```cypher
MATCH (u:User {id: 'user-123'})
MATCH (m:Movie {id: 'movie-456'})
MERGE (u)-[r:IN_WATCHLIST]->(m)
SET r.addedAt = datetime()
```

### Get User's Rated Movies
```cypher
MATCH (u:User {id: 'user-123'})-[r:RATED]->(m:Movie)
RETURN m.title, r.rating, r.timestamp, r.comment
ORDER BY r.timestamp DESC
```

### Get User's Watchlist
```cypher
MATCH (u:User {id: 'user-123'})-[r:IN_WATCHLIST]->(m:Movie)
RETURN m.title, m.releaseDate, r.addedAt
ORDER BY r.addedAt DESC
```

### Search Movies by Title (Partial Match)
```cypher
MATCH (m:Movie)
WHERE toLower(m.title) CONTAINS toLower('inception')
RETURN m.title, m.releaseDate, m.genre, m.imdbRating
ORDER BY m.imdbRating DESC
```

### Search Movies by Genre
```cypher
MATCH (m:Movie)-[:BELONGS_TO_GENRE]->(g:Genre {name: 'Sci-Fi'})
RETURN m.title, m.releaseDate, m.imdbRating
ORDER BY m.releaseDate DESC
```

### Get Similar Movies Based on Genre
```cypher
MATCH (m:Movie {id: 'movie-456'})-[:BELONGS_TO_GENRE]->(g:Genre)<-[:BELONGS_TO_GENRE]-(other:Movie)
WHERE other.id <> m.id
RETURN other.title, other.releaseDate, other.imdbRating, count(g) as commonGenres
ORDER BY commonGenres DESC, other.imdbRating DESC
LIMIT 10
```

### Get Movies Rated by Similar Users (Collaborative Filtering)
```cypher
MATCH (u:User {id: 'user-123'})-[r:RATED]->(m:Movie)
MATCH (other:User)-[r2:RATED]->(m)
MATCH (other)-[r3:RATED]->(rec:Movie)
WHERE NOT EXISTS((u)-[:RATED]->(rec))
RETURN rec.title, count(r3) as recommendationScore, avg(r3.rating) as avgRating
ORDER BY recommendationScore DESC, avgRating DESC
LIMIT 10
```

### Get User's Average Rating
```cypher
MATCH (u:User {id: 'user-123'})-[r:RATED]->(m:Movie)
RETURN count(r) as totalRatings, avg(r.rating) as avgRating
```

### Get Top Rated Movies
```cypher
MATCH (m:Movie)<-[r:RATED]-()
RETURN m.title, m.releaseDate, m.genre, avg(r.rating) as avgRating, count(r) as totalRatings
WHERE count(r) >= 5
ORDER BY avgRating DESC, totalRatings DESC
LIMIT 20
```

### Get Movies Recommended to User
```cypher
MATCH (u:User {id: 'user-123'})-[r:RECOMMENDED]->(m:Movie)
RETURN m.title, m.genre, r.score, r.reason
ORDER BY r.score DESC
LIMIT 20
```

### Generate Recommendations for User
```cypher
MATCH (u:User {id: 'user-123'})-[r:RATED]->(m:Movie)
WHERE r.rating >= 7

MATCH (m)-[:BELONGS_TO_GENRE]->(g:Genre)
WITH u, collect(DISTINCT g.name) as userGenres

MATCH (rec:Movie)-[:BELONGS_TO_GENRE]->(g:Genre)
WHERE g.name IN userGenres
AND NOT EXISTS((u)-[:RATED]->(rec))

WITH rec, count(g) as genreMatch, avg(g.name IN userGenres) as relevanceScore
ORDER BY genreMatch DESC, rec.imdbRating DESC
LIMIT 20

MATCH (u)-[r:RECOMMENDED]->(rec)
DELETE r

WITH u, rec, genreMatch
CREATE (u)-[recRel:RECOMMENDED {
  score: genreMatch * 1.0 + (rec.imdbRating * 0.1),
  reason: 'Based on your genre preferences',
  generatedAt: datetime()
}]->(rec)

RETURN rec.title, rec.genre, recRel.score
```

## Schema Summary

| Node Type | Properties | Relationships |
|-----------|------------|----------------|
| User | id, username, email, password, createdAt, twoFactorEnabled | RATED, IN_WATCHLIST, RECOMMENDED |
| Movie | id, title, releaseDate, genre, description, duration, posterUrl, imdbRating | BELONGS_TO_GENRE, RATED, IN_WATCHLIST, RECOMMENDED |
| Genre | name | BELONGS_TO_GENRE |
| Actor | id, name, birthDate | ACTED_IN |
| Director | id, name, birthDate | DIRECTED |

| Relationship Type | Properties | From → To |
|-------------------|------------|-----------|
| RATED | rating, comment, timestamp | User → Movie |
| IN_WATCHLIST | addedAt | User → Movie |
| RECOMMENDED | score, reason, generatedAt | User → Movie |
| BELONGS_TO_GENRE | - | Movie → Genre |
| ACTED_IN | role | Actor → Movie |
| DIRECTED | - | Director → Movie |
| SIMILAR_TO | similarityScore, commonGenres, commonRaters | Movie → Movie |
