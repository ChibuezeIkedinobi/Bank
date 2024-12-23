package com.ikedi.world_banking_app_v1.service;

import com.ikedi.world_banking_app_v1.payload.request.CreditAndDebitRequest;
import com.ikedi.world_banking_app_v1.payload.request.TransferRequest;
import com.ikedi.world_banking_app_v1.payload.response.BankResponse;
import com.ikedi.world_banking_app_v1.payload.request.EnquiryRequest;

import java.time.LocalDate;

public interface UserService {
    BankResponse balanceEnquiry(EnquiryRequest enquiryRequest);

    BankResponse creditAccount(CreditAndDebitRequest request);

    BankResponse debitAccount(CreditAndDebitRequest request);

    BankResponse transfer(TransferRequest request);

    BankResponse statementEnquiry(EnquiryRequest request, LocalDate startDate, LocalDate endDate);
}
