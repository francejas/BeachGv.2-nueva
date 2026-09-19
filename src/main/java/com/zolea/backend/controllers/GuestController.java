package com.zolea.backend.controllers;

import com.zolea.backend.dtos.guest.GuestValidationResponse;
import com.zolea.backend.services.GuestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guests")
@RequiredArgsConstructor
@Tag(name = "Huéspedes", description = "Validación de ingreso por QR o DNI")
public class GuestController {

    private final GuestService guestService;

    @PostMapping("/validate/{token}")
    @Operation(summary = "Validar ingreso por token QR", description = "Público. Marca el ingreso del huésped y devuelve sus datos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingreso registrado. El código queda usado y no sirve una segunda vez"),
            @ApiResponse(responseCode = "400", description = "La reserva no está paga, no empezó todavía, ya venció, o el código ya se usó", content = @Content),
            @ApiResponse(responseCode = "404", description = "El código no existe", content = @Content)
    })
    public ResponseEntity<GuestValidationResponse> validateGuestEntry(@PathVariable String token) {
        return ResponseEntity.ok(guestService.validateGuestEntry(token));
    }

    @PostMapping("/validate/dni/{dni}")
    @Operation(summary = "Validar ingreso por DNI", description = "Público. Alternativa al QR para validar ingreso.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingreso registrado"),
            @ApiResponse(responseCode = "400", description = "Hay una reserva con ese DNI, pero no está vigente hoy o ya registró su ingreso", content = @Content),
            @ApiResponse(responseCode = "404", description = "No hay ninguna reserva confirmada con ese DNI", content = @Content)
    })
    public ResponseEntity<GuestValidationResponse> validateGuestEntryByDni(@PathVariable String dni) {
        return ResponseEntity.ok(guestService.validateGuestEntryByDni(dni));
    }
}
