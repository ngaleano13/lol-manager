package com.ngaleano.lol_manager.task;

import com.ngaleano.lol_manager.model.Match;
import com.ngaleano.lol_manager.model.MatchStatus;
import com.ngaleano.lol_manager.model.Role;
import com.ngaleano.lol_manager.model.Team;
import com.ngaleano.lol_manager.model.User;
import com.ngaleano.lol_manager.repository.MatchRepository;
import com.ngaleano.lol_manager.repository.UserRepository;
import com.ngaleano.lol_manager.service.NotificationService;
import com.ngaleano.lol_manager.service.TournamentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TournamentScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TournamentScheduler.class);

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TournamentService tournamentService;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void runScheduledTasks() {
        processWalkovers();
        processStuckVotes();
    }

    private void processWalkovers() {
        LocalDateTime now = LocalDateTime.now();
        List<Match> expiredMatches = matchRepository.findPendingMatchesBefore(now);

        List<User> admins = userRepository
                .findByRole(Role.ADMIN);

        for (Match match : expiredMatches) {
            boolean teamAReady = match.isCheckInTeamA();
            boolean teamBReady = match.isCheckInTeamB();
            if (teamAReady && teamBReady) {
                continue;
            }

            if (!teamAReady && !teamBReady) {
                handleDoubleNoShow(match, admins);
            } else if (teamAReady && !teamBReady) {
                resolveWalkover(match, match.getTeamA());
            } else if (!teamAReady && teamBReady) {
                resolveWalkover(match, match.getTeamB());
            }
        }
    }

    private void processStuckVotes() {
        LocalDateTime limitTime = LocalDateTime.now().minusMinutes(5);
        List<Match> stuckMatches = matchRepository.findExpiredMatches(limitTime);

        for (Match match : stuckMatches) {
            Long autoWinner = (match.getVoteTeamA() != null) ? match.getVoteTeamA() : match.getVoteTeamB();
            if (autoWinner != null) {
                finalizeMatch(match, autoWinner);
                logger.info("Partido {} autoconfirmado por inactividad del rival (voto atrapado).", match.getId());
            }
        }
    }

    private void resolveWalkover(Match match, Team winner) {
        match.setWinnerId(winner.getId());
        match.setStatus(MatchStatus.COMPLETED);
        match.setWalkoverClaimed(true);
        matchRepository.save(match);
        logger.info("Partido {} resuelto por Walkover automatico. Ganador: {}", match.getId(), winner.getName());

        tournamentService.advanceWinner(match.getId());
    }

    private void finalizeMatch(Match match, Long winnerId) {
        match.setWinnerId(winnerId);
        match.setStatus(MatchStatus.COMPLETED);
        matchRepository.save(match);
        tournamentService.advanceWinner(match.getId());
    }

    private void handleDoubleNoShow(Match match, List<User> admins) {
        match.setStatus(MatchStatus.DISPUTE);
        match.setAdminRequested(true);
        matchRepository.save(match);

        String msg = "Partido #" + match.getId() + " sin presencia de ningun equipo. Requiere intervencion.";

        for (User admin : admins) {
            notificationService.sendNotification(admin, msg, "DISPUTE", match.getId());
        }

        if (match.getTournament() != null && match.getTournament().getCreator() != null) {
            notificationService.sendNotification(match.getTournament().getCreator(), msg, "DISPUTE", match.getId());
        }

        logger.warn("Partido {} marcado en Disputa por ausencia de ambos equipos.", match.getId());
    }
}