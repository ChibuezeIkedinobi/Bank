package com.ikedi.world_banking_app_v1.utils;

import com.ikedi.world_banking_app_v1.repository.UserEntityRepository;

import java.time.Year;

public class AccountUtils {

    public static final String ACCOUNT_EXISTS_CODE = "001";
    public static final String ACCOUNT_EXISTS_MESSAGE = "This user already has an account created🚨🚨";

    public static final String ACCOUNT_CREATION_SUCCESSFUL = "001";
    public static final String ACCOUNT_CREATION_SUCCESS_MESSAGE = "Account has been created successfully✅✅";

    public static final String ACCOUNT_NUMBER_NON_EXISTS_CODE = "003";
    public static final String ACCOUNT_NUMBER_NON_EXISTS_MESSAGE = "Provided account number does not exist🚨🚨";

    public static final String ACCOUNT_NUMBER_FOUND_CODE= "004";
    public static final String ACCOUNT_NUMBER_FOUND_MESSAGE = "Provided account number found✅✅";

    public static final String ACCOUNT_CREDITED_SUCCESS_CODE= "005";
    public static final String ACCOUNT_CREDITED_SUCCESS_MESSAGE = "Account credited successfully✅✅";

    public static final String INSUFFICIENT_BALANCE_CODE= "006";
    public static final String INSUFFICIENT_BALANCE_MESSAGE = "Insufficient Balance🚨🚨";

    public static final String ACCOUNT_DEBITED_CODE= "007";
    public static final String ACCOUNT_DEBITED_MESSAGE = "Account has been debited successfully✅✅";

    public static final String TRANSFER_SUCCESSFUL_CODE= "008";
    public static final String TRANSFER_SUCCESSFUL_MESSAGE = "Transfer Successful✅✅";

    public static final String NO_TRANSACTIONS_FOUND_CODE = "009";
    public static final String NO_TRANSACTIONS_FOUND_MESSAGE = "No Transactions found for this account.🚨🚨";

    public static final String ERROR_GENERATING_STATEMENT_CODE = "010";
    public static final String ERROR_GENERATING_STATEMENT_MESSAGE = "An error occurred while generating the bank statement🚨🚨";

    public static final String STATEMENT_GENERATED_CODE = "011";
    public static final String STATEMENT_GENERATED_MESSAGE = "Bank statement has been successfully generated and sent to your email.✅✅";


    public static String generateAccountNumber(UserEntityRepository userEntityRepository) {
        String accountNumber;

        do {
            Year currentYear = Year.now();

            int min = 100000;
            int max = 999999;
            int randomNumber = (int) Math.floor(Math.random() * (max - min + 1) + min);

            String year = String.valueOf(currentYear);
            String randomNum = String.valueOf(randomNumber);

            accountNumber = year + randomNum;

        } while (userEntityRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }



}
