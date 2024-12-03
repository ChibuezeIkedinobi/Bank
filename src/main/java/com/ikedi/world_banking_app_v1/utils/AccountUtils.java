package com.ikedi.world_banking_app_v1.utils;

import com.ikedi.world_banking_app_v1.repository.UserEntityRepository;

import java.time.Year;

public class AccountUtils {

    public static final String ACCOUNT_EXISTS_CODE = "001";
    public static final String ACCOUNT_EXISTS_MESSAGE = "This user already has an account created";

    public static final String ACCOUNT_CREATION_SUCCESSFUL = "001";
    public static final String ACCOUNT_CREATION_SUCCESS_MESSAGE = "Account has been created successfully";


    public static String generateAccountNumber(UserEntityRepository userEntityRepository) {
        String accountNumber;

        do {
            // Get the current year
            Year currentYear = Year.now();

            // Generate a random 6-digit number
            int min = 100000;
            int max = 999999;
            int randomNumber = (int) Math.floor(Math.random() * (max - min + 1) + min);

            // Concatenate year and random number
            String year = String.valueOf(currentYear);
            String randomNum = String.valueOf(randomNumber);

            accountNumber = year + randomNum;

            // Continue looping if account number exists
        } while (userEntityRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }



}
