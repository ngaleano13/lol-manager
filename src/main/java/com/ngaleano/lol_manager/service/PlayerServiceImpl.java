package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.dto.FreeAgentDTO;
import com.ngaleano.lol_manager.mapper.PlayerMapper;
import com.ngaleano.lol_manager.model.Player;
import com.ngaleano.lol_manager.model.User;
import com.ngaleano.lol_manager.repository.PlayerRepository;
import com.ngaleano.lol_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.ngaleano.lol_manager.exception.BusinessRuleException;
import com.ngaleano.lol_manager.exception.ResourceNotFoundException;

import jakarta.persistence.criteria.Predicate;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PlayerServiceImpl implements PlayerService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerServiceImpl.class);

    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RiotApiService riotApiService;
    @Autowired
    private PlayerMapper playerMapper;

    @Override
    public void updateDiscord(Long userId, String discordUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado (ID: " + userId + ")"));

        Player player = user.getPlayer();
        if (player == null) {
            throw new BusinessRuleException(
                    "Debes vincular tu cuenta de Riot antes de actualizar Discord.");
        }

        player.setDiscordUser(discordUser);
        playerRepository.save(player);
    }

    @Override
    public void updatePlayerRanks(Long userId) {
        try {
            User user = userRepository.findById(userId).orElseThrow();
            Player player = user.getPlayer();

            if (player == null || player.getPuuid() == null) {
                return;
            }

            Map<String, String> ranks = riotApiService.getRanks(player.getPuuid());

            player.setSoloRank(ranks.get("SOLO"));
            player.setFlexRank(ranks.get("FLEX"));

            playerRepository.save(player);

        } catch (Exception e) {
            logger.error("Advertencia API Riot: {}", e.getMessage(), e);
        }
    }

    @Override
    public void updatePlayerRoles(Long userId, String primary, String secondary) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado"));

        Player player = user.getPlayer();

        if (player == null) {
            throw new BusinessRuleException("Primero vincula tu cuenta de Riot.");
        }

        String pRole = primary.toUpperCase();
        String sRole = secondary.toUpperCase();

        List<String> validRoles = Arrays.asList("TOP", "JUNGLE", "MID", "ADC", "SUPPORT", "FILL");

        if (!validRoles.contains(pRole) || !validRoles.contains(sRole)) {
            throw new BusinessRuleException(
                    "Rol invalido. Usa: TOP, JUNGLE, MID, ADC, SUPPORT, FILL");
        }

        if (pRole.equals(sRole) && !pRole.equals("FILL")) {
            throw new BusinessRuleException(
                    "El rol secundario debe ser distinto al primario (salvo FILL).");
        }

        player.setPrimaryRole(pRole);
        player.setSecondaryRole(sRole);

        playerRepository.save(player);
    }

    @Override
    public void toggleLookingForTeam(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Player player = user.getPlayer();
        if (player == null) {
            throw new BusinessRuleException("Tenes que vincular tu cuenta de Riot primero.");
        }
        player.setLookingForTeam(!player.isLookingForTeam());
        playerRepository.save(player);
    }

    @Override
    public List<FreeAgentDTO> getPlayers(Boolean lookingForTeam, String role, String rank) {
        Specification<Player> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (lookingForTeam != null) {
                predicates.add(cb.equal(root.get("lookingForTeam"), lookingForTeam));
            }

            if (role != null && !role.isEmpty()) {
                String r = role.toUpperCase();
                predicates.add(cb.or(
                        cb.equal(root.get("primaryRole"), r),
                        cb.equal(root.get("secondaryRole"), r)));
            }

            if (rank != null && !rank.isEmpty()) {
                String rKey = "%" + rank.toUpperCase() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("soloRank"), rKey),
                        cb.like(root.get("flexRank"), rKey)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return playerRepository.findAll(spec).stream()
                .map(playerMapper::toFreeAgentDto)
                .toList();
    }
}