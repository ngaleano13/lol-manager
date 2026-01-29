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

    private String soloRank;
    private String flexRank;

    private String primaryRole;
    private String secondaryRole;

    private boolean verified = false;
    private Integer verificationIconId;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    private String discordUser;
    private boolean lookingForTeam = true;

    @OneToOne(mappedBy = "player")
    private User user;

    public String getVerificationIconUrl() {
        if (this.verificationIconId == null) {
            return null;
        }
        return "https://ddragon.leagueoflegends.com/cdn/14.1.1/img/profileicon/" + this.verificationIconId + ".png";
    }

    public boolean hasTeam() {
        return this.team != null;
    }
}