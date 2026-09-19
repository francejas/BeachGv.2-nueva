package com.zolea.backend.controllers;

import com.zolea.backend.dtos.auth.AuthRequest;
import com.zolea.backend.dtos.auth.AuthResponse;
import com.zolea.backend.dtos.auth.CurrentUserResponse;
import com.zolea.backend.security.UserPrincipal;
import com.zolea.backend.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login y obtención de JWT")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Devuelve un JWT y el ID del cliente autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token válido por una hora, más el identificador del cliente"),
            @ApiResponse(responseCode = "401",
                    description = "Credenciales incorrectas. La respuesta es la misma exista o no la cuenta:"
                            + " distinguirlas permitiría averiguar qué emails están registrados.",
                    content = @Content)
    })
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Quién está conectado, según el token que trae el pedido.
     *
     * <p>Es lo que permite recargar la página sin volver a iniciar sesión: el frontend guarda el
     * token, lo manda acá y recupera al usuario. La alternativa —abrirle el token por dentro al
     * cliente— funciona, pero hace que el frontend trate como dato propio algo que el servidor
     * firma para sí mismo, y que se quede con una foto vieja si la cuenta cambió.
     */
    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Quién está conectado",
            description = "Devuelve el usuario del token: sirve para recuperar la sesión al recargar la página.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "El usuario de esta sesión"),
            @ApiResponse(responseCode = "401", description = "Sin token, o con el token vencido", content = @Content)
    })
    public ResponseEntity<CurrentUserResponse> currentUser(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(authService.currentUser(principal));
    }
}
