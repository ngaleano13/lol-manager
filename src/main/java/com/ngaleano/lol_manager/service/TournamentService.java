package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.dto.MatchDTO;
import com.ngaleano.lol_manager.dto.TournamentBracketDTO;
import com.ngaleano.lol_manager.dto.TournamentDTO;
import java.util.List;

public interface TournamentService {
    TournamentDTO createTournament(TournamentDTO dto);

    List<TournamentDTO> getAllTournaments();

    void openRegistration(Long tournamentId);

    void registerTeam(Long tournamentId, Long teamId);

    TournamentDTO startTournament(Long tournamentId);

    void scheduleMatch(Long matchId, Long userId, java.time.LocalDateTime scheduledTime);

    List<MatchDTO> getFixture(Long tournamentId);

    TournamentBracketDTO getTournamentBrackets(Long tournamentId);

    MatchDTO getMatchById(Long matchId);

    String reportResult(Long matchId, Long reportingTeamId, Long claimedWinnerId, Long userId);

    void advanceWinner(Long matchId);
}