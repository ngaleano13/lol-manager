package com.ngaleano.lol_manager.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ngaleano.lol_manager.dto.RiotAccountDTO;
import com.ngaleano.lol_manager.model.Player;
import com.ngaleano.lol_manager.model.Team;
import com.ngaleano.lol_manager.repository.PlayerRepository;
import com.ngaleano.lol_manager.repository.TeamRepository;

@Service
public class PlayerServiceImpl implements PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private RiotApiService riotApiService;

    @Autowired
    private TeamRepository teamRepository;

    @Override
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    @Override
    public Player savePlayer(Player player) {

        try {
            RiotAccountDTO cuentaRiot = riotApiService.getAccountRiot(
                    player.getSummonerName(),
                    player.getTagLine());

            player.setPuuid(cuentaRiot.puuid());

        } catch (Exception e) {
            throw new RuntimeException("Error al guardar el jugador", e);
        }

        return playerRepository.save(player);

    }

    @Override
    public Player assignTeam(Long playerId, Long teamId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
        player.setTeam(team);
        return playerRepository.save(player);
    }
}
