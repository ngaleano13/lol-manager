package com.ngaleano.lol_manager.repository;

import com.ngaleano.lol_manager.model.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ngaleano.lol_manager.model.TournamentStatus;
import com.ngaleano.lol_manager.model.User;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    boolean existsByName(String name);

    boolean existsByCreatorAndStatusNot(User creator, TournamentStatus status);

}