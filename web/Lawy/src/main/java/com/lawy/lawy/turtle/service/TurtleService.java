package com.lawy.lawy.turtle.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lawy.lawy.dto.ClientPacket;
import com.lawy.lawy.dto.Precedent;
import com.lawy.lawy.turtle.dto.TurtlePacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Log4j2
@RequiredArgsConstructor
public class TurtleService {
    private WebSocketSession session;
    private final ObjectMapper mapper;
    private boolean isAvailable = false; // 통신 가능 여부
    private final Map<String, WebSocketSession> waitingClients = new HashMap<>(); // session_id: ws_session

    public void onEstablished(WebSocketSession session) {
        this.session = session; // TurtleMQ WS 세션 저장

        // WS Handshake 이후 TurtleMQ 클라이언트로 등록 요청을 보냄
        TurtlePacket packet = TurtlePacket.builder()
                .type(TurtlePacket.Type.REGISTER_CLIENT).build();

        sendToTurtle(packet);
    }

    public void onRegistered() {
        isAvailable = true;
    }

    public void requestTask(WebSocketSession session, String data) {
        // 통신이 가능하고, 같은 사용자가 보낸 이전 요청(처리중인 요청이 있는경우)이 없는경우 작업 요청
        if (isAvailable && !waitingClients.containsKey(session.getId())) {
            TurtlePacket packet = TurtlePacket.builder()
                    .type(TurtlePacket.Type.REQUEST_TASK)
                    .messageId(session.getId())
                    .data(data)
                    .build();
            sendToTurtle(packet);

            waitingClients.put(session.getId(), session);
        }
    }

    public void responseTask(TurtlePacket packet) {
        if (waitingClients.containsKey(packet.getMessageId())) {
            try {
                ClientPacket clientPacket = ClientPacket.builder()
                        .type(ClientPacket.Type.RESPONSE_TASK)
                        .responseData(mapper.readValue(packet.getData(), Precedent.class))
                        .build();

                sendToClient(waitingClients.get(packet.getMessageId()), clientPacket);
            } catch (IOException e) {
                log.error(e);
            }

            waitingClients.remove(packet.getMessageId());
        }
    }

    private void sendToTurtle(TurtlePacket packet) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(mapper.writeValueAsString(packet)));
                log.info("TurtleMQ ← " + packet.toString());
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void sendToClient(WebSocketSession session, ClientPacket data) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(mapper.writeValueAsString(data)));
                log.info(session.getId() + " ← " + data);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
