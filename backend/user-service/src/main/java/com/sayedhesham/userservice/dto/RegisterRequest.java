package com.sayedhesham.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Email is required")
    @NotBlank
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

}
