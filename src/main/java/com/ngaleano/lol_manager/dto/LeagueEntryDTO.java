package com.ngaleano.lol_manager.dto;

public record LeagueEntryDTO(
        String queueType,
        String tier,
        String rank,
        int leaguePoints) {
}