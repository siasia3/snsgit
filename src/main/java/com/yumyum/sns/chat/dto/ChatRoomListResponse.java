package com.yumyum.sns.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatRoomListResponse {
    private Long chatroomId;
    private Long opponentId;
    private String opponentNickname;
    private String opponentProfileImage;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
}
