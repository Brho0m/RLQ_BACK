package rlq.rlq_backend.match.dtos;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SelectionPhaseDTO {
    private Long matchQuestionId;
    private List<AnswerOptionDTO> options;
    private LocalDateTime deadline;
}

