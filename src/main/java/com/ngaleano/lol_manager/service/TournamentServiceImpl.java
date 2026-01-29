package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.dto.MatchDTO;
import com.ngaleano.lol_manager.dto.RoundDTO;
import com.ngaleano.lol_manager.dto.TournamentBracketDTO;
import com.ngaleano.lol_manager.dto.TournamentDTO;

import com.ngaleano.lol_manager.mapper.TournamentMapper;
import com.ngaleano.lol_manager.model.Match;
import com.ngaleano.lol_manager.model.MatchStatus;
import com.ngaleano.lol_manager.model.MatchmakingStrategy;
import com.ngaleano.lol_manager.model.Player;
import com.ngaleano.lol_manager.model.Team;
import com.ngaleano.lol_manager.model.Tournament;
import com.ngaleano.lol_manager.model.TournamentStatus;
import com.ngaleano.lol_manager.model.User;
import com.ngaleano.lol_manager.repository.MatchRepository;
import com.ngaleano.lol_manager.repository.TeamRepository;
import com.ngaleano.lol_manager.repository.TournamentRepository;
import com.ngaleano.lol_manager.repository.UserRepository;
import com.ngaleano.lol_manager.exception.BusinessRuleException;
import com.ngaleano.lol_manager.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TournamentServiceImpl implements TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private TournamentMapper tournamentMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public TournamentDTO createTournament(TournamentDTO dto) {

        if (tournamentRepository.existsByName(dto.name())) {
            throw new BusinessRuleException("El nombre del torneo ya existe.");
        }

        User creator = userRepository.findById(dto.creatorId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario creador no encontrado"));

        if (tournamentRepository.existsByCreatorAndStatusNot(creator, TournamentStatus.FINISHED)) {
            throw new BusinessRuleException("El usuario ya tiene un torneo activo.");
        }

        if (dto.maxTeams() == null || (dto.maxTeams() != 8 && dto.maxTeams() != 16 && dto.maxTeams() != 32)) {
            throw new BusinessRuleException("La cantidad de equipos debe ser 8, 16 o 32.");
        }

        Tournament tournament = tournamentMapper.toEntity(dto);
        tournament.setStatus(TournamentStatus.REGISTRATION);
        tournament.setCreator(creator);

        Tournament savedTournament = tournamentRepository.save(tournament);

        return tournamentMapper.toDto(savedTournament);
    }

    @Override
    public List<TournamentDTO> getAllTournaments() {
        return tournamentRepository.findAll()
                .stream()
                .map(tournamentMapper::toDto)
                .toList();
    }

    @Override
    public void openRegistration(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Torneo no encontrado (ID: " + tournamentId + ")"));

        if (tournament.getStatus() != TournamentStatus.DRAFT) {
            throw new BusinessRuleException("El torneo no esta en borrador.");
        }

        tournament.setStatus(TournamentStatus.REGISTRATION);
        tournamentRepository.save(tournament);
    }

    @Override
    public void registerTeam(Long tournamentId, Long teamId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Torneo no encontrado"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Equipo no encontrado"));

        if (tournament.getStatus() != TournamentStatus.REGISTRATION) {
            throw new BusinessRuleException("Las inscripciones no estan abiertas.");
        }

        if (tournament.getTeams().size() >= tournament.getMaxTeams()) {
            throw new BusinessRuleException("El torneo esta lleno.");
        }

        if (team.getPlayers().size() < 5) {
            throw new BusinessRuleException("El equipo debe tener al menos 5 jugadores para unirse.");
        }

        if (tournament.getTeams().contains(team)) {
            throw new BusinessRuleException("El equipo ya esta anotado.");
        }

        tournament.getTeams().add(team);
        tournamentRepository.save(tournament);
    }

    @Override
    public void scheduleMatch(Long matchId, Long userId, LocalDateTime scheduledTime) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado"));

        if (match.getStatus() == MatchStatus.COMPLETED) {
            throw new BusinessRuleException("El partido ya ha finalizado, no se puede reprogramar.");
        }

        Tournament tournament = match.getTournament();

        if (!tournament.getCreator().getId().equals(userId)) {
            throw new BusinessRuleException("Solo el creador puede programar partidos.");
        }

        if (tournament.getStartDate() == null) {
            throw new BusinessRuleException("Primero debes definir la fecha de inicio del torneo.");
        }

        if (scheduledTime.isBefore(tournament.getStartDate())) {
            throw new BusinessRuleException("El partido no puede ser antes del inicio del torneo.");
        }

        LocalDateTime limitDate = tournament.getStartDate().plusDays(14);

        if (scheduledTime.isAfter(limitDate)) {
            throw new BusinessRuleException("El partido excede el limite de 14 dias desde el inicio del torneo.");
        }

        match.setScheduledTime(scheduledTime);
        matchRepository.save(match);
    }

    @Override
    @Transactional
    public TournamentDTO startTournament(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Torneo no encontrado"));

        if (tournament.getStatus() != TournamentStatus.REGISTRATION) {
            throw new BusinessRuleException(
                    "El torneo no esta en etapa de inscripcion o ya inicio.");
        }

        List<Team> teams = tournament.getTeams();

        if (teams.size() < 2) {
            throw new BusinessRuleException(
                    "Se necesitan al menos 2 equipos para iniciar.");
        }

        for (Team team : teams) {
            if (team.getPlayers().size() < 5) {
                throw new BusinessRuleException(
                        "El equipo '" + team.getName() + "' tiene menos de 5 jugadores.");
            }
        }

        MatchmakingStrategy strategy = tournament.getMatchmakingStrategy();
        if (strategy == null)
            strategy = MatchmakingStrategy.RANDOM;

        List<Match> matches = generateMatches(tournament, new ArrayList<>(teams), strategy);

        matchRepository.saveAll(matches);
        tournament.setStatus(TournamentStatus.IN_PROGRESS);
        Tournament saved = tournamentRepository.save(tournament);
        return tournamentMapper.toDto(saved);
    }

    private List<Match> generateMatches(Tournament tournament, List<Team> teams, MatchmakingStrategy strategy) {
        List<Match> matches = new ArrayList<>();
        int teamCount = teams.size();

        if ((teamCount & (teamCount - 1)) != 0) {
            throw new BusinessRuleException(
                    "La cantidad de equipos debe ser potencia de 2 (8, 16, 32...).");
        }

        if (strategy == MatchmakingStrategy.RANDOM) {
            Collections.shuffle(teams);
        } else if (strategy == MatchmakingStrategy.BALANCED) {
            teams.sort((t1, t2) -> calculateTeamScore(t2) - calculateTeamScore(t1));

            List<Team> balanced = new ArrayList<>();
            int mid = teamCount / 2;
            for (int i = 0; i < mid; i++) {
                balanced.add(teams.get(i));
                balanced.add(teams.get(teamCount - 1 - i));
            }
            teams = balanced;
        }

        int totalRounds = (int) (Math.log(teamCount) / Math.log(2));

        for (int r = 1; r <= totalRounds; r++) {
            int matchesInRound = teamCount / (int) Math.pow(2, r);
            String roundName = getRoundName(r, totalRounds);

            for (int m = 0; m < matchesInRound; m++) {
                Team teamA = null;
                Team teamB = null;

                if (r == 1) {
                    teamA = teams.get(m * 2);
                    teamB = teams.get(m * 2 + 1);
                }

                Match match = new Match(tournament, teamA, teamB, roundName, r, m);
                matches.add(match);
            }
        }

        return matches;
    }

    private String getRoundName(int roundNumber, int totalRounds) {
        if (roundNumber == totalRounds)
            return "Final";
        if (roundNumber == totalRounds - 1)
            return "Semifinal";
        if (roundNumber == totalRounds - 2)
            return "Cuartos de Final";
        return "Ronda " + roundNumber;
    }

    @Override
    public List<MatchDTO> getFixture(Long tournamentId) {
        return matchRepository.findByTournamentId(tournamentId).stream()
                .map(tournamentMapper::toMatchDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TournamentBracketDTO getTournamentBrackets(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado"));

        List<Match> matches = matchRepository.findByTournamentId(tournamentId);

        Map<Integer, List<MatchDTO>> matchesByRound = matches.stream()
                .map(tournamentMapper::toMatchDTO)
                .collect(Collectors.groupingBy(MatchDTO::roundNumber));

        List<RoundDTO> roundDTOs = matchesByRound.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    String roundName = "Ronda " + entry.getKey();
                    if (!entry.getValue().isEmpty()) {
                        roundName = entry.getValue().get(0).round();
                    }
                    return new RoundDTO(
                            entry.getKey(),
                            roundName,
                            entry.getValue());
                })
                .toList();

        return new TournamentBracketDTO(
                tournament.getId(),
                tournament.getName(),
                roundDTOs);
    }

    @Override
    public MatchDTO getMatchById(Long matchId) {
        Match m = matchRepository.findById(matchId).orElseThrow(
                () -> new ResourceNotFoundException("Partido no encontrado"));
        return tournamentMapper.toMatchDTO(m);
    }

    @Override
    @Transactional
    public String reportResult(Long matchId, Long reportingTeamId, Long claimedWinnerId, Long userId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Partido no encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (user.getPlayer() == null || user.getPlayer().getTeam() == null) {
            throw new BusinessRuleException("No perteneces a ningun equipo.");
        }

        Team userTeam = user.getPlayer().getTeam();
        if (!userTeam.getId().equals(reportingTeamId)) {
            throw new BusinessRuleException("No tenes permiso para reportar por este equipo.");
        }

        if (userTeam.getLeader() == null || !userTeam.getLeader().getId().equals(user.getPlayer().getId())) {
            throw new BusinessRuleException("Solo el lider del equipo puede reportar el resultado.");
        }

        if (!claimedWinnerId.equals(match.getTeamA().getId()) && !claimedWinnerId.equals(match.getTeamB().getId())) {
            throw new BusinessRuleException("El equipo ganador debe ser uno de los participantes del partido.");
        }

        if (match.getStatus() == MatchStatus.COMPLETED)
            return "El partido ya finalizo.";

        LocalDateTime now = LocalDateTime.now();
        boolean isTeamA = match.getTeamA().getId().equals(reportingTeamId);

        if (match.getFirstReportTime() != null) {
            long diff = Duration.between(match.getFirstReportTime(), now).toMinutes();
            if (diff >= 5) {
                Long autoWinnerId = (match.getVoteTeamA() != null) ? match.getVoteTeamA() : match.getVoteTeamB();
                finalizeMatch(match, autoWinnerId);
                return "Victoria automatica por tiempo agotado del rival.";
            }
        }

        if (isTeamA)
            match.setVoteTeamA(claimedWinnerId);
        else
            match.setVoteTeamB(claimedWinnerId);

        if (match.getFirstReportTime() == null)
            match.setFirstReportTime(now);

        if (match.getVoteTeamA() != null && match.getVoteTeamB() != null) {
            if (match.getVoteTeamA().equals(match.getVoteTeamB())) {
                finalizeMatch(match, match.getVoteTeamA());
                return "Resultado confirmado por ambos equipos.";
            } else {
                match.setStatus(MatchStatus.DISPUTE);
                matchRepository.save(match);
                return "Conflicto: Los reportes no coinciden. Entra en DISPUTA.";
            }
        }

        matchRepository.save(match);
        return "Voto registrado. Esperando al rival (5 min).";
    }

    private int calculateTeamScore(Team team) {
        int totalScore = 0;
        for (Player p : team.getPlayers()) {
            totalScore += getRankScore(p);
        }
        return totalScore / Math.max(1, team.getPlayers().size());
    }

    private int getRankScore(Player p) {
        String rank = (p.getSoloRank() != null && !p.getSoloRank().equals("UNRANKED"))
                ? p.getSoloRank()
                : p.getFlexRank();

        if (rank == null || rank.equals("UNRANKED"))
            return 1000;
        if (rank.contains("CHALLENGER"))
            return 4500;
        if (rank.contains("GRANDMASTER"))
            return 4000;
        if (rank.contains("MASTER"))
            return 3500;
        if (rank.contains("DIAMOND"))
            return 3000;
        if (rank.contains("EMERALD"))
            return 2500;
        if (rank.contains("PLATINUM"))
            return 2000;
        if (rank.contains("GOLD"))
            return 1500;
        if (rank.contains("SILVER"))
            return 1200;
        if (rank.contains("BRONZE"))
            return 800;
        if (rank.contains("IRON"))
            return 400;

        return 1000;
    }

    @Override
    @Transactional
    public void advanceWinner(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado"));
        advanceWinner(match);
    }

    private void finalizeMatch(Match match, Long winnerId) {
        match.setWinnerId(winnerId);
        match.setStatus(MatchStatus.COMPLETED);
        matchRepository.save(match);

        advanceWinner(match);
    }

    private void advanceWinner(Match finishedMatch) {
        if (finishedMatch.getRoundNumber() == null || finishedMatch.getMatchIndex() == null) {
            return;
        }

        int nextRound = finishedMatch.getRoundNumber() + 1;
        int nextMatchIndex = finishedMatch.getMatchIndex() / 2;
        boolean isTeamAInNextMatch = (finishedMatch.getMatchIndex() % 2 == 0);

        Team winner = teamRepository.findById(finishedMatch.getWinnerId()).orElse(null);
        if (winner == null)
            return;

        List<Match> allMatches = matchRepository.findByTournamentId(finishedMatch.getTournament().getId());

        Match targetMatch = allMatches.stream()
                .filter(m -> m.getRoundNumber() != null && m.getRoundNumber() == nextRound)
                .filter(m -> m.getMatchIndex() != null && m.getMatchIndex() == nextMatchIndex)
                .findFirst()
                .orElse(null);

        if (targetMatch != null) {
            if (isTeamAInNextMatch) {
                targetMatch.setTeamA(winner);
            } else {
                targetMatch.setTeamB(winner);
            }
            matchRepository.save(targetMatch);
        } else {
            finishTournament(finishedMatch.getTournament(), winner);
        }
    }

    private void finishTournament(Tournament tournament, Team winner) {
        tournament.setStatus(TournamentStatus.FINISHED);
        tournament.setWinner(winner);
        tournament.setEndDate(LocalDateTime.now());
        tournamentRepository.save(tournament);

        String message = "El torneo '" + tournament.getName() + "' ha finalizado. Ganador: " + winner.getName();

        for (Team team : tournament.getTeams()) {
            for (Player player : team.getPlayers()) {
                userRepository.findByPlayerId(player.getId()).ifPresent(user -> {
                    notificationService.sendNotification(user, message, "TOURNAMENT_END", tournament.getId());
                });
            }
        }
    }
}