package rlq.rlq_backend.match.dtos;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionPhaseDTO {
    private Long matchQuestionId;
    private Long questionOrder;
    private String questionText;
    private LocalDateTime deadline;
}

