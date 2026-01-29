package com.ngaleano.lol_manager.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne
    @JoinColumn(name = "sender_player_id")
    private Player sender;

    @ManyToOne
    @JoinColumn(name = "invited_player_id")
    private Player invitedPlayer;

    @Enumerated(EnumType.STRING)
    private InvitationStatus status = InvitationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private InvitationType type = InvitationType.INVITATION;

    private LocalDateTime createdAt = LocalDateTime.now();
}