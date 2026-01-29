package com.ngaleano.lol_manager.dto;

public record InvitationResponseDTO(
                Long invitationId,
                String teamName,
                String teamTag,
                String inviterNick,
                String inviterDiscord,
                String status,
                String sentAt) {
}