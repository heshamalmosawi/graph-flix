package com.graphflix.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorRequest {

    @NotBlank
    @Pattern(regexp = "^\\d{6}$", message = "Code must be exactly 6 digits")
    private String code;

}
