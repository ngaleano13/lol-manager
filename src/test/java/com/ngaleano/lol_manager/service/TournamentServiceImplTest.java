package com.ngaleano.lol_manager.service;

import com.ngaleano.lol_manager.dto.TournamentDTO;
import com.ngaleano.lol_manager.exception.BusinessRuleException;
import com.ngaleano.lol_manager.exception.ResourceNotFoundException;
import com.ngaleano.lol_manager.mapper.TournamentMapper;
import com.ngaleano.lol_manager.model.Match;
import com.ngaleano.lol_manager.model.Tournament;
import com.ngaleano.lol_manager.model.TournamentStatus;
import com.ngaleano.lol_manager.model.User;
import com.ngaleano.lol_manager.repository.MatchRepository;
import com.ngaleano.lol_manager.repository.TeamRepository;
import com.ngaleano.lol_manager.repository.TournamentRepository;
import com.ngaleano.lol_manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TournamentServiceImplTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TournamentMapper tournamentMapper;

    @InjectMocks
    private TournamentServiceImpl tournamentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTournament_NameExists() {
        TournamentDTO dto = new TournamentDTO(null, "ExistingName", "Desc", null, null, null, null, 8, 0, null, 1L);
        when(tournamentRepository.existsByName("ExistingName")).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> tournamentService.createTournament(dto));
    }

    @Test
    void createTournament_UserNotFound() {
        TournamentDTO dto = new TournamentDTO(null, "NewName", "Desc", null, null, null, null, 8, 0, null, 99L);
        when(tournamentRepository.existsByName("NewName")).thenReturn(false);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tournamentService.createTournament(dto));
    }

    @Test
    void createTournament_UserHasActiveTournament() {
        TournamentDTO dto = new TournamentDTO(null, "NewName", "Desc", null, null, null, null, 8, 0, null, 1L);
        User user = new User();
        user.setId(1L);

        when(tournamentRepository.existsByName("NewName")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tournamentRepository.existsByCreatorAndStatusNot(user, TournamentStatus.FINISHED)).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> tournamentService.createTournament(dto));
    }

    @Test
    void createTournament_Success() {
        TournamentDTO dto = new TournamentDTO(null, "ValidName", "Desc", null, null, null, null, 8, 0, null, 1L);
        User user = new User();
        user.setId(1L);
        Tournament tournament = new Tournament();
        tournament.setName("ValidName");

        Tournament savedTournament = new Tournament();
        savedTournament.setId(123L);
        savedTournament.setName("ValidName");
        savedTournament.setCreator(user);

        when(tournamentRepository.existsByName("ValidName")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tournamentRepository.existsByCreatorAndStatusNot(user, TournamentStatus.FINISHED)).thenReturn(false);
        when(tournamentMapper.toEntity(dto)).thenReturn(tournament);
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(savedTournament);
        when(tournamentMapper.toDto(savedTournament)).thenReturn(
                new TournamentDTO(123L, "ValidName", "Desc", null, null, "REGISTRATION", "RANDOM", 8, 0,
                        List.of(), 1L));

        TournamentDTO result = tournamentService.createTournament(dto);

        assertNotNull(result);
        assertEquals("ValidName", result.name());
        verify(tournamentRepository).save(any(Tournament.class));
    }

    @Test
    void scheduleMatch_Success() {
        Long matchId = 100L;
        Long userId = 10L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime matchTime = start.plusHours(2);

        User creator = new User();
        creator.setId(userId);
        Tournament tournament = new Tournament();
        tournament.setCreator(creator);
        tournament.setStartDate(start);

        Match match = new Match();
        match.setId(matchId);
        match.setTournament(tournament);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

        tournamentService.scheduleMatch(matchId, userId, matchTime);

        assertEquals(matchTime, match.getScheduledTime());
        verify(matchRepository).save(match);
    }
}
