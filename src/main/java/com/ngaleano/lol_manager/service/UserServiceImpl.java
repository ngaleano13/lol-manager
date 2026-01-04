package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.dto.RiotAccountDTO;
import com.ngaleano.lol_manager.dto.SummonerDTO;
import com.ngaleano.lol_manager.model.Player;
import com.ngaleano.lol_manager.model.User;
import com.ngaleano.lol_manager.repository.PlayerRepository;
import com.ngaleano.lol_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private RiotApiService riotApiService;

    @Override
    public User registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email ya registrado");
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public Player startLinkAccount(Long userId, String summonerName, String tagLine) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        RiotAccountDTO riotAccount = riotApiService.getAccountRiot(summonerName, tagLine);
        String incomingPuuid = riotAccount.puuid();

        Optional<Player> playerOcupado = playerRepository.findByPuuid(incomingPuuid);

        if (playerOcupado.isPresent()) {
            Player p = playerOcupado.get();

            boolean esMiCuenta = false;
            if (user.getPlayer() != null && user.getPlayer().getId().equals(p.getId())) {
                esMiCuenta = true;
            }

            if (!esMiCuenta) {
                throw new RuntimeException("Esta cuenta de LoL ya esta vinculada a otro usuario");
            }
        }

        Player player = user.getPlayer();
        if (player == null) {
            player = new Player();
        }

        player.setSummonerName(riotAccount.gameName());
        player.setTagLine(riotAccount.tagLine());
        player.setPuuid(riotAccount.puuid());
        player.setVerified(false);
        player.setRank("UNRANKED");
        player.setRole("FILL");

        int randomIconId = new Random().nextInt(29);
        player.setVerificationIconId(randomIconId);

        user.setPlayer(player);
        userRepository.save(user);

        return player;
    }

    @Override
    public boolean verifyLinkAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Player player = user.getPlayer();

        if (player == null || player.getVerificationIconId() == null) {
            throw new RuntimeException("No se inicio la verificacion");
        }

        SummonerDTO summonerData = riotApiService.getSummonerByPuuid(player.getPuuid());

        if (summonerData.profileIconId() == player.getVerificationIconId()) {
            player.setVerified(true);
            player.setVerificationIconId(null);

            userRepository.save(user);
            return true;
        } else {
            throw new RuntimeException(
                    "Error en la verificacion, El icono esperado era el ID: " + player.getVerificationIconId() +
                            ", pero se encontro el ID: " + summonerData.profileIconId() +
                            ". Por favor espera 30 segundos para verificar de nuevo.");
        }
    }
}