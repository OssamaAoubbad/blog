package net.aoubbad.blog.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import net.aoubbad.blog.entity.Post;
import net.aoubbad.blog.entity.enums.PostStatus;
import net.aoubbad.blog.service.CustomUserDetails;
import net.aoubbad.blog.service.FileStorageService;
import net.aoubbad.blog.service.PostService;
import net.aoubbad.blog.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final FileStorageService fileStorageService;
    private final UserService userService;

    public PostController(PostService postService,
                          FileStorageService fileStorageService,
                          UserService userService) {
        this.postService = postService;
        this.fileStorageService = fileStorageService;
        this.userService = userService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/like")
    public String likePost(@PathVariable Long id,
                           @AuthenticationPrincipal CustomUserDetails currentUser) {
        postService.likePost(id, currentUser.getId());
        return "redirect:/posts/" + id;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/unlike")
    public String unlikePost(@PathVariable Long id,
                             @AuthenticationPrincipal CustomUserDetails currentUser) {
        postService.unlikePost(id, currentUser.getId());
        return "redirect:/posts/" + id;
    }

    @PostMapping("/{id}/share")
    public String sharePost(@PathVariable Long id) {
        postService.share(id);
        return "redirect:/posts/" + id;
    }

    // aâ€â‚¬aâ€â‚¬ Liste des articles publiÃƒ©s aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬

    @GetMapping
    public String listPosts(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false)    String keyword,
            Model model) {

        Page<Post> posts = (keyword != null && !keyword.isBlank())
                ? postService.searchPublished(keyword, page, size)
                : postService.findAllPublished(page, size);

        model.addAttribute("posts",       posts.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages",  posts.getTotalPages());
        model.addAttribute("totalItems",  posts.getTotalElements());
        model.addAttribute("keyword",     keyword);
        return "posts/list";
    }

    // aâ€â‚¬aâ€â‚¬ DÃƒ©tail d'un article aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬

    @GetMapping("/{id}")
    public String viewPost(@PathVariable Long id,
                           @AuthenticationPrincipal CustomUserDetails currentUser,
                           HttpServletRequest request,
                           Model model) {
        Post post = postService.findByIdWithDetails(id);
        Long viewerId = currentUser != null ? currentUser.getId() : null;
        String sessionId = request.getSession(true).getId();
        postService.recordView(post, viewerId, sessionId);
        boolean likedByCurrentUser = postService.hasUserLiked(post.getId(), viewerId);
        model.addAttribute("post", post);
        model.addAttribute("likedByCurrentUser", likedByCurrentUser);
        return "posts/view";
    }

    // aâ€â‚¬aâ€â‚¬ Formulaire de crÃƒ©ation aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬

    @GetMapping("/new")
    public String newPostForm(Model model) {
        model.addAttribute("post",     new Post());
        model.addAttribute("statuses", PostStatus.values());
        return "posts/form";
    }

    // aâ€â‚¬aâ€â‚¬ CrÃƒ©ation aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬

    @PostMapping("/new")
    public String createPost(
            @Valid @ModelAttribute("post") Post post,
            @RequestParam(value = "coverImageFile", required = false) MultipartFile coverImageFile,
            BindingResult result,
            @AuthenticationPrincipal UserDetails currentUser,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("statuses", PostStatus.values());
            return "posts/form";
        }

        try {
            String uploadedImagePath = fileStorageService.storePostImage(coverImageFile);
            if (uploadedImagePath != null) {
                post.setCoverImage(uploadedImagePath);
            }

            Long authorId = resolveUserId(currentUser);
            Post created  = postService.create(post, authorId);
            redirectAttributes.addFlashAttribute("successMessage", "Article cree avec succes !");
            return "redirect:/posts/" + created.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage",
                    (e.getMessage() != null && !e.getMessage().isBlank())
                            ? e.getMessage()
                            : "Erreur lors de la creation de l'article.");
            model.addAttribute("statuses", PostStatus.values());
            return "posts/form";
        }
    }

    // aâ€â‚¬aâ€â‚¬ Formulaire d'Ãƒ©dition aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬

    @GetMapping("/{id}/edit")
    public String editPostForm(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails currentUser,
                               Model model) {
        Post post     = postService.findById(id);
        Long authorId = resolveUserId(currentUser);

        // Seul l'auteur, un admin ou un moderateur peut editer
        boolean isPrivileged = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MODERATOR"));
        if (!post.getAuthor().getId().equals(authorId) && !isPrivileged) {
            return "redirect:/error/403";
        }

        model.addAttribute("post",     post);
        model.addAttribute("statuses", PostStatus.values());
        return "posts/form";
    }

    // aâ€â‚¬aâ€â‚¬ Mise Ãƒ  jour aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬

    @PostMapping("/{id}/edit")
    public String updatePost(
            @PathVariable Long id,
            @Valid @ModelAttribute("post") Post post,
            @RequestParam(value = "coverImageFile", required = false) MultipartFile coverImageFile,
            BindingResult result,
            @AuthenticationPrincipal UserDetails currentUser,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("statuses", PostStatus.values());
            return "posts/form";
        }

        try {
            String uploadedImagePath = fileStorageService.storePostImage(coverImageFile);
            if (uploadedImagePath != null) {
                post.setCoverImage(uploadedImagePath);
            }

            Long authorId = resolveUserId(currentUser);
            postService.update(id, post, authorId);
            redirectAttributes.addFlashAttribute("successMessage", "Article mis a jour avec succes !");
            return "redirect:/posts/" + id;
        } catch (SecurityException e) {
            return "redirect:/error/403";
        } catch (Exception e) {
            model.addAttribute("errorMessage",
                    (e.getMessage() != null && !e.getMessage().isBlank())
                            ? e.getMessage()
                            : "Erreur lors de la mise a jour.");
            model.addAttribute("statuses", PostStatus.values());
            return "posts/form";
        }
    }

    // aâ€â‚¬aâ€â‚¬ Publication / DÃƒ©publication aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬

    @PostMapping("/{id}/publish")
    public String publishPost(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails currentUser,
                              RedirectAttributes redirectAttributes) {
        try {
            postService.publish(id, resolveUserId(currentUser));
            redirectAttributes.addFlashAttribute("successMessage", "Article publie !");
        } catch (SecurityException e) {
            return "redirect:/error/403";
        }
        return "redirect:/posts/" + id;
    }

    @PostMapping("/{id}/unpublish")
    public String unpublishPost(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails currentUser,
                                RedirectAttributes redirectAttributes) {
        try {
            postService.unpublish(id, resolveUserId(currentUser));
            redirectAttributes.addFlashAttribute("successMessage", "Article depublie.");
        } catch (SecurityException e) {
            return "redirect:/error/403";
        }
        return "redirect:/posts/" + id;
    }

    // aâ€â‚¬aâ€â‚¬ Suppression aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬

    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails currentUser,
                             RedirectAttributes redirectAttributes) {
        try {
            postService.delete(id, resolveUserId(currentUser));
            redirectAttributes.addFlashAttribute("successMessage", "Article supprime.");
            return "redirect:/posts";
        } catch (SecurityException e) {
            return "redirect:/error/403";
        }
    }

    // aâ€â‚¬aâ€â‚¬ Mes articles (auteur connectÃƒ©) aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬aâ€â‚¬

    @GetMapping("/my-posts")
    public String myPosts(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails currentUser,
            Model model) {

        Long authorId  = resolveUserId(currentUser);
        Page<Post> posts = postService.findByAuthor(authorId, page, size);

        model.addAttribute("posts",       posts.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages",  posts.getTotalPages());
        return "posts/my-posts";
    }

    // aâ€â‚¬aâ€â‚¬ Helper : rÃƒ©soudre l'id de l'utilisateur connectÃƒ© aâ€â‚¬aâ€â‚¬

    private Long resolveUserId(UserDetails currentUser) {
        // L'username est unique aâ€ â€™ on le retrouve via UserService si besoin
        // Ici on utilise un cast ou un UserDetails personnalisÃƒ©
        if (currentUser instanceof CustomUserDetails customUser) {
            return customUser.getId();
        }
        return userService.findByUsername(currentUser.getUsername()).getId();
    }
}




