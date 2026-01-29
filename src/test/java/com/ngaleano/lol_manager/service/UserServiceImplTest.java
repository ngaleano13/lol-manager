package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.dto.LinkAccountResponseDTO;
import com.ngaleano.lol_manager.dto.RegisterUserDTO;
import com.ngaleano.lol_manager.dto.RiotAccountDTO;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private RiotApiService riotApiService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void startLinkAccount() {
        Long userId = 1L;
        String gameName = "Faker";
        String tagLine = "KR1";
        String puuid = "puuid-12345";

        User user = new User();
        user.setId(userId);
        user.setEmail("test@test.com");

        RiotAccountDTO riotAccount = new RiotAccountDTO(puuid, gameName, tagLine);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(riotApiService.getAccountRiot(gameName, tagLine)).thenReturn(riotAccount);
        when(playerRepository.findByPuuid(puuid)).thenReturn(Optional.empty());

        LinkAccountResponseDTO result = userService.startLinkAccount(userId, gameName, tagLine);

        assertNotNull(result);
        assertEquals(gameName, result.summonerName());
        assertNotNull(result.verificationIconId());

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void startLinkAccount_LinkedToOther() {
        Long userId = 1L;
        String puuid = "puuid-occupied";

        User user = new User();
        user.setId(userId);

        Player existingPlayer = new Player();
        existingPlayer.setId(500L);
        existingPlayer.setPuuid(puuid);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(riotApiService.getAccountRiot(anyString(), anyString()))
                .thenReturn(new RiotAccountDTO(puuid, "Faker", "KR1"));
        when(playerRepository.findByPuuid(puuid)).thenReturn(Optional.of(existingPlayer));

        assertThrows(BusinessRuleException.class, () -> userService.startLinkAccount(userId, "Faker", "KR1"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_EmailExists() {
        RegisterUserDTO dto = new RegisterUserDTO("existing@test.com", "Pass1$word", "Nick");
        User user = new User();
        user.setEmail("existing@test.com");
        when(userRepository.findByEmail("existing@test.com")).thenReturn(Optional.of(user));

        assertThrows(BusinessRuleException.class, () -> userService.registerUser(dto));
    }

    @Test
    void registerUser_NicknameExists() {
        RegisterUserDTO dto = new RegisterUserDTO("new@test.com", "Pass1$word", "ExistingNick");
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.existsByNickname("ExistingNick")).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> userService.registerUser(dto));
    }

    @Test
    void registerUser_WeakPassword() {
        RegisterUserDTO dto = new RegisterUserDTO("valid@test.com", "weak", "ValidNick");

        when(userRepository.findByEmail("valid@test.com")).thenReturn(Optional.empty());
        when(userRepository.existsByNickname("ValidNick")).thenReturn(false);

        assertThrows(BusinessRuleException.class, () -> userService.registerUser(dto));
    }

    @Test
    void registerUser_Success() {
        RegisterUserDTO dto = new RegisterUserDTO("valid@test.com", "StrongP@ss1", "ValidNick");

        when(userRepository.findByEmail("valid@test.com")).thenReturn(Optional.empty());
        when(userRepository.existsByNickname("ValidNick")).thenReturn(false);

        userService.registerUser(dto);

        verify(userRepository).save(any(User.class));
    }
}