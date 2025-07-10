package org.kiril.notesapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiril.notesapi.dto.NoteDto;
import org.kiril.notesapi.model.Note;
import org.kiril.notesapi.model.User;
import org.kiril.notesapi.model.Role;
import org.kiril.notesapi.repository.NoteRepository;
import org.kiril.notesapi.repository.UserRepository;
import org.kiril.notesapi.security.UserPrincipal;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {
    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoteService noteService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    private UserPrincipal userPrincipal;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole(Role.ROLE_USER);

        userPrincipal = new UserPrincipal(
                user.getId(),
                user.getEmail(),
                "password",
                List.of(new SimpleGrantedAuthority(user.getRole().name()))
        );

        // Set up security context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
    }


    @Test
    void getNotes_AsUser_ShouldReturnUserNotes() {
        List<Note> userNotes = Arrays.asList(
                createNote(1L, "Note 1", user),
                createNote(2L, "Note 2", user)
        );

        when(noteRepository.findByUserId(1L)).thenReturn(userNotes);

        List<NoteDto> result = noteService.getNotes(null);

        assertEquals(2, result.size());
        verify(noteRepository).findByUserId(1L);
    }

    @Test
    void getNotes_AsAdmin_ShouldReturnAllNotes() {
        user.setRole(Role.ROLE_ADMIN);

        userPrincipal = new UserPrincipal(
                user.getId(),
                user.getEmail(),
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        List<Note> allNotes = Arrays.asList(
                createNote(1L, "Note 1", user),
                createNote(2L, "Note 2", user)
        );

        when(noteRepository.findAll()).thenReturn(allNotes);

        List<NoteDto> result = noteService.getNotes(null);

        assertEquals(2, result.size());
        verify(noteRepository).findAll();
    }


    @Test
    void getNote_AsOwner_ShouldReturnNote() {
        Note note = createNote(1L, "Test Note", user);
        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));

        NoteDto result = noteService.getNote(1L);

        assertNotNull(result);
        assertEquals("Test Note", result.getTitle());
    }

    @Test
    void getNote_AsNonOwner_ShouldThrowAccessDeniedException() {
        User otherUser = new User();
        otherUser.setId(2L);
        Note note = createNote(1L, "Test Note", otherUser);

        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));

        assertThrows(ResponseStatusException.class, () ->
                noteService.getNote(1L)
        );
    }

    @Test
    void createNote_WithValidData_ShouldReturnCreatedNote() {
        NoteDto noteDto = new NoteDto();
        noteDto.setTitle("New Note");
        noteDto.setContent("Content");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(noteRepository.save(any(Note.class))).thenAnswer(i -> {
            Note note = (Note) i.getArguments()[0];
            note.setId(1L);
            return note;
        });

        NoteDto result = noteService.createNote(noteDto);

        assertNotNull(result.getId());
        assertEquals("New Note", result.getTitle());
        assertEquals(1L, result.getUserId());
    }

    @Test
    void updateNote_AsOwner_ShouldUpdateNote() {
        Note existingNote = createNote(1L, "Old Title", user);
        NoteDto updateDto = new NoteDto();
        updateDto.setTitle("Updated Title");
        updateDto.setContent("Updated Content");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user)); // Mock to return the user
        when(noteRepository.findById(1L)).thenReturn(Optional.of(existingNote));
        when(noteRepository.save(any(Note.class))).thenReturn(existingNote);

        NoteDto result = noteService.updateNote(1L, updateDto);

        assertEquals("Updated Title", result.getTitle());
    }

    @Test
    void deleteNote_AsOwner_ShouldDeleteNote() {
        Note note = createNote(1L, "Test Note", user);
        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));

        noteService.deleteNote(1L);

        verify(noteRepository).delete(note);
    }

    private Note createNote(Long id, String title, User user) {
        Note note = new Note();
        note.setId(id);
        note.setTitle(title);
        note.setContent("Test content");
        note.setUser(user);
        return note;
    }
}