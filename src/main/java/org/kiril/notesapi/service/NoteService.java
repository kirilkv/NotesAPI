package org.kiril.notesapi.service;

import lombok.RequiredArgsConstructor;
import org.kiril.notesapi.dto.NoteDto;
import org.kiril.notesapi.model.Note;
import org.kiril.notesapi.model.User;
import org.kiril.notesapi.repository.NoteRepository;
import org.kiril.notesapi.repository.UserRepository;
import org.kiril.notesapi.security.UserPrincipal;
import org.springframework.cache.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.kiril.notesapi.config.CacheConfig.*;

import org.springframework.cache.annotation.Cacheable;


@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Caching(cacheable = {
            @Cacheable(value = NOTES_CACHE,
                    key = "'admin:all'",
                    condition = "@noteService.isAdmin() && #userId == null"),
            @Cacheable(value = NOTES_CACHE,
                    key = "'user:' + #userId",
                    condition = "#userId != null"),
            @Cacheable(value = NOTES_CACHE,
                    key = "'user:' + @noteService.getCurrentUserId()",
                    condition = "!@noteService.isAdmin() && #userId == null")
    })
    public List<NoteDto> getNotes(Long userId) {
        UserPrincipal currentUser = getCurrentUser();
        boolean isAdmin = hasAdminRole(currentUser);

        if (isAdmin) {
            if (userId != null) {
                return noteRepository.findByUserId(userId).stream()
                        .map(this::mapToDto)
                        .toList();
            }
            return noteRepository.findAll().stream()
                    .map(this::mapToDto)
                    .toList();
        }

        if (userId != null && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to access these notes.");
        }

        return noteRepository.findByUserId(currentUser.getId()).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = NOTE_CACHE, key = "#id")
    public NoteDto getNote(Long id) {
        Note note = findNoteById(id);
        checkNoteAccess(note);
        return mapToDto(note);
    }

    @Caching(
            evict = {
                    @CacheEvict(value = NOTES_CACHE, key = "'user:' + @noteService.getCurrentUserId()"),
                    @CacheEvict(value = NOTES_CACHE, key = "'admin:all'")
            },
            put = {
                    @CachePut(value = NOTE_CACHE, key = "#result.id")
            }
    )
    @Transactional
    public NoteDto createNote(NoteDto noteDto) {
        UserPrincipal currentUser = getCurrentUser();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Note note = new Note();
        note.setTitle(noteDto.getTitle());
        note.setContent(noteDto.getContent());
        note.setUser(user);

        Note savedNote = noteRepository.save(note);
        return mapToDto(savedNote);
    }

    @Caching(
            evict = {
                    @CacheEvict(value = NOTES_CACHE, key = "'user:' + @noteService.getCurrentUserId()"),
                    @CacheEvict(value = NOTES_CACHE, key = "'admin:all'")
            },
            put = {
                    @CachePut(value = NOTE_CACHE, key = "#result.id")
            }
    )
    @Transactional
    public NoteDto updateNote(Long id, NoteDto noteDto) {
        Note note = findNoteById(id);
        checkNoteAccess(note);

        note.setTitle(noteDto.getTitle());
        note.setContent(noteDto.getContent());
        Note updatedNote = noteRepository.save(note);
        return mapToDto(updatedNote);
    }

    @Caching(
            evict = {
                    @CacheEvict(value = NOTES_CACHE, key = "'user:' + @noteService.getCurrentUserId()"),
                    @CacheEvict(value = NOTES_CACHE, key = "'admin:all'")
            },
            put = {
                    @CachePut(value = NOTE_CACHE, key = "#result.id")
            }
    )
    @Transactional
    public NoteDto partialUpdateNote(Long id, Map<String, Object> updates) {
        Note note = findNoteById(id);
        checkNoteAccess(note);

        if (updates.containsKey("title")) {
            note.setTitle((String) updates.get("title"));
        }
        if (updates.containsKey("content")) {
            note.setContent((String) updates.get("content"));
        }

        Note updatedNote = noteRepository.save(note);
        return mapToDto(updatedNote);
    }

    @Caching(
            evict = {
                    @CacheEvict(value = NOTE_CACHE, key = "#id"),
                    @CacheEvict(value = NOTES_CACHE, key = "'user:' + @noteService.getCurrentUserId()"),
                    @CacheEvict(value = NOTES_CACHE, key = "'admin:all'")
            }
    )
    @Transactional
    public void deleteNote(Long id) {
        Note note = findNoteById(id);
        checkNoteAccess(note);
        noteRepository.delete(note);
    }

    public boolean isAdmin() {
        UserPrincipal currentUser = getCurrentUser();
        return hasAdminRole(currentUser);
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }


    private Note findNoteById(Long id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Note not found with id: " + id));
    }

    private void checkNoteAccess(Note note) {
        UserPrincipal currentUser = getCurrentUser();
        boolean isAdmin = hasAdminRole(currentUser);
        boolean isOwner = note.getUser().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You don't have permission to access this note");
        }
    }

    private boolean hasAdminRole(UserPrincipal user) {
        return user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new AccessDeniedException("User not authenticated");
        }
        return (UserPrincipal) authentication.getPrincipal();
    }

    private NoteDto mapToDto(Note note) {
        NoteDto dto = new NoteDto();
        dto.setId(note.getId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setCreatedAt(note.getCreatedAt());
        dto.setUpdatedAt(note.getUpdatedAt());
        dto.setUserId(note.getUser().getId());
        return dto;
    }
}