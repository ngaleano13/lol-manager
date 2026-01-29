package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.dto.InvitationResponseDTO;
import java.util.List;

public interface InvitationService {

    void invitePlayer(Long hostUserId, Long targetPlayerId);

    void requestJoinTeam(Long userId, Long teamId);

    List<InvitationResponseDTO> getPendingInvitations(Long userId);

    List<InvitationResponseDTO> getPendingRequests(Long userId, Long teamId);

    String respondToInvitation(Long userId, Long invitationId, boolean accept);

    String manageRequest(Long userId, Long invitationId, boolean accept);
}