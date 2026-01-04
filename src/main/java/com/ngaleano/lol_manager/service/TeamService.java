package com.ngaleano.lol_manager.service;

import java.util.List;

import com.ngaleano.lol_manager.model.Team;

public interface TeamService {

    List<Team> getAllTeams();

    Team createTeam(Team team, Long userId);

}
