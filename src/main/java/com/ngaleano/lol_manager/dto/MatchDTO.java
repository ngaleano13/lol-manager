package com.ngaleano.lol_manager.dto;

import java.time.LocalDateTime;

public record MatchDTO(
        Long id,
        String teamAName,
        Long teamAId,
        String teamBName,
        Long teamBId,
        String round,
        Long winnerId,
        String winnerName,
        Long voteTeamA,
        Long voteTeamB,
        String status,
        LocalDateTime firstReportTime,
        LocalDateTime scheduledTime,
        Integer roundNumber,
        Integer matchIndex,
        java.util.List<MatchPlayerDTO> teamAPlayers,
        java.util.List<MatchPlayerDTO> teamBPlayers) {
}