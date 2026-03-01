package com.yumyum.sns.friend.service;

import com.yumyum.sns.error.exception.FriendNotFoundException;
import com.yumyum.sns.friend.dto.FriendResDto;
import com.yumyum.sns.friend.entity.Friend;
import com.yumyum.sns.friend.repository.FriendRepository;
import com.yumyum.sns.member.entity.Member;
import com.yumyum.sns.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService{

    private final MemberService memberService;
    private final FriendRepository friendRepository;

    //친구 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<FriendResDto> getFriendsByMemberId(Long myId) {
        //id 유효성 체크
        Member member = memberService.getMemberById(myId);
        return friendRepository.findFriendsBymemberId(myId);
    }

    //친구 생성
    @Override
    @Transactional
    public Long createFriend(Long senderId,Long receiverId) {
        Member memberA = memberService.getMemberById(senderId);
        Member memberB = memberService.getMemberById(receiverId);

        Friend friend = new Friend(memberA, memberB);
        return friendRepository.save(friend).getId();
    }

    // friendId를 이용한 친구 삭제
    @Override
    @Transactional
    public void removeFriendByFriendId(Long friendId) {
        Friend friend = getFriendById(friendId);
        friendRepository.delete(friend);
    }

    // 친구관계인 회원Id를 이용한 친구삭제
    @Override
    @Transactional
    public void removeFriendByMemberId(Long senderId, Long receiverId) {
        friendRepository.deleteFriend(senderId,receiverId);
    }





    // 친구 단건 조회
    @Override
    @Transactional(readOnly = true)
    public Friend getFriendById(Long friendId) {
        return friendRepository.findById(friendId).orElseThrow(()-> new FriendNotFoundException(friendId));
    }


}
