package com.yumyum.sns.member.service;

import com.yumyum.sns.error.exception.MemberNotFoundException;
import com.yumyum.sns.infra.s3.S3StorageService;
import com.yumyum.sns.member.dto.*;
import com.yumyum.sns.member.entity.Member;
import com.yumyum.sns.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{

    private final MemberRepository memberRepository;
    private final S3StorageService s3StorageService;

    //id값으로 회원 확인
    @Override
    public Boolean checkMember(Long memberId) {
        return memberRepository.findById(memberId).isPresent();
    }

    //식별자로 회원 조회
    @Override
    public Member getMemberByIdentifier(String identifier) {
        return memberRepository.findByIdentifier(identifier).orElseThrow(() -> new MemberNotFoundException(identifier + " 식별자를 가진 회원이 존재하지 않습니다"));
    }

    //id값으로 회원 조회
    @Override
    public Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new MemberNotFoundException(memberId + " id를 가진 회원이 존재하지 않습니다"));
    }

    //닉네임 회원 조회
    @Override
    public Member getMemberByNickname(String nickname) {
        return memberRepository.findByNickname(nickname).orElseThrow(() -> new MemberNotFoundException(nickname + " 닉네임을 가진 회원이 존재하지 않습니다."));
    }

    //닉네임 중복 확인
    public boolean checkNicknameDuplicate(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    //식별자 회원 확인
    @Override
    public Optional<Member> checkIdentifier(String identifier) {
        return memberRepository.findByIdentifier(identifier);
    }

    //소셜로그인 회원 생성
    @Override
    public Member createMember(Member member){
         return memberRepository.save(member);
    }

    //소셜로그인 회원정보 수정
    @Override
    public Member modifyMember(Member member){
        return memberRepository.save(member);
    }


    //닉네임 검색 시 회원 정보 조회
    @Override
    public MemberSearchDto getSearchMember(String nickName) {
        return memberRepository.findMemberSearch(nickName).orElseThrow(()-> new MemberNotFoundException(nickName + " 닉네임을 가진 회원이 존재하지 않습니다."));
    }

    //닉네임을 통해 회원 미리보기 조회
    @Override
    public List<NicknamePreviewDto> previewUserByNickname(String keyword) {
        List<Member> previewMembers = memberRepository.getPreviewByNickname(keyword);
        List<NicknamePreviewDto> previewDtos = previewMembers.stream()
                .map(member -> new NicknamePreviewDto(member.getId(), member.getNickname(), member.getProfileImage()))
                .collect(Collectors.toList());
        return previewDtos;
    }

    //프로필편집 정보 조회
    @Override
    public MemberEditDto getMemberEditInfo(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new MemberNotFoundException(memberId + " id를 가진 회원이 존재하지 않습니다"));
        return MemberEditDto.toDto(member);
    }

    //회원 프로필 수정
    @Override
    public MemberEditResponse EditMemberInfo(MemberEditDto memberEditDto) {
        Member member = getMemberById(memberEditDto.getMemberId());
        MultipartFile profileFile = memberEditDto.getMemberProfileFile();
        if(profileFile != null && !profileFile.isEmpty()) {
            if(profileFile.getContentType().startsWith("image/")) {
                String savedProfilePath = s3StorageService.uploadFile(memberEditDto.getMemberProfileFile());
                memberEditDto.setMemberProfilePath(savedProfilePath);
            }else{
                //파일이 존재하는데 이미지 파일이 아니라면 예외
                throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
            }
        }
        member.editMemberInfo(memberEditDto);
        return MemberEditResponse.toDto(member);
    }


}
