package com.ngaleano.lol_manager.controller;

import com.ngaleano.lol_manager.dto.FreeAgentDTO;
import com.ngaleano.lol_manager.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    @Autowired
    private PlayerService playerService;

    @PutMapping("/discord")
    public ResponseEntity<String> updateDiscord(@RequestParam Long userId, @RequestParam String discord) {
        playerService.updateDiscord(userId, discord);
        return ResponseEntity.ok("Discord actualizado correctamente");
    }

    @PutMapping("/roles")
    public ResponseEntity<String> setRoles(@RequestParam Long userId,
            @RequestParam String primary,
            @RequestParam String secondary) {
        playerService.updatePlayerRoles(userId, primary, secondary);
        return ResponseEntity.ok("Roles actualizados");
    }

    @PostMapping("/refresh-ranks")
    public ResponseEntity<String> refreshRanks(@RequestParam Long userId) {
        playerService.updatePlayerRanks(userId);
        return ResponseEntity.ok("Rangos actualizados desde Riot");
    }

    @GetMapping
    public ResponseEntity<List<FreeAgentDTO>> getPlayers(
            @RequestParam(required = false) Boolean lookingForTeam,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String rank) {
        return ResponseEntity.ok(playerService.getPlayers(lookingForTeam, role, rank));
    }

    @PostMapping("/looking-for-team")
    public ResponseEntity<String> toggleLookingForTeam(@RequestParam Long userId) {
        playerService.toggleLookingForTeam(userId);
        return ResponseEntity.ok("Estado actualizado correctamente");
    }
}