package com.ngaleano.lol_manager.repository;

import com.ngaleano.lol_manager.model.Invitation;
import com.ngaleano.lol_manager.model.InvitationStatus;
import com.ngaleano.lol_manager.model.InvitationType;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

        List<Invitation> findByInvitedPlayerIdAndStatus(Long playerId, InvitationStatus status);

        List<Invitation> findByTeamIdAndTypeAndStatus(Long teamId, InvitationType type, InvitationStatus status);

        boolean existsByTeamIdAndInvitedPlayerIdAndStatus(Long teamId, Long playerId, InvitationStatus status);

        boolean existsByTeamIdAndSenderIdAndTypeAndStatus(Long teamId, Long senderId, InvitationType type,
                        InvitationStatus status);
}