package ec.edu.uteq.inventariomercado.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtRoleAuthorizationTest {

    private static final String TEST_SECRET = "una-clave-secreta-de-pruebas-con-al-menos-32-bytes-de-longitud";

    private final JwtService jwtService = new JwtService(TEST_SECRET, 3_600_000L);

    @Test
    void tokenConRolYaPrefijadoCoincideConHasRoleAdmin() {
        String token = jwtService.generateToken("admin.mercado", "ROLE_ADMIN");
        String rolDesdeToken = jwtService.extractRole(token);

        assertThat(check(rolDesdeToken, "ADMIN")).isTrue();
        assertThat(check(rolDesdeToken, "USER")).isFalse();
    }

    @Test
    void tokenConRolSinPrefijoSeNormalizaYCoincideConHasRoleUser() {
        String token = jwtService.generateToken("legacy.user", "USER");
        String rolDesdeToken = jwtService.extractRole(token);

        assertThat(check(rolDesdeToken, "USER")).isTrue();
        assertThat(check(rolDesdeToken, "ADMIN")).isFalse();
    }

    private boolean check(String rolDesdeToken, String roleExigidoPorHasRole) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "usuario-test", null, List.of(RoleAuthorityUtils.toAuthority(rolDesdeToken))
        );

        AuthorityAuthorizationManager<Object> manager = AuthorityAuthorizationManager.hasRole(roleExigidoPorHasRole);
        AuthorizationResult decision = manager.authorize(() -> authentication, null);

        return decision.isGranted();
    }
}
