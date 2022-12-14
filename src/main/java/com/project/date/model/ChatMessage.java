package com.project.date.model;

import com.project.date.dto.request.ChatMessageDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.listener.ChannelTopic;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class ChatMessage extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // 메세지 작성자
    @ManyToOne
    private User user;
    private String otherNickname;
    private String otherUserId;
    private String otherImageUrl;
    private String roomId;

    // 채팅 메세지 내용
    @Size(max = 1000)
    private String message;

    @ManyToOne
    @JoinColumn(name = "chatroom_id")
    private ChatRoom chatRoom;

    public ChatMessage(User user, ChatMessageDto chatMessageDto, ChatRoom chatRoom) {

        this.user = user;
        this.message = chatMessageDto.getMessage();
        this.chatRoom = chatRoom;
        this.roomId = chatRoom.getChatRoomUuid();
    }

//    public static ChatMessage createMessage(User user, String message){
//        ChatMessage chatMessage = new ChatMessage();
//        chatMessage.user = user.getId();
//        chatMessage.message = message;
//        return chatMessage;
//    }

//    public ChatMessage() {
//    }

//    @Builder
//    public ChatMessage(MessageType type, String roomId, String sender, String message, long userCount) {
//        this.type = type;
//        this.roomId = roomId;
//        this.sender = sender;
//        this.message = message;
//        this.userCount = userCount;
//    }
//
//    // 메시지 타입 : 입장, 퇴장, 채팅
//    public enum MessageType {
//        ENTER, TALK, QUIT
//    }
//
//    private MessageType type; // 메시지 타입
//    private String roomId; // 방번호
//    private String sender; // 메시지 보낸사람
//    private String message; // 메시지
//    private long userCount; // 채팅방 인원수, 채팅방 내에서 메시지가 전달될때 인원수 갱신시 사용
}