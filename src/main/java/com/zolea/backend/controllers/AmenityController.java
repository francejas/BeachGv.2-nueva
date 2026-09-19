package com.zolea.backend.controllers;

import com.zolea.backend.dtos.amenity.AmenityRequest;
import com.zolea.backend.dtos.amenity.AmenityResponse;
import com.zolea.backend.services.AmenityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/amenities")
@RequiredArgsConstructor
@Tag(name = "Amenidades", description = "Servicios e instalaciones disponibles en los balnearios")
public class AmenityController {

    private final AmenityService amenityService;

    @GetMapping
    @Operation(summary = "Listar todas las amenidades", description = "Público.")
    public ResponseEntity<List<AmenityResponse>> getAllAmenities() {
        return ResponseEntity.ok(amenityService.getAllAmenities());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener amenidad por ID", description = "Público.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "El servicio pedido"),
            @ApiResponse(responseCode = "404", description = "No existe un servicio con ese identificador", content = @Content)
    })
    public ResponseEntity<AmenityResponse> getAmenityById(@PathVariable Long id) {
        return ResponseEntity.ok(amenityService.getAmenityById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Crear amenidad (Admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "El servicio creado"),
            @ApiResponse(responseCode = "400", description = "El nombre viene vacío", content = @Content),
            @ApiResponse(responseCode = "401", description = "Sin token, o con el token vencido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Hace falta rol de administrador", content = @Content)
    })
    public ResponseEntity<AmenityResponse> createAmenity(@RequestBody AmenityRequest request) {
        return new ResponseEntity<>(amenityService.createAmenity(request), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Actualizar amenidad (Admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "El servicio como quedó"),
            @ApiResponse(responseCode = "400", description = "El nombre viene vacío", content = @Content),
            @ApiResponse(responseCode = "401", description = "Sin token, o con el token vencido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Hace falta rol de administrador", content = @Content),
            @ApiResponse(responseCode = "404", description = "No existe un servicio con ese identificador", content = @Content)
    })
    public ResponseEntity<AmenityResponse> updateAmenity(@PathVariable Long id, @RequestBody AmenityRequest request) {
        return ResponseEntity.ok(amenityService.updateAmenity(id, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Eliminar amenidad (Admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Borrado. Si algún balneario lo ofrecía, queda desvinculado"),
            @ApiResponse(responseCode = "401", description = "Sin token, o con el token vencido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Hace falta rol de administrador", content = @Content),
            @ApiResponse(responseCode = "404", description = "No existe un servicio con ese identificador", content = @Content)
    })
    public ResponseEntity<Void> deleteAmenity(@PathVariable Long id) {
        amenityService.deleteAmenity(id);
        return ResponseEntity.noContent().build();
    }
}
