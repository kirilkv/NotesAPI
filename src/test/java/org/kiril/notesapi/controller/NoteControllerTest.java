package org.kiril.notesapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.kiril.notesapi.dto.NoteDto;
import org.kiril.notesapi.security.jwt.JwtTokenProvider;
import org.kiril.notesapi.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoteController.class)
class NoteControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoteService noteService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(roles = "USER")
    void getNotes_AsUser_ShouldReturnUserNotes() throws Exception {
        List<NoteDto> notes = Arrays.asList(
                createNoteDto(1L, "Note 1", 1L),
                createNoteDto(2L, "Note 2", 1L)
        );

        when(noteService.getNotes(null)).thenReturn(notes);

        mockMvc.perform(get("/api/notes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Note 1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getNotes_AsAdminWithUserId_ShouldReturnSpecificUserNotes() throws Exception {
        List<NoteDto> notes = List.of(createNoteDto(1L, "Note 1", 2L));

        when(noteService.getNotes(2L)).thenReturn(notes);

        mockMvc.perform(get("/api/notes")
                        .param("userId", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value(2));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getNote_WithValidId_ShouldReturnNote() throws Exception {
        NoteDto note = createNoteDto(1L, "Test Note", 1L);
        when(noteService.getNote(1L)).thenReturn(note);

        mockMvc.perform(get("/api/notes/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Note"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getNote_WithInvalidId_ShouldReturn404() throws Exception {
        when(noteService.getNote(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/notes/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createNote_WithValidData_ShouldReturnCreatedNote() throws Exception {
        NoteDto noteDto = createNoteDto(null, "New Note", null);
        NoteDto createdNote = createNoteDto(1L, "New Note", 1L);

        when(noteService.createNote(any(NoteDto.class))).thenReturn(createdNote);

        mockMvc.perform(post("/api/notes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/notes/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Note"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateNote_WithValidData_ShouldReturnUpdatedNote() throws Exception {
        NoteDto noteDto = createNoteDto(1L, "Updated Note", 1L);
        when(noteService.updateNote(eq(1L), any(NoteDto.class))).thenReturn(noteDto);

        mockMvc.perform(put("/api/notes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Note"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void partialUpdateNote_WithValidData_ShouldReturnUpdatedNote() throws Exception {
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", "Updated Title");

        NoteDto updatedNote = createNoteDto(1L, "Updated Title", 1L);
        when(noteService.partialUpdateNote(eq(1L), any())).thenReturn(updatedNote);

        mockMvc.perform(patch("/api/notes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteNote_WithValidId_ShouldReturn204() throws Exception {
        doNothing().when(noteService).deleteNote(1L);

        mockMvc.perform(delete("/api/notes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteNote_WithInvalidId_ShouldReturn404() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
                .when(noteService).deleteNote(99L);

        mockMvc.perform(delete("/api/notes/99"))
                .andExpect(status().isNotFound());
    }

    private NoteDto createNoteDto(Long id, String title, Long userId) {
        NoteDto dto = new NoteDto();
        dto.setId(id);
        dto.setTitle(title);
        dto.setContent("Test content");
        dto.setUserId(userId);
        return dto;
    }
}