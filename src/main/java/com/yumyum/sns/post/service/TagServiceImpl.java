package com.yumyum.sns.post.service;

import com.yumyum.sns.post.dto.TagDto;
import com.yumyum.sns.post.entity.Post;
import com.yumyum.sns.post.entity.PostTag;
import com.yumyum.sns.post.entity.Tag;
import com.yumyum.sns.post.repository.PostTagRepository;
import com.yumyum.sns.post.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class TagServiceImpl implements TagService{

    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;

    @Override
    public PostTag createTag(TagDto tagDto, Post post) {

        Optional<Tag> checkTag = tagRepository.findByContent(tagDto.getContent());
        if(!checkTag.isPresent()){
            Tag hashtag = new Tag(tagDto.getContent());
            Tag savedTag = tagRepository.save(hashtag);
            PostTag postTag = new PostTag(post, savedTag);
            return postTagRepository.save(postTag);
        }else{
            Tag hashtag = checkTag.get();
            PostTag postTag = new PostTag(post, hashtag);
            return postTagRepository.save(postTag);
        }
    }

    @Override
    public List<Tag> getTags(Long postId) {
        return null;
    }

    //게시글 태그 삭제
    @Override
    public void deleteTagByPostId(Long postId) {
        List<PostTag> tags = postTagRepository.findByPostId(postId);
        if(!tags.isEmpty()){
            for(PostTag postTag : tags){
                postTagRepository.delete(postTag);
            }
        }
    }

    @Override
    public PostTag updateTag(TagDto tagDto, Post post) {
        //기존 태그 삭제
        deleteTagByPostId(post.getId());
        //태그 create
        PostTag tag = createTag(tagDto, post);
        return tag;
    }


}
