package com.project.date.model;

import com.project.date.dto.request.PostRequestDto;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Post extends Timestamped {

  @Id
  @Column(name = "postId")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  //게시글제목
  @Column(nullable = false)
  private String title;
  //게시글내용
  @Column(nullable = false)
  private String content;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Comment> comments;


  @Transient
  @OneToMany(fetch = FetchType.LAZY)
  private final List<Img> imgList = new ArrayList<>();

  @JoinColumn(name = "userId", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private User user;
 //좋아요 count
  @Column(nullable = false)
  private int likes;
  //조회수 count
  @Column(nullable = false)
  private int view;


  public void update(PostRequestDto postRequestDto) {
    this.title = postRequestDto.getTitle();
    this.content = postRequestDto.getContent();
  }

  // 회원정보 검증
  public boolean validateUser(User user) {

    return !this.user.equals(user);
  }
  //좋아요
  public void addLike(){
    this.likes++;
  }
  //좋아요 취소
  public void minusLike(){
    this.likes--;
  }
  //조회수 증가
  public void updateViewCount(){
    this.view++;
  }
}
