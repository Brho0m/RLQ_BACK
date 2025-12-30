package rlq.rlq_backend.match.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rlq.rlq_backend.match.enums.MatchQuestionStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchQuestionDTO {
    private Long matchQuestionId;
    private Long questionOrder;
    private String questionText;
    private MatchQuestionStatus status;
    private LocalDateTime submissionDeadline;
    private LocalDateTime selectionDeadline;
}

