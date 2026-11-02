package br.com.openinvestment.service;

import br.com.openinvestment.dto.request.LoginRequest;
import br.com.openinvestment.dto.response.AuthResponse;
import br.com.openinvestment.entity.Usuario;
import br.com.openinvestment.repository.UsuarioRepository;
import br.com.openinvestment.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new org.springframework.security.core.userdetails
                        .UsernameNotFoundException("Usuário não encontrado: " + request.email()));
        String token = jwtService.generateToken(usuario);
        return new AuthResponse(token, usuario.getEmail(), usuario.getPerfil());
    }
}
