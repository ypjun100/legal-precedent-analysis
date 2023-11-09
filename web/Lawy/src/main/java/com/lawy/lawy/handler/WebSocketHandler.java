package com.lawy.lawy.handler;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.lawy.lawy.dto.ClientPacket;
import com.lawy.lawy.turtle.service.TurtleService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
@Log4j2
public class WebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper mapper = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS) // ignore escape character
            .build();

    private final TurtleService turtleService;

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
        ClientPacket packet = mapper.readValue(message.getPayload(), ClientPacket.class);

        log.info(session.getId() + " → " + packet.toString());
        if (packet.getType().equals(ClientPacket.Type.REQUEST_TASK)) {
            turtleService.requestTask(session, packet.getRequestData());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    }
}
