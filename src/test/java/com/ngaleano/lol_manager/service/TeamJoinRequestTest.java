package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.model.Invitation;
import com.ngaleano.lol_manager.model.InvitationStatus;
import com.ngaleano.lol_manager.model.InvitationType;
import com.ngaleano.lol_manager.model.Player;
import com.ngaleano.lol_manager.model.Team;
import com.ngaleano.lol_manager.model.User;
import com.ngaleano.lol_manager.repository.InvitationRepository;
import com.ngaleano.lol_manager.repository.PlayerRepository;
import com.ngaleano.lol_manager.repository.TeamRepository;
import com.ngaleano.lol_manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
public class TeamJoinRequestTest {

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    private User requesterUser;
    private User leaderUser;
    private Team team;

    @BeforeEach
    void setUp() {
        requesterUser = new User();
        requesterUser.setEmail("requester@test.com");
        requesterUser.setPassword("password123");
        requesterUser.setNickname("Requester");
        userRepository.save(requesterUser);

        Player requesterPlayer = new Player();
        requesterPlayer.setSummonerName("RequesterSummoner");
        requesterPlayer.setVerified(true);
        playerRepository.save(requesterPlayer);
        requesterUser.setPlayer(requesterPlayer);
        userRepository.save(requesterUser);

        leaderUser = new User();
        leaderUser.setEmail("leader@test.com");
        leaderUser.setPassword("password123");
        leaderUser.setNickname("Leader");
        userRepository.save(leaderUser);

        Player leaderPlayer = new Player();
        leaderPlayer.setSummonerName("LeaderSummoner");
        leaderPlayer.setVerified(true);
        playerRepository.save(leaderPlayer);
        leaderUser.setPlayer(leaderPlayer);
        userRepository.save(leaderUser);

        team = new Team();
        team.setName("Test Team");
        team.setTag("TEST");
        teamRepository.save(team);

        leaderPlayer.setTeam(team);
        playerRepository.save(leaderPlayer);
    }

    @Test
    void testFullJoinRequestFlow() {
        invitationService.requestJoinTeam(requesterUser.getId(), team.getId());

        List<Invitation> pending = invitationRepository.findByTeamIdAndTypeAndStatus(
                team.getId(), InvitationType.REQUEST, InvitationStatus.PENDING);
        assertEquals(1, pending.size());
        Invitation request = pending.get(0);
        assertEquals(requesterUser.getPlayer().getId(), request.getSender().getId());

        String result = invitationService.manageRequest(leaderUser.getId(), request.getId(), true);
        assertEquals("Solicitud aceptada. El jugador se unio al equipo.", result);

        Player updatedRequester = playerRepository.findById(requesterUser.getPlayer().getId()).get();
        assertNotNull(updatedRequester.getTeam());
        assertEquals(team.getId(), updatedRequester.getTeam().getId());

        Invitation updatedInvitation = invitationRepository.findById(request.getId()).get();
        assertEquals(InvitationStatus.ACCEPTED, updatedInvitation.getStatus());
    }
}
