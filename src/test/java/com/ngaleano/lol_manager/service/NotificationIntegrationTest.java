package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.model.Match;
import com.ngaleano.lol_manager.model.Player;
import com.ngaleano.lol_manager.model.Role;
import com.ngaleano.lol_manager.model.Team;
import com.ngaleano.lol_manager.model.User;
import com.ngaleano.lol_manager.dto.RegisterUserDTO;
import com.ngaleano.lol_manager.repository.UserRepository;
import com.ngaleano.lol_manager.repository.MatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationIntegrationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private MatchServiceImpl matchService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void registerUser_ShouldSetDefaultRole() {
        RegisterUserDTO dto = new RegisterUserDTO("test@email.com", "Pass1$word", "testnick");

        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.existsByNickname(any())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        userService.registerUser(dto);

        verify(userRepository).save(argThat(user -> user.getRole() == Role.USER));
    }

    @Test
    void requestAdmin_ShouldNotifyAdmins() {
        Long matchId = 1L;
        Long userId = 10L;

        Match match = new Match();
        match.setId(matchId);
        Team teamA = new Team();
        teamA.setId(100L);
        match.setTeamA(teamA);

        User user = new User();
        user.setId(userId);
        user.setNickname("Player1");
        Player p = new Player();
        p.setId(userId);
        p.setTeam(teamA);
        teamA.setLeader(p);

        user.setPlayer(p);
        User admin = new User();
        admin.setId(99L);
        admin.setRole(Role.ADMIN);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByRole(Role.ADMIN)).thenReturn(List.of(admin));

        matchService.requestAdmin(matchId, userId);

        assertTrue(match.isAdminRequested());
        verify(notificationService).sendNotification(eq(admin), contains("Disputa"), eq("DISPUTE"), eq(matchId));
    }
}
