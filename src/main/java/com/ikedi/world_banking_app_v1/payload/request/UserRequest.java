package com.ikedi.world_banking_app_v1.payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {

    private String firstName;
    private String lastName;
    private String otherName;
    private String email;
    private String gender;
    private String address;
    private String stateOfOrigin;
    private String phoneNumber;
    private String password;
}
