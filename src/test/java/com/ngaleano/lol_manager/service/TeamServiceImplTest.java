package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.exception.BusinessRuleException;
import com.ngaleano.lol_manager.exception.ResourceNotFoundException;
import com.ngaleano.lol_manager.model.Player;
import com.ngaleano.lol_manager.model.Team;
import com.ngaleano.lol_manager.model.User;
import com.ngaleano.lol_manager.repository.PlayerRepository;
import com.ngaleano.lol_manager.repository.TeamRepository;
import com.ngaleano.lol_manager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceImplTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private TeamServiceImpl teamService;

    @Test
    void createTeam_Success() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        Player player = new Player();
        player.setId(10L);
        player.setVerified(true);
        user.setPlayer(player);

        Team team = new Team();
        team.setName("T1");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(playerRepository.existsByIdAndTeamIsNotNull(player.getId())).thenReturn(false);
        when(teamRepository.save(team)).thenReturn(team);

        Team result = teamService.createTeam(team, userId);

        assertNotNull(result);
        assertEquals("T1", result.getName());
        verify(playerRepository).save(player);
    }

    @Test
    void createTeam_UserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        Team team = new Team();

        assertThrows(ResourceNotFoundException.class, () -> teamService.createTeam(team, 99L));
    }

    @Test
    void createTeam_PlayerNotVerified() {
        Long userId = 1L;
        User user = new User();
        Player player = new Player();
        player.setVerified(false);
        user.setPlayer(player);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Team team = new Team();

        assertThrows(BusinessRuleException.class, () -> teamService.createTeam(team, userId));
    }

    @Test
    void createTeam_PlayerAlreadyHasTeam() {
        Long userId = 1L;
        User user = new User();
        Player player = new Player();
        player.setId(10L);
        player.setVerified(true);
        user.setPlayer(player);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(playerRepository.existsByIdAndTeamIsNotNull(player.getId())).thenReturn(true);
        Team team = new Team();

        assertThrows(BusinessRuleException.class, () -> teamService.createTeam(team, userId));
    }
}
