package com.yumyum.sns.friend.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SentFriendRequestDto {

    private Long friendRequestId;
    private Long senderId;
    private String profilePath;
    private Long receiverId;
    private String nickname;

    @QueryProjection
    public SentFriendRequestDto(Long friendRequestId,String profilePath, Long requesterId,String nickname) {
        this.friendRequestId = friendRequestId;
        this.profilePath = profilePath;
        this.receiverId = requesterId;
        this.nickname = nickname;
    }
}
