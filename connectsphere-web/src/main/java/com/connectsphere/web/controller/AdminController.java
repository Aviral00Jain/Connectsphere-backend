package com.connectsphere.web.controller;

import com.connectsphere.web.service.GatewayClientService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final GatewayClientService gatewayClientService;

    @GetMapping("/admin")
    public String adminDashboard(HttpSession session, Model model) {
        Object token = session.getAttribute("token");
        if (token instanceof String tokenValue) {
            model.addAttribute("reports", gatewayClientService.getReports(tokenValue));
            model.addAttribute("payments", gatewayClientService.getPayments(tokenValue));
        }
        model.addAttribute("hashtags", gatewayClientService.getTrendingHashtags());
        return "admin/dashboard";
    }

    @GetMapping("/admin/users")
    public String manageUsers() {
        return "admin/users";
    }

    @GetMapping("/admin/posts")
    public String manageAllPosts() {
        return "admin/posts";
    }

    @GetMapping("/admin/comments")
    public String manageAllComments() {
        return "admin/comments";
    }

    @GetMapping("/admin/reports")
    public String reviewReports() {
        return "admin/reports";
    }
}
