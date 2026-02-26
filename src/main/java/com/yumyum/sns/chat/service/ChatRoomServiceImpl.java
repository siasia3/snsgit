package com.yumyum.sns.chat.service;

import com.yumyum.sns.chat.dto.ChatRoomListResponse;
import com.yumyum.sns.chat.dto.DirectChatMemRequest;
import com.yumyum.sns.chat.dto.ChatRoomResponse;
import com.yumyum.sns.chat.entity.ChatRoom;
import com.yumyum.sns.chat.entity.ChatRoomType;
import com.yumyum.sns.chat.repository.ChatRoomRepository;
import com.yumyum.sns.error.exception.ChatRoomNotFoundException;
import com.yumyum.sns.member.entity.Member;
import com.yumyum.sns.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService{

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberService chatMemService;
    private final MemberService memberService;

    //1:1 채팅방 조회 후 없으면 생성
    @Override
    public ChatRoomResponse getOrCreateChatRoom(DirectChatMemRequest directChatMemRequest) {

        //유효성 검사
        List<Long> chatMemIds = directChatMemRequest.getChatRoomMems();
        List<Member> chatMems = chatMemIds.stream().map(memberService::getMemberById).collect(Collectors.toList());
        //조회 후 없으면 생성
        ChatRoom chatRoom = chatRoomRepository.findChatRoom(directChatMemRequest)
                .orElseGet(() -> {
                    ChatRoom savedChatRoom = createChatRoom(ChatRoomType.ONETOONE);
                    chatMemService.createChatRoomMem(chatMems, savedChatRoom);
                    return savedChatRoom;
                });
        return new ChatRoomResponse(chatRoom.getId(),chatRoom.getCreatedAt(),directChatMemRequest.getChatRoomMems());
    }

    //채팅방 생성
    @Override
    public ChatRoom createChatRoom(ChatRoomType chatRoomType) {
        ChatRoom chatRoom = new ChatRoom(chatRoomType);
        ChatRoom savedChatroom = chatRoomRepository.save(chatRoom);
        return  savedChatroom;
    }

    @Override
    public ChatRoom checkChatRoom(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(chatRoomId));
    }

    @Override
    public List<ChatRoomListResponse> getMyChatRooms(Long memberId) {
        return chatRoomRepository.findMyChatRooms(memberId);
    }

}
