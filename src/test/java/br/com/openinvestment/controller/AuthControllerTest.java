package br.com.openinvestment.controller;

import br.com.openinvestment.dto.request.LoginRequest;
import br.com.openinvestment.dto.response.AuthResponse;
import br.com.openinvestment.security.JwtService;
import br.com.openinvestment.security.UserDetailsServiceImpl;
import br.com.openinvestment.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de camada web isolado: filtros de segurança desligados (addFilters = false)
 * e ObjectMapper real do Spring. O JwtAuthFilter é escaneado pelo slice WebMvc,
 * por isso JwtService e UserDetailsServiceImpl precisam existir como mocks.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AuthService authService;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("POST /api/auth/login com credenciais válidas retorna 200 e token")
    void loginValido() throws Exception {
        when(authService.login(any()))
                .thenReturn(new AuthResponse("jwt-token-fake", "admin@openinvestment.com", "ADMIN"));

        LoginRequest req = new LoginRequest("admin@openinvestment.com", "Admin@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-fake"))
                .andExpect(jsonPath("$.tipo").value("Bearer"));
    }

    @Test
    @DisplayName("POST /api/auth/login com body vazio retorna 400")
    void loginBodyVazio() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
