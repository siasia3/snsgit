package com.yumyum.sns.member.service;

import com.yumyum.sns.member.dto.*;
import com.yumyum.sns.member.entity.Member;

import java.util.List;
import java.util.Optional;

public interface MemberService {

    /**
     * PK로 회원 확인
     * @param memberId 회원 PK값
     * @return 존재하는 회원인지 여부
     */
    public Boolean checkMember(Long memberId);

    /**
     * 식별자로 회원 조회, 없는 경우 Exception
     * @param identifier 회원 식별코드
     * @return 조회된 회원 엔티티
     */
    public Member getMemberByIdentifier(String identifier);

    /**
     * PK로 회원 조회, 없는 경우 Exception
     * @param memberId 회원 PK
     * @return 조회된 회원 엔티티
     */
    Member getMemberById(Long memberId);


    /**
     * 닉네임으로 회원 조회
     * @param nickname 조회할 닉네임
     * @return 조회된 회원
     */
    public Member getMemberByNickname(String nickname);

    /**
     * 닉네임 중복 체크
     * @param nickname 체크할 닉네임
     * @return 중복인 경우 true, 중복이 아닌 경우 false
     */
    public boolean checkNicknameDuplicate(String nickname);

    /**
     * 회원ID 중복체크
     * @param userId 회원 ID
     * @return 중복인 경우 true, 중복이 아닌 경우 false
     */
    public boolean checkUserIdDuplicate(String userId);

    //식별자로 회원 확인

    /**
     * 식별코드로 이미 존재하는 회원인지 체크
     * @param identifier 회원 식별코드
     * @return 조회된 회원
     */
    public Optional<Member> checkIdentifier(String identifier);

    /**
     * 소셜로그인 회원 등록
     * @param member 등록할 회원 엔티티
     * @return 등록된 회원 엔티티
     */
    public Member createSocialMember(Member member);

    /**
     * 일반 회원가입
     * @param signupDTO 등록할 회원 DTO
     * @return 등록된 회원 엔티티
     */
    public Member createLocalMember(SignupDTO signupDTO);

    //회원 수정

    /**
     * 변경된 회원 이름 또는 이메일 수정
     * @param member 정보를 수정할 회원 엔티티
     * @return 수정된 회원 엔티티
     */
    public Member modifyMember(Member member);


    /**
     * 닉네임 검색을 통해 간단한 회원 정보를 가져옵니다.
     *
     * @param nickName 회원 닉네임
     * @return 검색된 회원 정보
     */
    public MemberSearchDto getSearchMember(String nickName);

    /**
     * 회원 검색 미리보기
     * @param keyword 입력된 검색어
     * @return 검색된 회원들의 미리보기
     */
    public List<NicknamePreviewDto> previewUserByNickname(String keyword);

    /**
     * 회원 프로필편집 정보 조회
     * @param memberId 회원 PK
     * @return 편집 회원정보 dto
     */
    public MemberEditDto getMemberEditInfo(Long memberId);

    /**
     * 회원 프로필 수정
     * @param memberEditDto 회원 프로필 수정시킬 정보
     * @return 수정된 프로필 정보
     */
    public MemberEditResponse EditMemberInfo(MemberEditDto memberEditDto);

}
