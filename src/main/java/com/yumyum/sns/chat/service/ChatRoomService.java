package com.yumyum.sns.chat.service;

import com.yumyum.sns.chat.dto.ChatRoomDto;
import com.yumyum.sns.chat.dto.ChatRoomListResponse;
import com.yumyum.sns.chat.dto.DirectChatMemRequest;
import com.yumyum.sns.chat.dto.ChatRoomResponse;
import com.yumyum.sns.chat.entity.ChatRoom;
import com.yumyum.sns.chat.entity.ChatRoomType;

import java.util.List;

public interface ChatRoomService {

    /**
     * 1:1 채팅방 유무를 확인하고 없으면 생성
     * @param directChatMemRequest 채팅방 회원들 id
     * @return 1:1 채팅방 정보
     */
    ChatRoomResponse getOrCreateChatRoom(DirectChatMemRequest directChatMemRequest);

    /**
     * 채팅방 생성
     * @param chatRoomType 채팅방 유형
     * @return 생성된 채팅방
     */
    ChatRoom createChatRoom(ChatRoomType chatRoomType);

    /**
     * 존재하는 채팅방인지 확인
     * @param chatRoomId 채팅방 id
     * @return 조회된 채팅방
     */
    ChatRoom checkChatRoom(Long chatRoomId);

    /**
     * 내가 속한 채팅방 목록 조회
     * @param memberId 내 memberId
     * @return 채팅방 목록 (상대방 정보 + 마지막 메시지 포함)
     */
    List<ChatRoomListResponse> getMyChatRooms(Long memberId);

}
