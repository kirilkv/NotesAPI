package org.kiril.notesapi.dto;

import lombok.Data;
import org.kiril.notesapi.model.Role;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private String email;
    private Role role;
}
