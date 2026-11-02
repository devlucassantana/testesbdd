package br.com.openinvestment.dto.response;

public record AuthResponse(String token, String tipo, String email, String perfil) {
    public AuthResponse(String token, String email, String perfil) {
        this(token, "Bearer", email, perfil);
    }
}
