package com.lawy.lawy.turtle;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
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
    private final ObjectMapper mapper;
    private final WebSocketConnectionManager connectionManager;

    public TurtleHandler(ObjectMapper mapper) {
        this.mapper = mapper;
        this.connectionManager =  new WebSocketConnectionManager(new StandardWebSocketClient(),
                this, "ws://localhost:8081/turtle");;
        connectionManager.start();
    }

    @PreDestroy
    public void destroy() {
        connectionManager.stop();
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.debug(mapper.readValue(message.getPayload(), Packet.class));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Registered as client.");

        Packet packet = Packet.builder()
                .type(Packet.Type.REGISTER_CLIENT)
                .build();
        session.sendMessage(new TextMessage(mapper.writeValueAsString(packet)));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error(exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status){
        log.warn("Connection closed");
    }
}
