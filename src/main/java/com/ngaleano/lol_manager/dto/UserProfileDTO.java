package com.ngaleano.lol_manager.dto;

public record UserProfileDTO(
        Long userId,
        String nickname,
        String discordUser,
        String summonerName,
        String tagLine,
        String soloRank,
        String flexRank,
        String primaryRole,
        String secondaryRole,
        String teamName,
        boolean hasTeam) {
}
