package com.ngaleano.lol_manager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del torneo es requerido")
    private String name;

    @Column(length = 1000)
    private String description;

    @FutureOrPresent(message = "La fecha de inicio debe ser futura o presente")
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private TournamentStatus status;

    @Enumerated(EnumType.STRING)
    private MatchmakingStrategy matchmakingStrategy;

    @ManyToMany
    @JoinTable(name = "tournament_teams", joinColumns = @JoinColumn(name = "tournament_id"), inverseJoinColumns = @JoinColumn(name = "team_id"))
    private List<Team> teams = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    @ManyToOne
    @JoinColumn(name = "winner_id")
    private Team winner;

    @Min(value = 2, message = "El torneo debe tener al menos 2 equipos")
    private int maxTeams = 8;
    private int minRank = 0;

    public TournamentStatus getStatus() {
        return status;
    }

    public void setStatus(TournamentStatus status) {
        this.status = status;
    }

    public MatchmakingStrategy getMatchmakingStrategy() {
        return matchmakingStrategy;
    }

    public void setMatchmakingStrategy(MatchmakingStrategy matchmakingStrategy) {
        this.matchmakingStrategy = matchmakingStrategy;
    }
}