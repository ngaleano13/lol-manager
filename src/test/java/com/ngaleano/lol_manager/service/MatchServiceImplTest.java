package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.exception.BusinessRuleException;
import com.ngaleano.lol_manager.model.Match;
import com.ngaleano.lol_manager.model.Player;
import com.ngaleano.lol_manager.model.Team;
import com.ngaleano.lol_manager.model.User;
import com.ngaleano.lol_manager.repository.MatchRepository;
import com.ngaleano.lol_manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MatchServiceImplTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private MatchServiceImpl matchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void checkIn_Success_Individual() {
        Long matchId = 1L;
        Long userId = 10L;
        Match match = new Match();
        match.setId(matchId);
        match.setScheduledTime(LocalDateTime.now().plusMinutes(10));

        User user = new User();
        user.setId(userId);
        Player player = new Player();
        player.setId(100L);
        user.setPlayer(player);

        Team teamA = new Team();
        teamA.setId(20L);
        teamA.setPlayers(List.of(player));
        player.setTeam(teamA);
        match.setTeamA(teamA);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        matchService.checkIn(matchId, userId);

        assertTrue(match.getCheckedInPlayers().contains(100L));
        assertTrue(match.isCheckInTeamA());
        verify(matchRepository).save(match);
    }

    @Test
    void checkIn_Fail_NotInTeam() {
        Long matchId = 1L;
        Long userId = 10L;
        Match match = new Match();
        match.setScheduledTime(LocalDateTime.now().plusMinutes(10));

        User user = new User();
        Player player = new Player();
        player.setTeam(new Team());
        user.setPlayer(player);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(BusinessRuleException.class, () -> matchService.checkIn(matchId, userId));
    }

    @Test
    void requestAdmin_Fail_NotLeader() {
        Long matchId = 1L;
        Long userId = 10L;

        Match match = new Match();
        Team team = new Team();
        team.setId(1L);
        Player leader = new Player();
        leader.setId(99L);
        team.setLeader(leader);
        match.setTeamA(team);

        User user = new User();
        user.setId(userId);
        Player player = new Player();
        player.setId(100L);
        player.setTeam(team);
        user.setPlayer(player);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(BusinessRuleException.class, () -> matchService.requestAdmin(matchId, userId));
    }

    @Test
    void requestAdmin_Success() {
        Long matchId = 1L;
        Long userId = 10L;

        Match match = new Match();
        Team team = new Team();
        team.setId(1L);
        Player leader = new Player();
        leader.setId(100L);
        leader.setDiscordUser("Leader#1234");
        team.setLeader(leader);
        match.setTeamA(team);

        User user = new User();
        user.setId(userId);
        user.setNickname("LeaderUser");
        user.setPlayer(leader);
        leader.setTeam(team);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User admin = new User();
        when(userRepository.findByRole(any())).thenReturn(List.of(admin));

        matchService.requestAdmin(matchId, userId);

        verify(notificationService).sendNotification(eq(admin), contains("LeaderUser"), eq("DISPUTE"), eq(matchId));
    }
}
