package com.ngaleano.lol_manager.controller;

import com.ngaleano.lol_manager.dto.InvitationResponseDTO;
import com.ngaleano.lol_manager.dto.TeamResponseDTO;
import com.ngaleano.lol_manager.model.Team;
import com.ngaleano.lol_manager.service.InvitationService;
import com.ngaleano.lol_manager.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private InvitationService invitationService;

    @GetMapping
    public ResponseEntity<List<TeamResponseDTO>> getAllTeams() {
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @PostMapping
    public ResponseEntity<Team> createTeam(@RequestBody @Valid Team team, @RequestParam Long userId) {
        Team newTeam = teamService.createTeam(team, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTeam);
    }

    @PostMapping("/{teamId}/join-request")
    public ResponseEntity<String> requestJoin(@PathVariable Long teamId, @RequestParam Long userId) {
        invitationService.requestJoinTeam(userId, teamId);
        return ResponseEntity.ok("Solicitud enviada correctamente.");
    }

    @GetMapping("/{teamId}/requests")
    public ResponseEntity<List<InvitationResponseDTO>> getJoinRequests(
            @PathVariable Long teamId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(invitationService.getPendingRequests(userId, teamId));
    }
}