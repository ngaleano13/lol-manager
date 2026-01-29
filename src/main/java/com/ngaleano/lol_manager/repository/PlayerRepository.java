package com.ngaleano.lol_manager.repository;

import com.ngaleano.lol_manager.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long>, JpaSpecificationExecutor<Player> {

        Optional<Player> findByPuuid(String puuid);

        boolean existsByIdAndTeamIsNotNull(Long id);

        @Query("SELECT p FROM Player p WHERE " +
                        "(:role IS NULL OR p.primaryRole = :role OR p.secondaryRole = :role) AND " +
                        "(:rank IS NULL OR p.soloRank LIKE :rank OR p.flexRank LIKE :rank) AND " +
                        "p.team IS NULL AND " +
                        "p.lookingForTeam = true")
        List<Player> findFreeAgents(@Param("role") String role, @Param("rank") String rank);
}