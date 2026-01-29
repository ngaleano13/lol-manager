package com.ngaleano.lol_manager.dto;

public record LinkAccountResponseDTO(
        String summonerName,
        String tagLine,
        Integer verificationIconId,
        String iconUrl) {
}
