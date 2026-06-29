package com.jordania.api.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.jordania.api.pet.Pet;
import com.jordania.api.pet.PetRepository;
import com.jordania.api.post.dto.CreatePostRequest;
import com.jordania.api.post.dto.PostResponse;
import com.jordania.api.post.dto.UpdatePostRequest;
import com.jordania.api.postpet.PostPet;
import com.jordania.api.postpet.PostPetRepository;
import com.jordania.api.tutor.Tutor;
import com.jordania.api.tutor.TutorRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final TutorRepository tutorRepository;
    private final PetRepository petRepository;
    private final PostPetRepository postPetRepository;

    public PostResponse createPost(CreatePostRequest request) {
        Tutor tutor = tutorRepository.findById(request.tutorId())
                .orElseThrow(() -> new RuntimeException("Tutor não encontrado"));

        List<Pet> pets = petRepository.findAllById(request.petIds());

        if (pets.size() != request.petIds().size()) {
            throw new RuntimeException("Um ou mais pets não foram encontrados");
        }

        Post post = Post.builder()
                .id(UUID.randomUUID())
                .tutor(tutor)
                .description(request.description())
                .build();

        Post savedPost = postRepository.save(post);

        List<PostPet> postPets = pets.stream()
                .map(pet -> PostPet.builder()
                        .id(UUID.randomUUID())
                        .post(savedPost)
                        .pet(pet)
                        .build())
                .toList();

        postPetRepository.saveAll(postPets);

        return toResponse(savedPost, postPets);
    }

    public List<PostResponse> getPosts() {
        return postRepository.findAll()
                .stream()
                .map(post -> {
                    List<PostPet> postPets = postPetRepository.findByPostId(post.getId());
                    return toResponse(post, postPets);
                })
                .toList();
    }

    public List<PostResponse> getPostsByTutor(UUID tutorId) {
        return postRepository.findByTutorIdOrderByPostedAtDesc(tutorId)
                .stream()
                .map(post -> {
                    List<PostPet> postPets = postPetRepository.findByPostId(post.getId());
                    return toResponse(post, postPets);
                })
                .toList();
    }

    public List<PostResponse> getPostsByPet(UUID petId) {
        return postPetRepository.findByPetId(petId)
                .stream()
                .map(PostPet::getPost)
                .distinct()
                .map(post -> {
                    List<PostPet> postPets = postPetRepository.findByPostId(post.getId());
                    return toResponse(post, postPets);
                })
                .toList();
    }

    public PostResponse updatePost(UUID postId, UpdatePostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post não encontrado"));

        post.setDescription(request.description());

        Post savedPost = postRepository.save(post);

        List<PostPet> postPets = postPetRepository.findByPostId(savedPost.getId());

        return toResponse(savedPost, postPets);
    }

    private PostResponse toResponse(Post post, List<PostPet> postPets) {
        List<UUID> petIds = postPets.stream()
                .map(postPet -> postPet.getPet().getId())
                .toList();

        return new PostResponse(
                post.getId(),
                post.getTutor().getId(),
                post.getTutor().getUsername(),
                post.getDescription(),
                petIds,
                post.getPostedAt(),
                post.getCounter1(),
                post.getCounter2(),
                post.getCounter3(),
                post.getCounter4(),
                post.getCounter5()
        );
    }
}