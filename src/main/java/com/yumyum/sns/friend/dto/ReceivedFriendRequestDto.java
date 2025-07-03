package com.yumyum.sns.friend.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReceivedFriendRequestDto {

    private Long friendRequestId;
    private Long senderId;
    private String profilePath;
    private Long receiverId;
    private String nickname;

    @QueryProjection
    public ReceivedFriendRequestDto(Long friendRequestId,String profilePath, Long senderId,String nickname) {
        this.friendRequestId = friendRequestId;
        this.profilePath = profilePath;
        this.senderId = senderId;
        this.nickname = nickname;
    }
}
