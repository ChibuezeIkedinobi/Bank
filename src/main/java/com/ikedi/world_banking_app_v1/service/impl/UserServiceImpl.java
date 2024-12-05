package com.ikedi.world_banking_app_v1.service.impl;

import com.ikedi.world_banking_app_v1.domain.entity.Transaction;
import com.ikedi.world_banking_app_v1.domain.entity.UserEntity;
import com.ikedi.world_banking_app_v1.domain.enums.TransactionType;
import com.ikedi.world_banking_app_v1.payload.request.CreditAndDebitRequest;
import com.ikedi.world_banking_app_v1.payload.request.EnquiryRequest;
import com.ikedi.world_banking_app_v1.payload.request.TransferRequest;
import com.ikedi.world_banking_app_v1.payload.response.AccountInfo;
import com.ikedi.world_banking_app_v1.payload.response.BankResponse;
import com.ikedi.world_banking_app_v1.repository.TransactionRepository;
import com.ikedi.world_banking_app_v1.repository.UserEntityRepository;
import com.ikedi.world_banking_app_v1.service.UserService;
import com.ikedi.world_banking_app_v1.utils.AccountUtils;
import com.ikedi.world_banking_app_v1.utils.DateUtils;
import com.ikedi.world_banking_app_v1.utils.mail.EmailDetails;
import com.ikedi.world_banking_app_v1.utils.mail.EmailService;
import com.ikedi.world_banking_app_v1.utils.sms.SmsService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserEntityRepository userEntityRepository;
    private final TransactionRepository transactionRepository;
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

        Transaction transaction = Transaction.builder()
                .user(userToCredit)
                .transactionType(TransactionType.CREDIT)
                .transactionDate(LocalDateTime.now())
                .amount(request.getAmount())
                .postTransactionBalance(userToCredit.getAccountBalance())
                .description("Account credited successfully.")
                .build();
        transactionRepository.save(transaction);


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
        }
        userToDebit.setAccountBalance(userToDebit.getAccountBalance().subtract(request.getAmount()));
        userEntityRepository.save(userToDebit);

        Transaction transaction = Transaction.builder()
                .user(userToDebit)
                .transactionType(TransactionType.DEBIT)
                .transactionDate(LocalDateTime.now())
                .amount(request.getAmount())
                .postTransactionBalance(userToDebit.getAccountBalance())
                .description("Account debited successfully.")
                .build();
        transactionRepository.save(transaction);

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

    @Override
    public BankResponse transfer(TransferRequest request) {

        UserEntity sourceAccountUser = userEntityRepository.findByAccountNumber(request.getSourceAccountNumber());
        if (sourceAccountUser == null) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NUMBER_NON_EXISTS_CODE)
                    .responseMessage("Source account does not exist.🚨🚨")
                    .accountInfo(null)
                    .build();
        }

        UserEntity destinationAccountUser = userEntityRepository.findByAccountNumber(request.getDestinationAccountNumber());
        if (destinationAccountUser == null) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NUMBER_NON_EXISTS_CODE)
                    .responseMessage("Destination account does not exist.🚨🚨")
                    .accountInfo(null)
                    .build();
        }

        if (sourceAccountUser.getAccountNumber().equals(destinationAccountUser.getAccountNumber())) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ERROR)
                    .responseMessage("Transfers to the same account are not allowed🚨🚨")
                    .accountInfo(null)
                    .build();
        }


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

        Transaction senderTransaction = Transaction.builder()
                .user(sourceAccountUser)
                .transactionType(TransactionType.TRANSFER_DEBIT)
                .amount(request.getAmount())
                .postTransactionBalance(sourceAccountUser.getAccountBalance())
                .description("Transfer to " + destinationAccountUser.getAccountNumber())
                .transactionDate(LocalDateTime.now())
                .build();
        transactionRepository.save(senderTransaction);


        Transaction recipientTransaction = Transaction.builder()
                .user(destinationAccountUser)
                .transactionType(TransactionType.TRANSFER_CREDIT)
                .amount(request.getAmount())
                .postTransactionBalance(destinationAccountUser.getAccountBalance())
                .description("Transfer from " + sourceAccountUser.getAccountNumber())
                .transactionDate(LocalDateTime.now())
                .build();
        transactionRepository.save(recipientTransaction);

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
    public BankResponse statementEnquiry(EnquiryRequest request) {

        boolean isAccountExisting = userEntityRepository.existsByAccountNumber(request.getAccountNumber());

        if (!isAccountExisting) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NUMBER_NON_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NUMBER_NON_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();
        }

        UserEntity user = userEntityRepository.findByAccountNumber(request.getAccountNumber());
        List<Transaction> transactions = transactionRepository.findByUser(user);

        if (transactions.isEmpty()) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.NO_TRANSACTIONS_FOUND_CODE)
                    .responseMessage(AccountUtils.NO_TRANSACTIONS_FOUND_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        try {
            ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
            PDDocument document = new PDDocument();

            PDPage newPage = new PDPage(PDRectangle.A4);
            document.addPage(newPage);

            PDPageContentStream contentStream = new PDPageContentStream(document, newPage);
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 9);
            contentStream.beginText();
            contentStream.setLeading(14.5f);
            contentStream.newLineAtOffset(50, 750);

            contentStream.showText("Bank Statement");
            contentStream.newLine();
            contentStream.showText("Account Name: " + user.getFirstName() + " " + user.getLastName() +" " + user.getOtherName());
            contentStream.newLine();
            contentStream.showText("Account Number: " + user.getAccountNumber());
            contentStream.newLine();
            contentStream.showText("Available Balance: " + user.getAccountBalance());
            contentStream.newLine();
            contentStream.newLine();

            contentStream.showText("Transactions:");
            contentStream.newLine();

            float yPosition = 750;
            float margin = 50;
            float lineHeight = 14.5f;

            for (Transaction transaction : transactions) {
                // Check if a new page is needed
                if (yPosition - lineHeight * 4 < margin) {
                    contentStream.endText();
                    contentStream.close();


                    PDPage page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 9);
                    contentStream.beginText();
                    contentStream.setLeading(lineHeight);
                    contentStream.newLineAtOffset(margin, 750); // Reset offset for the new page

                    yPosition = 750; // Reset yPosition for the new page
                }


                contentStream.showText("Date: " + DateUtils.formatDateTimeWithoutMilliseconds(transaction.getTransactionDate()));
                contentStream.newLine();
                yPosition -= lineHeight;

                contentStream.showText("Type: " + transaction.getTransactionType());
                contentStream.newLine();
                yPosition -= lineHeight;

                contentStream.showText("Amount: " + transaction.getAmount() + "        Balance: " + transaction.getPostTransactionBalance());
                contentStream.newLine();
                yPosition -= lineHeight;

                contentStream.showText("Description: " + transaction.getDescription());
                contentStream.newLine();
                yPosition -= lineHeight;

                contentStream.newLine();
                yPosition -= lineHeight;
            }

            contentStream.endText();
            contentStream.close();
            document.save(pdfOutputStream);

            //Encrypting the PDF
            String password = user.getAccountNumber().substring(Math.max(0, user.getAccountNumber().length() - 6));
            PDDocument securedDocument = PDDocument.load(new ByteArrayInputStream(pdfOutputStream.toByteArray()));

            AccessPermission accessPermission = new AccessPermission();
            StandardProtectionPolicy policy = new StandardProtectionPolicy(password, password, accessPermission);
            policy.setEncryptionKeyLength(128);
            policy.setPermissions(accessPermission);
            securedDocument.protect(policy);

            ByteArrayOutputStream encryptedPdfStream = new ByteArrayOutputStream();
            securedDocument.save(encryptedPdfStream);
            securedDocument.close();

            EmailDetails emailDetails = EmailDetails.builder()
                    .subject("Bank Statement")
                    .recipient(user.getEmail())
                    .messageBody(String.format(
                            """
                                    Dear %s,
    
                                    Please find attached your bank statement. 
                                    The document is password-protected. 
                                    Use the last 6 digits of your account number as the password.
    
                                    Thank you for banking with us.
    
                                    Best regards,
                                    Kaki Bank
                                    """,
                            user.getFirstName()))
                    .attachmentName("BankStatement.pdf")
                    .attachmentContent(encryptedPdfStream.toByteArray())
                    .build();
            emailService.sendEmailAlert(emailDetails);

            return BankResponse.builder()
                    .responseCode(AccountUtils.STATEMENT_GENERATED_CODE)
                    .responseMessage(AccountUtils.STATEMENT_GENERATED_MESSAGE)
                    .accountInfo(AccountInfo.builder()
                            .accountName(user.getFirstName() + " " + user.getLastName())
                            .accountNumber(user.getAccountNumber())
                            .accountBalance(user.getAccountBalance())
                            .build())
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return BankResponse.builder()
                    .responseCode(AccountUtils.ERROR_GENERATING_STATEMENT_CODE)
                    .responseMessage(AccountUtils.ERROR_GENERATING_STATEMENT_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
    }
}
