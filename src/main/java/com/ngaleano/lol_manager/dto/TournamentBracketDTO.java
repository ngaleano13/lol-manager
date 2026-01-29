package com.ngaleano.lol_manager.dto;

import java.util.List;

public record TournamentBracketDTO(
                Long tournamentId,
                String tournamentName,
                List<RoundDTO> rounds) {
}
