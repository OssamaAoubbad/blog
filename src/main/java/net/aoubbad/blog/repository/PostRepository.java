package net.aoubbad.blog.repository;

import net.aoubbad.blog.entity.Post;
import net.aoubbad.blog.entity.User;
import net.aoubbad.blog.entity.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {


    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    List<Post> findByStatus(PostStatus status);


    Page<Post> findByAuthor(User author, Pageable pageable);

    Page<Post> findByAuthorAndStatus(User author, PostStatus status, Pageable pageable);

    List<Post> findByAuthorId(Long authorId);


    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.author " +
            "WHERE p.status = :status " +
            "ORDER BY p.publishedAt DESC")
    List<Post> findPublishedWithAuthor(@Param("status") PostStatus status);

    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.comments " +
            "WHERE p.id = :id")
    Optional<Post> findByIdWithAuthorAndComments(@Param("id") Long id);


    @Query("SELECT p FROM Post p " +
            "WHERE p.status = 'PUBLISHED' AND (" +
            "LOWER(p.title)   LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.summary) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Post> searchPublished(@Param("keyword") String keyword, Pageable pageable);


    @Modifying
    @Query("UPDATE Post p SET p.viewsCount = p.viewsCount + 1 WHERE p.id = :id")
    void incrementViewsCount(@Param("id") Long id);


    long countByStatus(PostStatus status);

    long countByAuthor(User author);

    @Query("SELECT SUM(p.viewsCount) FROM Post p WHERE p.author.id = :authorId")
    Long sumViewsByAuthorId(@Param("authorId") Long authorId);


    @Query("SELECT p FROM Post p " +
            "WHERE p.status = 'PUBLISHED' " +
            "ORDER BY p.viewsCount DESC")
    List<Post> findTopByViews(Pageable pageable);


    @Query("SELECT p FROM Post p " +
            "WHERE p.author.id = :authorId AND p.status = 'PUBLISHED' " +
            "ORDER BY p.publishedAt DESC")
    List<Post> findRecentByAuthorId(@Param("authorId") Long authorId, Pageable pageable);
}