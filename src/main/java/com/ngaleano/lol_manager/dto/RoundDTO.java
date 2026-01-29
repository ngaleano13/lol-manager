package com.ngaleano.lol_manager.dto;

import java.util.List;

public record RoundDTO(
        int roundNumber,
        String roundName,
        List<MatchDTO> matches) {
}
