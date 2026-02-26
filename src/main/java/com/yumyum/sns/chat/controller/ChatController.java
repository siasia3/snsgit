package com.yumyum.sns.chat.controller;

import com.yumyum.sns.chat.dto.ChatResponse;
import com.yumyum.sns.chat.dto.ChatRoomListResponse;
import com.yumyum.sns.chat.dto.ChatRoomResponse;
import com.yumyum.sns.chat.dto.DirectChatMemRequest;
import com.yumyum.sns.chat.service.ChatRoomService;
import com.yumyum.sns.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final ChatService chatService;

    //1:1 채팅방 조회
    @GetMapping("/chatroom/member")
    public ResponseEntity<ChatRoomResponse> getChatroomBychatMem(@Valid @ModelAttribute DirectChatMemRequest directChatMemRequest){

        if (directChatMemRequest.getSenderId().equals(directChatMemRequest.getReceiverId())) {
            throw new IllegalArgumentException("보내는 사람과 받는 사람 ID는 동일할 수 없습니다.");
        }
        directChatMemRequest.setChatRoomMems();
        ChatRoomResponse orCreateChatRoom = chatRoomService.getOrCreateChatRoom(directChatMemRequest);
        return ResponseEntity.ok(orCreateChatRoom);
    }

    @GetMapping("/chat/{chatRoomId}")
    public ResponseEntity<List<ChatResponse>> getChats(@RequestParam(required = false) Long lastChatId,
                                                       @PathVariable Long chatRoomId,
                                                       @RequestParam(defaultValue = "30") int size) {

        List<ChatResponse> chats = chatService.getChats(chatRoomId, lastChatId, size);
        return ResponseEntity.ok(chats);
    }

    //내 채팅방 목록 조회
    @GetMapping("/chatrooms/me")
    public ResponseEntity<List<ChatRoomListResponse>> getMyChatRooms(@RequestParam Long memberId) {
        List<ChatRoomListResponse> chatRooms = chatRoomService.getMyChatRooms(memberId);
        return ResponseEntity.ok(chatRooms);
    }

}
