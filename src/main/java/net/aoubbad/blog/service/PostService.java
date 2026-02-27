package net.aoubbad.blog.service;

import jakarta.persistence.EntityNotFoundException;
import net.aoubbad.blog.entity.Post;
import net.aoubbad.blog.entity.PostLike;
import net.aoubbad.blog.entity.PostViewLog;
import net.aoubbad.blog.entity.User;
import net.aoubbad.blog.entity.enums.PostStatus;
import net.aoubbad.blog.entity.enums.RoleName;
import net.aoubbad.blog.repository.PostLikeRepository;
import net.aoubbad.blog.repository.PostRepository;
import net.aoubbad.blog.repository.PostViewLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserService userService;
    private final PostViewLogRepository postViewLogRepository;
    private final PostLikeRepository postLikeRepository;

    public PostService(PostRepository postRepository,
                       UserService userService,
                       PostViewLogRepository postViewLogRepository,
                       PostLikeRepository postLikeRepository) {
        this.postRepository = postRepository;
        this.userService = userService;
        this.postViewLogRepository = postViewLogRepository;
        this.postLikeRepository = postLikeRepository;
    }

    // â”€â”€ Lecture publique â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional(readOnly = true)
    public Page<Post> findAllPublished(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        return postRepository.findByStatus(PostStatus.PUBLISHED, pageable);
    }

    @Transactional(readOnly = true)
    public Post findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article introuvable : id=" + id));
    }

    @Transactional(readOnly = true)
    public Post findByIdWithDetails(Long id) {
        return postRepository.findByIdWithAuthorAndComments(id)
                .orElseThrow(() -> new EntityNotFoundException("Article introuvable : id=" + id));
    }

    @Transactional(readOnly = true)
    public Page<Post> searchPublished(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        return postRepository.searchPublished(keyword, pageable);
    }

    @Transactional(readOnly = true)
    public List<Post> findTopPosts(int limit) {
        return postRepository.findTopByViews(PageRequest.of(0, limit));
    }

    // â”€â”€ Lecture auteur â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional(readOnly = true)
    public Page<Post> findByAuthor(Long authorId, int page, int size) {
        User author = userService.findById(authorId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postRepository.findByAuthor(author, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Post> findByAuthorAndStatus(Long authorId, PostStatus status, int page, int size) {
        User author = userService.findById(authorId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postRepository.findByAuthorAndStatus(author, status, pageable);
    }

    @Transactional(readOnly = true)
    public List<Post> findRecentByAuthor(Long authorId, int limit) {
        return postRepository.findRecentByAuthorId(authorId, PageRequest.of(0, limit));
    }

    // â”€â”€ Lecture admin â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional(readOnly = true)
    public Page<Post> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Post> findByStatus(PostStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postRepository.findByStatus(status, pageable);
    }

    // â”€â”€ Creation â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public Post create(Post post, Long authorId) {
        User author = userService.findById(authorId);
        post.setAuthor(author);

        if (post.getStatus() == null) {
            post.setStatus(PostStatus.DRAFT);
        }

        return postRepository.save(post);
    }

    // â”€â”€ Mise a jour â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public Post update(Long id, Post updatedData, Long requestingUserId) {
        Post existing = findById(id);
        checkOwnershipOrPrivileged(existing, requestingUserId);

        if (updatedData.getTitle()   != null) existing.setTitle(updatedData.getTitle());
        if (updatedData.getSummary() != null) existing.setSummary(updatedData.getSummary());
        if (updatedData.getContent() != null) existing.setContent(updatedData.getContent());
        if (updatedData.getCoverImage() != null) existing.setCoverImage(updatedData.getCoverImage());

        // Changement de statut
        if (updatedData.getStatus() != null && updatedData.getStatus() != existing.getStatus()) {
            existing.setStatus(updatedData.getStatus());
        }

        return postRepository.save(existing);
    }

    // â”€â”€ Publication / Depublication â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public Post publish(Long id, Long requestingUserId) {
        Post post = findById(id);
        checkOwnershipOrPrivileged(post, requestingUserId);
        post.setStatus(PostStatus.PUBLISHED);
        return postRepository.save(post);
    }

    public Post unpublish(Long id, Long requestingUserId) {
        Post post = findById(id);
        checkOwnershipOrPrivileged(post, requestingUserId);
        post.setStatus(PostStatus.DRAFT);
        return postRepository.save(post);
    }

    public Post archive(Long id, Long requestingUserId) {
        Post post = findById(id);
        checkOwnershipOrPrivileged(post, requestingUserId);
        post.setStatus(PostStatus.ARCHIVED);
        return postRepository.save(post);
    }

    // â”€â”€ Incrementation des vues â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public void recordView(Post post, Long viewerId, String sessionId) {
        if (viewerId != null) {
            if (postViewLogRepository.existsByPostAndViewerId(post, viewerId)) {
                return;
            }
        } else if (sessionId != null) {
            if (postViewLogRepository.existsByPostAndSessionId(post, sessionId)) {
                return;
            }
        } else {
            return;
        }

        PostViewLog log = new PostViewLog(post, viewerId, sessionId);
        postViewLogRepository.save(log);
        postRepository.incrementViewsCount(post.getId());
        post.incrementViews();
    }

    public boolean likePost(Long postId, Long userId) {
        if (userId == null || postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            return false;
        }

        Post post = findById(postId);
        User user = userService.findById(userId);
        PostLike like = new PostLike(post, user);
        postLikeRepository.save(like);
        post.incrementLikes();
        postRepository.save(post);
        return true;
    }

    public boolean unlikePost(Long postId, Long userId) {
        if (userId == null) {
            return false;
        }
        Optional<PostLike> existing = postLikeRepository.findByPostIdAndUserId(postId, userId);
        if (existing.isEmpty()) {
            return false;
        }

        Post post = findById(postId);
        postLikeRepository.delete(existing.get());
        post.decrementLikes();
        postRepository.save(post);
        return true;
    }

    public boolean hasUserLiked(Long postId, Long userId) {
        if (userId == null) {
            return false;
        }
        return postLikeRepository.existsByPostIdAndUserId(postId, userId);
    }

    public void share(Long postId) {
        Post post = findById(postId);
        post.incrementShares();
        postRepository.save(post);
    }

    // â”€â”€ Suppression â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public void delete(Long id, Long requestingUserId) {
        Post post = findById(id);
        checkOwnershipOrPrivileged(post, requestingUserId);
        postRepository.deleteById(id);
    }

    public void deleteByAdmin(Long id) {
        if (!postRepository.existsById(id)) {
            throw new EntityNotFoundException("Article introuvable : id=" + id);
        }
        postRepository.deleteById(id);
    }

    // â”€â”€ Statistiques â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional(readOnly = true)
    public long countByStatus(PostStatus status) {
        return postRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countByAuthor(Long authorId) {
        User author = userService.findById(authorId);
        return postRepository.countByAuthor(author);
    }

    @Transactional(readOnly = true)
    public Long sumViewsByAuthor(Long authorId) {
        Long total = postRepository.sumViewsByAuthorId(authorId);
        return total != null ? total : 0L;
    }

    // â”€â”€ Controle d'acces â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private void checkOwnershipOrPrivileged(Post post, Long requestingUserId) {
        User requester = userService.findById(requestingUserId);
        boolean isPrivileged = requester.getRoles().stream()
                .anyMatch(r -> r.getName() == RoleName.ROLE_ADMIN || r.getName() == RoleName.ROLE_MODERATOR);
        boolean isOwner = post.getAuthor().getId().equals(requestingUserId);

        if (!isOwner && !isPrivileged) {
            throw new SecurityException("Acces refuse : vous n'etes pas l'auteur de cet article.");
        }
    }
}
