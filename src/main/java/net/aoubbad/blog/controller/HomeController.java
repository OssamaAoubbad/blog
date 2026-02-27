package net.aoubbad.blog.controller;


import net.aoubbad.blog.service.PostService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final PostService postService;

    public HomeController(PostService postService) {
        this.postService = postService;
    }

    // ── Page d'accueil ─────────────────────────────────────

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("recentPosts", postService.findAllPublished(0, 6).getContent());
        model.addAttribute("topPosts",    postService.findTopPosts(3));
        return "home";
    }

    // ── Pages d'erreur ─────────────────────────────────────

    @GetMapping("/error/403")
    public String forbidden() {
        return "error/403";
    }

    @GetMapping("/error/404")
    public String notFound() {
        return "error/404";
    }
}