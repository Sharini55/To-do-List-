package com.todo;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("Todo App — Full Test Suite")
class TaskControllerTest {

    @Autowired MockMvc mvc;

    @Test @DisplayName("Health endpoint returns UP")
    void healthCheck() throws Exception {
        mvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test @DisplayName("GET /api/tasks returns empty list on startup")
    void getTasksEmpty() throws Exception {
        mvc.perform(get("/api/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test @DisplayName("Create a valid task returns 201")
    void createTask_valid() throws Exception {
        mvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Study for exam\",\"priority\":\"Red\",\"dueDate\":\"2027-01-01\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.title").value("Study for exam"))
            .andExpect(jsonPath("$.priority").value("Red"))
            .andExpect(jsonPath("$.completed").value(false));
    }

    @Test @DisplayName("Create task with no priority defaults to None")
    void createTask_defaultPriority() throws Exception {
        mvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Buy groceries\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.priority").value("None"));
    }

    @Test @DisplayName("Create task with empty title returns 400")
    void createTask_emptyTitle() throws Exception {
        mvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\",\"priority\":\"None\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test @DisplayName("Create task with null title returns 400")
    void createTask_nullTitle() throws Exception {
        mvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"priority\":\"None\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test @DisplayName("Create task with whitespace-only title returns 400")
    void createTask_whitespaceTitle() throws Exception {
        mvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"   \"}"))
            .andExpect(status().isBadRequest());
    }

    @Test @DisplayName("Created tasks are stored and retrievable")
    void tasksArePersisted() throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Persistent task\",\"priority\":\"Green\"}"));
        mvc.perform(get("/api/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title").value("Persistent task"));
    }

    @Test @DisplayName("All tasks returned after multiple creates")
    void allTasksReturned() throws Exception {
        for (int i = 1; i <= 5; i++) {
            mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"Task " + i + "\"}"));
        }
        mvc.perform(get("/api/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test @DisplayName("Toggle complete marks task as done")
    void toggleComplete_marksDone() throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Finish report\"}"));
        mvc.perform(patch("/api/tasks/1/complete"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completed").value(true));
    }

    @Test @DisplayName("Toggle complete twice returns to incomplete")
    void toggleComplete_twice() throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Test task\"}"));
        mvc.perform(patch("/api/tasks/1/complete"));
        mvc.perform(patch("/api/tasks/1/complete"))
            .andExpect(jsonPath("$.completed").value(false));
    }

    @Test @DisplayName("Toggle non-existent task returns 404")
    void toggleComplete_notFound() throws Exception {
        mvc.perform(patch("/api/tasks/999/complete"))
            .andExpect(status().isNotFound());
    }

    @Test @DisplayName("Update task title works correctly")
    void updateTask_title() throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Old title\"}"));
        mvc.perform(put("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"New title\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("New title"));
    }

    @Test @DisplayName("Update task priority works correctly")
    void updateTask_priority() throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Task\",\"priority\":\"None\"}"));
        mvc.perform(put("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"priority\":\"Red\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.priority").value("Red"));
    }

    @Test @DisplayName("Update non-existent task returns 404")
    void updateTask_notFound() throws Exception {
        mvc.perform(put("/api/tasks/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Ghost task\"}"))
            .andExpect(status().isNotFound());
    }

    @Test @DisplayName("Delete existing task returns 204")
    void deleteTask_exists() throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"To delete\"}"));
        mvc.perform(delete("/api/tasks/1"))
            .andExpect(status().isNoContent());
        mvc.perform(get("/api/tasks"))
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test @DisplayName("Delete non-existent task returns 404")
    void deleteTask_notFound() throws Exception {
        mvc.perform(delete("/api/tasks/999"))
            .andExpect(status().isNotFound());
    }

    @Test @DisplayName("Delete one task does not affect others")
    void deleteTask_otherTasksUnaffected() throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Keep me\"}"));
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Delete me\"}"));
        mvc.perform(delete("/api/tasks/2"));
        mvc.perform(get("/api/tasks"))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title").value("Keep me"));
    }

    @Test @DisplayName("Task created via POST is visible in GET — cross-view sync")
    void crossViewSync_createThenGet() throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Synced task\",\"priority\":\"Yellow\",\"dueDate\":\"2027-06-15\"}"));
        mvc.perform(get("/api/tasks"))
            .andExpect(jsonPath("$[0].title").value("Synced task"))
            .andExpect(jsonPath("$[0].dueDate").value("2027-06-15"));
    }

    @Test @DisplayName("Task updated is reflected in subsequent GET")
    void crossViewSync_updateThenGet() throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Original\"}"));
        mvc.perform(put("/api/tasks/1").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Updated\",\"priority\":\"Red\"}"));
        mvc.perform(get("/api/tasks"))
            .andExpect(jsonPath("$[0].title").value("Updated"))
            .andExpect(jsonPath("$[0].priority").value("Red"));
    }

    @Test @DisplayName("Completed task still appears in GET list")
    void completedTask_stillInList() throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Done task\"}"));
        mvc.perform(patch("/api/tasks/1/complete"));
        mvc.perform(get("/api/tasks"))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].completed").value(true));
    }

    @Test @DisplayName("Malformed JSON returns 400")
    void malformedJson() throws Exception {
        mvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ this is not json }"))
            .andExpect(status().isBadRequest());
    }

    @Test @DisplayName("Task with very long title is accepted")
    void longTitle() throws Exception {
        String longTitle = "A".repeat(500);
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"" + longTitle + "\"}"))
            .andExpect(status().isCreated());
    }

    @Test @DisplayName("Task with special characters in title is accepted")
    void specialCharacters() throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Buy milk & eggs\"}"))
            .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Red", "Yellow", "Green", "None"})
    @DisplayName("All valid priorities are accepted")
    void validPriorities(String priority) throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Task\",\"priority\":\"" + priority + "\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.priority").value(priority));
    }

    @Test @DisplayName("Wrong Content-Type returns 4xx error")
    void wrongContentType() throws Exception {
        mvc.perform(post("/api/tasks")
                .contentType(MediaType.TEXT_PLAIN)
                .content("title=test"))
            .andExpect(jsonPath("$.error").exists());
    }

    @Test @DisplayName("PUT to /api/tasks with no ID returns error")
    void wrongMethod_putWithoutId() throws Exception {
        mvc.perform(put("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Test\"}"))
            .andExpect(jsonPath("$.error").exists());
    }

    @Test @DisplayName("DELETE to /api/tasks with no ID returns error")
    void wrongMethod_deleteWithoutId() throws Exception {
        mvc.perform(delete("/api/tasks"))
            .andExpect(jsonPath("$.error").exists());
    }

    @Test @DisplayName("Creating 100 tasks all succeed and are stored")
    void stressTest_100tasks() throws Exception {
        for (int i = 1; i <= 100; i++) {
            mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"Task " + i + "\"}"))
                .andExpect(status().isCreated());
        }
        mvc.perform(get("/api/tasks"))
            .andExpect(jsonPath("$", hasSize(100)));
    }
}
