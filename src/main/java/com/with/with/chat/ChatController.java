package com.with.with.chat;

import com.with.with.member.CustomUser;
import com.with.with.post.Post;
import com.with.with.post.PostRepository;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Controller
public class ChatController {

    private final SimpMessagingTemplate template;
    private Map<String, Set<String>> participants = new ConcurrentHashMap<>();
    private  final PostRepository postRepository;

    public ChatController(SimpMessagingTemplate template, PostRepository postRepository) {
        this.template = template;
        this.postRepository = postRepository;
    }

    @GetMapping("/chat/{postId}")
    public String getChatPage(@PathVariable String postId, Model model, HttpSession session, HttpServletResponse response) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("roomId", postId);
        if (auth != null && auth.isAuthenticated()) {
            CustomUser user = (CustomUser) auth.getPrincipal();
            model.addAttribute("username", user.getUsername());
            session.setAttribute("roomId", postId);

            // DB에서 게시물 정보 조회
            Post post = postRepository.findById(Long.parseLong(postId)).orElse(null);
            if (post == null) {
                response.sendRedirect("/404"); // 게시물이 존재하지 않는 경우
                return null;
            }

            model.addAttribute("writer", post.getWriter());

            int maxParticipants = post.getPersonnel();
            ParticipantInfo participantInfo = getCurrentParticipants(postId);

            if (participantInfo.getCount() >= maxParticipants) {
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.println("<script>alert('인원이 가득 찼습니다.'); history.go(-1);</script>");
                out.flush();
                return null;
            }

            joinRoom(postId, user.getUsername());
            return "chat.html";
        }
        return "redirect:/login";
    }


    private ParticipantInfo getCurrentParticipants(String roomId) {
        Set<String> currentParticipants = participants.getOrDefault(roomId, new HashSet<>());
        return new ParticipantInfo(currentParticipants.size(), currentParticipants);
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
        String username = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : null;
        String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");

        if (roomId == null || username == null) {
            System.out.println("Disconnection event ignored: Missing roomId or username");
            return;  // roomId 또는 username이 null인 경우 메소드 실행 중단
        }

        Set<String> roomParticipants = participants.getOrDefault(roomId, new HashSet<>());
        if (roomParticipants.contains(username)) {
            leaveRoom(roomId, username);
            System.out.println(username + "님이 채팅에서 나갔습니다.");
            ChatMessage leaveMessage = new ChatMessage();
            leaveMessage.setSender("System");
            leaveMessage.setContent(username + "님이 채팅에서 나갔습니다.");
            leaveMessage.setType(ChatMessage.MessageType.LEAVE);
            template.convertAndSend("/topic/" + roomId, leaveMessage);
        } else {
            System.out.println("No action needed: User was not in chat or invalid session.");
        }
    }


    @GetMapping("/chat/participants")
    public ResponseEntity<Map<String, Integer>> getAllParticipants() {
        Map<String, Integer> participantCounts = new HashMap<>();
        participants.forEach((roomId, users) -> {
            participantCounts.put(roomId, users.size());
        });
        return ResponseEntity.ok(participantCounts);
    }

    @MessageMapping("/chat.kickUser/{roomId}")
    public void kickUser(@DestinationVariable String roomId, @RequestBody KickRequest kickRequest, SimpMessageHeaderAccessor headerAccessor) {
        String requestingUser = headerAccessor.getUser().getName();
        if (participants.get(roomId).contains(requestingUser) && requestingUser.equals(kickRequest.getAdmin())) {
            leaveRoom(roomId, kickRequest.getUsernameToKick());

            // 강퇴 알림 메시지 전송
            ChatMessage kickMessage = new ChatMessage();
            kickMessage.setSender("System");
            kickMessage.setContent(kickRequest.getUsernameToKick() + "님이 방에서 강퇴되었습니다.");
            kickMessage.setType(ChatMessage.MessageType.LEAVE);
            template.convertAndSend("/topic/" + roomId, kickMessage);

            // 강퇴된 사용자를 기본 페이지로 리다이렉트하라는 메시지 전송
            template.convertAndSendToUser(kickRequest.getUsernameToKick(), "/queue/kick", "redirect:/");

            System.out.println(kickRequest.getUsernameToKick() + " has been kicked out from " + roomId + " by " + requestingUser);
        } else {
            System.out.println("Kick request denied for " + requestingUser + " in " + roomId);
        }
    }
}