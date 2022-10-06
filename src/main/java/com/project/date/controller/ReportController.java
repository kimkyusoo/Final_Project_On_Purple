package com.project.date.controller;

import com.project.date.dto.request.PostRequestDto;
import com.project.date.dto.request.ReportRequestDto;
import com.project.date.dto.response.ResponseDto;
import com.project.date.service.ReportService;
import com.project.date.util.AwsS3UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class ReportController {
    private final ReportService reportService;
    private final AwsS3UploadService s3Service;

    // 게시글 작성
    @PostMapping( "/report")
    public ResponseDto<?> createReport(@RequestPart(value = "data",required = false) ReportRequestDto requestDto,
                                     HttpServletRequest request, @RequestPart(value = "imageUrl",required = false) List<MultipartFile> multipartFiles) {

        if (multipartFiles == null) {
            throw new NullPointerException("사진을 업로드해주세요");
        }
        List<String> imgPaths = s3Service.upload(multipartFiles);
        return reportService.createReport(requestDto,request, imgPaths);
    }

    // 카테고리별 전체 게시글 가져오기
    @GetMapping("/report") //기본 카테고리 meet 번개
    public ResponseDto<?> getAllPosts() {
        return reportService.getAllReport();
    }

    // 상세 게시글 가져오기
    @GetMapping( "/report/{reportId}")
    public ResponseDto<?> getPost(@PathVariable Long reportId) {
        return reportService.getReport(reportId);
    }


    // 게시글 수정
    @PutMapping( "/report/{reportId}")
    public ResponseDto<?> updatePost(@PathVariable Long reportId,
                                     @RequestPart(value = "data") ReportRequestDto requestDto,
                                     @RequestPart("imageUrl") List<MultipartFile> multipartFiles,
                                     HttpServletRequest request) {

        if (multipartFiles == null) {
            throw new NullPointerException("사진을 업로드해주세요");
        }
        List<String> imgPaths = s3Service.upload(multipartFiles);
        return reportService.updateReport(reportId, requestDto, request, imgPaths);
    }

    //게시글 삭제
    @DeleteMapping( "/report/{reportId}")
    public ResponseDto<?> deleteReport(@PathVariable Long reportId,
                                     HttpServletRequest request) {
        return reportService.deleteReport(reportId, request);
    }
}
