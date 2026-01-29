package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.dto.LinkAccountResponseDTO;
import com.ngaleano.lol_manager.dto.RegisterUserDTO;
import com.ngaleano.lol_manager.dto.RiotAccountDTO;
import com.ngaleano.lol_manager.dto.SummonerDTO;
import com.ngaleano.lol_manager.dto.UserProfileDTO;
import com.ngaleano.lol_manager.model.Player;
import com.ngaleano.lol_manager.model.Role;
import com.ngaleano.lol_manager.model.User;
import com.ngaleano.lol_manager.repository.PlayerRepository;
import com.ngaleano.lol_manager.repository.UserRepository;
import com.ngaleano.lol_manager.exception.BusinessRuleException;
import com.ngaleano.lol_manager.exception.ResourceNotFoundException;

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
    public void registerUser(RegisterUserDTO dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new BusinessRuleException("El email ya se encuentra registrado.");
        }

        if (dto.nickname() != null && userRepository.existsByNickname(dto.nickname())) {
            throw new BusinessRuleException("El nickname ya esta en uso.");
        }

        validatePassword(dto.password());

        User user = new User();
        user.setEmail(dto.email());
        user.setPassword(dto.password());
        user.setNickname(dto.nickname());
        user.setRole(Role.USER);

        userRepository.save(user);
    }

    private void validatePassword(String password) {
        if (password == null) {
            throw new BusinessRuleException("La contraseña es requerida.");
        }
        String regex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).+$";
        if (!password.matches(regex)) {
            throw new BusinessRuleException("La contraseña debe tener al menos una mayuscula, un numero y un signo.");
        }
    }

    @Override
    @Transactional
    public LinkAccountResponseDTO startLinkAccount(Long userId, String summonerName, String tagLine) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado (ID: " + userId + ")"));

        RiotAccountDTO riotAccount = riotApiService.getAccountRiot(summonerName, tagLine);
        String incomingPuuid = riotAccount.puuid();

        Optional<Player> playerOcupado = playerRepository.findByPuuid(incomingPuuid);

        if (playerOcupado.isPresent()) {
            Player p = playerOcupado.get();
            boolean esMiCuenta = (user.getPlayer() != null && user.getPlayer().getId().equals(p.getId()));

            if (!esMiCuenta) {
                throw new BusinessRuleException(
                        "Esta cuenta de Riot ya esta vinculada a otro usuario.");
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

        player.setSoloRank("UNRANKED");
        player.setFlexRank("UNRANKED");
        player.setPrimaryRole("FILL");
        player.setSecondaryRole("FILL");

        int randomIconId = new Random().nextInt(29);
        player.setVerificationIconId(randomIconId);

        user.setPlayer(player);
        userRepository.save(user);

        return new LinkAccountResponseDTO(
                player.getSummonerName(),
                player.getTagLine(),
                player.getVerificationIconId(),
                player.getVerificationIconUrl());
    }

    @Override
    public boolean verifyLinkAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado (ID: " + userId + ")"));

        Player player = user.getPlayer();

        if (player == null || player.getVerificationIconId() == null) {
            throw new BusinessRuleException(
                    "No se ha iniciado el proceso de verificacion para este usuario.");
        }

        SummonerDTO summonerData = riotApiService.getSummonerByPuuid(player.getPuuid());

        if (summonerData.profileIconId() == player.getVerificationIconId()) {
            player.setVerified(true);
            player.setVerificationIconId(null);
            userRepository.save(user);
            return true;
        } else {
            throw new BusinessRuleException(
                    "Fallo la verificacion. Icono esperado: " + player.getVerificationIconId()
                            + ", encontrado: " + summonerData.profileIconId());
        }
    }

    @Override
    public UserProfileDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado (ID: " + userId + ")"));

        Player player = user.getPlayer();
        if (player == null) {
            return new UserProfileDTO(
                    user.getId(),
                    user.getNickname(),
                    "No vinculado",
                    null, null, null, null, null, null, null, false);
        }

        return new UserProfileDTO(
                user.getId(),
                user.getNickname(),
                player.getDiscordUser(),
                player.getSummonerName(),
                player.getTagLine(),
                player.getSoloRank(),
                player.getFlexRank(),
                player.getPrimaryRole(),
                player.getSecondaryRole(),
                player.getTeam() != null ? player.getTeam().getName() : null,
                player.getTeam() != null);
    }
}