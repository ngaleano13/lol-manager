package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.model.Player;
import com.ngaleano.lol_manager.model.Team;
import com.ngaleano.lol_manager.model.User;
import com.ngaleano.lol_manager.repository.PlayerRepository;
import com.ngaleano.lol_manager.repository.TeamRepository;
import com.ngaleano.lol_manager.repository.UserRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamServiceImpl implements TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Override
    @Transactional
    public Team createTeam(Team team, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Player player = user.getPlayer();

        if (player == null || !player.isVerified()) {
            throw new RuntimeException("Debes verificar tu cuenta de LoL antes de crear un equipo.");
        }
        boolean yaTieneEquipo = playerRepository.existsByIdAndTeamIsNotNull(player.getId());

        if (yaTieneEquipo) {
            String nombreEquipo = player.getTeam() != null ? player.getTeam().getName() : "un equipo";
            throw new RuntimeException("Ya perteneces a " + nombreEquipo + ". No podes crear otro.");
        }
        Team savedTeam = teamRepository.save(team);

        player.setTeam(savedTeam);
        playerRepository.save(player);

        return savedTeam;
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

}