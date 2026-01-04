package com.ngaleano.lol_manager.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String puuid;
    private String summonerName;
    private String tagLine;

    @Column(name = "player_rank")
    private String rank;

    @Column(name = "game_role")
    private String role;

    private boolean verified = false;

    private Integer verificationIconId;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    public String getVerificationIconUrl() {
        if (this.verificationIconId == null) {
            return null;
        }
        return "http://ddragon.leagueoflegends.com/cdn/13.24.1/img/profileicon/" + this.verificationIconId + ".png";
    }

}