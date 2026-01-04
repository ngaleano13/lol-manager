package com.ngaleano.lol_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ngaleano.lol_manager.model.Team;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

}
