package com.ikedi.world_banking_app_v1.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountInfo {

    private String accountName;
    private BigDecimal accountBalance;
    private String accountNumber;
}
