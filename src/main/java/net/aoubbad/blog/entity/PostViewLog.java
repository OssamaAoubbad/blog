package net.aoubbad.blog.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "post_views",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"post_id", "viewer_id"}),
                @UniqueConstraint(columnNames = {"post_id", "session_id"})
        })
public class PostViewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "viewer_id")
    private Long viewerId;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "viewed_at", nullable = false, updatable = false)
    private LocalDateTime viewedAt;

    public PostViewLog(Post post, Long viewerId, String sessionId) {
        this.post = post;
        this.viewerId = viewerId;
        this.sessionId = sessionId;
    }

    @PrePersist
    private void onPersist() {
        this.viewedAt = LocalDateTime.now();
    }
}
