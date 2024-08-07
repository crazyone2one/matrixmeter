package cn.master.matrix.controller;

import cn.master.matrix.constants.OperationLogType;
import cn.master.matrix.entity.UserKey;
import cn.master.matrix.exception.CustomException;
import cn.master.matrix.handler.annotation.HasAuthorize;
import cn.master.matrix.handler.annotation.Log;
import cn.master.matrix.mapper.UserKeyMapper;
import cn.master.matrix.payload.LoginRequest;
import cn.master.matrix.payload.dto.request.user.PersonalUpdatePasswordRequest;
import cn.master.matrix.payload.dto.request.user.PersonalUpdateRequest;
import cn.master.matrix.payload.dto.user.PersonalDTO;
import cn.master.matrix.payload.response.JwtResponse;
import cn.master.matrix.security.JwtProvider;
import cn.master.matrix.security.UserPrinciple;
import cn.master.matrix.service.UserKeyService;
import cn.master.matrix.service.UserService;
import cn.master.matrix.service.log.UserLogService;
import cn.master.matrix.util.SessionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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
    private final UserService userService;

    @PostMapping("/signin")
    @Operation(summary = "使用账号密码登录")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        //SecurityContextHolder.getContext().setAuthentication(authentication);
        val token = jwtProvider.generateAccessToken(authentication);
        val refreshToken = jwtProvider.generateRefreshToken(authentication);
        UserPrinciple userDetails = (UserPrinciple) authentication.getPrincipal();
        revokeAllTokenByUser(userDetails);
        saveUserToken(token, refreshToken, userDetails);
        val jwtResponse = new JwtResponse(token, refreshToken);
        val userDTO = userService.getUserDTO(userDetails.getUser().getId());
        userService.autoSwitch(userDTO);
        BeanUtils.copyProperties(userService.getUserDTO(userDetails.getUser().getId()), jwtResponse);
        return ResponseEntity.ok(jwtResponse);
    }

    private void saveUserToken(String accessToken, String refreshToken, UserPrinciple userDetails) {
        val userKey = UserKey.builder().accessKey(accessToken).secretKey(refreshToken).enable(true)
                .userId(userDetails.getUser().getId()).build();
        userKeyMapper.insert(userKey);
    }

    @PostMapping("/refreshToken")
    @Operation(summary = "刷新令牌")
    public void authenticateUser(@Valid HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken = authHeader.substring(7);
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        if (jwtProvider.isValid(refreshToken, (UserDetails) authentication.getPrincipal())) {
            val accessToken = jwtProvider.generateAccessToken(authentication);
            val principal = (UserPrinciple) authentication.getPrincipal();
            revokeAllTokenByUser(principal);
            saveUserToken(accessToken, refreshToken, principal);
            val jwtResponse = new JwtResponse(accessToken, refreshToken);
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

    @GetMapping("/get/{id}")
    @Operation(summary = "个人中心-获取信息")
    public PersonalDTO getInformation(@PathVariable String id) {
        this.checkPermission(id);
        return userService.getPersonalById(id);
    }

    @PostMapping("/update-info")
    @Operation(summary = "个人中心-修改信息")
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.updateAccountLog(#request)", mmClass = UserLogService.class)
    public boolean updateUser(@Validated @RequestBody PersonalUpdateRequest request) {
        this.checkPermission(request.getId());
        return userService.updateAccount(request, SessionUtils.getUserId());
    }

    @PostMapping("/update-password")
    @Operation(summary = "个人中心-修改密码")
    @Log(type = OperationLogType.UPDATE, expression = "#mmClass.updatePasswordLog(#request)", mmClass = UserLogService.class)
    public String updateUser(@Validated @RequestBody PersonalUpdatePasswordRequest request) {
        this.checkPermission(request.getId());
        if (userService.updatePassword(request)) {
            // todo 下线
        }
        return "OK";
    }

    @GetMapping("/demo")
    @HasAuthorize("ORGANIZATION_USER_ROLE")
    public String demo() {
        return "demo";
    }

    private void checkPermission(String id) {
        if (!StringUtils.equals(id, SessionUtils.getUserId())) {
            throw new CustomException("personal.no.permission");
        }
    }
}
