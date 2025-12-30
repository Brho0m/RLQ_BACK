package rlq.rlq_backend.match.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import rlq.rlq_backend.match.entity.Match;
import rlq.rlq_backend.match.entity.MatchQuestion;
import rlq.rlq_backend.match.enums.GameStatus;
import rlq.rlq_backend.match.enums.MatchQuestionStatus;
import rlq.rlq_backend.match.enums.MatchStatus;
import rlq.rlq_backend.match.mapper.MatchMapper;
import rlq.rlq_backend.match.repository.MatchQuestionRepository;
import rlq.rlq_backend.match.repository.MatchRepository;
import rlq.rlq_backend.question.entity.Question;
import rlq.rlq_backend.question.repository.QuestionRepository;
import rlq.rlq_backend.user.entity.User;
import rlq.rlq_backend.user.enums.Roles;
import rlq.rlq_backend.user.util.UserUtil;
import rlq.rlq_backend.exception.BusinessException;
import rlq.rlq_backend.match.dtos.MatchDTO;
import rlq.rlq_backend.match.dtos.MatchEvent;
import rlq.rlq_backend.match.dtos.MatchList;
import rlq.rlq_backend.match.dtos.MatchQuestionDTO;

@Service
@AllArgsConstructor
public class AdminMatchService {

    private final MatchRepository matchRepository;
    private final MatchMapper matchMapper;
    private final QuestionRepository questionRepository;
    private final MatchQuestionRepository matchQuestionRepository;
    private final MatchWsPublisher matchWsPublisher;
    private final GameService gameService;
    private final UserUtil userUtil;

    public MatchDTO createMatch() {
        User user = userUtil.getCurrentUser();
        if (user.getRole() != Roles.ROLE_ADMIN) {
            throw new BusinessException("User is not an admin");
        }
        Match match = Match.builder()
                .status(MatchStatus.WAITING)
                .gameStatus(GameStatus.MATCH_CREATED)
                .createdAt(LocalDateTime.now())
                .build();
        MatchDTO matchDTO = matchMapper.toDTO(match);
        matchWsPublisher.publishMatchLobby(MatchEvent.<MatchDTO>builder()
                .type(GameStatus.MATCH_CREATED)
                .data(matchDTO)
                .matchId(matchDTO.getId())
                .build());
        matchRepository.save(match);

        matchWsPublisher.publishMatches();
        return matchDTO;
    }

    @Transactional
    public MatchDTO startMatch(Long matchId) {
        Match match = matchRepository.findById(matchId).orElseThrow(() -> new BusinessException("Match not found"));

        if (match.getStatus() != MatchStatus.WAITING) {
            throw new BusinessException("Match is not waiting");
        }

        if (match.getGameStatus() != GameStatus.MATCH_STARTED) {
            throw new BusinessException("Match is not created");
        }

        match.setStatus(MatchStatus.IN_PROGRESS);
        match.setGameStatus(GameStatus.QUESTION_STARTED);
        match.setStartedAt(LocalDateTime.now());

        List<Question> questions = questionRepository.findRandomQuestions(3);

        if (questions.isEmpty() || questions.size() != 3) {
            throw new BusinessException("Not enough questions");
        }

        List<MatchQuestion> matchQuestions = new ArrayList<>();

        Long questionOrder = 1L;
        for (Question q : questions) {
            matchQuestions.add(MatchQuestion.builder()
                    .match(match)
                    .question(q)
                    .status(questionOrder == 1 ? MatchQuestionStatus.SUBMITTING_ANSWERS : MatchQuestionStatus.PENDING)
                    .submissionDeadline(LocalDateTime.now().plusSeconds(10))
                    .questionOrder(questionOrder++)
                    .build());
        }

        match.setMatchQuestions(matchQuestions);
        match.setCurrentMatchQuestion(matchQuestions.get(0));
        matchRepository.save(match);

        MatchDTO matchDTO = matchMapper.toDTO(match);
        matchWsPublisher.publishMatchLobby(MatchEvent.<MatchDTO>builder()
                .type(GameStatus.QUESTION_STARTED)
                .data(matchDTO)
                .matchId(matchDTO.getId())
                .build());

        gameService.startSubmittingQuestion(matchId, match.getCurrentMatchQuestion().getId(), 70);

        return matchDTO;
    }

    @Transactional
    public MatchDTO closeMatch(Long matchId) {
        Match match = matchRepository.findById(matchId).orElseThrow(() -> new BusinessException("Match not found"));

        if (match.getStatus() != MatchStatus.WAITING) {
            throw new BusinessException("Match is not waiting");
        }
        match.setGameStatus(GameStatus.MATCH_STARTED);
        matchRepository.save(match);

        MatchDTO matchDTO = matchMapper.toDTO(match);
        matchWsPublisher.publishMatchLobby(MatchEvent.<MatchDTO>builder()
                .type(GameStatus.MATCH_STARTED)
                .data(matchDTO)
                .matchId(matchDTO.getId())
                .build());
        return matchDTO;
    }
}
