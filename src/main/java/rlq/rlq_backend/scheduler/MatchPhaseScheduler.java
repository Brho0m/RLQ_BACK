package rlq.rlq_backend.scheduler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import rlq.rlq_backend.match.entity.Match;
import rlq.rlq_backend.match.entity.MatchQuestion;
import rlq.rlq_backend.match.enums.GameStatus;
import rlq.rlq_backend.match.enums.MatchQuestionStatus;
import rlq.rlq_backend.match.enums.MatchStatus;
import rlq.rlq_backend.match.repository.MatchRepository;
import rlq.rlq_backend.match.repository.MatchQuestionRepository;
import rlq.rlq_backend.match.service.GameService;

@Service
@AllArgsConstructor
public class MatchPhaseScheduler {

    private final MatchQuestionRepository matchQuestionRepository;
    private final MatchRepository matchRepository;
    private final GameService gameService;

    // Run often; logic is cheap and ensures deadlines are respected.
    @Scheduled(fixedDelay = 1000)
    public void scheduleMatchPhase() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Riyadh"));

        // 1) Submission deadline reached (with grace period) -> move to selection phase
        List<MatchQuestion> submitExpired = matchQuestionRepository.findByStatusAndSubmissionDeadlineBefore(
                MatchQuestionStatus.SUBMITTING_ANSWERS, now);
        submitExpired.forEach(mq -> gameService.transitionToSelectionPhase(mq.getId()));

        // 2) Selection deadline reached (with grace period) -> move to results
        List<MatchQuestion> selectExpired = matchQuestionRepository.findByStatusAndSelectionDeadlineBefore(
                MatchQuestionStatus.SELECTING_ANSWERS, now);
        selectExpired.forEach(mq -> gameService.transitionToResults(mq.getId()));

        List<MatchQuestion> finishedQuestions = matchQuestionRepository.findByStatusAndSubmissionDeadlineAfter(MatchQuestionStatus.SUBMITTING_ANSWERS, now);
        finishedQuestions.stream().filter(mq -> mq.getAnswerOptions().size() == mq.getMatch().getPlayers().size()).forEach(mq -> gameService.transitionToSelectionPhase(mq.getId()));

        // 3) Results window finished -> show leaderboard for 10s
        List<Match> resultsExpired = matchRepository.findByStatusAndGameStatusAndResultsUntilBefore(
                MatchStatus.IN_PROGRESS, GameStatus.QUESTION_RESULTS, now);
        for (Match match : resultsExpired) {
            match.setGameStatus(GameStatus.LEADERBOARD);
            match.setLeaderboardUntil(now.plusSeconds(10));
            match.setResultsUntil(null);
            matchRepository.save(match);
            gameService.publishLeaderboard(match.getId());
        }

        // 4) Leaderboard timer finished -> start next question OR end match
        List<Match> leaderboardExpired = matchRepository.findByStatusAndGameStatusAndLeaderboardUntilBefore(
                MatchStatus.IN_PROGRESS, GameStatus.LEADERBOARD, now);

        for (Match match : leaderboardExpired) {
            Long matchId = match.getId();

            // Find next pending question by order
            List<MatchQuestion> pending = matchQuestionRepository.findByMatchIdAndStatus(matchId, MatchQuestionStatus.PENDING);
            MatchQuestion next = pending.stream()
                    .sorted((a, b) -> Long.compare(a.getQuestionOrder(), b.getQuestionOrder()))
                    .findFirst()
                    .orElse(null);

            if (next == null) {
                gameService.endMatch(matchId);
                continue;
            }

            match.setCurrentMatchQuestion(next);
            match.setGameStatus(GameStatus.QUESTION_STARTED);
            match.setLeaderboardUntil(null);
            match.setResultsUntil(null);
            matchRepository.save(match);

            gameService.startSubmittingQuestion(matchId, next.getId(), 70);
        }
    }
}
