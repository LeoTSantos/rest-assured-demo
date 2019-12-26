package com.wipro;

import com.google.gson.Gson;
import com.wipro.domain.Note;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.util.regex.Pattern;

import static io.restassured.RestAssured.delete;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest
public class MyNotesControllerTests {

    final static String ROOT_URI = "http://localhost:9090/notes";
    final static String NOTE_PATTERN = "\\{\"id\":%s,\"note\":\"%s\"\\}";

    @AfterMethod
    public void cleanUp() {
        delete(ROOT_URI + "/deleteAll");
    }

    //Get All Notes

    @Test
    public void getAllWithoutAddingNote_ReturnsEmpty() {
        Response response = get(ROOT_URI);
        System.out.println(response.asString());

        response.then()
                .statusCode(200)
                .body(equalTo("[]"));
    }

    @Test
    public void getAllAfterAddingOneNote_ReturnsOneNote() {

        String note = "My First Note";
        String pattern = String.format(NOTE_PATTERN, "[0-9]*", note);

        given().
                contentType(ContentType.TEXT)
                .accept(ContentType.TEXT)
                .body(note)
                .when()
                .post(ROOT_URI);

        Response response = get(ROOT_URI);
        System.out.println(response.asString());

        response.then()
                .statusCode(200)
                .body(Matchers.matchesRegex("\\[" + pattern + "\\]"));
    }

    @Test
    public void getAllAfterAddingTwoNotes_ReturnsTwoNotes() {

        String note = "My First Note";
        String note2 = "My Second Note";
        String pattern1 = String.format(NOTE_PATTERN, "[0-9]*", note);
        String pattern2 = String.format(NOTE_PATTERN, "[0-9]*", note2);

        given().
                contentType(ContentType.TEXT)
                .accept(ContentType.TEXT)
                .body(note)
                .when()
                .post(ROOT_URI);

        given().
                contentType(ContentType.TEXT)
                .accept(ContentType.TEXT)
                .body(note2)
                .when()
                .post(ROOT_URI);

        Response response = get(ROOT_URI);
        System.out.println(response.asString());

        response.then()
                .statusCode(200)
                .body(Matchers.matchesRegex("\\[" + pattern1 + "," + pattern2 + "\\]"));
    }

    //Add Note

    @Test
    public void textNoteAdded_ReturnsNote() {

        String note = "My First Note";
        String pattern = String.format(NOTE_PATTERN, "[0-9]*", note);

        Response response = given().
                contentType(ContentType.TEXT)
                .body(note)
                .when()
                .post(ROOT_URI);

        response.then()
                .statusCode(200)
                .body(Matchers.matchesRegex(pattern));
    }

    @Test
    public void numericNoteAdded_ReturnsNote() {

        String note = "1234567890";
        String pattern = String.format(NOTE_PATTERN, "[0-9]*", note);

        Response response = given().
                contentType(ContentType.TEXT)
                .body(note)
                .when()
                .post(ROOT_URI);

        response.then()
                .statusCode(200)
                .body(Matchers.matchesRegex(pattern));
    }

    @Test
    public void specialCharactersNoteAdded_ReturnsNote() {

        String note = "!@#$%¨&*(){}[]";
        String pattern = String.format(NOTE_PATTERN, "[0-9]*", escapeMultipleCharacters(note));

        Response response = given().
                contentType(ContentType.TEXT)
                .body(note)
                .when()
                .post(ROOT_URI);

        response.then()
                .statusCode(200)
                .body(Matchers.matchesRegex(pattern));
    }

    @Test
    public void BlankSpaceNoteAdded_ReturnsNote() {

        String note = " ";
        String pattern = String.format(NOTE_PATTERN, "[0-9]*", note);

        Response response = given().
                contentType(ContentType.TEXT)
                .body(note)
                .when()
                .post(ROOT_URI);

        response.then()
                .statusCode(200)
                .body(Matchers.matchesRegex(pattern));
    }

    @Test
    public void nullNoteAdded_ReturnsNote() {

        String note = "null";
        String pattern = String.format(NOTE_PATTERN, "[0-9]*", note);

        Response response = given().
                contentType(ContentType.TEXT)
                .body(note)
                .when()
                .post(ROOT_URI);

        response.then()
                .statusCode(200)
                .body(Matchers.matchesRegex(pattern));
    }

    @Test
    public void emptyNoteAdded_ReturnsBadRequest() {

        String note = "";

        Response response = given().
                contentType(ContentType.TEXT)
                .body(note)
                .when()
                .post(ROOT_URI);

        response.then()
                .statusCode(400);
    }

    //Get Note

    @Test
    public void getExistingNote_ReturnsNote() {
        String note = "My First Note";
        Gson gson = new Gson();

        Response response = given().
                contentType(ContentType.TEXT)
                .body(note)
                .when()
                .post(ROOT_URI);

        Long noteId = gson.fromJson(response.body().asString(), Note.class).getId();
        String expected = String.format(NOTE_PATTERN, noteId, note);

        response = get(ROOT_URI + "/" + noteId);
        System.out.println(response.asString());

        response.then()
                .statusCode(200)
                .body(Matchers.matchesRegex(expected));
    }

    @Test
    public void getNonExistingNote_ReturnsNotFound() {
        Response response = get(ROOT_URI + "/1");
        response.then()
                .statusCode(404);
    }

    @Test
    public void getNoteWithInvalidId_ReturnsBadRequest() {
        Response response = get(ROOT_URI + "/a");
        response.then()
                .statusCode(400);
    }

    //Edit Note

    @Test
    public void existingTextNoteEdited_ReturnsNote() {

        String note = "My First Note";
        String editedNote = "My Edited Note";
        Gson gson = new Gson();

        Response response = given().
                contentType(ContentType.TEXT)
                .body(note)
                .when()
                .post(ROOT_URI);

        Long noteId = gson.fromJson(response.body().asString(), Note.class).getId();
        String expected = String.format(NOTE_PATTERN, noteId, editedNote);

        response = given().
                contentType(ContentType.TEXT)
                .body(editedNote)
                .when()
                .put(ROOT_URI + "/" + noteId);

        response.then()
                .statusCode(200)
                .body(Matchers.matchesRegex(expected));
    }

    @Test
    public void existingNumericNoteEdited_ReturnsNote() {

        String note = "1234567890";
        String editedNote = "0987654321";
        Gson gson = new Gson();

        Response response = given().
                contentType(ContentType.TEXT)
                .body(note)
                .when()
                .post(ROOT_URI);

        Long noteId = gson.fromJson(response.body().asString(), Note.class).getId();
        String expected = String.format(NOTE_PATTERN, noteId, editedNote);

        response = given().
                contentType(ContentType.TEXT)
                .body(editedNote)
                .when()
                .put(ROOT_URI + "/" + noteId);

        response.then()
                .statusCode(200)
                .body(Matchers.matchesRegex(expected));
    }

    @Test
    public void existingSpecialCharactersNoteEdited_ReturnsNote() {

        String note = "!@#$%¨&*(){}[]";
        String editedNote = "][}{)(*&¨%$#@!";
        Gson gson = new Gson();

        Response response = given().
                contentType(ContentType.TEXT)
                .body(note)
                .when()
                .post(ROOT_URI);

        Long noteId = gson.fromJson(response.body().asString(), Note.class).getId();
        String expected = String.format(NOTE_PATTERN, noteId, escapeMultipleCharacters(editedNote));

        response = given().
                contentType(ContentType.TEXT)
                .body(editedNote)
                .when()
                .put(ROOT_URI + "/" + noteId);

        response.then()
                .statusCode(200)
                .body(Matchers.matchesRegex(expected));
    }

    @Test
    public void existingBlankSpaceNoteEdited_ReturnsNote() {

        String note = " ";
        String editedNote = "   ";
        Gson gson = new Gson();

        Response response = given().
                contentType(ContentType.TEXT)
                .body(note)
                .when()
                .post(ROOT_URI);

        Long noteId = gson.fromJson(response.body().asString(), Note.class).getId();
        String expected = String.format(NOTE_PATTERN, noteId, editedNote);

        response = given().
                contentType(ContentType.TEXT)
                .body(editedNote)
                .when()
                .put(ROOT_URI + "/" + noteId);

        response.then()
                .statusCode(200)
                .body(Matchers.matchesRegex(expected));
    }

    @Test
    public void existingNullNoteEdited_ReturnsNote() {

        String note = "My First Note";
        String editedNote = "null";
        Gson gson = new Gson();

        Response response = given().
                contentType(ContentType.TEXT)
                .body(note)
                .when()
                .post(ROOT_URI);

        Long noteId = gson.fromJson(response.body().asString(), Note.class).getId();
        String expected = String.format(NOTE_PATTERN, noteId, editedNote);

        response = given().
                contentType(ContentType.TEXT)
                .body(editedNote)
                .when()
                .put(ROOT_URI + "/" + noteId);

        response.then()
                .statusCode(200)
                .body(Matchers.matchesRegex(expected));
    }

    @Test
    public void existingNoteEditedEmpty_ReturnsBadRequest() {

        String note = "My First Note";
        String editedNote = "";
        Gson gson = new Gson();

        Response response = given().
                contentType(ContentType.TEXT)
                .body(note)
                .when()
                .post(ROOT_URI);

        Long noteId = gson.fromJson(response.body().asString(), Note.class).getId();

        response = given().
                contentType(ContentType.TEXT)
                .body(editedNote)
                .when()
                .put(ROOT_URI + "/" + noteId);

        response.then()
                .statusCode(400);
    }

    @Test
    public void nonExistingNoteEdited_ReturnsNotFound() {

        String editedNote = "My Edited Note";

        Response response = given().
                contentType(ContentType.TEXT)
                .body(editedNote)
                .when()
                .put(ROOT_URI + "/1");

        response.then()
                .statusCode(404);
    }

    @Test
    public void invalidIdRequestedToEditEndpoint_ReturnsBadRequest() {

        String editedNote = "My Edited Note";

        Response response = given().
                contentType(ContentType.TEXT)
                .body(editedNote)
                .when()
                .put(ROOT_URI + "/a");

        response.then()
                .statusCode(400);
    }

    //Delete Note

    @Test
    public void deleteExistingNote_ReturnsOk() {
        String note = "My First Note";
        Gson gson = new Gson();

        Response response = given().
                contentType(ContentType.TEXT)
                .body(note)
                .when()
                .post(ROOT_URI);

        Long noteId = gson.fromJson(response.body().asString(), Note.class).getId();
        String expected = "\"Successfully deleted note " + noteId + "\"";

        response = delete(ROOT_URI + "/" + noteId);
        System.out.println(response.asString());

        response.then()
                .statusCode(200)
                .body(equalTo(expected));
    }

    @Test
    public void deleteNonExistingNote_ReturnsNotFound() {
        Response response = delete(ROOT_URI + "/1");
        response.then()
                .statusCode(404);
    }

    @Test
    public void deleteNoteWithInvalidId_ReturnsBadRequest() {
        Response response = delete(ROOT_URI + "/a");
        response.then()
                .statusCode(400);
    }

    private String escapeMultipleCharacters(String s) {
        Pattern p= Pattern.compile("([-&\\|!\\(\\){}\\[\\]\\^\"\\~\\*\\?:\\\\@#$%¨])");
        s=p.matcher(s).replaceAll("\\\\$1");
        System.out.println(s);
        return s;
    }
}
