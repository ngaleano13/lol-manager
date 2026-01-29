package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.dto.LeagueEntryDTO;
import com.ngaleano.lol_manager.dto.RiotAccountDTO;
import com.ngaleano.lol_manager.dto.SummonerDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class RiotApiService {

    @Value("${riot.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public RiotAccountDTO getAccountRiot(String gameName, String tagLine) {
        String url = "https://americas.api.riotgames.com/riot/account/v1/accounts/by-riot-id/" + gameName + "/"
                + tagLine + "?api_key=" + apiKey;
        return restTemplate.getForObject(url, RiotAccountDTO.class);
    }

    public SummonerDTO getSummonerByPuuid(String puuid) {
        String url = "https://la2.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/" + puuid + "?api_key=" + apiKey;
        return restTemplate.getForObject(url, SummonerDTO.class);
    }

    public Map<String, String> getRanks(String puuid) {
        String url = "https://la2.api.riotgames.com/lol/league/v4/entries/by-puuid/" + puuid + "?api_key=" + apiKey;

        LeagueEntryDTO[] entries = restTemplate.getForObject(url, LeagueEntryDTO[].class);

        Map<String, String> ranks = new HashMap<>();
        ranks.put("SOLO", "UNRANKED");
        ranks.put("FLEX", "UNRANKED");

        if (entries != null) {
            for (LeagueEntryDTO entry : entries) {
                String fullRank = entry.tier() + " " + entry.rank();

                if ("RANKED_SOLO_5x5".equals(entry.queueType())) {
                    ranks.put("SOLO", fullRank);
                } else if ("RANKED_FLEX_SR".equals(entry.queueType())) {
                    ranks.put("FLEX", fullRank);
                }
            }
        }
        return ranks;
    }
}