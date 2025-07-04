package org.kiril.notesapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.kiril.notesapi.model.Role;

@Data
public class AdminRegisterRequestDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private Role role = Role.ROLE_ADMIN;
}