package com.yumyum.sns.friend.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yumyum.sns.friend.dto.QReceivedFriendRequestDto;
import com.yumyum.sns.friend.dto.QSentFriendRequestDto;
import com.yumyum.sns.friend.dto.ReceivedFriendRequestDto;
import com.yumyum.sns.friend.dto.SentFriendRequestDto;
import com.yumyum.sns.friend.entity.FriendRequest;
import com.yumyum.sns.friend.entity.FriendRequestStatus;
import com.yumyum.sns.friend.entity.QFriendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.yumyum.sns.friend.entity.QFriendRequest.*;


@Repository
@RequiredArgsConstructor
public class FriendRequestRepositoryImpl implements FriendRequestRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    //받은 친구요청들 조회
    @Override
    public List<ReceivedFriendRequestDto> findFriendRequestsByReceiver(Long receiverId) {
        List<ReceivedFriendRequestDto> receivedFriendReq = queryFactory.select(
                        new QReceivedFriendRequestDto(
                                friendRequest.id,
                                friendRequest.requester.profileImage,
                                friendRequest.requester.id,
                                friendRequest.requester.nickname
                        )
                )
                .from(friendRequest)
                .innerJoin(friendRequest.requester)
                .innerJoin(friendRequest.receiver)
                .where(friendRequest.receiver.id.eq(receiverId),
                        isRequested())
                .fetch();
        return receivedFriendReq;
    }

    //보낸 친구요청들 조회
    @Override
    public List<SentFriendRequestDto> findFriendRequestsBySender(Long senderId) {
        List<SentFriendRequestDto> sentFriendReq = queryFactory.select(
                        new QSentFriendRequestDto(
                                friendRequest.id,
                                friendRequest.receiver.profileImage,
                                friendRequest.receiver.id,
                                friendRequest.receiver.nickname
                        )
                )
                .from(friendRequest)
                .innerJoin(friendRequest.requester)
                .innerJoin(friendRequest.receiver)
                .where(friendRequest.requester.id.eq(senderId),
                        isRequested())
                .fetch();
        return sentFriendReq;
    }

    //특정회원과의 친구요청 확인
    @Override
    public Optional<FriendRequest> findFriendRequestIdByMemberIds(Long myId, Long memberId) {
        FriendRequest checkedFriendReq = queryFactory
                .selectFrom(friendRequest)
                .innerJoin(friendRequest.receiver)
                .innerJoin(friendRequest.requester)
                .where(
                    friendRequestCondition(myId, memberId)
                )
                .fetchOne();

        return Optional.ofNullable(checkedFriendReq);
    }

    //본인 과 친구회원id를 이용해서 친구요청 삭제
    @Override
    public void deleteFriendRequestOnFriendRemoval(Long myId, Long memberId) {
        queryFactory
                .delete(friendRequest)
                .where(
                    friendRequestCondition(myId, memberId)
                )
                .execute();
    }


    private BooleanExpression friendRequestCondition(Long memberAId, Long memberBId) {
        return (friendRequest.requester.id.eq(memberAId)
                .and(friendRequest.receiver.id.eq(memberBId)))
                .or(friendRequest.requester.id.eq(memberBId)
                        .and(friendRequest.receiver.id.eq(memberAId)));
    }

    BooleanExpression isRequested() {
        return friendRequest.state.eq(FriendRequestStatus.REQUESTED);
    }

}
