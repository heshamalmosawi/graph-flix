package com.graphflix.recommendationservice.repository;

import java.util.List;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.graphflix.recommendationservice.model.Movie;

@Repository
public interface RecommendationRepository extends Neo4jRepository<Movie, String> {
    
    @Query("""
        MATCH (user:User {email: $email})-[r:RATED]->(likedMovie:Movie)
        WHERE r.rating >= $minRating
        WITH user, likedMovie
        MATCH (likedMovie)<-[:ACTED_IN]-(actor:Person)-[:ACTED_IN]->(candidateMovie:Movie)
        WHERE NOT (user)-[:RATED]->(candidateMovie)
        AND candidateMovie <> likedMovie
        RETURN candidateMovie, count(actor) as actorMatches
        ORDER BY actorMatches DESC, candidateMovie.released DESC
        LIMIT $limit
        """)
    List<Movie> findMoviesByLikedActors(
        @Param("email") String email,
        @Param("minRating") Integer minRating,
        @Param("limit") Integer limit
    );
    
    @Query("""
        MATCH (user:User {email: $email})-[r:RATED]->(likedMovie:Movie)
        WHERE r.rating >= $minRating
        MATCH (likedMovie)<-[:DIRECTED]-(director:Person)-[:DIRECTED]->(candidateMovie:Movie)
        WHERE NOT (user)-[:RATED]->(candidateMovie)
        AND candidateMovie <> likedMovie
        RETURN candidateMovie, count(director) as directorMatches
        ORDER BY directorMatches DESC, candidateMovie.released DESC
        LIMIT $limit
        """)
    List<Movie> findMoviesByLikedDirectors(
        @Param("email") String email,
        @Param("minRating") Integer minRating,
        @Param("limit") Integer limit
    );
    
    @Query("""
        MATCH (m:Movie)<-[r:RATED]-(u:User)
        WITH m, count(r) as ratingCount, avg(r.rating) as avgRating
        WHERE ratingCount >= 1
        RETURN m, avgRating
        ORDER BY ratingCount DESC, avgRating DESC
        LIMIT $limit
        """)
    List<Movie> findTrendingMovies(@Param("limit") Integer limit);
    
    @Query("""
        MATCH (user:User {email: $email})-[r:RATED]->(m:Movie)
        RETURN count(r) as ratingCount
        """)
    Long countUserRatings(@Param("email") String email);
}
