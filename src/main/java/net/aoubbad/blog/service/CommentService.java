package net.aoubbad.blog.service;


import jakarta.persistence.EntityNotFoundException;
import net.aoubbad.blog.entity.Comment;
import net.aoubbad.blog.entity.Post;
import net.aoubbad.blog.entity.User;
import net.aoubbad.blog.entity.enums.RoleName;
import net.aoubbad.blog.repository.CommentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;
    private final UserService userService;

    public CommentService(CommentRepository commentRepository,
                          PostService postService,
                          UserService userService) {
        this.commentRepository = commentRepository;
        this.postService       = postService;
        this.userService       = userService;
    }

    // â”€â”€ Lecture â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional(readOnly = true)
    public Comment findById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commentaire introuvable : id=" + id));
    }

    @Transactional(readOnly = true)
    public List<Comment> findRootCommentsByPost(Long postId) {
        Post post = postService.findById(postId);
        return commentRepository.findRootCommentsByPost(post);
    }

    @Transactional(readOnly = true)
    public Page<Comment> findByPostPaginated(Long postId, int page, int size) {
        Post post = postService.findById(postId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        return commentRepository.findByPostAndParentIsNullAndApprovedTrue(post, pageable);
    }

    @Transactional(readOnly = true)
    public List<Comment> findReplies(Long parentId) {
        return commentRepository.findRepliesByParentId(parentId);
    }

    @Transactional(readOnly = true)
    public Page<Comment> findByAuthor(Long authorId, int page, int size) {
        User author = userService.findById(authorId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return commentRepository.findByAuthor(author, pageable);
    }

    // â”€â”€ Moderation (admin) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional(readOnly = true)
    public List<Comment> findPendingComments() {
        return commentRepository.findByApprovedFalseOrderByCreatedAtAsc();
    }

    @Transactional(readOnly = true)
    public Page<Comment> findPendingPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        return commentRepository.findByApprovedFalse(pageable);
    }

    @Transactional(readOnly = true)
    public List<Comment> findRecentComments(int limit) {
        return commentRepository.findRecentComments(PageRequest.of(0, limit));
    }

    // â”€â”€ Creation â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public Comment addComment(Long postId, Long authorId, String content) {
        Post post   = postService.findById(postId);
        User author = userService.findById(authorId);

        if (!post.isPublished()) {
            throw new IllegalStateException("Impossible de commenter un article non publie.");
        }

        Comment comment = new Comment(content, author, post);
        return commentRepository.save(comment);
    }

    public Comment addReply(Long postId, Long parentCommentId, Long authorId, String content) {
        Post    post   = postService.findById(postId);
        User    author = userService.findById(authorId);
        Comment parent = findById(parentCommentId);

        if (!post.isPublished()) {
            throw new IllegalStateException("Impossible de repondre sur un article non publie.");
        }

        // On n'autorise pas les reponses imbriquees au-dela du 1er niveau
        if (parent.getParent() != null) {
            throw new IllegalArgumentException("Les reponses imbriquees a plus d'un niveau ne sont pas autorisees.");
        }

        Comment reply = new Comment(content, author, post);
        reply.setParent(parent);
        return commentRepository.save(reply);
    }

    // â”€â”€ Mise a jour â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public Comment update(Long commentId, String newContent, Long requestingUserId) {
        Comment comment = findById(commentId);
        checkOwnership(comment, requestingUserId);

        comment.setContent(newContent);
        return commentRepository.save(comment);
    }

    // â”€â”€ Moderation â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public Comment approve(Long commentId) {
        Comment comment = findById(commentId);
        comment.setApproved(true);
        return commentRepository.save(comment);
    }

    public Comment reject(Long commentId) {
        Comment comment = findById(commentId);
        comment.setApproved(false);
        return commentRepository.save(comment);
    }

    // â”€â”€ Suppression â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public void delete(Long commentId, Long requestingUserId) {
        Comment comment = findById(commentId);
        checkOwnershipOrPrivileged(comment, requestingUserId);
        commentRepository.deleteById(commentId);
    }

    public void deleteByAdmin(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new EntityNotFoundException("Commentaire introuvable : id=" + commentId);
        }
        commentRepository.deleteById(commentId);
    }

    // â”€â”€ Statistiques â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Transactional(readOnly = true)
    public long countByPost(Long postId) {
        Post post = postService.findById(postId);
        return commentRepository.countByPostAndApprovedTrue(post);
    }

    @Transactional(readOnly = true)
    public long countByAuthor(Long authorId) {
        User author = userService.findById(authorId);
        return commentRepository.countByAuthor(author);
    }

    @Transactional(readOnly = true)
    public long countPending() {
        return commentRepository.countByApprovedFalse();
    }

    // â”€â”€ Controle d'acces â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private void checkOwnership(Comment comment, Long requestingUserId) {
        if (!comment.getAuthor().getId().equals(requestingUserId)) {
            throw new SecurityException("Acces refuse : vous n'etes pas l'auteur de ce commentaire.");
        }
    }

    private void checkOwnershipOrPrivileged(Comment comment, Long requestingUserId) {
        User requester = userService.findById(requestingUserId);
        boolean isPrivileged = requester.getRoles().stream()
                .anyMatch(r -> r.getName() == RoleName.ROLE_ADMIN || r.getName() == RoleName.ROLE_MODERATOR);
        boolean isOwner = comment.getAuthor().getId().equals(requestingUserId);

        if (!isOwner && !isPrivileged) {
            throw new SecurityException("Acces refuse : vous n'etes pas l'auteur de ce commentaire.");
        }
    }
}

