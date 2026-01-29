package com.ngaleano.lol_manager.controller;

import com.ngaleano.lol_manager.dto.MatchDTO;
import com.ngaleano.lol_manager.dto.TournamentBracketDTO;
import com.ngaleano.lol_manager.dto.TournamentDTO;
import com.ngaleano.lol_manager.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    @PostMapping
    public ResponseEntity<TournamentDTO> createTournament(@RequestBody @Valid TournamentDTO tournamentDTO) {
        TournamentDTO created = tournamentService.createTournament(tournamentDTO);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/{tournamentId}/register")
    public ResponseEntity<String> registerTeam(@PathVariable Long tournamentId, @RequestParam Long teamId) {
        tournamentService.registerTeam(tournamentId, teamId);
        return ResponseEntity.ok("Equipo registrado correctamente en el torneo.");
    }

    @PostMapping("/{tournamentId}/start")
    public ResponseEntity<String> startTournament(@PathVariable Long tournamentId) {
        tournamentService.startTournament(tournamentId);
        return ResponseEntity.ok("¡Torneo iniciado! Se han generado los cruces.");
    }

    @GetMapping("/{tournamentId}/matches")
    public ResponseEntity<List<MatchDTO>> getMatches(@PathVariable Long tournamentId) {
        List<MatchDTO> fixture = tournamentService.getFixture(tournamentId);
        return ResponseEntity.ok(fixture);
    }

    @GetMapping("/{tournamentId}/brackets")
    public ResponseEntity<TournamentBracketDTO> getBrackets(
            @PathVariable Long tournamentId) {
        return ResponseEntity.ok(tournamentService.getTournamentBrackets(tournamentId));
    }

    @GetMapping("/match/{matchId}")
    public ResponseEntity<MatchDTO> getMatch(@PathVariable Long matchId) {
        MatchDTO match = tournamentService.getMatchById(matchId);
        return ResponseEntity.ok(match);
    }

    @GetMapping
    public ResponseEntity<List<TournamentDTO>> getAllTournaments() {
        return ResponseEntity.ok(tournamentService.getAllTournaments());
    }

    @PostMapping("/match/{matchId}/report")
    public ResponseEntity<String> reportMatchResult(
            @PathVariable Long matchId,
            @RequestParam Long myTeamId,
            @RequestParam Long winnerId,
            @RequestParam Long userId) {

        String message = tournamentService.reportResult(matchId, myTeamId, winnerId, userId);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/matches/{matchId}/schedule")
    public ResponseEntity<String> scheduleMatch(
            @PathVariable Long matchId,
            @RequestParam Long userId,
            @RequestBody LocalDateTime scheduledTime) {
        tournamentService.scheduleMatch(matchId, userId, scheduledTime);
        return ResponseEntity.ok("Partido programado exitosamente.");
    }
}