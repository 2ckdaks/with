package com.with.with.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;

@Controller
public class ChatController {

    private final SimpMessagingTemplate template;

    // 생성자 주입만 사용
    public ChatController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @GetMapping("/chat/{postId}")
    public String getChatPage(@PathVariable String postId, Model model) {
        model.addAttribute("roomId", postId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            ChatMessage joinMessage = new ChatMessage();
            joinMessage.setType(ChatMessage.MessageType.JOIN);
            joinMessage.setSender(auth.getName());
            joinMessage.setContent(auth.getName() + " 님이 입장하셨습니다.");

            // 메시지를 해당 채팅방의 모든 구독자에게 전송
            template.convertAndSend("/topic/" + postId, joinMessage);
        }
        return "chat.html";
    }


    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage sendMessage(@DestinationVariable String roomId, ChatMessage chatMessage) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        chatMessage.setSender(auth.getName()); // 현재 사용자의 이름을 설정
        System.out.println("Sending message: " + chatMessage.getContent() + " from " + auth.getName());
        return chatMessage;
    }

    @MessageMapping("/chat.addUser/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage addUser(@DestinationVariable String roomId, ChatMessage chatMessage) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        chatMessage.setContent(auth.getName() + " has joined the chat!");
        chatMessage.setType(ChatMessage.MessageType.JOIN);
        chatMessage.setSender(auth.getName());
        System.out.println("User added: " + auth.getName());
        return chatMessage;
    }

}
