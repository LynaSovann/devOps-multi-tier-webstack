package com.java.backend.service.ServiceImpl;

import com.java.backend.model.User;
import com.java.backend.model.request.AccountRequest;
import com.java.backend.model.response.AccountResponse;
import com.java.backend.repository.AccountRepository;
import com.java.backend.service.AccountService;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ModelMapper modelMapper;

    public AccountServiceImpl(AccountRepository accountRepository, BCryptPasswordEncoder bCryptPasswordEncoder, ModelMapper modelMapper) {
        this.accountRepository = accountRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.modelMapper = modelMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return accountRepository.findByEmail(email);
    }

    @Override
    public AccountResponse createAccount(AccountRequest accountRequest) {

        String username = accountRequest.getFirstname() + "_" + accountRequest.getLastname();
        accountRequest.setPassword(bCryptPasswordEncoder.encode(accountRequest.getPassword()));
        User userEmail = accountRepository.findByEmail(accountRequest.getEmail());
        if (userEmail != null) {
            System.out.println("Email already exists");
            return null;
        }

        User user = accountRepository.createAccount(accountRequest, username);


        return modelMapper.map(accountRepository.findByEmail(user.getEmail()), AccountResponse.class);
    }
}
