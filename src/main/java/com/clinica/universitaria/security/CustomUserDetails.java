package com.clinica.universitaria.security;

import com.clinica.universitaria.model.Usuario;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

@Getter
public class CustomUserDetails extends User {

    private final Usuario usuario;

    public CustomUserDetails(Usuario usuario) {
        super(
                usuario.getCorreo(),
                usuario.getPasswordHash(),
                usuario.getEstado(),
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre().name()))
        );
        this.usuario = usuario;
    }
}
