package com.yumyum.sns.member.dto;

import com.yumyum.sns.member.entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SignupDTO {
    @NotBlank
    private String userId;

    @NotBlank
    private String password;

    @NotBlank
    private String nickname;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotNull
    private LocalDate birthdate;

    @NotBlank
    private String gender;

    public SignupDTO(String userId, String password, String nickname, String name, String email, LocalDate birthdate, String gender) {
        this.userId = userId;
        this.password = password;
        this.nickname = nickname;
        this.name = name;
        this.email = email;
        this.birthdate = birthdate;
        this.gender = gender;
    }

    public Member toEntity(){
        return Member.builder()
                .userId(this.userId)
                .nickname(this.nickname)
                .name(this.name)
                .email(this.email)
                .birthdate(this.birthdate)
                .gender(this.gender)
                .build();
    }
}
