package com.java.backend.controller;

import com.java.backend.jwt.JwtService;
import com.java.backend.model.request.AccountInfoRequest;
import com.java.backend.model.request.AccountRequest;
import com.java.backend.model.request.AuthRequest;
import com.java.backend.model.response.APIResponse;
import com.java.backend.model.response.AccountResponse;
import com.java.backend.model.response.AuthResponse;
import com.java.backend.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("api/v1/accounts")
public class AccountController {

    private final AccountService accountService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AccountController(AccountService accountService, BCryptPasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            UserDetails userApp = accountService.loadUserByUsername(username);
            if (userApp == null) {
//                throw new BadRequestException("Wrong Email");
                System.out.println("user not found");
            }
            if (!passwordEncoder.matches(password, userApp.getPassword())) {
//                throw new BadRequestException("Wrong Password");
                System.out.println("wrong password");
            }
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }

    @PostMapping("/create-account")
    @Operation(summary = "register")
    public ResponseEntity<APIResponse<?>> createAccount(@RequestBody AccountRequest accountRequest) {
        APIResponse<AccountResponse> res = APIResponse.<AccountResponse>builder()
                .message("Created Account successfully!")
                .payload(accountService.createAccount(accountRequest))
                .status(HttpStatus.CREATED)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PostMapping("/login")
    @Operation(summary = "login")
    public ResponseEntity<APIResponse<?>> login(@RequestBody AuthRequest authRequest) throws Exception {
        authenticate(authRequest.getEmail(), authRequest.getPassword());
        final UserDetails userDetails = accountService.loadUserByUsername(authRequest.getEmail());
        final String token = jwtService.generateToken(userDetails);
        AuthResponse authResponse = new AuthResponse(token);
        APIResponse<AuthResponse> res = APIResponse.<AuthResponse>builder()
                .message("Login successfully")
                .payload(authResponse)
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(res);
    }

    @GetMapping
    @Operation(summary = "get all accounts")
    public ResponseEntity<APIResponse<?>> getAllAccounts() throws Exception {

        APIResponse<List<AccountResponse>> res = APIResponse.<List<AccountResponse>>builder()
                .message("Get all accounts successfully")
                .payload(accountService.getAllAccount())
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(res);
    }


}
