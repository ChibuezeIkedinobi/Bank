package com.ikedi.world_banking_app_v1.service.impl;

import com.ikedi.world_banking_app_v1.domain.entity.UserEntity;
import com.ikedi.world_banking_app_v1.domain.enums.Role;
import com.ikedi.world_banking_app_v1.payload.request.UserRequest;
import com.ikedi.world_banking_app_v1.payload.response.AccountInfo;
import com.ikedi.world_banking_app_v1.payload.response.BankResponse;
import com.ikedi.world_banking_app_v1.repository.UserEntityRepository;
import com.ikedi.world_banking_app_v1.service.AuthService;
import com.ikedi.world_banking_app_v1.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserEntityRepository userEntityRepository;
    @Override
    public BankResponse registerUser(UserRequest userRequest) {

        if (userEntityRepository.existsByEmail(userRequest.getEmail())) {
            BankResponse response = BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_EXISTS_CODE)
                    .accountInfo(null)
                    .build();

            return response;
        }
        UserEntity newUser = UserEntity.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .otherName(userRequest.getOtherName())
                .gender(userRequest.getGender())
                .address(userRequest.getAddress())
                .accountNumber(AccountUtils.generateAccountNumber())
                .accountBalance(BigDecimal.ZERO)
                .email(userRequest.getEmail())
                .password(userRequest.getPassword())
                .status("ACTIVE")
                .profilePicture(null)
                .phoneNumber(userRequest.getPhoneNumber())
                .role(Role.USER)
                .build();

        UserEntity  savedUser = userEntityRepository.save(newUser);

        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_CREATION_SUCCESSFUL)
                .responseMessage(AccountUtils.ACCOUNT_CREATION_SUCCESS_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountBalance(savedUser.getAccountBalance())
                        .accountNumber(savedUser.getAccountNumber())
                        .accountName(savedUser.getFirstName()+" "+savedUser.getLastName()+" "+savedUser.getOtherName())
                        .build())
                .build();
    }




}


















