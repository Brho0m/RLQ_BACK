package rlq.rlq_backend.match.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rlq.rlq_backend.match.enums.MatchQuestionStatus;
import rlq.rlq_backend.question.entity.Question;

@Entity
@Data
@Table(indexes = @Index(name = "idx_match_question_order", columnList = "match_id, question_order"))
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MatchQuestionStatus status;

    @Column(nullable = false)
    private Long questionOrder;

    @Column
    private LocalDateTime submissionDeadline;

    @Column
    private LocalDateTime selectionDeadline;

    @OneToMany(mappedBy = "matchQuestion", fetch = FetchType.EAGER)
    private List<AnswerOption> answerOptions;

    @OneToMany(mappedBy = "matchQuestion")
    private List<UserSelection> userSelections;

}


