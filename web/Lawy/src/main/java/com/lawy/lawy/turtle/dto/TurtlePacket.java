package com.lawy.lawy.turtle.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TurtlePacket {
    public enum Type { REGISTER_CLIENT, REQUEST_TASK, RESPONSE_TASK }

    private Type type;
    private String messageId;
    private String data;
}
