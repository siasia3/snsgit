package com.yumyum.sns.post.repository;

import com.yumyum.sns.post.dto.PostCursorRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@SpringBootTest
@Transactional
class PostRepositoryImplTest {


    @Autowired
    private PostRepository postRepository;

    @Test
    public void testFindPosts(){

        PostCursorRequest testCursor =
                new PostCursorRequest(
                        1L,
                        LocalDateTime.of(2025, 12, 31, 20, 6, 33),
                        10
                );
        postRepository.findPagingPosts(testCursor,1L);
    }



}