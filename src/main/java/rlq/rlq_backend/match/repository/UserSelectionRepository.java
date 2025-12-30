package rlq.rlq_backend.match.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import rlq.rlq_backend.match.entity.UserSelection;

import java.util.List;
import java.util.Optional;

public interface UserSelectionRepository extends JpaRepository<UserSelection, Long> {
    List<UserSelection> findByMatchQuestionId(Long matchQuestionId);
    Optional<UserSelection> findByMatchQuestionIdAndUserId(Long matchQuestionId, Long userId);
    boolean existsByMatchQuestionIdAndUserId(Long matchQuestionId, Long userId);
    List<UserSelection> findByAnswerOptionId(Long answerOptionId);
}


