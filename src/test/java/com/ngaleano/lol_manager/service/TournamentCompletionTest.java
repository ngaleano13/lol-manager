package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.model.Match;
import com.ngaleano.lol_manager.model.MatchStatus;
import com.ngaleano.lol_manager.model.Player;
import com.ngaleano.lol_manager.model.Team;
import com.ngaleano.lol_manager.model.Tournament;
import com.ngaleano.lol_manager.model.TournamentStatus;
import com.ngaleano.lol_manager.model.User;
import com.ngaleano.lol_manager.repository.MatchRepository;
import com.ngaleano.lol_manager.repository.TeamRepository;
import com.ngaleano.lol_manager.repository.TournamentRepository;
import com.ngaleano.lol_manager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TournamentCompletionTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private TournamentServiceImpl tournamentService;

    @Test
    void testTournamentCompletion() {
        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.setName("Test Tournament");
        tournament.setStatus(TournamentStatus.IN_PROGRESS);

        Team teamA = new Team();
        teamA.setId(10L);
        teamA.setName("Team A");
        Player providerA = new Player();
        providerA.setId(100L);
        teamA.setPlayers(new ArrayList<>(List.of(providerA)));
        teamA.setLeader(providerA);

        Team teamB = new Team();
        teamB.setId(20L);
        teamB.setName("Team B");
        Player providerB = new Player();
        providerB.setId(200L);
        teamB.setPlayers(new ArrayList<>(List.of(providerB)));
        teamB.setLeader(providerB);

        tournament.setTeams(new ArrayList<>(Arrays.asList(teamA, teamB)));

        Match finalMatch = new Match(tournament, teamA, teamB, "Final", 1, 0);
        finalMatch.setId(50L);
        finalMatch.setStatus(MatchStatus.PENDING);

        when(matchRepository.findById(50L)).thenReturn(Optional.of(finalMatch));
        when(matchRepository.findByTournamentId(1L)).thenReturn(List.of(finalMatch));
        when(teamRepository.findById(10L)).thenReturn(Optional.of(teamA));

        User userA = new User();
        userA.setId(1000L);
        userA.setPlayer(providerA);
        providerA.setTeam(teamA);

        when(userRepository.findByPlayerId(100L)).thenReturn(Optional.of(userA));
        when(userRepository.findById(1000L)).thenReturn(Optional.of(userA));

        finalMatch.setVoteTeamB(10L);
        tournamentService.reportResult(50L, 10L, 10L, 1000L);

        verify(tournamentRepository).save(tournament);
        assert tournament.getStatus() == TournamentStatus.FINISHED;
        assert tournament.getWinner() == teamA;
        verify(notificationService).sendNotification(eq(userA), anyString(), eq("TOURNAMENT_END"), eq(1L));
    }
}
