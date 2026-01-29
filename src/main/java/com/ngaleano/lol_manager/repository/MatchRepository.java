package com.ngaleano.lol_manager.repository;

import com.ngaleano.lol_manager.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByTournamentId(Long tournamentId);

    List<Match> findByTournamentIdAndRound(Long tournamentId, String round);

    @Query("SELECT m FROM Match m WHERE m.status = 'PENDING' " +
            "AND m.firstReportTime IS NOT NULL " +
            "AND m.firstReportTime <= :limitTime")
    List<Match> findExpiredMatches(@Param("limitTime") LocalDateTime limitTime);

    @Query("SELECT m FROM Match m WHERE m.status = 'PENDING' AND m.scheduledTime <= :limitTime")
    List<Match> findPendingMatchesBefore(@Param("limitTime") LocalDateTime limitTime);
}
