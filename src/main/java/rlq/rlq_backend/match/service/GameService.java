package rlq.rlq_backend.match.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import rlq.rlq_backend.exception.BusinessException;
import rlq.rlq_backend.match.dtos.*;
import rlq.rlq_backend.match.entity.*;
import rlq.rlq_backend.match.enums.GameStatus;
import rlq.rlq_backend.match.enums.MatchQuestionStatus;
import rlq.rlq_backend.match.enums.MatchStatus;
import rlq.rlq_backend.match.mapper.MatchMapper;
import rlq.rlq_backend.match.repository.AnswerOptionRepository;
import rlq.rlq_backend.match.repository.MatchRepository;
import rlq.rlq_backend.match.repository.MatchPlayerRepository;
import rlq.rlq_backend.match.repository.MatchQuestionRepository;
import rlq.rlq_backend.match.repository.UserSelectionRepository;
import rlq.rlq_backend.user.entity.User;
import rlq.rlq_backend.user.mapper.UserMapper;
import rlq.rlq_backend.user.util.UserUtil;

@Service
@RequiredArgsConstructor
public class GameService {

        private final MatchRepository matchRepository;
        private final MatchQuestionRepository matchQuestionRepository;
        private final MatchPlayerRepository matchPlayerRepository;
        private final AnswerOptionRepository answerOptionRepository;
        private final UserSelectionRepository userSelectionRepository;
        private final MatchWsPublisher matchWsPublisher;
        private final UserUtil userUtil;
        private final UserMapper userMapper;
        private final MatchMapper matchMapper;
        private final EmbeddingService embeddingService;
        public GameStateDTO getGameState(Long matchId) {
                User user = userUtil.getCurrentUser();
                Match match = matchRepository.findById(matchId)
                                .orElseThrow(() -> new BusinessException("Match not found"));

                MatchQuestionDTO current = matchMapper.toMatchQuestionDTO(match.getCurrentMatchQuestion());
                List<PlayerScoreDTO> leaderboard = getLeaderboard(matchId);

                switch (match.getGameStatus()) {
                        case MATCH_ENDED:
                                return GameStateDTO.builder()
                                                .phase(GameStatus.MATCH_ENDED)
                                                .leaderboard(leaderboard)
                                                .build();
                        case QUESTION_STARTED:
                                boolean hasSubmitted = answerOptionRepository
                                                .existsByMatchQuestionIdAndUserId(current.getMatchQuestionId(),
                                                                user.getId());
                                return GameStateDTO.builder()
                                                .phase(GameStatus.QUESTION_STARTED)
                                                .currentQuestion(current)
                                                .hasSubmitted(hasSubmitted)
                                                .build();
                        case SELECTION_PHASE:
                                boolean hasSelected = userSelectionRepository
                                                .existsByMatchQuestionIdAndUserId(current.getMatchQuestionId(),
                                                                user.getId());

                                List<AnswerOptionDTO> options = answerOptionRepository
                                                .findByMatchQuestionId(match.getCurrentMatchQuestion().getId()).stream()
                                                .map(o -> AnswerOptionDTO.builder().id(o.getId()).text(o.getText())
                                                                .build())
                                                .collect(Collectors.toList());

                                SelectionPhaseDTO selectionPhase = SelectionPhaseDTO.builder()
                                                .matchQuestionId(current.getMatchQuestionId()).options(options)
                                                .deadline(current.getSelectionDeadline()).build();

                                return GameStateDTO.builder()
                                                .phase(GameStatus.SELECTION_PHASE)
                                                .currentQuestion(current)
                                                .hasSubmitted(hasSelected)
                                                .selectionPhaseDTO(selectionPhase)
                                                .build();

                        case QUESTION_RESULTS:
                                AnswerOption correctAnswer = answerOptionRepository
                                                .findByMatchQuestionIdAndIsCorrectAnswerTrue(
                                                                current.getMatchQuestionId())
                                                .orElseThrow(() -> new BusinessException("Correct answer not found"));

                                List<AnswerOption> result = answerOptionRepository
                                                .findByMatchQuestionId(current.getMatchQuestionId());

                                List<AnswerResultsDTO> answerResults = result.stream().map(r -> AnswerResultsDTO
                                                .builder()
                                                .answerOptionId(r.getId())
                                                .answerText(r.getText())
                                                .answerBy(userMapper.toDto(r.getUser()))
                                                .users(r.getUserSelections().stream().map(userMapper::toUserDto)
                                                                .collect(Collectors.toList()))
                                                .build()).collect(Collectors.toList());

                                QuestionResultDTO questionResult = QuestionResultDTO.builder()
                                                .correctAnswerId(correctAnswer.getId())
                                                .userSelections(answerResults)
                                                .scores(leaderboard)
                                                .build();

                                return GameStateDTO.builder()
                                                .phase(GameStatus.QUESTION_RESULTS)
                                                .currentQuestion(current)
                                                .questionResultDTO(questionResult)
                                                .build();
                        case LEADERBOARD:
                                return GameStateDTO.builder()
                                                .phase(GameStatus.LEADERBOARD)
                                                .leaderboard(leaderboard)
                                                .build();
                        case MATCH_CREATED:
                                return GameStateDTO.builder()
                                                .phase(GameStatus.MATCH_CREATED)
                                                .match(matchMapper.toDTO(match))
                                                .build();
                }
                return GameStateDTO.builder()
                                .phase(match.getGameStatus())
                                .leaderboard(leaderboard)
                                .build();
        }

        /**
         * Called when a match is started (first question) or after leaderboard timer to
         * start next question.
         * Publishes WS: GameStatus.QUESTION_STARTED with MatchQuestionDTO payload.
         */
        @Transactional
        public void startSubmittingQuestion(Long matchId, Long matchQuestionId, long submitSeconds) {
                MatchQuestion mq = matchQuestionRepository.findById(matchQuestionId)
                                .orElseThrow(() -> new BusinessException("Question not found"));

                if (!mq.getMatch().getId().equals(matchId)) {
                        throw new BusinessException("Question does not belong to this match");
                }

                mq.setStatus(MatchQuestionStatus.SUBMITTING_ANSWERS);
                mq.setSubmissionDeadline(LocalDateTime.now().plusSeconds(submitSeconds));
                matchQuestionRepository.save(mq);

                MatchQuestionDTO dto = matchMapper.toMatchQuestionDTO(mq);

                matchWsPublisher.publishGameEvent(matchId, GameStatus.QUESTION_STARTED, dto);
        }

        @Transactional
        public void submitAnswer(Long matchId, Long matchQuestionId, SubmitAnswerDTO dto) {
                User user = userUtil.getCurrentUser();
                // Use shared lock - allows parallel submissions but blocks scheduler's exclusive lock
                MatchQuestion mq = matchQuestionRepository.findByIdWithSharedLock(matchQuestionId)
                                .orElseThrow(() -> new BusinessException("Question not found"));
                matchPlayerRepository.findByMatchIdAndUserId(matchId, user.getId())
                                .orElseThrow(() -> new BusinessException("Player not found"));

                if (!mq.getMatch().getId().equals(matchId)) {
                        throw new BusinessException("Question does not belong to this match");
                }

                if (mq.getStatus() != MatchQuestionStatus.SUBMITTING_ANSWERS) {
                        throw new BusinessException("Not in answer submission phase");
                }

                if (mq.getSubmissionDeadline() != null && mq.getSubmissionDeadline().isBefore(LocalDateTime.now())) {
                        throw new BusinessException("Submission deadline has passed");
                }

                if (answerOptionRepository.existsByMatchQuestionIdAndUserId(matchQuestionId, user.getId())) {
                        throw new BusinessException("Already submitted an answer");
                }

                embeddingService.assertNotTooClose(dto.getAnswerText(), mq.getQuestion().getCorrectAnswer());
                AnswerOption answer = AnswerOption.builder()
                                .matchQuestion(mq)
                                .user(user)
                                .text(dto.getAnswerText())
                                .createdAt(LocalDateTime.now())
                                .isCorrectAnswer(false)
                                .build();

                answerOptionRepository.save(answer);

                if (mq.getMatch().getPlayers().size() == answerOptionRepository.findByMatchQuestionId(matchQuestionId)
                                .size()) {
                        transitionToSelectionPhase(matchQuestionId);
                }
        }

        @Transactional
        public void selectAnswer(Long matchId, Long matchQuestionId, SelectAnswerDTO dto) {
                User user = userUtil.getCurrentUser();
                MatchQuestion mq = matchQuestionRepository.findById(matchQuestionId)
                                .orElseThrow(() -> new BusinessException("Question not found"));

                if (!mq.getMatch().getId().equals(matchId)) {
                        throw new BusinessException("Question does not belong to this match");
                }

                if (mq.getStatus() != MatchQuestionStatus.SELECTING_ANSWERS) {
                        throw new BusinessException("Not in selection phase");
                }

                if (userSelectionRepository.existsByMatchQuestionIdAndUserId(matchQuestionId, user.getId())) {
                        throw new BusinessException("Already selected an answer");
                }

                AnswerOption selectedOption = answerOptionRepository.findById(dto.getAnswerOptionId())
                                .orElseThrow(() -> new BusinessException("Answer option not found"));

                UserSelection selection = UserSelection.builder()
                                .matchQuestion(mq)
                                .user(user)
                                .answerOption(selectedOption)
                                .selectedAt(LocalDateTime.now())
                                .isCorrect(selectedOption.getIsCorrectAnswer())
                                .build();

                userSelectionRepository.save(selection);

                if (selectedOption.getIsCorrectAnswer()) {
                        updatePlayerScore(mq.getMatch().getId(), user.getId(), 1);

                } else if (selectedOption.getUser().getId() != user.getId()) {
                        updatePlayerScore(mq.getMatch().getId(), selectedOption.getUser().getId(), 1);
                }

                List<UserSelection> selections = userSelectionRepository.findByMatchQuestionId(matchQuestionId);
                if (mq.getMatch().getPlayers().size() == selections.size()) {
                        transitionToResults(matchQuestionId);
                }
        }

        // Called by scheduler when submission deadline passes OR when last player submits
        @Transactional
        public void transitionToSelectionPhase(Long matchQuestionId) {
                // Use exclusive lock - waits for all in-progress submissions (shared locks) to complete
                MatchQuestion mq = matchQuestionRepository.findByIdWithExclusiveLock(matchQuestionId)
                                .orElseThrow(() -> new BusinessException("Question not found"));

                // Guard: prevent double transition (scheduler + last player submit can both call this)
                if (mq.getStatus() != MatchQuestionStatus.SUBMITTING_ANSWERS) {
                        return; // Already transitioned by another caller
                }

                AnswerOption correctOption = AnswerOption.builder()
                                .createdAt(LocalDateTime.now())
                                .isCorrectAnswer(true)
                                .text(mq.getQuestion().getCorrectAnswer())
                                .matchQuestion(mq)
                                .user(null)
                                .build();

                answerOptionRepository.save(correctOption);

                mq.setStatus(MatchQuestionStatus.SELECTING_ANSWERS);
                mq.setSelectionDeadline(LocalDateTime.now().plusSeconds(500));
                mq.getMatch().setGameStatus(GameStatus.SELECTION_PHASE);
                matchQuestionRepository.save(mq);
                matchRepository.save(mq.getMatch());

                // Shuffle and publish options
                List<AnswerOption> options = answerOptionRepository.findByMatchQuestionId(matchQuestionId);
                Collections.shuffle(options);
                SelectionPhaseDTO selectionDTO = SelectionPhaseDTO.builder()
                                .matchQuestionId(matchQuestionId)
                                .options(options.stream()
                                                .map(o -> AnswerOptionDTO.builder().id(o.getId()).text(o.getText())
                                                                .build())
                                                .collect(Collectors.toList()))
                                .deadline(mq.getSelectionDeadline())
                                .build();

                matchWsPublisher.publishGameEvent(mq.getMatch().getId(), GameStatus.SELECTION_PHASE, selectionDTO);
        }

        // Called by scheduler when selection deadline passes OR when last player selects
        @Transactional
        public void transitionToResults(Long matchQuestionId) {
                MatchQuestion mq = matchQuestionRepository.findById(matchQuestionId)
                                .orElseThrow(() -> new BusinessException("Question not found"));

                // Guard: prevent double transition (scheduler + last player select can both call this)
                if (mq.getStatus() != MatchQuestionStatus.SELECTING_ANSWERS) {
                        return; // Already transitioned by another caller
                }

                mq.setStatus(MatchQuestionStatus.COMPLETED);
                mq.getMatch().setGameStatus(GameStatus.QUESTION_RESULTS);
                // Give clients time to display results before we move to leaderboard
                mq.getMatch().setResultsUntil(LocalDateTime.now().plusSeconds(9));
                mq.getMatch().setLeaderboardUntil(null);
                matchRepository.save(mq.getMatch());
                matchQuestionRepository.save(mq);

                AnswerOption correctAnswer = answerOptionRepository
                                .findByMatchQuestionIdAndIsCorrectAnswerTrue(matchQuestionId)
                                .orElseThrow(() -> new BusinessException("Correct answer not found"));

                List<AnswerOption> result = answerOptionRepository.findByMatchQuestionId(matchQuestionId);
                List<PlayerScoreDTO> scores = getLeaderboard(mq.getMatch().getId());

                List<AnswerResultsDTO> answerResults = result.stream().map(r -> AnswerResultsDTO.builder()
                                .answerOptionId(r.getId())
                                .answerBy(userMapper.toDto(r.getUser()))
                                .answerText(r.getText())
                                .users(r.getUserSelections().stream().map(userMapper::toUserDto)
                                                .collect(Collectors.toList()))
                                                .answerText(r.getText())
                                .build()).collect(Collectors.toList());

                QuestionResultDTO resultDTO = QuestionResultDTO.builder()
                                .correctAnswerId(correctAnswer.getId())
                                .userSelections(answerResults)
                                .scores(scores)
                                .build();

                matchWsPublisher.publishGameEvent(mq.getMatch().getId(), GameStatus.QUESTION_RESULTS, resultDTO);
        }

        public void publishLeaderboard(Long matchId) {
                List<PlayerScoreDTO> scores = getLeaderboard(matchId);
                matchWsPublisher.publishGameEvent(matchId, GameStatus.LEADERBOARD, scores);
        }

        @Transactional
        public void endMatch(Long matchId) {
                Match match = matchRepository.findById(matchId)
                                .orElseThrow(() -> new BusinessException("Match not found"));
                match.setStatus(MatchStatus.COMPLETED);
                match.setGameStatus(GameStatus.MATCH_ENDED);
                match.setCompletedAt(LocalDateTime.now());
                match.setLeaderboardUntil(null);
                match.setResultsUntil(null);
                matchRepository.save(match);

                publishLeaderboard(matchId);
                matchWsPublisher.publishGameEvent(matchId, GameStatus.MATCH_ENDED, getLeaderboard(matchId));
        }

        private void updatePlayerScore(Long matchId, Long userId, int points) {
                MatchPlayer player = matchPlayerRepository.findByMatchIdAndUserId(matchId, userId)
                                .orElseThrow(() -> new BusinessException("Player not found"));
                player.setScore(player.getScore() + points);
                matchPlayerRepository.save(player);
        }

        public List<PlayerScoreDTO> getLeaderboard(Long matchId) {
                return matchPlayerRepository.findByMatchIdOrderByScoreDesc(matchId).stream()
                                .map(p -> PlayerScoreDTO.builder()
                                                .userId(p.getUser().getId())
                                                .username(p.getUser().getUsername())
                                                .score(p.getScore())
                                                .build())
                                .collect(Collectors.toList());
        }
}
