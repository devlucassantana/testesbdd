package br.com.openinvestment.security;

import br.com.openinvestment.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret",
                "openInvestmentSecretKey2026ESGSocialProjectsSecureKey");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 86400000L);

        usuario = Usuario.builder()
                .idUsuario(1L)
                .email("admin@openinvestment.com")
                .senha("hash")
                .perfil("ADMIN")
                .ativo(true)
                .build();
    }

    @Test
    @DisplayName("generateToken cria token não nulo")
    void generateTokenNaoNulo() {
        String token = jwtService.generateToken(usuario);
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("extractUsername retorna email correto")
    void extractUsername() {
        String token = jwtService.generateToken(usuario);
        String email = jwtService.extractUsername(token);
        assertThat(email).isEqualTo("admin@openinvestment.com");
    }

    @Test
    @DisplayName("isTokenValid retorna true para token válido")
    void tokenValido() {
        String token = jwtService.generateToken(usuario);
        assertThat(jwtService.isTokenValid(token, usuario)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid retorna false para token adulterado")
    void tokenAdulterado() {
        String token = jwtService.generateToken(usuario) + "adulterado";
        assertThat(jwtService.isTokenValid(token, usuario)).isFalse();
    }
}
