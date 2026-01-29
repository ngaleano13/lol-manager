package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.exception.BusinessRuleException;
import com.ngaleano.lol_manager.model.Invitation;
import com.ngaleano.lol_manager.model.InvitationStatus;
import com.ngaleano.lol_manager.model.Player;
import com.ngaleano.lol_manager.model.Team;
import com.ngaleano.lol_manager.model.User;
import com.ngaleano.lol_manager.repository.InvitationRepository;
import com.ngaleano.lol_manager.repository.PlayerRepository;
import com.ngaleano.lol_manager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvitationServiceImplTest {

    @Mock
    private InvitationRepository invitationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private InvitationServiceImpl invitationService;

    @Test
    void invitePlayer_Success() {
        User hostUser = new User();
        Player sender = new Player();
        Team team = new Team();
        team.setId(1L);
        team.setPlayers(new ArrayList<>());
        sender.setTeam(team);
        hostUser.setPlayer(sender);

        Player target = new Player();
        target.setId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(hostUser));
        when(playerRepository.findById(2L)).thenReturn(Optional.of(target));
        when(invitationRepository.existsByTeamIdAndInvitedPlayerIdAndStatus(1L, 2L, InvitationStatus.PENDING))
                .thenReturn(false);

        User targetUser = new User();
        targetUser.setId(20L);
        when(userRepository.findByPlayerId(2L)).thenReturn(Optional.of(targetUser));

        assertDoesNotThrow(() -> invitationService.invitePlayer(1L, 2L));
        verify(invitationRepository).save(any(Invitation.class));
        verify(notificationService).sendNotification(any(), anyString(), eq("INVITATION"), any());
    }

    @Test
    void invitePlayer_Fail_NoTeam() {
        User hostUser = new User();
        Player sender = new Player();
        hostUser.setPlayer(sender);

        when(userRepository.findById(1L)).thenReturn(Optional.of(hostUser));

        assertThrows(BusinessRuleException.class, () -> invitationService.invitePlayer(1L, 2L));
    }

    @Test
    void respondToInvitation_Accept_Success() {
        User user = new User();
        Player player = new Player();
        player.setId(10L);
        user.setPlayer(player);

        Invitation invitation = new Invitation();
        invitation.setId(100L);
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setInvitedPlayer(player);
        Team team = new Team();
        team.setPlayers(new ArrayList<>());
        invitation.setTeam(team);

        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        String response = invitationService.respondToInvitation(1L, 100L, true);

        assertEquals("Te uniste al equipo correctamente", response);
        assertEquals(InvitationStatus.ACCEPTED, invitation.getStatus());
        verify(playerRepository).save(player);
    }

    @Test
    void respondToInvitation_Reject_Success() {
        User user = new User();
        Player player = new Player();
        player.setId(10L);
        user.setPlayer(player);

        Invitation invitation = new Invitation();
        invitation.setId(100L);
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setInvitedPlayer(player);

        when(invitationRepository.findById(100L)).thenReturn(Optional.of(invitation));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        String response = invitationService.respondToInvitation(1L, 100L, false);

        assertEquals("Invitacion rechazada", response);
        assertEquals(InvitationStatus.REJECTED, invitation.getStatus());
    }
}
