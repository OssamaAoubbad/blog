package net.aoubbad.blog.repository;

import net.aoubbad.blog.entity.Comment;
import net.aoubbad.blog.entity.Post;
import net.aoubbad.blog.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {


    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.author " +
            "WHERE c.post = :post AND c.parent IS NULL AND c.approved = true " +
            "ORDER BY c.createdAt ASC")
    List<Comment> findRootCommentsByPost(@Param("post") Post post);


    Page<Comment> findByPostAndParentIsNullAndApprovedTrue(Post post, Pageable pageable);


    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.author " +
            "WHERE c.parent.id = :parentId AND c.approved = true " +
            "ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentId(@Param("parentId") Long parentId);


    Page<Comment> findByAuthor(User author, Pageable pageable);

    List<Comment> findByAuthorId(Long authorId);


    List<Comment> findByApprovedFalseOrderByCreatedAtAsc();

    Page<Comment> findByApprovedFalse(Pageable pageable);


    long countByPost(Post post);

    long countByPostAndApprovedTrue(Post post);

    long countByAuthor(User author);

    long countByApprovedFalse();


    boolean existsByIdAndAuthorId(Long commentId, Long authorId);


    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.author " +
            "JOIN FETCH c.post " +
            "ORDER BY c.createdAt DESC")
    List<Comment> findRecentComments(Pageable pageable);
}