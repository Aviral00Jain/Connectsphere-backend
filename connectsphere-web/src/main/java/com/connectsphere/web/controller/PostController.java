package com.connectsphere.web.controller;

import com.connectsphere.web.service.GatewayClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PostController {

    private final GatewayClientService gatewayClientService;

    @GetMapping("/feed")
    public String newsFeed(Model model) {
        model.addAttribute("posts", gatewayClientService.getPublicPosts());
        return "post/feed";
    }

    @GetMapping("/post/view")
    public String viewPost(@RequestParam Long id, Model model) {
        model.addAttribute("postId", id);
        return "post/view";
    }

    @GetMapping("/post/create")
    public String createPost() {
        return "post/create";
    }

    @GetMapping("/post/edit")
    public String editPost(@RequestParam Long id, Model model) {
        model.addAttribute("postId", id);
        return "post/edit";
    }

    @GetMapping("/hashtags")
    public String viewHashtag(@RequestParam String tag, Model model) {
        model.addAttribute("tag", tag);
        model.addAttribute("posts", gatewayClientService.searchPosts(tag));
        return "post/hashtag";
    }

    @GetMapping("/search/posts")
    public String searchPosts(@RequestParam(defaultValue = "") String keyword, Model model) {
        model.addAttribute("keyword", keyword);
        model.addAttribute("posts", gatewayClientService.searchPosts(keyword));
        return "post/search-posts";
    }
}
