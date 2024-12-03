package com.ikedi.world_banking_app_v1.service.impl;

import com.ikedi.world_banking_app_v1.domain.entity.UserEntity;
import com.ikedi.world_banking_app_v1.domain.enums.Role;
import com.ikedi.world_banking_app_v1.payload.request.UserRequest;
import com.ikedi.world_banking_app_v1.payload.response.AccountInfo;
import com.ikedi.world_banking_app_v1.payload.response.BankResponse;
import com.ikedi.world_banking_app_v1.repository.UserEntityRepository;
import com.ikedi.world_banking_app_v1.service.AuthService;
import com.ikedi.world_banking_app_v1.utils.AccountUtils;
import com.ikedi.world_banking_app_v1.utils.mail.EmailDetails;
import com.ikedi.world_banking_app_v1.utils.mail.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserEntityRepository userEntityRepository;

    private final EmailService emailService;
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
                .accountNumber(AccountUtils.generateAccountNumber(userEntityRepository))
                .accountBalance(BigDecimal.ZERO)
                .email(userRequest.getEmail())
                .password(userRequest.getPassword())
                .status("ACTIVE")
                .profilePicture(null)
                .phoneNumber(userRequest.getPhoneNumber())
                .role(Role.USER)
                .build();

        UserEntity  savedUser = userEntityRepository.save(newUser);

        EmailDetails emailDetails = EmailDetails.builder()
                .subject("Welcome to Kaki Bank - Account Created Successfully!")
                .recipient(savedUser.getEmail())
                .messageBody("Dear " + savedUser.getFirstName() + " " + savedUser.getLastName() + ",\n\n" +
                        "Congratulations! Your bank account has been successfully created.\n\n" +
                        "Account Details:\n" +
                        "Account Name: " + savedUser.getFirstName() + " " + savedUser.getLastName() + " " + savedUser.getOtherName() + "\n" +
                        "Account Number: " + savedUser.getAccountNumber() + "\n" +
                        "Account Balance: NGN " + savedUser.getAccountBalance() + "\n\n" +
                        "Thank you for choosing World Banking App. We are excited to serve you.\n" +
                        "Best regards,\n" +
                        "Kaki Banking App Team")
                .build();
        emailService.sendEmailAlert(emailDetails);

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


















