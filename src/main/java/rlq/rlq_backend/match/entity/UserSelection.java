package rlq.rlq_backend.match.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import rlq.rlq_backend.user.entity.User;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
    uniqueConstraints = @UniqueConstraint(columnNames = {"match_question_id", "user_id"}),
    indexes = @Index(name = "idx_user_selection_match_question_user", columnList = "match_question_id, user_id")
)
public class UserSelection {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_question_id", nullable = false)
    private MatchQuestion matchQuestion;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "answer_option_id", nullable = false)
    private AnswerOption answerOption;

    @Column(nullable = false)
    private LocalDateTime selectedAt;

    @Column(nullable = false)
    private Boolean isCorrect;

}


