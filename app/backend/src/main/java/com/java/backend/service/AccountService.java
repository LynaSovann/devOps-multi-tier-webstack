package com.java.backend.service;


import com.java.backend.model.request.AccountRequest;
import com.java.backend.model.response.AccountInfoResponse;
import com.java.backend.model.response.AccountResponse;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AccountService extends UserDetailsService {

    AccountResponse createAccount(AccountRequest accountRequest);

    AccountInfoResponse getUserInfo(String userEmail);

    List<AccountResponse> getAllAccount();

    void deleteProfileImage(Integer userId);
    String uploadProfileImage (Integer userId, MultipartFile file);
}
