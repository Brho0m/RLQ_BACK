package rlq.rlq_backend.match.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import rlq.rlq_backend.match.entity.MatchQuestion;
import rlq.rlq_backend.match.enums.MatchQuestionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchQuestionRepository extends JpaRepository<MatchQuestion, Long> {
    List<MatchQuestion> findByMatchIdOrderByQuestionOrder(Long matchId);
    Optional<MatchQuestion> findByMatchIdAndQuestionOrder(Long matchId, Integer questionOrder);
    List<MatchQuestion> findByMatchIdAndStatus(Long matchId, MatchQuestionStatus status);
    List<MatchQuestion> findByStatusAndSubmissionDeadlineBefore(MatchQuestionStatus status, LocalDateTime submissionDeadline);
    List<MatchQuestion> findByStatusAndSelectionDeadlineBefore(MatchQuestionStatus status, LocalDateTime selectionDeadline);
    List<MatchQuestion> findByStatusAndSubmissionDeadlineAfter(MatchQuestionStatus status, LocalDateTime selectionDeadline);

    // Shared lock - allows multiple submissions in parallel
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT mq FROM MatchQuestion mq WHERE mq.id = :id")
    Optional<MatchQuestion> findByIdWithSharedLock(@Param("id") Long id);

    // Exclusive lock - waits for all shared locks to release before acquiring
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT mq FROM MatchQuestion mq WHERE mq.id = :id")
    Optional<MatchQuestion> findByIdWithExclusiveLock(@Param("id") Long id);
}


