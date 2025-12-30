package rlq.rlq_backend.match.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import rlq.rlq_backend.match.entity.MatchPlayer;

import java.util.List;
import java.util.Optional;

public interface MatchPlayerRepository extends JpaRepository<MatchPlayer, Long> {
    List<MatchPlayer> findByMatchId(Long matchId);
    List<MatchPlayer> findByMatchIdOrderByScoreDesc(Long matchId);
    Optional<MatchPlayer> findByMatchIdAndUserId(Long matchId, Long userId);
    boolean existsByMatchIdAndUserId(Long matchId, Long userId);
}


