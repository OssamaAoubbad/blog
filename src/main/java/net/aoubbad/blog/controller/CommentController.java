package net.aoubbad.blog.controller;


import net.aoubbad.blog.service.CommentService;
import net.aoubbad.blog.service.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // â”€â”€ Ajouter un commentaire sur un post â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @PostMapping("/post/{postId}")
    public String addComment(
            @PathVariable Long postId,
            @RequestParam String content,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes) {

        try {
            commentService.addComment(postId, currentUser.getId(), content);
            redirectAttributes.addFlashAttribute("successMessage", "Commentaire ajoute !");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/posts/" + postId;
    }

    // â”€â”€ Repondre a un commentaire â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @PostMapping("/post/{postId}/reply/{parentId}")
    public String addReply(
            @PathVariable Long postId,
            @PathVariable Long parentId,
            @RequestParam String content,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes) {

        try {
            commentService.addReply(postId, parentId, currentUser.getId(), content);
            redirectAttributes.addFlashAttribute("successMessage", "Reponse ajoutee !");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/posts/" + postId;
    }

    // â”€â”€ Modifier un commentaire â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @PostMapping("/{id}/edit")
    public String updateComment(
            @PathVariable Long id,
            @RequestParam String content,
            @RequestParam Long postId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes) {

        try {
            commentService.update(id, content, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Commentaire modifie.");
        } catch (SecurityException e) {
            return "redirect:/error/403";
        }
        return "redirect:/posts/" + postId;
    }

    // â”€â”€ Supprimer un commentaire â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @PostMapping("/{id}/delete")
    public String deleteComment(
            @PathVariable Long id,
            @RequestParam Long postId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes) {

        try {
            commentService.delete(id, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Commentaire supprime.");
        } catch (SecurityException e) {
            return "redirect:/error/403";
        }
        return "redirect:/posts/" + postId;
    }
}

