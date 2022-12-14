package com.project.date.service;

import com.project.date.dto.request.*;
import com.project.date.dto.response.ResponseDto;
import com.project.date.dto.response.UserResponseDto;
import com.project.date.jwt.TokenProvider;
import com.project.date.model.*;
import com.project.date.repository.ImgRepository;
import com.project.date.repository.UserRepository;
import com.project.date.util.AwsS3UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final ImgRepository imgRepository;
    private final AwsS3UploadService awsS3UploadService;
    private static final String ADMIN_TOKEN = ("AAABnvxRVklrnYxKZ0aHgTBcXukeZygoC");

    //  회원가입. 유저가 존재하는지, 비밀번호와 비밀번호확인이 일치하는지의 여부를 if문을 통해 확인하고 이를 통과하면 user에 대한 정보를 생성.
    @Transactional
    public ResponseDto<?> checkUser(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (null != isPresentUser(username))
            return ResponseDto.fail("DUPLICATED_USERNAME", "중복된 ID 입니다.");
        return ResponseDto.success("사용 가능한 ID입니다.");
    }

    @Transactional
    public ResponseDto<?> checkNickname(String nickname) {
        Optional<User> user = userRepository.findByNickname(nickname);
        if (null != isPresentNickname(nickname))
            return ResponseDto.fail("DUPLICATED_NICKANAME", "중복된 닉네임 입니다.");
        return ResponseDto.success("사용 가능한 닉네임 입니다.");
    }

    @Transactional
    public ResponseDto<?> createUser(SignupRequestDto requestDto, UserInfoRequestDto userInfoRequestDto, List<String> imgPaths,HttpServletResponse response) {
        if (!requestDto.getPassword().equals(requestDto.getPasswordConfirm())) {
            return ResponseDto.fail("PASSWORDS_NOT_MATCHED",
                    "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
//        String gender = requestDto.getGender();
//        Gender genderSet = Gender.GENDER;
//
//        if (gender.equals("male")) {
//            genderSet = Gender.MALE;
//        } else if (gender.equals("female")) {
//            genderSet = Gender.FEMALE;
//        }
        Authority role = Authority.USER;
        if (requestDto.isAdmin()) {
            if (!requestDto.getAdminToken().equals(ADMIN_TOKEN)) {
                return ResponseDto.fail("BAD_REQUEST", "관리자 암호가 틀려 등록이 불가합니다.");

            }
            role = Authority.ADMIN;
        }

        User user = User.builder()
                .username(requestDto.getUsername())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .nickname(requestDto.getNickname())
                .gender(requestDto.getGender())
                .imageUrl(requestDto.getImageUrl())
                .role(role)
                .age(userInfoRequestDto.getAge())
                .mbti(userInfoRequestDto.getMbti())
                .introduction(userInfoRequestDto.getIntroduction())
                .area(userInfoRequestDto.getArea())
                .idealType(userInfoRequestDto.getIdealType())
                .job(userInfoRequestDto.getJob())
                .hobby(userInfoRequestDto.getHobby())
                .smoke(userInfoRequestDto.getSmoke())
                .drink(userInfoRequestDto.getDrink())
                .likeMovieType(userInfoRequestDto.getLikeMovieType())
                .pet(userInfoRequestDto.getPet())
                .build();
        userRepository.save(user);

        postBlankCheck(imgPaths);

        List<String> imgList = new ArrayList<>();
        for (String imgUrl : imgPaths) {
            Img img = new Img(imgUrl, user);
            imgList.add(img.getImageUrl());
        }
        user.imageSave(imgList.get(0));

        TokenDto tokenDto = tokenProvider.generateTokenDto(user);
        tokenToHeaders(tokenDto, response);

        if(user.getRole().equals(Authority.ADMIN)) {
            return ResponseDto.success("관리자 회원가입이 완료되었습니다");
        }

        return ResponseDto.success(
                UserResponseDto.builder()
                        .userId(user.getId())
                        .nickname(user.getNickname())
                        .imageUrl(user.getImageUrl())
                        .gender(user.getGender())
                        .build()
        );

    }
    private void postBlankCheck(List<String> imgPaths) {
        if (imgPaths == null || imgPaths.isEmpty()) { //.isEmpty()도 되는지 확인해보기
            throw new NullPointerException("이미지를 등록해주세요(Blank Check)");
        }
    }

    @Transactional
//  로그인. 가입할때 사용된 정보를 SignupRequestDto에 보내고 HttpServletResponse에 속한 권한이 확인.
//  사용자의 아이디가 존재하지 않거나 비밀번호확인이 일치하지 않았을 때 오류 메시지를 출력.
//  정상일 경우 tokenProvider를 통하여 유저에게 토큰을 생성하고 이를 헤더에 보낸다.
    public ResponseDto<?> login(LoginRequestDto requestDto, HttpServletResponse response) {
        User user = isPresentUser(requestDto.getUsername());
        if (null == user) {
            return ResponseDto.fail("USER_NOT_FOUND",
                    "사용자를 찾을 수 없습니다.");
        }

        if (!user.validatePassword(passwordEncoder, requestDto.getPassword())) {
            return ResponseDto.fail("INVALID_USER", "비밀번호가 틀렸습니다..");
        }

        TokenDto tokenDto = tokenProvider.generateTokenDto(user);
        tokenToHeaders(tokenDto, response);

        if(user.getRole().equals(Authority.ADMIN)) {
            return ResponseDto.success("관리자 로그인에 성공하였습니다");
        }

        return ResponseDto.success(
                UserResponseDto.builder()
                        .userId(user.getId())
                        .nickname(user.getNickname())
                        .gender(user.getGender())
                        .imageUrl(user.getImageUrl())
                        .build()
        );

    }

    @Transactional
    public ResponseDto<?> getUser(HttpServletRequest request) {

        if (null == request.getHeader("RefreshToken")) {
            return ResponseDto.fail("USER_NOT_FOUND",
                    "로그인이 필요합니다.");
        }

        if (null == request.getHeader("Authorization")) {
            return ResponseDto.fail("USER_NOT_FOUND",
                    "로그인이 필요합니다.");
        }
        User user = validateUser(request);
        if (null == user) {
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
        }
        return ResponseDto.success(
                UserResponseDto.builder()
                        .userId(user.getId())
                        .nickname(user.getNickname())
                        .gender((user.getGender()))
                        .imageUrl(user.getImageUrl())
                        .role(String.valueOf(user.getRole()))
                        .build()
        );
    }

    @Transactional
    public ResponseDto<?> updatePassword(UserUpdateRequestDto requestDto,
                                         HttpServletRequest request) {


        if (null == request.getHeader("RefreshToken")) {
            return ResponseDto.fail("USER_NOT_FOUND",
                    "로그인이 필요합니다.");
        }

        if (null == request.getHeader("Authorization")) {
            return ResponseDto.fail("USER_NOT_FOUND",
                    "로그인이 필요합니다.");
        }

        User user = validateUser(request);
        if (null == user) {
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
        }

        if (!requestDto.getPassword().equals(requestDto.getPasswordConfirm())) {
            return ResponseDto.fail("PASSWORDS_NOT_MATCHED",
                    "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }


        user.update(requestDto);
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        userRepository.save(user);

        return ResponseDto.success("비밀번호 수정이 완료되었습니다!");
    }


    @Transactional
    public ResponseDto<?> updateImage(HttpServletRequest request, List<String> imgPaths, ImageUpdateRequestDto requestDto) {
        if (null == request.getHeader("RefreshToken")) {
            return ResponseDto.fail("USER_NOT_FOUND",
                    "로그인이 필요합니다.");
        }

        if (null == request.getHeader("Authorization")) {
            return ResponseDto.fail("USER_NOT_FOUND",
                    "로그인이 필요합니다.");
        }

        User user = validateUser(request);
        if (null == user) {
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
        }
        if (imgPaths != null) {
            String deleteImage = user.getImageUrl();
            awsS3UploadService.deleteFile(AwsS3UploadService.getFileNameFromURL(deleteImage));
        }
        user.update(requestDto);

        List<String> imgList = new ArrayList<>();
        for (String imgUrl : imgPaths) {
            Img img = new Img(imgUrl, user);
            imgList.add(img.getImageUrl());
        }
        user.imageSave(imgList.get(0));

        userRepository.save(user);

        return ResponseDto.success("프로필 사진 수정이 완료되었습니다!");

    }


    //  로그아웃. HttpServletRequest에 있는 권한을 보내 토큰을 확인하여 일치하지 않거나 유저 정보가 없
    public ResponseDto<?> logout(HttpServletRequest request) {
        if (!tokenProvider.validateToken(request.getHeader("RefreshToken"))) {
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
        }
        User user = tokenProvider.getUserFromAuthentication();
        if (null == user) {
            return ResponseDto.fail("USER_NOT_FOUND",
                    "사용자를 찾을 수 없습니다.");
        }
        List<Img> findImgList = imgRepository.findByUser_Id(user.getId());
        List<String> imgList = new ArrayList<>();
        for (Img img : findImgList) {
            imgList.add(img.getImageUrl());
        }

        for (String imgUrl : imgList) {
            awsS3UploadService.deleteFile(AwsS3UploadService.getFileNameFromURL(imgUrl));
        }

        return tokenProvider.deleteRefreshToken(user);
    }

    @Transactional
    public ResponseDto<?> deleteUser(String ADMIN_TOKEN, Long userId) {

        User user = isPresentId(userId);
        if (null == user) {
            return ResponseDto.fail("NOT_FOUND", "존재하지 않는 사용자입니다.");
        }

        if (ADMIN_TOKEN == null) {
            return ResponseDto.fail("BAD_REQUEST", "관리자 코드가 입력되지 않았습니다.");
        }
        userRepository.delete(user);

        return ResponseDto.success("회원삭제가 완료되었습니다");
    }

    @Transactional(readOnly = true)
    public User isPresentUser(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        return optionalUser.orElse(null);
    }

    @Transactional(readOnly = true)
    public User isPresentId(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        return optionalUser.orElse(null);
    }

    @Transactional(readOnly = true)
    public User isPresentNickname(String nickname) {
        Optional<User> optionalUser = userRepository.findByNickname(nickname);
        return optionalUser.orElse(null);
    }

    @Transactional
    public User validateUser(HttpServletRequest request) {
        if (!tokenProvider.validateToken(request.getHeader("RefreshToken"))) {
            return null;
        }
        return tokenProvider.getUserFromAuthentication();
    }

    //  TokenDto와 HttpServletResponse 응답을 헤더에 보낼 경우
    //  권한과 tokenDto에 있는 AccessToken을 추가
    //  Refresh-token을 추가
    //  AccessToken의 유효기간을 추가한다.
    public void tokenToHeaders(TokenDto tokenDto, HttpServletResponse response) {
        response.addHeader("Authorization", "Bearer " + tokenDto.getAccessToken());
        response.addHeader("RefreshToken", tokenDto.getRefreshToken());
        response.addHeader("Access-Token-Expire-Time", tokenDto.getAccessTokenExpiresIn().toString());
    }
}
