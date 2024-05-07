package com.with.with.chat;

import com.with.with.member.CustomUser;
import com.with.with.post.Post;
import com.with.with.post.PostRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatService {
    private final PostRepository postRepository;
    private final SimpMessagingTemplate template;
    private Map<String, Set<String>> participants = new ConcurrentHashMap<>();

    public ChatService(PostRepository postRepository, SimpMessagingTemplate template) {
        this.postRepository = postRepository;
        this.template = template;
    }

    // 채팅방 진입 처리: 방 존재 확인 및 인원 검사 후 입장
    public String handleRoomEntry(Long postId, CustomUser user, Model model, HttpServletResponse response) throws IOException {
        Post post = validateAndGetPost(postId);
        model.addAttribute("writer", post.getWriter());
        return checkRoomCapacityAndJoin(postId.toString(), user, response);
    }

    // 게시물 존재 유무 확인
    public Post validateAndGetPost(Long postId) throws IOException {
        return postRepository.findById(postId).orElseThrow(() -> new IOException("Post not found"));
    }

    // 채팅방에 사용자 추가
    public void joinRoom(String roomId, String username) {
        participants.computeIfAbsent(roomId, k -> new HashSet<>()).add(username);
        sendParticipantUpdate(roomId);
    }

    // 채팅방에서 사용자 제거
    public void leaveRoom(String roomId, String username) {
        participants.getOrDefault(roomId, new HashSet<>()).remove(username);
        sendParticipantUpdate(roomId);
    }

    // 채팅방 참가자 정보 업데이트 메시지 전송
    private void sendParticipantUpdate(String roomId) {
        Set<String> currentParticipants = participants.getOrDefault(roomId, new HashSet<>());
        ParticipantInfo info = new ParticipantInfo(currentParticipants.size(), currentParticipants);
        template.convertAndSend("/topic/participants/" + roomId, info);
    }

    // 현재 채팅방 참가자 정보 조회
    public ParticipantInfo getCurrentParticipants(String roomId) {
        Set<String> currentParticipants = participants.getOrDefault(roomId, new HashSet<>());
        return new ParticipantInfo(currentParticipants.size(), currentParticipants);
    }

    // 방 인원수 초과 시 입장 제한, 아닐 경우 입장 처리
    public String checkRoomCapacityAndJoin(String roomId, CustomUser user, HttpServletResponse response) throws IOException {
        Set<String> currentParticipants = participants.getOrDefault(roomId, new HashSet<>());
        Post post = postRepository.findById(Long.valueOf(roomId)).orElseThrow(() -> new IOException("Post not found"));
        if (currentParticipants.size() >= post.getPersonnel()) {
            response.setContentType("text/html; charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<script>alert('인원이 가득 찼습니다.'); history.go(-1);</script>");
            out.flush();
            return null;
        } else {
            joinRoom(roomId, user.getUsername());
            return "chat.html";
        }
    }

    // 채팅 메시지 처리
    public ChatMessage processMessage(String roomId, ChatMessage chatMessage, Authentication authentication) {
        CustomUser user = (CustomUser) authentication.getPrincipal();
        chatMessage.setSender(user.getUsername());
        return chatMessage;
    }

    // 새 사용자 채팅방 추가 알림
    public ChatMessage addUserToRoom(String roomId, Authentication authentication) {
        CustomUser user = (CustomUser) authentication.getPrincipal();
        String username = user.getUsername();
        joinRoom(roomId, username);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(username + "님이 채팅에 참여했습니다.");
        chatMessage.setType(ChatMessage.MessageType.JOIN);
        chatMessage.setSender("System");
        sendParticipantUpdate(roomId);
        return chatMessage;
    }

    // 사용자 연결 해제 처리
    public void handleDisconnect(DisconnectRequest disconnectRequest) {
        String roomId = disconnectRequest.getRoomId();
        String username = disconnectRequest.getUsername();
        leaveRoom(roomId, username);
    }

    // 웹소켓 연결 해제 이벤트 처리
    public void processDisconnect(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String username = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : null;
        String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");
        if (roomId != null && username != null) {
            leaveRoom(roomId, username);
        }
    }

    // 모든 채팅방의 참가자 수 조회
    public Map<String, Integer> getParticipantCounts() {
        Map<String, Integer> participantCounts = new HashMap<>();
        participants.forEach((id, users) -> participantCounts.put(id, users.size()));
        return participantCounts;
    }

    // 사용자 강퇴 처리
    public void handleKickUser(String roomId, KickRequest kickRequest, SimpMessageHeaderAccessor headerAccessor) {
        String requestingUser = headerAccessor.getUser().getName();
        String usernameToKick = kickRequest.getUsernameToKick();
        if (participants.get(roomId).contains(requestingUser) && requestingUser.equals(kickRequest.getAdmin())) {
            leaveRoom(roomId, usernameToKick);

            // 강퇴 알림 메시지 전송
            ChatMessage kickMessage = new ChatMessage();
            kickMessage.setSender("System");
            kickMessage.setContent(usernameToKick + "님이 방에서 강퇴되었습니다.");
            kickMessage.setType(ChatMessage.MessageType.LEAVE);
            template.convertAndSend("/topic/" + roomId, kickMessage);

            // 강퇴된 사용자를 게시물 상세 페이지로 리다이렉트하라는 메시지 전송
            String redirectUrl = "/detail/" + roomId;  // roomId를 게시물 ID로 사용
            template.convertAndSendToUser(usernameToKick, "/queue/kick", redirectUrl);

            System.out.println(usernameToKick + " has been kicked out from " + roomId + " by " + requestingUser);
        } else {
            System.out.println("Kick request denied for " + requestingUser + " in " + roomId);
        }
    }
}
