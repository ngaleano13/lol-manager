package com.ngaleano.lol_manager.service;

public interface MatchService {
    void checkIn(Long matchId, Long userId);

    void claimWalkover(Long matchId, Long userId);

    void requestAdmin(Long matchId, Long userId);

    void resolveMatch(Long matchId, Long winnerId, Long adminId);
}
