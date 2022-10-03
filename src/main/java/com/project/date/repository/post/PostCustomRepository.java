package com.project.date.repository.post;

import com.project.date.dto.response.PostResponseDto;
import com.project.date.model.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface PostCustomRepository {
    Slice<PostResponseDto> findAllByCategory(String category, String keyword, Pageable pageable);
}