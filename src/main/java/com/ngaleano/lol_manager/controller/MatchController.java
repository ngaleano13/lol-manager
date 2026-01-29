package com.ngaleano.lol_manager.controller;

import com.ngaleano.lol_manager.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    @Autowired
    private MatchService matchService;

    @PostMapping("/{matchId}/check-in")
    public ResponseEntity<String> checkIn(@PathVariable Long matchId, @RequestParam Long userId) {
        matchService.checkIn(matchId, userId);
        return ResponseEntity.ok("Check-in exitoso.");
    }

    @PostMapping("/{matchId}/walkover")
    public ResponseEntity<String> claimWalkover(@PathVariable Long matchId, @RequestParam Long userId) {
        matchService.claimWalkover(matchId, userId);
        return ResponseEntity.ok("Walkover reclamado con exito. Victoria asignada.");
    }

    @PostMapping("/{matchId}/admin")
    public ResponseEntity<String> requestAdmin(@PathVariable Long matchId, @RequestParam Long userId) {
        matchService.requestAdmin(matchId, userId);
        return ResponseEntity.ok("Solicitud de admin enviada. Se ha notificado a los administradores.");
    }

    @PostMapping("/{matchId}/resolve")
    public ResponseEntity<String> resolveMatch(
            @PathVariable Long matchId,
            @RequestParam Long winnerId,
            @RequestParam Long adminId) {
        matchService.resolveMatch(matchId, winnerId, adminId);
        return ResponseEntity.ok("Partido resuelto por administrador.");
    }
}
