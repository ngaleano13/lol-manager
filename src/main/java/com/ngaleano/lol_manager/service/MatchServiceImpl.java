package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.exception.BusinessRuleException;
import com.ngaleano.lol_manager.exception.ResourceNotFoundException;
import com.ngaleano.lol_manager.model.Match;
import com.ngaleano.lol_manager.model.MatchStatus;
import com.ngaleano.lol_manager.model.Player;
import com.ngaleano.lol_manager.model.Role;
import com.ngaleano.lol_manager.model.User;
import com.ngaleano.lol_manager.model.Team;
import com.ngaleano.lol_manager.repository.MatchRepository;
import com.ngaleano.lol_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MatchServiceImpl implements MatchService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public void checkIn(Long matchId, Long userId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (match.getStatus() == MatchStatus.COMPLETED) {
            throw new BusinessRuleException("El partido ya termino.");
        }

        validateCheckInWindow(match);

        Player player = user.getPlayer();
        if (player == null) {
            throw new BusinessRuleException("Tenes que tener un perfil de jugador para hacer check-in.");
        }

        boolean isTeamA = isUserInTeam(user, match.getTeamA());
        boolean isTeamB = isUserInTeam(user, match.getTeamB());

        if (!isTeamA && !isTeamB) {
            throw new BusinessRuleException("No perteneces a ningun equipo de este partido.");
        }

        match.getCheckedInPlayers().add(player.getId());

        if (isTeamA) {
            if (hasTeamCompletedCheckIn(match.getTeamA(), match.getCheckedInPlayers())) {
                match.setCheckInTeamA(true);
            }
        } else {
            if (hasTeamCompletedCheckIn(match.getTeamB(), match.getCheckedInPlayers())) {
                match.setCheckInTeamB(true);
            }
        }

        matchRepository.save(match);
    }

    private boolean hasTeamCompletedCheckIn(Team team, Set<Long> checkedInPlayers) {
        if (team == null || team.getPlayers() == null)
            return false;
        Set<Long> teamPlayerIds = team.getPlayers().stream()
                .map(Player::getId)
                .collect(Collectors.toSet());
        return checkedInPlayers.containsAll(teamPlayerIds);
    }

    @Override
    @Transactional
    public void claimWalkover(Long matchId, Long userId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado"));

        if (match.getStatus() != MatchStatus.PENDING) {
            throw new BusinessRuleException("El partido no esta pendiente.");
        }

        if (match.getScheduledTime() == null) {
            throw new BusinessRuleException("El partido no tiene horario programado.");
        }

        if (LocalDateTime.now().isBefore(match.getScheduledTime())) {
            throw new BusinessRuleException("Todavia no es la hora del partido.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        validateLeader(user, match);

        boolean isTeamA = isUserInTeam(user, match.getTeamA());
        boolean isTeamB = isUserInTeam(user, match.getTeamB());

        if (!isTeamA && !isTeamB) {
            throw new BusinessRuleException("No perteneces a ninguno de los equipos.");
        }

        if (match.isWalkoverClaimed()) {
            throw new BusinessRuleException("El walkover ya ha sido reclamado.");
        }

        if (isTeamA) {
            if (!match.isCheckInTeamA()) {
                throw new BusinessRuleException("Tu equipo no completo el check-in.");
            }
            if (match.isCheckInTeamB()) {
                throw new BusinessRuleException("El rival se presento. Jueguen el partido.");
            }
            match.setWinnerId(match.getTeamA().getId());
        } else {
            if (!match.isCheckInTeamB()) {
                throw new BusinessRuleException("Tu equipo no completo el check-in.");
            }
            if (match.isCheckInTeamA()) {
                throw new BusinessRuleException("El rival se presento. Jueguen el partido.");
            }
            match.setWinnerId(match.getTeamB().getId());
        }

        match.setWalkoverClaimed(true);
        match.setStatus(MatchStatus.COMPLETED);
        matchRepository.save(match);
        tournamentService.advanceWinner(matchId);
    }

    @Override
    @Transactional
    public void requestAdmin(Long matchId, Long userId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (match.getStatus() == MatchStatus.COMPLETED) {
            throw new BusinessRuleException("El partido ya ha finalizado.");
        }

        validateLeader(user, match);

        if (!isUserInTeam(user, match.getTeamA()) && !isUserInTeam(user, match.getTeamB())) {
            throw new BusinessRuleException("No tenes permiso para solicitar admin en este partido.");
        }

        match.setAdminRequested(true);
        match.setStatus(MatchStatus.DISPUTE);
        matchRepository.save(match);

        String discord = (user.getPlayer() != null) ? user.getPlayer().getDiscordUser() : "No vinculado";
        String msg = "Disputa solicitada en partido #" + matchId + " por " + user.getNickname() + " (Discord: "
                + discord + ")";

        List<User> admins = userRepository.findByRole(Role.ADMIN);
        for (User admin : admins) {
            notificationService.sendNotification(
                    admin,
                    msg,
                    "DISPUTE",
                    matchId);
        }
    }

    private void validateLeader(User user, Match match) {
        if (user.getPlayer() == null || user.getPlayer().getTeam() == null) {
            throw new BusinessRuleException("Debes tener equipo.");
        }
        Team userTeam = user.getPlayer().getTeam();
        if (userTeam.getLeader() == null || !userTeam.getLeader().getId().equals(user.getPlayer().getId())) {
            throw new BusinessRuleException("Solo el lider del equipo puede realizar esta accion.");
        }
    }

    private void validateCheckInWindow(Match match) {
        if (match.getScheduledTime() == null) {
            throw new BusinessRuleException("El partido no tiene horario programado.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = match.getScheduledTime().minusMinutes(20);
        LocalDateTime matchTime = match.getScheduledTime();

        if (now.isBefore(windowStart)) {
            throw new BusinessRuleException("El check-in aun no esta habilitado (20 min antes).");
        }

        if (now.isAfter(matchTime)) {
            throw new BusinessRuleException("El tiempo de check-in ha finalizado.");
        }
    }

    private boolean isUserInTeam(User user, Team team) {
        if (team == null || user.getPlayer() == null)
            return false;
        if (user.getPlayer().getTeam() == null)
            return false;
        return user.getPlayer().getTeam().getId().equals(team.getId());
    }

    @Override
    @Transactional
    public void resolveMatch(Long matchId, Long winnerId, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario administrador no encontrado"));

        if (admin.getRole() != Role.ADMIN) {
            throw new BusinessRuleException("Solo un administrador puede resolver partidos.");
        }

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado"));

        if (match.getStatus() == MatchStatus.COMPLETED) {
            throw new BusinessRuleException("El partido ya fue completado.");
        }

        match.setWinnerId(winnerId);
        match.setStatus(MatchStatus.COMPLETED);
        match.setAdminRequested(false);
        matchRepository.save(match);

        tournamentService.advanceWinner(matchId);
    }

}
