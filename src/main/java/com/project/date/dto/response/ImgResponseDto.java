package com.project.date.dto.response;


import com.project.date.model.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImgResponseDto {

    private Long id;
    private String imgUrl;
    private Post post;
}
