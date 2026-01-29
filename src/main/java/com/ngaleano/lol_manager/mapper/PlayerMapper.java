package com.ngaleano.lol_manager.mapper;

import com.ngaleano.lol_manager.dto.FreeAgentDTO;
import com.ngaleano.lol_manager.model.Player;
import org.springframework.stereotype.Component;

@Component
public class PlayerMapper {

    public FreeAgentDTO toFreeAgentDto(Player player) {
        if (player == null) {
            return null;
        }

        return new FreeAgentDTO(
                player.getId(),
                player.getSummonerName(),
                player.getTagLine(),
                player.getSoloRank(),
                player.getFlexRank(),
                player.getPrimaryRole(),
                player.getSecondaryRole(),
                player.getDiscordUser());
    }
}