package net.aoubbad.blog.repository;

import net.aoubbad.blog.entity.Post;
import net.aoubbad.blog.entity.PostViewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostViewLogRepository extends JpaRepository<PostViewLog, Long> {

    boolean existsByPostAndViewerId(Post post, Long viewerId);

    boolean existsByPostAndSessionId(Post post, String sessionId);
}
