package com.lawy.lawy.turtle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Packet {
    public enum Type { REGISTER_CLIENT, REQUEST_TASK, RESPONSE_TASK }

    private Type type;
    private String data;
    private String messageId;
}
