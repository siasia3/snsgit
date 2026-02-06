package com.yumyum.sns.security.oauthjwt.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDTO {

    private String role;
    private String name;
    private String username;

    public UserDTO(String role, String name, String username) {
        this.role = role;
        this.name = name;
        this.username = username;
    }

    public UserDTO(String username, String role){
        this.role = role;
        this.username = username;
    }



}
