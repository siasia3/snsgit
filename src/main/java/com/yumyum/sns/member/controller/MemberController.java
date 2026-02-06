package com.yumyum.sns.member.controller;

import com.yumyum.sns.member.dto.*;
import com.yumyum.sns.member.entity.Member;
import com.yumyum.sns.member.service.MemberService;
import com.yumyum.sns.security.common.AuthMember;
import com.yumyum.sns.security.oauthjwt.dto.CustomOAuth2User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {

    private final MemberService memberService;

    //로그인 후 현재 회원 정보 가져오는 컨트롤러
    @GetMapping("/member/info")
    public ResponseEntity<?> getCurrentMemberId(@AuthenticationPrincipal AuthMember userDetail){

        String identifier = userDetail != null ? userDetail.getIdentifier(): null;
        Member checkMember = memberService.getMemberByIdentifier(identifier);

        if (identifier == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인인 된 회원이 아닙니다."));
        }
        MemberResponse memberResponse = new MemberResponse(checkMember.getId(), checkMember.getNickname(), checkMember.getProfileImage());
        return ResponseEntity.ok(memberResponse);
    }

    //검색 후 회원정보 조회 컨트롤러
    @GetMapping("/search/user/{nickname}")
    public ResponseEntity<MemberSearchDto> searchMember(@PathVariable String nickname){
        MemberSearchDto searchMember = memberService.getSearchMember(nickname);
        return ResponseEntity.ok(searchMember);
    }

    //닉네임을 통해 회원 조회
    @GetMapping("/user/{nickname}")
    public ResponseEntity<MemberResponse> getMemberByNickname(@PathVariable String nickname){
        Member member = memberService.getMemberByNickname(nickname);
        MemberResponse memberResponse = new MemberResponse(member.getId(), member.getNickname(), member.getProfileImage());
        return ResponseEntity.ok(memberResponse);
    }

    // 검색창 회원 미리보기 컨트롤러
    @GetMapping("/members/preview")
    public ResponseEntity<List<NicknamePreviewDto>> getMembersPreview(@RequestParam String keyword){
        if (keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어는 공백을 넣을 수 없습니다.");
        }
        List<NicknamePreviewDto> memberPreviews = memberService.previewUserByNickname(keyword);
        return ResponseEntity.ok(memberPreviews);
    }


    // 프로필 편집 회원정보 가져오기
    @GetMapping("/member/{memberId}")
    public ResponseEntity<MemberEditDto> getMemberInfo(@PathVariable Long memberId){
        MemberEditDto memberEditInfo = memberService.getMemberEditInfo(memberId);
        return ResponseEntity.ok(memberEditInfo);
    }

    //회원 프로필 수정 컨트롤러
    @PatchMapping(value = "/member", consumes = "multipart/form-data")
    public ResponseEntity<?> EditMemberProfile(@ModelAttribute MemberEditDto memberEditDto){
        MultipartFile file = memberEditDto.getMemberProfileFile();
        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            System.out.println("contentType = " + contentType);
            if (!contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("message", "이미지 파일만 업로드 가능합니다."));
            }
        }

        MemberEditResponse memberEditResponse = memberService.EditMemberInfo(memberEditDto);
        return ResponseEntity.ok(memberEditResponse);
    }

    //닉네임 중복체크 컨트롤러
    @GetMapping("/member/check-nickname")
    public ResponseEntity<Void> checkNickname(@RequestParam String nickname){
        boolean isDuplicate = memberService.checkNicknameDuplicate(nickname);

        if (isDuplicate) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/member/check-userId")
    public ResponseEntity<Void> checkUserId(@RequestParam String userId){
        boolean isDuplicate = memberService.checkUserIdDuplicate(userId);

        if (isDuplicate) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.ok().build();
    }

    //회원가입
    @PostMapping("/member/signup")
    public ResponseEntity<Void> signup(@Valid @ModelAttribute SignupDTO signupDTO,
                                    BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        memberService.createLocalMember(signupDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
