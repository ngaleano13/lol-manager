package com.ngaleano.lol_manager.controller;

import com.ngaleano.lol_manager.dto.InvitationResponseDTO;
import com.ngaleano.lol_manager.service.InvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    @Autowired
    private InvitationService invitationService;

    @PostMapping("/invite/{targetPlayerId}")
    public ResponseEntity<String> sendInvite(@PathVariable Long targetPlayerId, @RequestParam Long myUserId) {
        invitationService.invitePlayer(myUserId, targetPlayerId);
        return ResponseEntity.ok("Invitacion enviada correctamente");
    }

    @GetMapping("/pending")
    public ResponseEntity<List<InvitationResponseDTO>> getMyInvitations(@RequestParam Long userId) {
        List<InvitationResponseDTO> invitations = invitationService.getPendingInvitations(userId);
        return ResponseEntity.ok(invitations);
    }

    // Para que el jugador responda a la invitacion
    @PostMapping("/{invitationId}/respond")
    public ResponseEntity<String> respondToInvite(@PathVariable Long invitationId,
            @RequestParam Long userId,
            @RequestParam boolean accept) {
        String result = invitationService.respondToInvitation(userId, invitationId, accept);
        return ResponseEntity.ok(result);
    }

    // Para que el administrador del equipo maneje la invitacion
    @PostMapping("/{invitationId}/manage")
    public ResponseEntity<String> manageRequest(@PathVariable Long invitationId,
            @RequestParam Long userId,
            @RequestParam boolean accept) {
        String result = invitationService.manageRequest(userId, invitationId, accept);
        return ResponseEntity.ok(result);
    }
}