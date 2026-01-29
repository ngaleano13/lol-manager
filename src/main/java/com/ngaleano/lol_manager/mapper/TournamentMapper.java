package com.ngaleano.lol_manager.mapper;

import com.ngaleano.lol_manager.dto.MatchDTO;
import com.ngaleano.lol_manager.dto.TournamentDTO;
import com.ngaleano.lol_manager.model.Match;
import com.ngaleano.lol_manager.model.MatchStatus;
import com.ngaleano.lol_manager.model.Tournament;
import com.ngaleano.lol_manager.model.MatchmakingStrategy;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class TournamentMapper {

    public TournamentDTO toDto(Tournament t) {
        if (t == null)
            return null;
        return new TournamentDTO(
                t.getId(),
                t.getName(),
                t.getDescription(),
                t.getStartDate(),
                t.getEndDate(),
                t.getStatus() != null ? t.getStatus().name() : "REGISTRO",
                t.getMatchmakingStrategy() != null ? t.getMatchmakingStrategy().name() : "ALEATORIO",
                t.getMaxTeams(),
                t.getTeams() != null ? t.getTeams().size() : 0,
                t.getTeams() != null
                        ? t.getTeams().stream().map(com.ngaleano.lol_manager.model.Team::getName).toList()
                        : java.util.List.of(),
                t.getCreator() != null ? t.getCreator().getId() : null);
    }

    public Tournament toEntity(TournamentDTO dto) {
        if (dto == null)
            return null;
        Tournament t = new Tournament();
        t.setName(dto.name());
        t.setDescription(dto.description());
        t.setMaxTeams(dto.maxTeams() != null ? dto.maxTeams() : 8);
        t.setStartDate(dto.startDate());
        t.setEndDate(dto.endDate());

        if (dto.matchmakingStrategy() != null) {
            try {
                t.setMatchmakingStrategy(MatchmakingStrategy.valueOf(dto.matchmakingStrategy()));
            } catch (IllegalArgumentException e) {
                t.setMatchmakingStrategy(MatchmakingStrategy.RANDOM);
            }
        } else {
            t.setMatchmakingStrategy(MatchmakingStrategy.RANDOM);
        }

        return t;
    }

    public MatchDTO toMatchDTO(Match match) {
        if (match == null)
            return null;

        String winnerName = null;
        if (match.getWinnerId() != null) {
            if (match.getTeamA() != null && match.getWinnerId().equals(match.getTeamA().getId())) {
                winnerName = match.getTeamA().getName();
            } else if (match.getTeamB() != null && match.getWinnerId().equals(match.getTeamB().getId())) {
                winnerName = match.getTeamB().getName();
            }
        }

        return new MatchDTO(
                match.getId(),
                match.getTeamA() != null ? match.getTeamA().getName() : "Por definir",
                match.getTeamA() != null ? match.getTeamA().getId() : null,
                match.getTeamB() != null ? match.getTeamB().getName() : "Por definir",
                match.getTeamB() != null ? match.getTeamB().getId() : null,
                match.getRound(),
                match.getWinnerId(),
                winnerName,
                match.getVoteTeamA(),
                match.getVoteTeamB(),
                determineDisplayStatus(match),
                match.getFirstReportTime(),
                match.getScheduledTime(),
                match.getRoundNumber(),
                match.getMatchIndex(),
                mapPlayers(match.getTeamA(), match.getCheckedInPlayers()),
                mapPlayers(match.getTeamB(), match.getCheckedInPlayers()));
    }

    private String determineDisplayStatus(Match match) {
        if (match.getStatus() == MatchStatus.COMPLETED) {
            return "Completado";
        }
        if (match.getStatus() == MatchStatus.DISPUTE) {
            return "En Disputa";
        }

        if (match.isCheckInTeamA() && match.isCheckInTeamB()) {
            return "En partida";
        }

        if (match.getScheduledTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime windowStart = match.getScheduledTime().minusMinutes(20);

            if (now.isBefore(windowStart)) {
                return "Proximamente";
            }
            return "Pendiente confirmación";
        }

        return "Proximamente";
    }

    private java.util.List<com.ngaleano.lol_manager.dto.MatchPlayerDTO> mapPlayers(
            com.ngaleano.lol_manager.model.Team team, java.util.Set<Long> checkedInPlayers) {
        if (team == null || team.getPlayers() == null) {
            return java.util.List.of();
        }
        return team.getPlayers().stream()
                .map(p -> new com.ngaleano.lol_manager.dto.MatchPlayerDTO(
                        p.getId(),
                        p.getUser().getNickname(), // Assuming Player has User and User has Nickname. Check Player model
                                                   // if unsure.
                        checkedInPlayers != null && checkedInPlayers.contains(p.getId())))
                .toList();
    }
}