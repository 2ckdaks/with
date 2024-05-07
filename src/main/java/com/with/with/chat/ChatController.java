package com.with.with.chat;

import com.with.with.member.CustomUser;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@Controller
public class ChatController {

    private final SimpMessagingTemplate template;
    private final ChatService chatService;

    public ChatController(SimpMessagingTemplate template, ChatService chatService) {
        this.template = template;
        this.chatService = chatService;
    }

    // 채팅 페이지 요청 처리: 인증된 사용자에 대해 채팅 페이지 로드
    @GetMapping("/chat/{postId}")
    public String getChatPage(@PathVariable Long postId, Model model, HttpSession session, HttpServletResponse response) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            model.addAttribute("roomId", postId);
            CustomUser user = (CustomUser) auth.getPrincipal();
            model.addAttribute("username", user.getUsername());
            session.setAttribute("roomId", postId.toString());

            return chatService.handleRoomEntry(postId, user, model, response);
        }
        return "redirect:/login";
    }

    // 채팅 메시지 전송 처리: 인증된 사용자가 보낸 메시지를 채팅방에 전송
    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage sendMessage(@DestinationVariable String roomId, ChatMessage chatMessage, Authentication authentication) {
        return chatService.processMessage(roomId, chatMessage, authentication);
    }

    // 새로운 사용자 추가 처리: 인증된 사용자를 채팅방에 추가하고 알림 메시지 전송
    @MessageMapping("/chat.addUser/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage addUser(@DestinationVariable String roomId, Authentication authentication) {
        return chatService.addUserToRoom(roomId, authentication);
    }

    // 사용자 연결 해제 처리: 사용자의 연결 해제 요청을 처리
    @MessageMapping("/app/disconnect")
    public void handleDisconnect(@RequestBody DisconnectRequest disconnectRequest) {
        chatService.handleDisconnect(disconnectRequest);
    }

    // 웹소켓 연결 해제 이벤트 처리: 사용자의 웹소켓 연결이 해제될 때 처리
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        chatService.processDisconnect(event);
    }

    // 모든 참가자 수 조회: 현재 채팅방의 모든 참가자 수를 조회하여 반환
    @GetMapping("/chat/participants")
    public ResponseEntity<Map<String, Integer>> getAllParticipants() {
        return ResponseEntity.ok(chatService.getParticipantCounts());
    }

    // 사용자 강퇴 처리: 특정 사용자를 채팅방에서 강퇴
    @MessageMapping("/chat.kickUser/{roomId}")
    public void kickUser(@DestinationVariable String roomId, @RequestBody KickRequest kickRequest, SimpMessageHeaderAccessor headerAccessor) {
        chatService.handleKickUser(roomId, kickRequest, headerAccessor);
    }
}
