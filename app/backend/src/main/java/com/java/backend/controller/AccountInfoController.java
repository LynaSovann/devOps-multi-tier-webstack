package com.java.backend.controller;

import com.java.backend.model.User;
import com.java.backend.model.UserInfo;
import com.java.backend.model.response.APIResponse;
import com.java.backend.model.response.AccountInfoResponse;
import com.java.backend.model.response.DataWrapper;
import com.java.backend.repository.AccountInfoRepository;
import com.java.backend.repository.AccountRepository;
import com.java.backend.service.AccountService;
import com.java.backend.service.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/v1/account-info")
@SecurityRequirement(name = "bearerAuth")
public class AccountInfoController {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final MinioService minioService;
    private final AccountInfoRepository accountInfoRepository;


    public AccountInfoController(AccountService accountService, AccountRepository accountRepository, MinioService minioService, AccountInfoRepository accountInfoRepository) {
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.minioService = minioService;
        this.accountInfoRepository = accountInfoRepository;
    }

    String getEmailCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        return userDetails.getUsername();
    }

    @GetMapping
    @Operation(summary = "Get Account Information")
    public ResponseEntity<APIResponse<?>> getAccountInfo() {
        String userEmail = getEmailCurrentUser();
        APIResponse<AccountInfoResponse> res = APIResponse.<AccountInfoResponse>builder()
                .message("Get user information successfully")
                .payload(accountService.getUserInfo(userEmail))
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(res);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update Profile Image")
    public ResponseEntity<APIResponse<DataWrapper>> uploadProfileImage(MultipartFile file) {
        String userEmail = getEmailCurrentUser();
        User user = accountRepository.findByEmail(userEmail);

        String imageUrl = accountService.uploadProfileImage(user.getUserId(), file);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("imageUrl", imageUrl);
        attributes.put("uploadedAt", java.time.LocalDateTime.now());

        DataWrapper data = DataWrapper.builder()
                .type("profile-image")
                .id(user.getUserId().toString())
                .attributes(attributes)
                .build();

        APIResponse<DataWrapper> res = APIResponse.<DataWrapper>builder()
                .message("Upload profile successfully")
                .payload(data)
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(res);
    }

    @DeleteMapping
    @Operation(summary = "Delete profile image", description = "Delete the current profile image")
    public ResponseEntity<APIResponse<DataWrapper>> deleteUserProfile() {
        String userEmail = getEmailCurrentUser();
        User user = accountRepository.findByEmail(userEmail);
        UserInfo userInfo = accountInfoRepository.getUserInfo(user.getUserId());
        accountService.deleteProfileImage(userInfo.getUserId());

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("deleted", true);
        attributes.put("deletedAt", java.time.LocalDateTime.now());

        DataWrapper data = DataWrapper.builder()
                .type("profile-image")
                .id(user.getUserId().toString())
                .attributes(attributes)
                .build();
        APIResponse<DataWrapper> res = APIResponse.<DataWrapper>builder()
                .message("Deleted profile successfully")
                .payload(data)
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(res);

    }

//    @PutMapping
//    @Operation(summary = "Update User information")
//    public ResponseEntity<APIResponse<?>> updateAccountInfo() {
//        String userEmail = getEmailCurrentUser();
//        APIResponse<AccountInfoResponse> res = APIResponse.<AccountInfoResponse>builder()
//                .message("Get user information successfully")
//                .payload(accountService.getUserInfo(userEmail))
//                .status(HttpStatus.OK)
//                .timestamp(LocalDateTime.now())
//                .build();
//        return ResponseEntity.ok(res);
//    }

}
