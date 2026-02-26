package com.yumyum.sns.chat.repository;

import com.yumyum.sns.chat.dto.ChatRoomListResponse;
import com.yumyum.sns.chat.dto.DirectChatMemRequest;
import com.yumyum.sns.chat.entity.ChatRoom;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepositoryCustom {
    /**
     *
     * 1:1 채팅방이 존재하는지 아닌지 확인
     * @param directChatMemRequest 1:1 채팅 회원들
     * @return 채팅방
     */
    Optional<ChatRoom> findChatRoom(DirectChatMemRequest directChatMemRequest);

    /**
     * 내가 속한 채팅방 목록 조회 (상대방 정보 + 마지막 메시지 포함)
     * @param memberId 내 memberId
     * @return 채팅방 목록
     */
    List<ChatRoomListResponse> findMyChatRooms(Long memberId);
}
