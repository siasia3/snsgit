package com.yumyum.sns.friend.service;

import com.yumyum.sns.error.exception.FriendRequestNotFoundException;
import com.yumyum.sns.friend.dto.FriendRequestResDto;
import com.yumyum.sns.friend.dto.ReceivedFriendRequestDto;
import com.yumyum.sns.friend.dto.SentFriendRequestDto;
import com.yumyum.sns.friend.entity.FriendRequest;
import com.yumyum.sns.friend.entity.FriendRequestStatus;
import com.yumyum.sns.friend.repository.FriendRequestRepository;
import com.yumyum.sns.member.entity.Member;
import com.yumyum.sns.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendRequestServiceImpl implements FriendRequestService{

    private final FriendRequestRepository friendRequestRepository;
    private final MemberService memberService;

    //받은 친구요청 조회
    @Transactional(readOnly = true)
    @Override
    public List<ReceivedFriendRequestDto> getReceivedFriendReqs(Long receiverId) {
        memberService.getMemberById(receiverId);
        return friendRequestRepository.findFriendRequestsByReceiver(receiverId);
    }

    //보낸 친구요청 조회
    @Override
    @Transactional(readOnly = true)
    public List<SentFriendRequestDto> getSentFriendReqs(Long senderId) {
        memberService.getMemberById(senderId);
        return friendRequestRepository.findFriendRequestsBySender(senderId);
    }

    // 친구요청 거절, 취소 또는 친구삭제
    @Override
    @Transactional
    public void deleteFriendReq(Long friendRequestId) {
        FriendRequest friendRequest = getFriendRequestById(friendRequestId);
        friendRequestRepository.delete(friendRequest);
    }

    // 친구 삭제시 친구요청 삭제(사용X)
    @Override
    @Transactional
    public void removeFriendRequestOnFriendRemoval(Long myId, Long memberId) {
        friendRequestRepository.deleteFriendRequestOnFriendRemoval(myId,memberId);
    }

    // 친구요청 생성(insert)
    @Override
    @Transactional
    public FriendRequestResDto createFriendRequest(Long senderId, Long receiverId) {
        Member receiver = memberService.getMemberById(receiverId);
        Member sender = memberService.getMemberById(senderId);
        FriendRequest friendRequest = new FriendRequest(sender,receiver);
        FriendRequest savedFriendRequest = friendRequestRepository.save(friendRequest);
        return FriendRequestResDto.toDto(savedFriendRequest);
    }

    //친구요청 단건조회 및 예외
    @Override
    @Transactional(readOnly = true)
    public FriendRequest getFriendRequestById(Long friendRequestId) {
        return friendRequestRepository.findById(friendRequestId).orElseThrow((() -> new FriendRequestNotFoundException(friendRequestId)));
    }

    //친구요청 상태 확인
    @Override
    @Transactional(readOnly = true)
    public Optional<FriendRequestResDto> getFriendRequestIdByMemberIds(Long myId, Long userId) {
        Member myMember = memberService.getMemberById(myId);
        Member userMember = memberService.getMemberById(userId);

        Optional<FriendRequest> friendRequestIdByMemberIds = friendRequestRepository.findFriendRequestIdByMemberIds(myMember.getId(), userMember.getId());
        Optional<FriendRequestResDto> friendRequestResDto = friendRequestIdByMemberIds.map(friendRequest ->
                FriendRequestResDto.toDto(friendRequest));
        return friendRequestResDto;
    }

    //친구요청 수락(update)
    @Override
    @Transactional
    public FriendRequestResDto acceptFriendRequest(Long friendRequestId) {
        FriendRequest friendRequest = getFriendRequestById(friendRequestId);
        friendRequest.changeFriendRequestState(FriendRequestStatus.ACCEPTED);
        return FriendRequestResDto.toDto(friendRequest);
    }




}
