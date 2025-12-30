package rlq.rlq_backend.match.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import rlq.rlq_backend.match.entity.Match;
import rlq.rlq_backend.match.enums.GameStatus;
import rlq.rlq_backend.match.enums.MatchStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByStatus(MatchStatus status);
    List<Match> findByStatusAndGameStatus(MatchStatus status, GameStatus gameStatus);
    List<Match> findByStatusAndGameStatusAndLeaderboardUntilBefore(MatchStatus status, GameStatus gameStatus, LocalDateTime now);
    List<Match> findByStatusAndGameStatusAndResultsUntilBefore(MatchStatus status, GameStatus gameStatus, LocalDateTime now);
}


