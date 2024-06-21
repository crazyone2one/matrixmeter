package cn.master.matrix.controller;

import cn.master.matrix.entity.UserKey;
import cn.master.matrix.mapper.UserKeyMapper;
import cn.master.matrix.payload.LoginRequest;
import cn.master.matrix.payload.response.JwtResponse;
import cn.master.matrix.security.JwtProvider;
import cn.master.matrix.security.UserPrinciple;
import cn.master.matrix.service.UserKeyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Created by 11's papa on 06/21/2024
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
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

        SecurityContextHolder.getContext().setAuthentication(authentication);
        val token = jwtProvider.generateJwtToken(authentication);
        UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        revokeAllTokenByUser(userDetails);
        val userKey = UserKey.builder().accessKey(token).enable(true).userId(userDetails.getUserId()).build();
        userKeyMapper.insert(userKey);
        return ResponseEntity.ok(new JwtResponse(token,
                userDetails.getUserId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    private void revokeAllTokenByUser(UserPrinciple userDetails) {
        val userKeys = userKeyService.listAllByUserId(userDetails.getUserId());
        if (!userKeys.isEmpty()) {
            userKeys.forEach(t -> t.setEnable(false));
        }
        userKeyService.updateBatch(userKeys);
    }

    @GetMapping("/demo")
    public ResponseEntity<?> demo() {
        return ResponseEntity.ok("demo");
    }
}
