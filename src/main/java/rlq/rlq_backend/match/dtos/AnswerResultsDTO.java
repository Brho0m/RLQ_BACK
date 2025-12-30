package rlq.rlq_backend.match.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import rlq.rlq_backend.user.dtos.UserDto;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnswerResultsDTO {
    private Long answerOptionId;
    private String answerText;
    private UserDto answerBy;
    private List<UserDto> users;
}
