package com.ngaleano.lol_manager.dto;

public record MatchPlayerDTO(
        Long id,
        String nickname,
        boolean hasCheckedIn) {
}
