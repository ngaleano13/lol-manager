package com.ngaleano.lol_manager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ngaleano.lol_manager.model.Player;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByPuuid(String puuid);

    boolean existsByIdAndTeamIsNotNull(Long id);
}
