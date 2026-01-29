package com.ngaleano.lol_manager.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "team_a_id")
    private Team teamA;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "team_b_id")
    private Team teamB;

    private String round;
    private Integer roundNumber;
    private Integer matchIndex;

    private Long winnerId;

    private Long voteTeamA;
    private Long voteTeamB;

    @Enumerated(EnumType.STRING)
    private MatchStatus status = MatchStatus.PENDING;

    private LocalDateTime firstReportTime;

    private LocalDateTime scheduledTime;

    private boolean checkInTeamA = false;
    private boolean checkInTeamB = false;

    private boolean adminRequested = false;
    private boolean walkoverClaimed = false;

    @ElementCollection
    private java.util.Set<Long> checkedInPlayers = new java.util.HashSet<>();

    public Match() {
    }

    public Match(Tournament tournament, Team teamA, Team teamB, String round, Integer roundNumber, Integer matchIndex) {
        this.tournament = tournament;
        this.teamA = teamA;
        this.teamB = teamB;
        this.round = round;
        this.roundNumber = roundNumber;
        this.matchIndex = matchIndex;
        this.status = MatchStatus.PENDING;
    }
}