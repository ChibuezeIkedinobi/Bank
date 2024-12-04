package com.ikedi.world_banking_app_v1.service.impl;

import com.ikedi.world_banking_app_v1.domain.entity.UserEntity;
import com.ikedi.world_banking_app_v1.payload.request.CreditAndDebitRequest;
import com.ikedi.world_banking_app_v1.payload.request.EnquiryRequest;
import com.ikedi.world_banking_app_v1.payload.request.TransferRequest;
import com.ikedi.world_banking_app_v1.payload.response.AccountInfo;
import com.ikedi.world_banking_app_v1.payload.response.BankResponse;
import com.ikedi.world_banking_app_v1.repository.UserEntityRepository;
import com.ikedi.world_banking_app_v1.service.UserService;
import com.ikedi.world_banking_app_v1.utils.AccountUtils;
import com.ikedi.world_banking_app_v1.utils.mail.EmailDetails;
import com.ikedi.world_banking_app_v1.utils.mail.EmailService;
import com.ikedi.world_banking_app_v1.utils.sms.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserEntityRepository userEntityRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    @Override
    public BankResponse balanceEnquiry(EnquiryRequest request) {
        boolean isAccountExisting = userEntityRepository.existsByAccountNumber(request.getAccountNumber());

        if (!isAccountExisting) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NUMBER_NON_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NUMBER_NON_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();
        }

        UserEntity foundUser = userEntityRepository.findByAccountNumber(request.getAccountNumber());

        EmailDetails emailDetails = EmailDetails.builder()
                .subject("Balance Enquiry")
                .recipient(foundUser.getEmail())
                .messageBody(String.format("""
                                Dear %s,

                                Thank you for using our banking services.

                                Here are the details of your account balance enquiry:

                                Account Name: %s
                                Account Number: %s
                                Available Balance: %s

                                If you have any further questions or need assistance, please do not hesitate to contact our customer service team.

                                Best regards,
                                Kaki Bank
                                Customer Service Team""",
                        foundUser.getFirstName() + " " + foundUser.getLastName(),
                        foundUser.getFirstName() + " " + foundUser.getLastName() + " " + foundUser.getOtherName(),
                        request.getAccountNumber(),
                        foundUser.getAccountBalance()))
                .build();
        emailService.sendEmailAlert(emailDetails);


        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_NUMBER_FOUND_CODE)
                .responseMessage(AccountUtils.ACCOUNT_NUMBER_FOUND_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountName(foundUser.getFirstName()+" "+ foundUser.getLastName()+" "+foundUser.getOtherName())
                        .accountNumber(request.getAccountNumber())
                        .accountBalance(foundUser.getAccountBalance())
                        .build())
                .build();
    }

    @Override
    public BankResponse creditAccount(CreditAndDebitRequest request) {

        boolean isAccountExisting = userEntityRepository.existsByAccountNumber(request.getAccountNumber());

        if (!isAccountExisting) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NUMBER_NON_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NUMBER_NON_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();
        }

        UserEntity userToCredit = userEntityRepository.findByAccountNumber(request.getAccountNumber());
        userToCredit.setAccountBalance(userToCredit.getAccountBalance().add(request.getAmount()));
        userEntityRepository.save(userToCredit);

        EmailDetails emailDetails = EmailDetails.builder()
                .subject("Credit Alert: Transaction Successful")
                .recipient(userToCredit.getEmail())
                .messageBody(String.format(
                        """
                                Dear %s,

                                Your account has been credited successfully.

                                Details of the transaction:
                                Account Name: %s %s %s
                                Account Number: %s
                                Credited Amount: %s
                                New Balance: %s

                                Thank you for banking with us.

                                Best regards,
                                Kaki Bank""",
                        userToCredit.getFirstName(), userToCredit.getFirstName(), userToCredit.getLastName(), userToCredit.getOtherName(),
                        userToCredit.getAccountNumber(), request.getAmount().toPlainString(),
                        userToCredit.getAccountBalance().toPlainString()))
                .build();
        emailService.sendEmailAlert(emailDetails);

        String smsMessage = String.format(
                "Dear %s, your account has been credited with %s. New balance: %s.",
                userToCredit.getFirstName(), request.getAmount().toPlainString(),
                userToCredit.getAccountBalance().toPlainString()
        );
        smsService.sendSms(userToCredit.getPhoneNumber(), smsMessage);

        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_CREDITED_SUCCESS_CODE)
                .responseMessage(AccountUtils.ACCOUNT_CREDITED_SUCCESS_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountName(userToCredit.getFirstName()+" "+ userToCredit.getLastName()+" "+userToCredit.getOtherName())
                        .accountNumber(request.getAccountNumber())
                        .accountBalance(userToCredit.getAccountBalance())
                        .build())
                .build();

    }

    @Override
    public BankResponse debitAccount(CreditAndDebitRequest request) {

        boolean isAccountExisting = userEntityRepository.existsByAccountNumber(request.getAccountNumber());

        if (!isAccountExisting) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NUMBER_NON_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NUMBER_NON_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();
        }

        UserEntity userToDebit = userEntityRepository.findByAccountNumber(request.getAccountNumber());
        BigInteger availableBalance = userToDebit.getAccountBalance().toBigInteger();
        BigInteger debitAmount = request.getAmount().toBigInteger();

        if (availableBalance.intValue() < debitAmount.intValue()) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                    .responseMessage(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                    .accountInfo(null)
                    .build();
        } else {
            userToDebit.setAccountBalance(userToDebit.getAccountBalance().subtract(request.getAmount()));

            userEntityRepository.save(userToDebit);

            EmailDetails debitAlert = EmailDetails.builder()
                    .subject("Debit Alert: Transaction Successful")
                    .recipient(userToDebit.getEmail())
                    .messageBody(String.format(
                            """
                                    Dear %s,
    
                                    Your account has been debited successfully.
    
                                    Details of the transaction:
                                    Account Name: %s %s %s
                                    Account Number: %s
                                    Debited Amount: %s
                                    Remaining Balance: %s
    
                                    Thank you for banking with us.
    
                                    Best regards,
                                    Kaki Bank""",
                            userToDebit.getFirstName(), userToDebit.getFirstName(), userToDebit.getLastName(), userToDebit.getOtherName(),
                            userToDebit.getAccountNumber(), request.getAmount().toPlainString(),
                            userToDebit.getAccountBalance().toPlainString()))
                    .build();
            emailService.sendEmailAlert(debitAlert);

            String smsMessage = String.format(
                    "Dear %s, your account has been debited with %s. Remaining balance: %s.",
                    userToDebit.getFirstName(), request.getAmount().toPlainString(),
                    userToDebit.getAccountBalance().toPlainString()
            );
            smsService.sendSms(userToDebit.getPhoneNumber(), smsMessage);

            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_DEBITED_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_DEBITED_MESSAGE)
                    .accountInfo(AccountInfo.builder()
                            .accountName(userToDebit.getFirstName() + " " + userToDebit.getLastName() + " " + userToDebit.getOtherName())
                            .accountNumber(request.getAccountNumber())
                            .accountBalance(userToDebit.getAccountBalance())
                            .build())
                    .build();
        }
    }

    @Override
    public BankResponse transfer(TransferRequest request) {

        boolean isDestinationAccountExists = userEntityRepository.existsByAccountNumber(request.getSourceAccountNumber());

        if (!isDestinationAccountExists) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NUMBER_NON_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NUMBER_NON_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();
        }

        UserEntity sourceAccountUser = userEntityRepository.findByAccountNumber(request.getSourceAccountNumber());
        UserEntity destinationAccountUser = userEntityRepository.findByAccountNumber(request.getDestinationAccountNumber());


        if (request.getAmount().compareTo(sourceAccountUser.getAccountBalance()) > 0) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                    .responseMessage(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                    .accountInfo(null)
                    .build();
        }


        sourceAccountUser.setAccountBalance(sourceAccountUser.getAccountBalance().subtract(request.getAmount()));  // debit
        userEntityRepository.save(sourceAccountUser);

        destinationAccountUser.setAccountBalance(destinationAccountUser.getAccountBalance().add(request.getAmount()));  // credit
        userEntityRepository.save(destinationAccountUser);


        EmailDetails senderEmail = EmailDetails.builder()
                .subject("Debit Alert: Transfer Successful")
                .recipient(sourceAccountUser.getEmail())
                .messageBody(String.format(
                        """
                                Dear %s,
    
                                Your account has been debited successfully for a transfer.
    
                                Details of the transaction:
                                Sender Name: %s %s %s
                                Account Number: %s
                                Transferred Amount: %s
                                Remaining Balance: %s
                                Recipient Name: %s %s %s
                                Recipient Account Number: %s
    
                                Thank you for banking with us.
    
                                Best regards,
                                Kaki Bank""",
                        sourceAccountUser.getFirstName(),
                        sourceAccountUser.getFirstName(), sourceAccountUser.getLastName(), sourceAccountUser.getOtherName(),
                        sourceAccountUser.getAccountNumber(),
                        request.getAmount().toPlainString(),
                        sourceAccountUser.getAccountBalance().toPlainString(),
                        destinationAccountUser.getFirstName(), destinationAccountUser.getLastName(), destinationAccountUser.getOtherName(),
                        destinationAccountUser.getAccountNumber()))
                .build();
        emailService.sendEmailAlert(senderEmail);

        EmailDetails recipientEmail = EmailDetails.builder()
                .subject("Credit Alert: Transfer Received")
                .recipient(destinationAccountUser.getEmail())
                .messageBody(String.format(
                        """
                                Dear %s,
    
                                Your account has been credited successfully.
    
                                Details of the transaction:
                                Recipient Name: %s %s %s
                                Account Number: %s
                                Credited Amount: %s
                                New Balance: %s
                                Sender Name: %s %s %s
                                Sender Account Number: %s
    
                                Thank you for banking with us.
    
                                Best regards,
                                Kaki Bank""",
                        destinationAccountUser.getFirstName(),
                        destinationAccountUser.getFirstName(), destinationAccountUser.getLastName(), destinationAccountUser.getOtherName(),
                        destinationAccountUser.getAccountNumber(),
                        request.getAmount().toPlainString(),
                        destinationAccountUser.getAccountBalance().toPlainString(),
                        sourceAccountUser.getFirstName(), sourceAccountUser.getLastName(), sourceAccountUser.getOtherName(),
                        sourceAccountUser.getAccountNumber()))
                .build();
        emailService.sendEmailAlert(recipientEmail);

        return BankResponse.builder()
                .responseCode(AccountUtils.TRANSFER_SUCCESSFUL_CODE)
                .responseMessage(AccountUtils.TRANSFER_SUCCESSFUL_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountName(sourceAccountUser.getFirstName() + " " + sourceAccountUser.getLastName() + " " + sourceAccountUser.getOtherName())
                        .accountNumber(request.getSourceAccountNumber())
                        .accountBalance(sourceAccountUser.getAccountBalance())
                        .build())
                .build();
    }

    @Override
    public BankResponse nameEnquiry(EnquiryRequest request) {
        return null;
    }
}
