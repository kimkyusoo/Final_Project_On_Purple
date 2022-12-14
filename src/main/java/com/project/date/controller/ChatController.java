package com.project.date.controller;

import com.project.date.dto.request.ChatMessageDto;
import com.project.date.dto.request.Message;
import com.project.date.model.ChatMessage;
import com.project.date.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
//@RequestMapping("/chat")
public class ChatController {

    private final SimpMessagingTemplate template;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * websocket "/pub/chat/enter"로 들어오는 메시징을 처리한다.
     * 채팅방에 입장했을 경우
     * Client가 send할 수 있는 경로 /pub/chat/enter
     */
    //, @Header("Authorization") String token
    @MessageMapping(value = "/chat/enter")
    public void enter(Message message) {

        if(ChatMessageDto.MessageType.JOIN.equals(message.getType())) {
            message.setMessage(message.getSender()+"님이 입장하셨습니다.");
        }
//
//        chatMessageRepository.save(chatMessageDto);
        template.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);   // roomId를 topic으로 생성-> roomId로 구분, 메시지 전달

//        String nickname = tokenProvider.decodeUsername(token);
//        User user = userRepository.findByNickname(nickname).orElseThrow(
//                () -> new NotFoundException("해당 유저를 찾을 수 없습니다.")
//        );
//        chatService.enter(user.getId(), chatMessageDto.getRoomId());
    }


    /**
     * websocket "/pub/chat/message"로 들어오는 메시징을 처리한다.
     */
    //, @Header("Authorization") String token
    @MessageMapping(value = "/chat/message") //메시지 보내는거야
    public void message(Message message) {

        template.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);

//        String username = jwtDecoder.decodeUsername(token);
//        String nickname = tokenProvider.decodeUsername(token);
//
//        User user = userRepository.findByNickname(nickname).orElseThrow(
//                () -> new IllegalArgumentException("존재하지 않는 사용자입니다.")
//        );
//        chatService.sendMessage(chatMessageDto, user);
//        chatService.updateUnReadMessageCount(chatMessageDto);

    }
}


//    private final TokenProvider TokenProvider;
//    private final ChatRoomRepository chatRoomRepository;
//    private final ChatService chatService;
//    private final JwtDecoder jwtDecoder;
//    private final UserRepository userRepository;
//
//    @MessageMapping("/chat/enter")
//    public void enter(ChatMessage chatMessageDto, @Header("token") String token) {
//        String nickname = jwtDecoder.decodeUsername(token);
//        User user = userRepository.findByNickname(nickname).orElseThrow(
//                () -> new NotFoundException("해당 유저를 찾을 수 없습니다.")
//        );
//        chatService.enter(user.getId(), chatMessageDto.getRoomId());
//    }
//
//    /**
//     * websocket "/pub/chat/message"로 들어오는 메시징을 처리한다.
//     */
//    @MessageMapping("/chat/message")
//    public void message(ChatMessage message, @Header("token") String token) {
//        String nickname = String.valueOf(TokenProvider.validateToken(token));
////        String nickname = TokenProvider.getUserNameFromJwt(token);
//
//        // 로그인 회원 정보로 대화명 설정
//        message.setSender(nickname);
//
//        // 채팅방 인원수 세팅
//        message.setUserCount(chatRoomRepository.getUserCount(message.getRoomId()));
//
//        // Websocket에 발행된 메시지를 redis로 발행(publish)
//        chatService.sendChatMessage(message);
//    }