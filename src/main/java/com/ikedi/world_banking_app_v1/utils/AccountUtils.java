package com.ikedi.world_banking_app_v1.utils;

import java.time.Year;

public class AccountUtils {

    public static final String ACCOUNT_EXISTS_CODE = "001";
    public static final String ACCOUNT_EXISTS_MESSAGE = "This user already has an account created";

    public static final String ACCOUNT_CREATION_SUCCESSFUL = "001";
    public static final String ACCOUNT_CREATION_SUCCESS_MESSAGE = "Account has been created successfully";


    public static String generateAccountNumber() {
        //get current year
        Year currentYear = Year.now();

        // random 6 digits
        int min = 100000;
        int max = 999999;

        //generate a random number between min and max
        int randomNumber = (int)Math.floor(Math.random() * (max - min + 1) + min);

        //convert current yest and random number to string and then concatenate
        String year = String.valueOf(currentYear);
        String randomNum = String.valueOf(randomNumber);

        //append both the current year and the random number to generate the 10-digit account  number
        StringBuilder accountNumber = new StringBuilder();
        return accountNumber.append(year).append(randomNum).toString();
    }


}
