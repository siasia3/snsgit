package com.yumyum.sns.comment.repository;


import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yumyum.sns.comment.dto.*;
import com.yumyum.sns.comment.entity.Comment;
import com.yumyum.sns.comment.entity.QComment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.yumyum.sns.comment.entity.QComment.comment;
import static com.yumyum.sns.member.entity.QMember.*;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CommentCntDto> findCommentCntsByPostIds(List<Long> postIds) {
        List<CommentCntDto> totalCommentCnts = queryFactory
                .select(Projections.constructor(CommentCntDto.class,
                        comment.post.id,
                        comment.id.count().as("commentCount")))
                .from(comment)
                .where(comment.post.id.in(postIds))
                .groupBy(comment.post.id)
                .fetch();
        return totalCommentCnts;
    }

    @Override
    public List<CommentDto> findCommentsByPost(Pageable pageable,Long postId) {

        QComment parent = QComment.comment;
        QComment child = new QComment("child"); // 대댓글 별칭

        List<CommentDto> comments = queryFactory
                .select(new QCommentDto(
                        comment.id,
                        comment.content,
                        comment.createdAt,
                        member.id,
                        member.nickname,
                        member.profileImage,
                        JPAExpressions
                                .select(child.count()) // 대댓글 개수 조회
                                .from(child)
                                .where(child.parent.eq(parent)) // 부모 댓글과 연결된 대댓글만 조회
                ))
                .from(parent)
                .join(parent.member, member)
                .groupBy(parent.id)
                .where(parent.post.id.eq(postId).and(parent.parent.isNull()))
                .orderBy(comment.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();
        return comments;
    }

    @Override
    public Optional<Comment> findByIdWithMember(Long commentId) {
        return  Optional.ofNullable(queryFactory
                .selectFrom(comment)
                .join(comment.member, member)
                .where(comment.id.eq(commentId))
                .fetchOne());
    }

    @Override
    public List<ReplyDto> findRepliesByComment(Pageable pageable, Long parentId) {
        QComment parentComment = new QComment("parentComment");

        List<ReplyDto> replies = queryFactory
                .select(new QReplyDto(
                        comment.id,
                        member.nickname,
                        comment.content,
                        member.id,
                        member.profileImage,
                        comment.createdAt,
                        comment.parent.id
                ))
                .from(comment)
                .join(comment.member, member)
                .join(comment.parent, parentComment)
                .where(comment.parent.id.eq(parentId))
                .orderBy(comment.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return replies;
    }


}
