package com.yumyum.sns.post.service;

import com.yumyum.sns.post.dto.PostRequestDto;
import com.yumyum.sns.post.dto.TagDto;
import com.yumyum.sns.post.entity.Post;
import com.yumyum.sns.post.entity.PostTag;
import com.yumyum.sns.post.entity.Tag;

import java.util.List;

public interface TagService {

    /**
     * 해시태그와 게시글해시태그 중간테이블 생성
     * @param tagDto 생성할 태그 정보
     * @param post 게시글
     * @return 게시글과 태그 중간 엔티티
     */
    PostTag createTag(TagDto tagDto, Post post);

    /**
     * 게시글 해시태그 조회
     * @param postId 게시글 PK
     * @return 게시글 해시태그들
     */
    List<Tag> getTags(Long postId);

    /**
     * 게시글의 해시태그 삭제
     * @param postId 게시글 PK
     */
    void deleteTagByPostId(Long postId);

    /**
     * 게시글 태그 수정
     * @param tagDto 새로 생성할 태그 정보
     * @param post 태그를 수정할 게시글
     * @return 게시글과 태그 중간 엔티티
     */
    PostTag updateTag(TagDto tagDto,Post post);
}
