package rlq.rlq_backend.match.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import rlq.rlq_backend.match.entity.AnswerOption;

import java.util.List;
import java.util.Optional;

public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Long> {
    List<AnswerOption> findByMatchQuestionId(Long matchQuestionId);

    Optional<AnswerOption> findByMatchQuestionIdAndIsCorrectAnswerTrue(Long matchQuestionId);
    Optional<AnswerOption> findByMatchQuestionIdAndUserId(Long matchQuestionId, Long userId);
    boolean existsByMatchQuestionIdAndUserId(Long matchQuestionId, Long userId);
}


