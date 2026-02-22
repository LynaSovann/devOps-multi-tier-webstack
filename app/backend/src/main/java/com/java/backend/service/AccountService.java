package com.java.backend.service;


import com.java.backend.model.request.AccountRequest;
import com.java.backend.model.response.AccountResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends UserDetailsService {

    AccountResponse createAccount(AccountRequest accountRequest);

}
