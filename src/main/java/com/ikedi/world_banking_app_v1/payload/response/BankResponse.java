package com.ikedi.world_banking_app_v1.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BankResponse <T> {

    private String responseCode;
    private String responseMessage;
    private AccountInfo accountInfo;

    public BankResponse(String responseMessage) {
        this.responseMessage = responseMessage;
    }
}
