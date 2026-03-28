package com.recipemanager.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipemanager.api.dto.LoginRequest;
import com.recipemanager.api.dto.RegisterRequest;
import com.recipemanager.api.dto.RecipeIngredientRequest;
import com.recipemanager.api.dto.RecipeRequest;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecipeApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void recipesRequireAuth() throws Exception {
        mockMvc.perform(get("/api/recipes")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/recipes/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void fullRecipeCrudFlow() throws Exception {
        String token = registerAndLogin("crud@example.com", "password123", "cruduser");
        RecipeRequest create = new RecipeRequest(
                "Пирог",
                "С яблоками",
                "Выпекать 30 мин",
                List.of(new RecipeIngredientRequest("Яблоки", new BigDecimal("3"), "шт")));

        MvcResult created = mockMvc.perform(post("/api/recipes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Пирог"))
                .andExpect(jsonPath("$.ingredients[0].name").value("Яблоки"))
                .andReturn();

        long id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/recipes").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value((int) id))
                .andExpect(jsonPath("$[0].title").value("Пирог"));

        mockMvc.perform(get("/api/recipes/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instructions").value("Выпекать 30 мин"));

        RecipeRequest update = new RecipeRequest(
                "Пирог",
                "С грушами",
                "Выпекать 35 мин",
                List.of(new RecipeIngredientRequest("Груши", new BigDecimal("2"), "шт")));
        mockMvc.perform(put("/api/recipes/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("С грушами"));

        mockMvc.perform(delete("/api/recipes/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/recipes/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void userCannotSeeOtherUsersRecipe() throws Exception {
        String tokenA = registerAndLogin("a@example.com", "password123", "usera");
        RecipeRequest req = new RecipeRequest("R", "D", "I", List.of());
        MvcResult res = mockMvc.perform(post("/api/recipes")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        long recipeId =
                objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

        String tokenB = registerAndLogin("b@example.com", "password123", "userb");
        mockMvc.perform(get("/api/recipes/" + recipeId).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    private String registerAndLogin(String email, String password, String username) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest(email, password, username))))
                .andExpect(status().isCreated());
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(login.getResponse().getContentAsString()).get("token").asText();
    }
}
