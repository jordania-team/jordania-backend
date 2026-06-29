package com.jordania.api.post;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.jordania.api.post.dto.CreatePostRequest;
import com.jordania.api.post.dto.PostResponse;
import com.jordania.api.post.dto.UpdatePostRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public PostResponse createPost(@RequestBody CreatePostRequest request) {
        return postService.createPost(request);
    }

    @GetMapping
    public List<PostResponse> getPosts() {
        return postService.getPosts();
    }

    @GetMapping("/tutor/{tutorId}")
    public List<PostResponse> getPostsByTutor(@PathVariable UUID tutorId) {
        return postService.getPostsByTutor(tutorId);
    }

    @GetMapping("/pet/{petId}")
    public List<PostResponse> getPostsByPet(@PathVariable UUID petId) {
        return postService.getPostsByPet(petId);
    }

    @PutMapping("/{postId}")
    public PostResponse updatePost(
            @PathVariable UUID postId,
            @RequestBody UpdatePostRequest request
    ) {
        return postService.updatePost(postId, request);
    }
}