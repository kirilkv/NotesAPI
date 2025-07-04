package org.kiril.notesapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kiril.notesapi.dto.NoteDto;
import org.kiril.notesapi.service.NoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {
    private final NoteService noteService;

    @GetMapping
    public ResponseEntity<List<NoteDto>> getNotes(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(noteService.getNotes(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteDto> getNote(@PathVariable Long id) {
        return ResponseEntity.ok(noteService.getNote(id));
    }

    @PostMapping
    public ResponseEntity<NoteDto> createNote(@Valid @RequestBody NoteDto noteDto) {
        NoteDto created = noteService.createNote(noteDto);
        return ResponseEntity
                .created(URI.create("/api/notes/" + created.getId()))
                .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteDto> updateNote(@PathVariable Long id,
                                              @Valid @RequestBody NoteDto noteDto) {
        return ResponseEntity.ok(noteService.updateNote(id, noteDto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<NoteDto> partialUpdateNote(@PathVariable Long id,
                                                     @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(noteService.partialUpdateNote(id, updates));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }
}