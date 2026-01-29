package com.ngaleano.lol_manager.dto;

import java.util.List;

public record TeamResponseDTO(
        Long id,
        String name,
        String tag,
        List<PlayerSummaryDTO> players) {
}
