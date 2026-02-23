package com.java.backend.model.response;

import com.java.backend.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountInfoResponse {

    private Integer userInfoId;
    private String firstname;
    private String lastname;
    private String profileImage;
    private String bio;
    private AccountResponse user;
}
