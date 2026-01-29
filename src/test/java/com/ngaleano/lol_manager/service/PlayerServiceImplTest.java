package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.exception.BusinessRuleException;
import com.ngaleano.lol_manager.model.Player;
import com.ngaleano.lol_manager.model.User;
import com.ngaleano.lol_manager.repository.PlayerRepository;
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
class PlayerServiceImplTest {

    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RiotApiService riotApiService;

    @InjectMocks
    private PlayerServiceImpl playerService;

    @Test
    void updateDiscord_Success() {
        Long userId = 1L;
        User user = new User();
        Player player = new Player();
        user.setPlayer(player);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        playerService.updateDiscord(userId, "ds_user");

        assertEquals("ds_user", player.getDiscordUser());
        verify(playerRepository).save(player);
    }

    @Test
    void updateDiscord_Fail_NoPlayer() {
        Long userId = 1L;
        User user = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(BusinessRuleException.class, () -> playerService.updateDiscord(userId, "ds"));
    }

    @Test
    void updatePlayerRoles_Success() {
        Long userId = 1L;
        User user = new User();
        Player player = new Player();
        user.setPlayer(player);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        playerService.updatePlayerRoles(userId, "TOP", "MID");

        assertEquals("TOP", player.getPrimaryRole());
        assertEquals("MID", player.getSecondaryRole());
        verify(playerRepository).save(player);
    }

    @Test
    void updatePlayerRoles_Fail_SameRoles() {
        Long userId = 1L;
        User user = new User();
        Player player = new Player();
        user.setPlayer(player);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(BusinessRuleException.class, () -> playerService.updatePlayerRoles(userId, "TOP", "TOP"));
    }

    @Test
    void updatePlayerRoles_Fail_InvalidRole() {
        Long userId = 1L;
        User user = new User();
        Player player = new Player();
        user.setPlayer(player);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(BusinessRuleException.class, () -> playerService.updatePlayerRoles(userId, "INVALID", "MID"));
    }
}
