package com.java.backend.model.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data

public class AccountResponse {

    private Integer userId;
    private String username;
    private String email;

}
