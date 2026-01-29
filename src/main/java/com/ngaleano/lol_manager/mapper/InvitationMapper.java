package com.ngaleano.lol_manager.mapper;

import com.ngaleano.lol_manager.dto.InvitationResponseDTO;
import com.ngaleano.lol_manager.model.Invitation;
import org.springframework.stereotype.Component;

@Component
public class InvitationMapper {

    public InvitationResponseDTO toDto(Invitation invitation) {
        if (invitation == null) {
            return null;
        }

        String nick = (invitation.getSender() != null) ? invitation.getSender().getSummonerName() : "Desconocido";
        String discord = (invitation.getSender() != null) ? invitation.getSender().getDiscordUser() : "No disponible";

        return new InvitationResponseDTO(
                invitation.getId(),
                invitation.getTeam().getName(),
                invitation.getTeam().getTag(),
                nick,
                discord,
                invitation.getStatus().toString(),
                invitation.getCreatedAt().toString());
    }
}