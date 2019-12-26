package com.wipro;

import com.wipro.domain.Note;
import com.wipro.exception.NoteNotFoundException;
import com.wipro.repository.NotesRepository;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notes")
public class MyNotesController {

    private NotesRepository repository;

    @Autowired
    public MyNotesController(NotesRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Note> getAllNotes(){
        return repository.findAll();
    }

    @GetMapping("/{id}")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Note found"),
            @ApiResponse(code = 400, message = "Invalid Id"),
            @ApiResponse(code = 404, message = "note not found")})
    public Note getNote(@PathVariable("id") Long id) {
        try{
            return repository.findById(id).get();
        } catch (Exception e) {
            throw new NoteNotFoundException("Note not found.");
        }
    }

    @PostMapping
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Note succesfully created"),
            @ApiResponse(code = 400, message = "No note to add")})
    public Note addNote(@RequestBody String note){
        System.out.println(note);
        Note newNote = new Note(note);
        return repository.save(newNote);
    }

    @DeleteMapping("/{id}")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Note succesfully deleted"),
            @ApiResponse(code = 400, message = "Invalid Id"),
            @ApiResponse(code = 404, message = "Note not found")})
    public String deleteNote(@PathVariable("id") Long id) {
        try{
            repository.deleteById(id);
        } catch (Exception e) {
            throw new NoteNotFoundException("Note not found.");
        }
        return "\"Successfully deleted note " + id + "\"";
    }

    @PutMapping("/{id}")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Note succesfully edited"),
            @ApiResponse(code = 400, message = "Invalid input"),
            @ApiResponse(code = 404, message = "Note not found")})
    public Note editNote(@PathVariable("id") Long id, @RequestBody String note) {
        try {
            Note editedNote = repository.findById(id).get();
            editedNote.setNote(note);
            return repository.save(editedNote);
        } catch (Exception e) {
            throw new NoteNotFoundException("Note not found.");
        }
    }

    @DeleteMapping("/deleteAll")
    public void deleteAll() {
        for (Note n: repository.findAll()) {
            repository.deleteById(n.getId());
        }
    }
}
