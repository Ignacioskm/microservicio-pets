package com.vitalpet.mspets.controller;

import com.vitalpet.mspets.dto.PetRequestDTO;
import com.vitalpet.mspets.dto.PetResponseDTO;
import com.vitalpet.mspets.dto.SpeciesResponseDTO;
import com.vitalpet.mspets.service.PetService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    // -----------------------------------------
    // Funciones auxiliares de autorizacion
    // -----------------------------------------

    private boolean hasRole(
            Authentication authentication,
            String role
    ) {
        return authentication != null &&
                authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(("ROLE_" + role)::equals);
    }

    private boolean isAdmin(Authentication authentication) {
        return hasRole(authentication, "ADMIN");
    }

    private boolean isInternalService(Authentication authentication) {
        return hasRole(authentication, "SERVICE");
    }

    private boolean isOwner(
            Authentication authentication,
            Long ownerId
    ) {
        if (authentication == null || ownerId == null) {
            return false;
        }

        Object principal = authentication.getPrincipal();

        return principal instanceof Long userId &&
                userId.equals(ownerId);
    }

    private boolean canAccessPet(
            Authentication authentication,
            PetResponseDTO pet
    ) {
        return isAdmin(authentication) ||
               isInternalService(authentication) ||
               isOwner(authentication, pet.getOwnerId());
    }

    // -----------------------------------------
    // Catalogo de especies - PUBLICO
    // -----------------------------------------

    @GetMapping("/species")
    public ResponseEntity<List<SpeciesResponseDTO>> getAllSpecies() {
        return ResponseEntity.ok(petService.getAllSpecies());
    }

    // -----------------------------------------
    // Listado general - SOLO ADMIN
    // -----------------------------------------

    @GetMapping
    public ResponseEntity<List<PetResponseDTO>> getAll() {
        return ResponseEntity.ok(petService.getAll());
    }

    // -----------------------------------------
    // Mascotas disponibles
    // CLIENT o ADMIN autenticado
    // -----------------------------------------

    @GetMapping("/available")
    public ResponseEntity<List<PetResponseDTO>> getAvailablePets() {
        return ResponseEntity.ok(petService.getAvailablePets());
    }

    // -----------------------------------------
    // Obtener mascota por ID
    // Propietario, ADMIN o servicio interno
    // -----------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<PetResponseDTO> getById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        PetResponseDTO pet = petService.getById(id);

        if (!canAccessPet(authentication, pet)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(pet);
    }

    // -----------------------------------------
    // Crear mascota
    // CLIENT para si mismo o ADMIN
    // -----------------------------------------

    @PostMapping
    public ResponseEntity<PetResponseDTO> create(
            @Valid @RequestBody PetRequestDTO dto,
            Authentication authentication
    ) {
        if (!isAdmin(authentication) &&
            !isOwner(authentication, dto.getOwnerId())) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(petService.create(dto));
    }

    // -----------------------------------------
    // Modificar mascota
    // Propietario actual o ADMIN
    // El CLIENT no puede cambiar ownerId
    // -----------------------------------------

    @PutMapping("/{id}")
    public ResponseEntity<PetResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PetRequestDTO dto,
            Authentication authentication
    ) {
        PetResponseDTO existingPet = petService.getById(id);

        if (!isAdmin(authentication)) {

            if (!isOwner(authentication, existingPet.getOwnerId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (!isOwner(authentication, dto.getOwnerId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.ok(petService.update(id, dto));
    }

    // -----------------------------------------
    // Desactivar mascota
    // Propietario o ADMIN
    // -----------------------------------------

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Authentication authentication
    ) {
        PetResponseDTO existingPet = petService.getById(id);

        if (!isAdmin(authentication) &&
            !isOwner(authentication, existingPet.getOwnerId())) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        petService.deactivate(id);

        return ResponseEntity.noContent().build();
    }

    // -----------------------------------------
    // Listar mascotas por propietario
    // Solo ese propietario o ADMIN
    // -----------------------------------------

    @GetMapping("/{id}/owner")
    public ResponseEntity<List<PetResponseDTO>> getPetsByOwnerId(
            @PathVariable Long id,
            Authentication authentication
    ) {
        if (!isAdmin(authentication) &&
            !isOwner(authentication, id)) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(petService.getByOwnerId(id));
    }

    // -----------------------------------------
    // Verificar existencia
    // ADMIN o servicio interno
    // -----------------------------------------

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> petExists(@PathVariable Long id) {
        return ResponseEntity.ok(petService.petExists(id));
    }

    // -----------------------------------------
    // Reasignar propietario - SOLO ADMIN
    // La restriccion se aplica en SecurityConfig
    // -----------------------------------------

    @PutMapping("/{petId}/owner/{userId}")
    public ResponseEntity<Void> assignOwner(
            @PathVariable Long petId,
            @PathVariable Long userId
    ) {
        petService.assignOwner(petId, userId);
        return ResponseEntity.noContent().build();
    }
}

