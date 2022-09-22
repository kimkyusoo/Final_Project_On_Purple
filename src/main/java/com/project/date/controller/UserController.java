package com.project.date.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.date.dto.request.LoginRequestDto;
import com.project.date.dto.request.SignupRequestDto;
import com.project.date.dto.response.ResponseDto;
import com.project.date.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;
//    private final KakaoLoginService kakaoLoginService;

    // POST방식 회원가입 API UserRequestDto에서 표현한 정규표현식을 따른 정보를 받아 UserService에서 정의한 createUser메소드에 따라 아이디와 비밀번호 확인을 거치고 이를 만족시키면 아이디 비밀번호를 생성.

    @RequestMapping(value = "/user/signup", method = RequestMethod.POST)
    public ResponseDto<?> signup(@RequestBody @Valid SignupRequestDto requestDto, @RequestPart(value = "imageUrl", required = false) MultipartFile multipartFile){
        return userService.createUser(requestDto, multipartFile);
    }

    // POST방식 로그인 API SignupRequestDto에서 정보를 받아 권한인증을 거치고 이를 UserService에서 정의한 login메소드에 따라 아이디와 비밀번호 확인을 거치고 이를 만족시키면 토큰을 발행. 로그인에 성공한 후 작업처리를 진행한다.
    @RequestMapping(value = "/user/login", method = RequestMethod.POST)
    public ResponseDto<?> login(@RequestBody @Valid LoginRequestDto requestDto,
                                HttpServletResponse response) {
        return userService.login(requestDto, response);
    }

    //  POST방식 로그아웃 API 권한인증을 받은 사용자가 해당 api를 요청하면 UserService에 정의한 logout 메소드를 따라 로그아웃을 진행.
    @RequestMapping(value = "/user/logout", method = RequestMethod.POST)
    public ResponseDto<?> logout(HttpServletRequest request) {
        return userService.logout(request);

    }

}