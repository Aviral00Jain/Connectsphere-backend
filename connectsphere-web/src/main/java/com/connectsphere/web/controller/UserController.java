package com.connectsphere.web.controller;

import com.connectsphere.web.service.GatewayClientService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final GatewayClientService gatewayClientService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("posts", gatewayClientService.getPublicPosts());
        model.addAttribute("hashtags", gatewayClientService.getTrendingHashtags());
        return "user/home";
    }

    @GetMapping("/register")
    public String register() {
        return "user/register";
    }

    @GetMapping("/login")
    public String login() {
        return "user/login";
    }

    @GetMapping("/profile")
    public String viewProfile(@RequestParam String username, Model model) {
        var profile = gatewayClientService.getUserProfile(username);
        model.addAttribute("profile", profile);
        Object userId = profile.get("id");
        if (userId instanceof Number number) {
            model.addAttribute("posts", gatewayClientService.getUserPosts(number.longValue()));
        }
        return "user/profile";
    }

    @GetMapping("/followers")
    public String viewFollowers(@RequestParam String username, Model model) {
        model.addAttribute("username", username);
        return "user/followers";
    }

    @GetMapping("/following")
    public String viewFollowing(@RequestParam String username, Model model) {
        model.addAttribute("username", username);
        return "user/following";
    }

    @GetMapping("/suggestions")
    public String viewSuggestions() {
        return "user/suggestions";
    }

    @GetMapping("/notifications")
    public String viewNotifications(HttpSession session, Model model) {
        Object token = session.getAttribute("token");
        Object userId = session.getAttribute("userId");
        if (token instanceof String tokenValue && userId instanceof Number number) {
            model.addAttribute("notifications", gatewayClientService.getNotifications(number.longValue(), tokenValue));
        }
        return "user/notifications";
    }

    @GetMapping("/search/users")
    public String searchUsers(@RequestParam(defaultValue = "") String keyword, Model model) {
        model.addAttribute("keyword", keyword);
        model.addAttribute("users", gatewayClientService.searchUsers(keyword));
        return "user/search-users";
    }
}
