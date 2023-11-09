package com.lawy.lawy.turtle.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lawy.lawy.turtle.dto.TurtlePacket;
import com.lawy.lawy.turtle.service.TurtleService;
import jakarta.annotation.PreDestroy;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@Log4j2
public class TurtleHandler extends TextWebSocketHandler {
    private final WebSocketConnectionManager connectionManager;
    private final TurtleService service;
    private final ObjectMapper mapper;

    public TurtleHandler(ObjectMapper mapper, TurtleService service, @Value("${turtle.url}") String url) {
        this.connectionManager =  new WebSocketConnectionManager(new StandardWebSocketClient(),
                this, url); // URL 접속
        this.service = service;
        this.mapper = mapper;

        connectionManager.start(); // 소켓 통신 시작
    }

    @PreDestroy
    public void destroy() {
        // 서버가 종료되기 전 소켓 연결을 종료시킴
        connectionManager.stop();
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        service.onEstablished(session);
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) {
        log.error(exception);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status){
        log.warn("Connection closed");
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
        TurtlePacket response = mapper.readValue(message.getPayload(), TurtlePacket.class);

        log.info("TurtleMQ → " + response.toString());

        if (response.getType().equals(TurtlePacket.Type.REGISTER_CLIENT)) {
            service.onRegistered();
        } else if (response.getType().equals(TurtlePacket.Type.RESPONSE_TASK)) {
            service.responseTask(response);
        }
    }
}
