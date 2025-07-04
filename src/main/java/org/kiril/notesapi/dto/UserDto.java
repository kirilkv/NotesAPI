package org.kiril.notesapi.dto;

import lombok.Data;
import org.kiril.notesapi.model.Role;

@Data
public class UserDto {
    private Long id;
    private String email;
    private Role role;
}
