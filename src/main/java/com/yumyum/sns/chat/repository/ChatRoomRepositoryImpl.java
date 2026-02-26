package com.yumyum.sns.chat.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yumyum.sns.chat.dto.ChatRoomListResponse;
import com.yumyum.sns.chat.dto.DirectChatMemRequest;
import com.yumyum.sns.chat.entity.Chat;
import com.yumyum.sns.chat.entity.ChatRoom;
import com.yumyum.sns.chat.entity.ChatRoomMember;
import com.yumyum.sns.chat.entity.ChatRoomType;
import com.yumyum.sns.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


import static com.yumyum.sns.chat.entity.QChat.*;
import static com.yumyum.sns.chat.entity.QChatRoom.*;
import static com.yumyum.sns.chat.entity.QChatRoomMember.*;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryImpl implements ChatRoomRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    //1:1 채팅방 확인
    @Override
    public Optional<ChatRoom> findChatRoom(DirectChatMemRequest directChatMemRequest) {
        ChatRoom result = queryFactory
                .select(chatRoom)
                .from(chatRoom)
                .where(
                        chatRoom.chatroomType.eq(ChatRoomType.ONETOONE),
                        chatRoom.id.in(
                                queryFactory
                                        .select(chatRoomMember.chatroom.id)
                                        .from(chatRoomMember)
                                        .where(chatRoomMember.member.id.in(directChatMemRequest.getReceiverId(), directChatMemRequest.getSenderId()))
                                        .groupBy(chatRoomMember.chatroom.id)
                                        .having(chatRoomMember.member.id.countDistinct().eq(2L))
                        ))
                .fetchOne();
        return Optional.ofNullable(result);
    }

    //내 채팅방 목록 조회 (상대방 정보 + 마지막 메시지 포함)
    @Override
    public List<ChatRoomListResponse> findMyChatRooms(Long memberId) {
        // 내가 속한 채팅방 ID 목록
        List<Long> myChatroomIds = queryFactory
                .select(chatRoomMember.chatroom.id)
                .from(chatRoomMember)
                .where(chatRoomMember.member.id.eq(memberId))
                .fetch();

        // 채팅방별 상대방 + 마지막 메시지 조회
        List<ChatRoomMember> opponents = queryFactory
                .selectFrom(chatRoomMember)
                .join(chatRoomMember.member).fetchJoin()
                .join(chatRoomMember.chatroom).fetchJoin()
                .where(
                        chatRoomMember.chatroom.id.in(myChatroomIds),
                        chatRoomMember.member.id.ne(memberId)
                )
                .fetch();

        return opponents.stream().map(opp -> {
            Member opponent = opp.getMember();
            ChatRoom room = opp.getChatroom();

            // 마지막 메시지 조회
            Chat lastChat = queryFactory
                    .selectFrom(chat)
                    .where(chat.chatroom.id.eq(room.getId()))
                    .orderBy(chat.createdAt.desc())
                    .limit(1)
                    .fetchOne();

            return new ChatRoomListResponse(
                    room.getId(),
                    opponent.getId(),
                    opponent.getNickname(),
                    opponent.getProfileImage(),
                    lastChat != null ? lastChat.getContent() : null,
                    lastChat != null ? lastChat.getCreatedAt() : null
            );
        }).sorted((a, b) -> {
            if (a.getLastMessageAt() == null) return 1;
            if (b.getLastMessageAt() == null) return -1;
            return b.getLastMessageAt().compareTo(a.getLastMessageAt());
        }).collect(Collectors.toList());
    }
}
