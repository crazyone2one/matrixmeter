package cn.master.matrix.controller;

import cn.master.matrix.entity.UserKey;
import cn.master.matrix.mapper.UserKeyMapper;
import cn.master.matrix.payload.LoginRequest;
import cn.master.matrix.payload.response.JwtResponse;
import cn.master.matrix.security.JwtProvider;
import cn.master.matrix.security.UserPrinciple;
import cn.master.matrix.service.UserKeyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Created by 11's papa on 06/21/2024
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Transactional(rollbackFor = Exception.class)
@Tag(name = "Auth", description = "Authentication")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final UserKeyMapper userKeyMapper;
    private final UserKeyService userKeyService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        //SecurityContextHolder.getContext().setAuthentication(authentication);
        val token = jwtProvider.generateAccessToken(authentication);
        val refreshToken = jwtProvider.generateRefreshToken(authentication);
        UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        revokeAllTokenByUser(userDetails);
        saveUserToken(token, refreshToken, userDetails);
        return ResponseEntity.ok(new JwtResponse(token, refreshToken,
                userDetails.getUser().getId(),
                userDetails.getUsername(),
                userDetails.getUser().getEmail(),
                roles));
    }

    private void saveUserToken(String accessToken, String refreshToken, UserPrinciple userDetails) {
        val userKey = UserKey.builder().accessKey(accessToken).secretKey(refreshToken).enable(true)
                .userId(userDetails.getUser().getId()).build();
        userKeyMapper.insert(userKey);
    }

    @PostMapping("/refreshToken")
    public void authenticateUser(@Valid HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken = authHeader.substring(7);
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        if (jwtProvider.isValid(refreshToken, (UserDetails) authentication.getPrincipal())) {
            val accessToken = jwtProvider.generateAccessToken(authentication);
            val principal = (UserPrinciple) authentication.getPrincipal();
            revokeAllTokenByUser(principal);
            saveUserToken(accessToken, refreshToken, principal);
            List<String> roles = principal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            val jwtResponse = new JwtResponse(accessToken, refreshToken,
                    principal.getUser().getId(),
                    principal.getUsername(),
                    principal.getUser().getEmail(),
                    roles);
            new ObjectMapper().writeValue(response.getOutputStream(), jwtResponse);
        }
    }

    private void revokeAllTokenByUser(UserPrinciple userDetails) {
        val userKeys = userKeyService.listAllByUserId(userDetails.getUser().getId());
        if (!userKeys.isEmpty()) {
            userKeys.forEach(t -> t.setEnable(false));
        }
        userKeyService.updateBatch(userKeys);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('test1234')")
    public ResponseEntity<?> me() {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        val principal = authentication.getPrincipal();
        val details = authentication.getDetails();
        return ResponseEntity.ok(principal);
    }

    @GetMapping("/demo")
    @PreAuthorize("hasAuthority('test')")
    public String demo() {
        return "demo";
    }
}
