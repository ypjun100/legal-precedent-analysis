package com.lawy.lawy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientPacket {
    public enum Type { REQUEST_TASK, RESPONSE_TASK, PING }

    private Type type;
    private String requestData;
    private Precedent responseData;
}
