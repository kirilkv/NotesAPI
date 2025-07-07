package org.kiril.notesapi.service;

import lombok.RequiredArgsConstructor;
import org.kiril.notesapi.dto.UserDto;
import org.kiril.notesapi.model.User;
import org.kiril.notesapi.repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.kiril.notesapi.config.CacheConfig.USERS_CACHE;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = USERS_CACHE,
            key = "'all'",
            unless = "#result.isEmpty()")
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserDto)
                .collect(Collectors.toList());
    }

    private UserDto mapToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }
}