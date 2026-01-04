package com.ngaleano.lol_manager.service;

import java.util.List;
import com.ngaleano.lol_manager.model.Player;

public interface PlayerService {

    List<Player> getAllPlayers();

    Player savePlayer(Player player);

    Player assignTeam(Long playerId, Long teamId);
}
