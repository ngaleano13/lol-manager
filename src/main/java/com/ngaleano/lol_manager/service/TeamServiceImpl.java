package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.model.Player;
import com.ngaleano.lol_manager.model.Team;
import com.ngaleano.lol_manager.model.User;
import com.ngaleano.lol_manager.repository.PlayerRepository;
import com.ngaleano.lol_manager.repository.TeamRepository;
import com.ngaleano.lol_manager.repository.UserRepository;

import com.ngaleano.lol_manager.exception.BusinessRuleException;
import com.ngaleano.lol_manager.exception.ResourceNotFoundException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ngaleano.lol_manager.dto.PlayerSummaryDTO;
import com.ngaleano.lol_manager.dto.TeamResponseDTO;

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
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado (ID: " + userId + ")"));

        Player player = user.getPlayer();

        if (player == null || !player.isVerified()) {
            throw new BusinessRuleException(
                    "Debes verificar tu cuenta de Riot antes de crear un equipo.");
        }
        boolean yaTieneEquipo = playerRepository.existsByIdAndTeamIsNotNull(player.getId());

        if (yaTieneEquipo) {
            String nombreEquipo = player.getTeam() != null ? player.getTeam().getName() : "un equipo";
            throw new BusinessRuleException(
                    "Ya perteneces a " + nombreEquipo + ". No puedes crear otro.");
        }

        team.setLeader(player);
        Team savedTeam = teamRepository.save(team);

        player.setTeam(savedTeam);
        playerRepository.save(player);

        return savedTeam;
    }

    @Override
    public List<TeamResponseDTO> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(team -> new TeamResponseDTO(
                        team.getId(),
                        team.getName(),
                        team.getTag(),
                        team.getPlayers().stream()
                                .map(p -> new PlayerSummaryDTO(p.getId(), p.getSummonerName(), p.getTagLine()))
                                .toList()))
                .toList();
    }

}