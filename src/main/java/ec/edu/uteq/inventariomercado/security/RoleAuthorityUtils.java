package ec.edu.uteq.inventariomercado.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public final class RoleAuthorityUtils {

    private static final String ROLE_PREFIX = "ROLE_";

    private RoleAuthorityUtils() {
    }

    /**
     * Normaliza un valor de rol (con o sin prefijo "ROLE_") al GrantedAuthority
     * que Spring Security espera para que hasRole(...) coincida siempre,
     * sin depender de que el dato de origen (BD o claim del JWT) ya lo traiga.
     */
    public static SimpleGrantedAuthority toAuthority(String role) {
        String normalizado = role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role;
        return new SimpleGrantedAuthority(normalizado);
    }
}
