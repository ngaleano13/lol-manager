package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.dto.InvitationResponseDTO;
import com.ngaleano.lol_manager.mapper.InvitationMapper;
import com.ngaleano.lol_manager.model.*;
import com.ngaleano.lol_manager.repository.InvitationRepository;
import com.ngaleano.lol_manager.repository.PlayerRepository;
import com.ngaleano.lol_manager.repository.TeamRepository;
import com.ngaleano.lol_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ngaleano.lol_manager.exception.BusinessRuleException;
import com.ngaleano.lol_manager.exception.ResourceNotFoundException;
import java.util.List;

@Service
public class InvitationServiceImpl implements InvitationService {

    @Autowired
    private InvitationRepository invitationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private InvitationMapper invitationMapper;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void invitePlayer(Long hostUserId, Long targetPlayerId) {
        User hostUser = userRepository.findById(hostUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado"));

        Player senderPlayer = hostUser.getPlayer();
        Team myTeam = senderPlayer.getTeam();

        if (myTeam == null) {
            throw new BusinessRuleException("No tienes equipo para invitar.");
        }

        Player targetPlayer = playerRepository.findById(targetPlayerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Jugador destino no encontrado"));

        validateInvitationRules(myTeam, targetPlayer);

        Invitation invitation = new Invitation();
        invitation.setTeam(myTeam);
        invitation.setSender(senderPlayer);
        invitation.setInvitedPlayer(targetPlayer);
        invitation.setStatus(InvitationStatus.PENDING);

        invitationRepository.save(invitation);

        userRepository.findByPlayerId(targetPlayer.getId()).ifPresent(user -> {
            notificationService.sendNotification(
                    user,
                    "Has recibido una invitacion para unirte al equipo '" + myTeam.getName() + "'",
                    "INVITATION",
                    invitation.getId());
        });
    }

    @Override
    public List<InvitationResponseDTO> getPendingInvitations(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Player player = user.getPlayer();

        if (player == null)
            return List.of();

        List<Invitation> invitations = invitationRepository.findByInvitedPlayerIdAndStatus(player.getId(),
                InvitationStatus.PENDING);

        return invitations.stream()
                .map(invitationMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public String respondToInvitation(Long userId, Long invitationId, boolean accept) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invitacion no encontrada"));

        User user = userRepository.findById(userId).orElseThrow();

        if (!invitation.getInvitedPlayer().getId().equals(user.getPlayer().getId())) {
            throw new BusinessRuleException("Esta invitacion no es para vos.");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BusinessRuleException("Ya respondiste a esta invitacion.");
        }

        if (accept) {
            Team team = invitation.getTeam();

            if (team.getPlayers().size() >= 6) {
                invitation.setStatus(InvitationStatus.REJECTED);
                invitationRepository.save(invitation);
                throw new BusinessRuleException("El equipo esta lleno.");
            }

            Player player = user.getPlayer();
            player.setTeam(team);
            playerRepository.save(player);

            invitation.setStatus(InvitationStatus.ACCEPTED);
            invitationRepository.save(invitation);

            return "Te uniste al equipo correctamente";

        } else {
            invitation.setStatus(InvitationStatus.REJECTED);
            invitationRepository.save(invitation);
            return "Invitacion rechazada";
        }
    }

    private void validateInvitationRules(Team team, Player targetPlayer) {
        if (team.isFull()) {
            throw new BusinessRuleException("El equipo esta lleno.");
        }
        if (targetPlayer.hasTeam()) {
            throw new BusinessRuleException("El jugador ya tiene equipo.");
        }

        boolean alreadyInvited = invitationRepository.existsByTeamIdAndInvitedPlayerIdAndStatus(
                team.getId(), targetPlayer.getId(), InvitationStatus.PENDING);

        if (alreadyInvited) {
            throw new BusinessRuleException("Ya existe una invitacion pendiente.");
        }
    }

    @Override
    @Transactional
    public void requestJoinTeam(Long userId, Long teamId) {
        User user = userRepository.findById(userId).orElseThrow();
        Player player = user.getPlayer();

        if (player.hasTeam()) {
            throw new BusinessRuleException("Ya tienes equipo.");
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado"));

        if (team.isFull()) {
            throw new BusinessRuleException("El equipo esta lleno.");
        }

        boolean alreadyRequested = invitationRepository.existsByTeamIdAndSenderIdAndTypeAndStatus(
                teamId, player.getId(), InvitationType.REQUEST, InvitationStatus.PENDING);

        if (alreadyRequested) {
            throw new BusinessRuleException("Ya enviaste una solicitud a este equipo.");
        }

        Invitation invitation = new Invitation();
        invitation.setTeam(team);
        invitation.setSender(player);
        invitation.setInvitedPlayer(null);
        invitation.setType(InvitationType.REQUEST);
        invitation.setStatus(InvitationStatus.PENDING);

        invitationRepository.save(invitation);
    }

    @Override
    public List<InvitationResponseDTO> getPendingRequests(Long userId, Long teamId) {
        User user = userRepository.findById(userId).orElseThrow();
        Player player = user.getPlayer();
        if (player.getTeam() == null || !player.getTeam().getId().equals(teamId)) {
            throw new BusinessRuleException("No perteneces a este equipo.");
        }

        return invitationRepository
                .findByTeamIdAndTypeAndStatus(teamId, InvitationType.REQUEST, InvitationStatus.PENDING)
                .stream()
                .map(invitationMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public String manageRequest(Long userId, Long invitationId, boolean accept) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

        if (invitation.getType() != InvitationType.REQUEST) {
            throw new BusinessRuleException("Esto no es una solicitud de union.");
        }

        User user = userRepository.findById(userId).orElseThrow();
        Player actingPlayer = user.getPlayer();

        if (actingPlayer.getTeam() == null || !actingPlayer.getTeam().getId().equals(invitation.getTeam().getId())) {
            throw new BusinessRuleException("No tienes permiso para gestionar esta solicitud.");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BusinessRuleException("Esta solicitud ya fue procesada.");
        }

        if (accept) {
            Team team = invitation.getTeam();
            if (team.isFull()) {
                invitation.setStatus(InvitationStatus.REJECTED);
                invitationRepository.save(invitation);
                throw new BusinessRuleException("El equipo esta lleno.");
            }

            Player applicant = invitation.getSender();
            if (applicant.hasTeam()) {
                invitation.setStatus(InvitationStatus.REJECTED);
                invitationRepository.save(invitation);
                throw new BusinessRuleException("El solicitante ya tiene equipo.");
            }

            applicant.setTeam(team);
            playerRepository.save(applicant);

            invitation.setStatus(InvitationStatus.ACCEPTED);
            invitationRepository.save(invitation);
            return "Solicitud aceptada. El jugador se unio al equipo.";
        } else {
            invitation.setStatus(InvitationStatus.REJECTED);
            invitationRepository.save(invitation);
            return "Solicitud rechazada.";
        }
    }
}