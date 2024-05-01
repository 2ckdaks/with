package com.with.with.chat;

import com.with.with.member.CustomUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Controller
public class ChatController {

    private final SimpMessagingTemplate template;
    private Map<String, Set<String>> participants = new ConcurrentHashMap<>();

    public ChatController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @GetMapping("/chat/{postId}")
    public String getChatPage(@PathVariable String postId, Model model, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("roomId", postId);
        if (auth != null && auth.isAuthenticated()) {
            CustomUser user = (CustomUser) auth.getPrincipal();
            model.addAttribute("username", user.getUsername());
            session.setAttribute("roomId", postId); // HttpSession에 roomId 저장
            joinRoom(postId, user.getUsername());
        }
        return "chat.html";
    }


    private void joinRoom(String roomId, String username) {
        participants.computeIfAbsent(roomId, k -> new HashSet<>()).add(username);
        sendParticipantUpdate(roomId);
    }


    private void leaveRoom(String roomId, String username) {
        participants.getOrDefault(roomId, new HashSet<>()).remove(username);
        sendParticipantUpdate(roomId);
    }

    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage sendMessage(@DestinationVariable String roomId, ChatMessage chatMessage, Authentication authentication) {
        CustomUser user = (CustomUser) authentication.getPrincipal();
        chatMessage.setSender(user.getUsername());
        return chatMessage;
    }

    @MessageMapping("/chat.addUser/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage addUser(@DestinationVariable String roomId, Authentication authentication) {
        CustomUser user = (CustomUser) authentication.getPrincipal();
        String username = user.getUsername();
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(username + "님이 채팅에 참여했습니다.");
        chatMessage.setType(ChatMessage.MessageType.JOIN);
        chatMessage.setSender("System");
        joinRoom(roomId, username);
        System.out.println(username + "님이 채팅에 참여했습니다.");
        sendParticipantUpdate(roomId);  // 참가자 정보 업데이트
        return chatMessage;  // 채팅 메시지 반환
    }


    @MessageMapping("/app/disconnect")
    public void handleDisconnect(@RequestBody DisconnectRequest disconnectRequest) {
        String roomId = disconnectRequest.getRoomId();
        String username = disconnectRequest.getUsername();
        leaveRoom(roomId, username);
        System.out.println(username + "님이 방을 나갔습니다.");
    }


    private ParticipantInfo sendParticipantUpdate(String roomId) {
        Set<String> currentParticipants = participants.getOrDefault(roomId, new HashSet<>());
        ParticipantInfo info = new ParticipantInfo(currentParticipants.size(), currentParticipants);
        template.convertAndSend("/topic/participants/" + roomId, info);
        return info;
    }

    @MessageMapping("/chat.getParticipants/{roomId}")
    public void getParticipants(@DestinationVariable String roomId, SimpMessageHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        Set<String> currentParticipants = participants.getOrDefault(roomId, new HashSet<>());
        ParticipantInfo info = new ParticipantInfo(currentParticipants.size(), currentParticipants);
        template.convertAndSendToUser(sessionId, "/topic/participants/" + roomId, info);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String username = headerAccessor.getUser().getName();
        String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");

        if (roomId != null && username != null) {
            leaveRoom(roomId, username);
            System.out.println(username + "님이 채팅에서 나갔습니다.");
        } else {
            System.out.println("Error: Room ID or Username not found in session.");
        }
    }
}
