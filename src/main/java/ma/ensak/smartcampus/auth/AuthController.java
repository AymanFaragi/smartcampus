package ma.ensak.smartcampus.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String FORMAT_MESSAGE = "Format d'e-mail invalide (domaine institutionnel requis)";

    private final LoginService loginService;

    public AuthController(LoginService loginService) {
        this.loginService = loginService;
    }

    /** 200 si la connexion réussit, 401 dans tous les cas d'échec. */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest http) {
        LoginResult result = loginService.authenticate(request.email(), request.password(), http.getRemoteAddr());
        if (result == LoginResult.SUCCESS) {
            return ResponseEntity.ok(new LoginResponse(result.name(), "Connexion réussie"));
        }
        String message = result == LoginResult.INVALID_EMAIL_FORMAT ? FORMAT_MESSAGE : LoginService.ERROR_MESSAGE;
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginResponse(result.name(), message));
    }
}
