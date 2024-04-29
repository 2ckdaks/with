package com.with.with.chat;

import com.with.with.member.CustomUser;
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

    // 생성자 주입을 사용합니다.
    public ChatController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @GetMapping("/chat/{postId}")
    public String getChatPage(@PathVariable String postId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("roomId", postId);
        if (auth != null && auth.isAuthenticated()) {
            CustomUser user = (CustomUser) auth.getPrincipal();
            ChatMessage joinMessage = new ChatMessage();
            joinMessage.setType(ChatMessage.MessageType.JOIN);
            joinMessage.setSender(user.getUsername());
            joinMessage.setContent(user.getUsername() + " 님이 입장하셨습니다.");

            // 메시지를 해당 채팅방의 모든 구독자에게 전송
            template.convertAndSend("/topic/" + postId, joinMessage);
        }
        return "chat.html";
    }

    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage sendMessage(@DestinationVariable String roomId, ChatMessage chatMessage, Authentication authentication) {
        CustomUser user = (CustomUser) authentication.getPrincipal();
        chatMessage.setSender(user.getUsername()); // 현재 사용자의 이름을 설정
        System.out.println("Sending message: " + chatMessage.getContent() + " from " + user.getUsername());
        return chatMessage;
    }

    @MessageMapping("/chat.addUser/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage addUser(@DestinationVariable String roomId, ChatMessage chatMessage, Authentication authentication) {
        CustomUser user = (CustomUser) authentication.getPrincipal();
        chatMessage.setContent(user.getUsername() + " has joined the chat!");
        chatMessage.setType(ChatMessage.MessageType.JOIN);
        chatMessage.setSender(user.getUsername());
        System.out.println("User added: " + user.getUsername());
        return chatMessage;
    }
}
