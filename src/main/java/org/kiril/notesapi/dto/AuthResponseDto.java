package org.kiril.notesapi.dto;

import lombok.Data;
import org.kiril.notesapi.model.Role;

@Data
public class AuthResponseDto {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private Role role;
}