package com.ngaleano.lol_manager.service;

import java.util.List;

import com.ngaleano.lol_manager.model.Team;

import com.ngaleano.lol_manager.dto.TeamResponseDTO;

public interface TeamService {

    List<TeamResponseDTO> getAllTeams();

    Team createTeam(Team team, Long userId);

}
