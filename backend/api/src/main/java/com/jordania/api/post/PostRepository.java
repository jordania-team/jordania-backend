package com.jordania.api.post;
//acesso ao banco para posts -> busca e salva posts
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    List<Post> findByTutorIdOrderByPostedAtDesc(UUID tutorId);
}

/*
busca todos os posts de um tutor, do mais novo para o mais antigo
*/