package com.yumyum.sns.member.entity;

import com.yumyum.sns.chat.entity.Chat;
import com.yumyum.sns.comment.entity.Comment;
import com.yumyum.sns.friend.entity.Friend;
import com.yumyum.sns.friend.entity.FriendRequest;
import com.yumyum.sns.member.dto.MemberEditDto;
import com.yumyum.sns.post.entity.Likes;
import com.yumyum.sns.post.entity.Post;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "member")
    private List<Chat> chatList = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Post> postList = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Likes> likesList = new ArrayList<>();

    @OneToMany(mappedBy = "requester")
    private List<FriendRequest> sentRequests = new ArrayList<>();

    @OneToMany(mappedBy = "receiver")
    private List<FriendRequest> receivedRequests = new ArrayList<>();

    @OneToMany(mappedBy = "memberA")
    private List<Friend> friendsA = new ArrayList<>();

    @OneToMany(mappedBy = "memberB")
    private List<Friend> friendsB = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Comment> commentList = new ArrayList<>();

    @Column(length = 30)
    private String userId;
    @Column(length = 50)
    private String name;
    private String contact;
    private LocalDate birthdate;
    @Column(length = 50)
    private String email;
    @Column(length = 20)
    private String nickname;
    @Column(length = 100)
    private String profileImage;
    @Column(length = 10)
    private String gender;
    @Column(length = 255)
    private String password;
    @Column(length = 15)
    private String role;
    private String identifier;

    public Member(Long id) {
        this.id = id;
    }

    public Member(String identifier, String name, String nickname, String email, String role) {
        this.identifier = identifier;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.role = role;
    }

    public Member(String name, String phone, LocalDate birthday, String nickname,
                  String gender, String identifier, String role) {
        this.name = name;
        this.contact = phone;
        this.birthdate = birthday;
        this.nickname = nickname;
        this.gender = gender;
        this.identifier = identifier;
        this.role = role;
    }

    //이름과 이메일값이 바뀌었으면 세팅
    public void modifyPersonalInfo(String name, String email) {
        this.name = name;
        this.email = email;
    }

    //회원 프로필 수정
    public void editMemberInfo(MemberEditDto memberEditDto){
        this.gender = memberEditDto.getGender();
        this.nickname = memberEditDto.getNickname();
        this.birthdate = memberEditDto.getBirthdate();
        this.profileImage = memberEditDto.getMemberProfilePath();
    }

    //회원가입 세팅
    public void settingSignUp(){
        this.role = "ROLE.USER";
        this.identifier = "local "+ UUID.randomUUID().toString();
    }

    public void setPassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
