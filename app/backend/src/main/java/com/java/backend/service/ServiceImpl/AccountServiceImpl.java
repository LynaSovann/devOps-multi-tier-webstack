package com.java.backend.service.ServiceImpl;

import com.java.backend.model.User;
import com.java.backend.model.UserInfo;
import com.java.backend.model.request.AccountRequest;
import com.java.backend.model.request.UserInfoRequest;
import com.java.backend.model.response.AccountInfoResponse;
import com.java.backend.model.response.AccountResponse;
import com.java.backend.repository.AccountInfoRepository;
import com.java.backend.repository.AccountRepository;
import com.java.backend.service.AccountService;
import com.java.backend.service.MinioService;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ModelMapper modelMapper;
    private final AccountInfoRepository accountInforRepository;
    private final MinioService minioService;

    public AccountServiceImpl(AccountRepository accountRepository, BCryptPasswordEncoder bCryptPasswordEncoder, ModelMapper modelMapper, AccountInfoRepository accountInforRepository, MinioService minioService) {
        this.accountRepository = accountRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.modelMapper = modelMapper;
        this.accountInforRepository = accountInforRepository;
        this.minioService = minioService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return accountRepository.findByEmail(email);
    }

    @Override
    public AccountResponse createAccount(AccountRequest accountRequest) {

        accountRequest.setPassword(bCryptPasswordEncoder.encode(accountRequest.getPassword()));
        User userEmail = accountRepository.findByEmail(accountRequest.getEmail());
        if (userEmail != null) {
            System.out.println("Email already exists");
            return null;
        }

        User user = accountRepository.createAccount(accountRequest);

        accountInforRepository.createUserInfo(new UserInfoRequest(user.getUserId()));

        return modelMapper.map(accountRepository.findByEmail(user.getEmail()), AccountResponse.class);
    }

    @Override
    public AccountInfoResponse getUserInfo(String userEmail) {
        Integer userId = accountRepository.findByEmail(userEmail).getUserId();
        UserInfo userInfo = accountInforRepository.getUserInfo(userId);
        return new AccountInfoResponse(
                userInfo.getUserInfoId(),
                userInfo.getFirstname(),
                userInfo.getLastname(),
                userInfo.getProfileImage(),
                userInfo.getBio(),
                modelMapper.map(accountRepository.findByEmail(userEmail), AccountResponse.class)
        );
    }

    @Override
    public List<AccountResponse> getAllAccount() {
        List<User> users = accountRepository.getAllAccounts();

        return users.stream()
                .map(user -> modelMapper.map(user, AccountResponse.class))
                .toList();
    }

    @Override
    public void deleteProfileImage(Integer userId) {
        UserInfo userInfo = accountInforRepository.getUserInfo(userId);
        minioService.deleteProfileImage(userInfo.getProfileImage());
        accountInforRepository.deleteProfileImage(userId);
    }

    @Override
    public String uploadProfileImage(Integer userId, MultipartFile file) {

        UserInfo  userInfo = accountInforRepository.getUserInfo(userId);
        if(!Objects.equals(userInfo.getProfileImage(), "N/A")) {
            minioService.deleteProfileImage(userInfo.getProfileImage());
        }

        String imageUrl = minioService.uploadProfileImage(userId, file);
        userInfo.setProfileImage(imageUrl);

        accountInforRepository.addProfileImage(userId, imageUrl);
        return imageUrl;
    }
}
