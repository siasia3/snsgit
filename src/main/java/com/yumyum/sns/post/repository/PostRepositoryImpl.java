package com.yumyum.sns.post.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yumyum.sns.post.dto.*;
import com.yumyum.sns.post.entity.QLikes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


import static com.yumyum.sns.attachment.entity.QAttachment.attachment;
import static com.yumyum.sns.comment.entity.QComment.*;
import static com.yumyum.sns.member.entity.QMember.member;
import static com.yumyum.sns.post.entity.QLikes.likes;
import static com.yumyum.sns.post.entity.QPost.post;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    //페이징된 게시글 조회
    @Override
    public List<PostResponseDTO> findPagingPosts(PostCursorRequest cursor,Long memberId) {

        QLikes likesSub = new QLikes("likesSub");

        BooleanExpression cursorCondition = null;
        LocalDateTime cursorCreatedAt = cursor.getCursorCreatedAt();
        Long cursorPostId = cursor.getCursorPostId();

        if (cursorCreatedAt != null && cursorPostId != null) {
            cursorCondition = post.createdAt.lt(cursorCreatedAt)
                    .or(
                            post.createdAt.eq(cursorCreatedAt)
                                    .and(post.id.lt(cursorPostId))
                    );
        }

        List<PostResponseDTO> postList = queryFactory
                .select(Projections.constructor(PostResponseDTO.class,
                        post.id,
                        member.id,
                        member.nickname,
                        member.profileImage,
                        attachment.id,
                        post.content,
                        post.createdAt,
                        JPAExpressions
                                .select(likes.id.count())
                                .from(likes)
                                .where(likes.post.id.eq(post.id)),
                        JPAExpressions
                                .select(comment.id.count())
                                .from(comment)
                                .where(comment.post.id.eq(post.id)),
                        JPAExpressions
                                .select(likesSub.id)
                                .from(likesSub)
                                .where(
                                        likesSub.post.id.eq(post.id),
                                        likesSub.member.id.eq(memberId)
                                )
                                .limit(1)
                ))
                .from(post)
                .join(post.member, member)
                .join(post.attachment,attachment)
                .where(cursorCondition)
                .orderBy(post.createdAt.desc())
                .limit(cursor.getSize()+1)
                .fetch();
        return postList;
    }

    //페이징된 게시글 조회 (WHERE IN 배치 조회 버전)
    @Override
    public List<PostResponseDTO> findPagingPostsWithIn(PostCursorRequest cursor, Long memberId) {

        BooleanExpression cursorCondition = null;
        LocalDateTime cursorCreatedAt = cursor.getCursorCreatedAt();
        Long cursorPostId = cursor.getCursorPostId();

        if (cursorCreatedAt != null && cursorPostId != null) {
            cursorCondition = post.createdAt.lt(cursorCreatedAt)
                    .or(
                            post.createdAt.eq(cursorCreatedAt)
                                    .and(post.id.lt(cursorPostId))
                    );
        }

        // Step 1: 기본 게시글 목록 조회 (집계 없이)
        List<Tuple> rows = queryFactory
                .select(post.id, member.id, member.nickname, member.profileImage, attachment.id, post.content, post.createdAt)
                .from(post)
                .join(post.member, member)
                .join(post.attachment, attachment)
                .where(cursorCondition)
                .orderBy(post.createdAt.desc())
                .limit(cursor.getSize() + 1)
                .fetch();

        if (rows.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = rows.stream()
                .map(t -> t.get(post.id))
                .collect(Collectors.toList());

        // Step 2: 좋아요 수 IN 배치 조회
        Map<Long, Long> likeCountMap = queryFactory
                .select(likes.post.id, likes.id.count())
                .from(likes)
                .where(likes.post.id.in(postIds))
                .groupBy(likes.post.id)
                .fetch()
                .stream()
                .collect(Collectors.toMap(t -> t.get(likes.post.id), t -> t.get(likes.id.count())));

        // Step 3: 댓글 수 IN 배치 조회
        Map<Long, Long> commentCountMap = queryFactory
                .select(comment.post.id, comment.id.count())
                .from(comment)
                .where(comment.post.id.in(postIds))
                .groupBy(comment.post.id)
                .fetch()
                .stream()
                .collect(Collectors.toMap(t -> t.get(comment.post.id), t -> t.get(comment.id.count())));

        // Step 4: 현재 회원의 좋아요 ID IN 배치 조회
        Map<Long, Long> likeIdMap = queryFactory
                .select(likes.post.id, likes.id)
                .from(likes)
                .where(
                        likes.post.id.in(postIds),
                        likes.member.id.eq(memberId)
                )
                .fetch()
                .stream()
                .collect(Collectors.toMap(t -> t.get(likes.post.id), t -> t.get(likes.id)));

        // Step 5: 결과 조립
        return rows.stream()
                .map(t -> {
                    Long postId = t.get(post.id);
                    return new PostResponseDTO(
                            postId,
                            t.get(member.id),
                            t.get(member.nickname),
                            t.get(member.profileImage),
                            t.get(attachment.id),
                            t.get(post.content),
                            t.get(post.createdAt),
                            likeCountMap.getOrDefault(postId, 0L),
                            commentCountMap.getOrDefault(postId, 0L),
                            likeIdMap.get(postId)
                    );
                })
                .collect(Collectors.toList());
    }

    //게시글 상세 조회
    @Override
    public PostDetailDto findPostDetail(Long postId, Long memberId) {
        PostDetailDto postDetailDto = queryFactory
                .select(new QPostDetailDto(
                        member.id,
                        member.profileImage,
                        member.nickname,
                        post.id,
                        post.content,
                        post.createdAt,
                        attachment.id,
                        likes.id.countDistinct().as("likeCount"),
                        ExpressionUtils.as(
                                JPAExpressions.select(likes.id)
                                        .from(likes)
                                        .where(likes.member.id.eq(memberId)
                                                .and(likes.post.id.eq(postId))),
                                "likedByMember"
                        )
                ))
                .from(post)
                .join(post.member, member)
                .join(post.attachment, attachment)
                .leftJoin(post.likesList, likes)
                .where(post.id.eq(postId))
                .fetchOne();
        return postDetailDto;
    }

    //회원이 작성한 게시글 조회
    @Override
    public List<MemberPostDto> findMemberPosts(Pageable pageable, Long memberId) {
        List<MemberPostDto> memberPosts = queryFactory
                .select(new QMemberPostDto(
                        post.id,
                        post.thumbnailPath,
                        likes.id.countDistinct().as("likeCount"),
                        comment.id.countDistinct().as("commentCount")
                ))
                .from(post)
                .leftJoin(post.likesList, likes)
                .leftJoin(post.commentList, comment)
                .where(post.member.id.eq(memberId))
                .groupBy(post.id)
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();
        return memberPosts;
    }

    //회원이 좋아요를 누른 게시글 조회
    @Override
    public List<LikedPostDto> findLikedPosts(int pageSize, LocalDateTime cursorCreatedAt, Long memberId) {

        List<LikedPostDto> likedPosts = queryFactory
                .select(new QLikedPostDto(
                        post.id,
                        post.thumbnailPath,
                        likes.id.countDistinct().as("likeCount"),
                        comment.id.countDistinct().as("commentCount"),
                        likes.createdAt
                ))
                .from(post)
                .leftJoin(post.likesList, likes)
                .leftJoin(post.commentList, comment)
                .where(
                    likes.member.id.eq(memberId)
                    .and(cursorCreatedAt != null ? likes.createdAt.lt(cursorCreatedAt) : null)
                )
                .groupBy(post.id, likes.id)
                .orderBy(likes.createdAt.desc())
                .limit(pageSize)
                .fetch();

        return likedPosts;
    }


    @Override
    public Long countTotalPosts() {
        Long totalPostsCount = Optional.ofNullable(queryFactory
                    .select(post.count())
                    .from(post)
                    .fetchOne())
                    .orElse(0L);
        return totalPostsCount;
    }
}

/*List<PostResponseDTO> postList = queryFactory
                .selectFrom(post
                )
                .join(post.member, member)
                .leftJoin(post.commentList, comment)
                .leftJoin(comment.replyList, reply)
                .leftJoin(post.attachment, attachment)
                .join(attachment.attachments, attachmentDetail)
                .leftJoin(post.likesList, likes)
                .leftJoin(post.PostTag, postTag)
                .join(postTag.tag, tag)
                .groupBy(post.id,attachment.id,tag.id,attachmentDetail.id)
                .offset(0)
                .limit(5)
                .transform(groupBy(post.id).list(
                                Projections.constructor(PostResponseDTO.class,
                                        post.id,
                                        member.id,
                                        post.content,
                                        post.createdAt,
                                        likes.id.countDistinct(),
                                        comment.id.countDistinct(),
                                        reply.id.countDistinct(),
                                        set(Projections.constructor(AttachDto.class,
                                                attachment.id,
                                                attachmentDetail.id,
                                                attachmentDetail.path)),
                                        set(Projections.constructor(TagDto.class,
                                                tag.id,
                                                tag.content))
                )));*/
