package com.ngaleano.lol_manager.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record TournamentDTO(
        Long id,
        @NotBlank(message = "El nombre del torneo es requerido") String name,
        String description,
        @FutureOrPresent(message = "La fecha de inicio no puede ser en el pasado") LocalDateTime startDate,
        LocalDateTime endDate,
        String status,
        String matchmakingStrategy,
        @Min(value = 2, message = "El minimo de equipos es 2") Integer maxTeams,
        Integer currentTeamsCount,
        java.util.List<String> registeredTeams,
        Long creatorId) {
}