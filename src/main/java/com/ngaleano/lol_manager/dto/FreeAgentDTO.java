package com.ngaleano.lol_manager.dto;

public record FreeAgentDTO(
                Long playerId,
                String summonerName,
                String tagLine,
                String soloRank,
                String flexRank,
                String primaryRole,
                String secondaryRole,
                String discordUser) {
}