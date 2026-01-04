package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.dto.RiotAccountDTO;
import com.ngaleano.lol_manager.dto.SummonerDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RiotApiService {

    private final RestClient accountClient;
    private final RestClient leagueClient;

    public RiotApiService(@Value("${riot.api.key}") String apiKey) {
        this.accountClient = RestClient.builder()
                .baseUrl("https://americas.api.riotgames.com")
                .defaultHeader("X-Riot-Token", apiKey)
                .build();
        this.leagueClient = RestClient.builder()
                .baseUrl("https://la2.api.riotgames.com")
                .defaultHeader("X-Riot-Token", apiKey)
                .build();
    }

    public RiotAccountDTO getAccountRiot(String gameName, String tagLine) {
        return accountClient.get()
                .uri("/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}", gameName, tagLine)
                .retrieve()
                .body(RiotAccountDTO.class);
    }

    public SummonerDTO getSummonerByPuuid(String puuid) {
        return leagueClient.get()
                .uri("/lol/summoner/v4/summoners/by-puuid/{puuid}", puuid)
                .retrieve()
                .body(SummonerDTO.class);
    }
}