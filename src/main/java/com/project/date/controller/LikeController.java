package com.project.date.controller;

import com.project.date.dto.response.ResponseDto;
import com.project.date.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
@RequiredArgsConstructor
@RestController
public class LikeController {

    private final LikeService likeService;


    // 게시글 좋아요
    @PostMapping( "/post/like/{postId}")
    public ResponseDto<?> createPostLike(@PathVariable Long postId, HttpServletRequest request) {
        return likeService.PostLike(postId, request);
    }
    // 댓글 좋아요
    @PostMapping( "/comment/like/{commentId}")
    public ResponseDto<?> createCommentLike(@PathVariable Long commentId, HttpServletRequest request) {
        return likeService.CommentLike(commentId, request);
    }

    // 회원 좋아요
    @PostMapping( "/user/like/{targetId}")
    public ResponseDto<?> createUserLike(@PathVariable Long targetId, HttpServletRequest request) {
        return likeService.UserLike(targetId, request);
    }

    // 회원 싫어요
    @PostMapping( "/user/unlike/{targetId}")
    public ResponseDto<?> createUserUnLike(@PathVariable Long targetId, HttpServletRequest request) {
        return likeService.ProfileUnLike(targetId, request);
    }

    @PostMapping( "/user/match/{userId}")
    public ResponseDto<?> createCheckUser(@PathVariable Long userId, HttpServletRequest request) {
        return likeService.likeCheck(userId, request);
    }
    @GetMapping("/user/like")
    public ResponseDto<?> getLikeList(HttpServletRequest request){
        return likeService.getLike(request);
    }

    @GetMapping("/user/unLike")
    public ResponseDto<?> getUnLikeList(HttpServletRequest request){
        return likeService.getUnLike(request);
    }
}
