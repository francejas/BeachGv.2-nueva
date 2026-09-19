package com.zolea.backend.services;

import com.zolea.backend.dtos.amenity.AmenityRequest;
import com.zolea.backend.dtos.amenity.AmenityResponse;
import com.zolea.backend.exceptions.amenity.AmenityNotFoundException;
import com.zolea.backend.exceptions.amenity.InvalidAmenityException;
import com.zolea.backend.models.Amenity;
import com.zolea.backend.models.Resort;
import com.zolea.backend.repositories.AmenityRepository;
import com.zolea.backend.repositories.ResortRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p><b>Transacciones:</b> la clase entera es de solo lectura y cada método que escribe lo declara.
 * Antes no había ninguna transacción declarada acá: el mapeo a DTO recorría colecciones LAZY fuera
 * de toda transacción y funcionaba únicamente porque {@code spring.jpa.open-in-view} dejaba la
 * sesión de base abierta durante toda la petición. Es decir, la transacción existía pero no la
 * había decidido nadie, y su límite lo ponía un filtro web.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AmenityService {

    private final AmenityRepository amenityRepository;
    private final ResortRepository resortRepository;

    public List<AmenityResponse> getAllAmenities() {
        return amenityRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList(); //
    }

    public AmenityResponse getAmenityById(Long id) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new AmenityNotFoundException("Amenity con id " + id + " no encontrada"));
        return mapToResponse(amenity);
    }

    @Transactional
    public AmenityResponse createAmenity(AmenityRequest request) {
        validateAmenity(request);
        Amenity amenity = new Amenity();
        amenity.setName(request.name());

        return mapToResponse(amenityRepository.save(amenity));
    }

    @Transactional
    public AmenityResponse updateAmenity(Long id, AmenityRequest request) {
        validateAmenity(request);
        Amenity existingAmenity = amenityRepository.findById(id)
                .orElseThrow(() -> new AmenityNotFoundException("Amenity con id " + id + " no encontrada"));

        existingAmenity.setName(request.name());
        return mapToResponse(amenityRepository.save(existingAmenity));
    }

    /**
     * Borra un servicio y lo saca de los balnearios que lo ofrecían.
     *
     * <p>Desvincularlo primero no es prolijidad: la relación entre balneario y servicio vive en una
     * tabla intermedia, y borrar una fila a la que esa tabla apunta falla con un error de integridad.
     * Antes de esto, borrar un servicio que algún balneario ofreciera —que es el caso normal—
     * terminaba en un error en lugar de en un borrado.
     *
     * <p>La alternativa habría sido negarse a borrarlo mientras esté en uso. Se eligió desvincular
     * porque es lo que el administrador quiere decir cuando borra un servicio del catálogo: que deje
     * de existir, no que se lo saque uno por uno de los cinco balnearios antes.
     */
    @Transactional
    public void deleteAmenity(Long id) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new AmenityNotFoundException("Amenity con id " + id + " no encontrada"));

        for (Resort resort : resortRepository.findByAmenities_IdAmenity(id)) {
            resort.getAmenities().removeIf(a -> a.getIdAmenity().equals(id));
        }

        amenityRepository.delete(amenity);
    }

    private void validateAmenity(AmenityRequest request) {
        if (request.name() == null || request.name().trim().isEmpty()) {
            throw new InvalidAmenityException("El nombre de la amenity no puede estar vacío");
        }
        if (request.name().length() < 3) {
            throw new InvalidAmenityException("El nombre debe tener al menos 3 caracteres");
        }
    }

    // MAPPER interno
    private AmenityResponse mapToResponse(Amenity amenity) {
        return new AmenityResponse(amenity.getIdAmenity(), amenity.getName());
    }
}