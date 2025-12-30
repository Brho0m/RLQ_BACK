package rlq.rlq_backend.match.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import rlq.rlq_backend.match.dtos.*;
import rlq.rlq_backend.match.service.GameService;
import rlq.rlq_backend.match.service.MatchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final GameService gameService;

    @GetMapping()
    public ResponseEntity<List<MatchList>> getAllMatches() {
        return ResponseEntity.ok(matchService.getAllMatches());
    }

    @GetMapping("{matchId}")
    public ResponseEntity<MatchDTO> getMatch(@PathVariable Long matchId) {
        return ResponseEntity.ok(matchService.getMatch(matchId));
    }

    @PostMapping("{matchId}/join")
    public ResponseEntity<MatchDTO> joinMatch(@PathVariable Long matchId) {
        return ResponseEntity.ok(matchService.joinMatch(matchId));
    }

    @GetMapping("{matchId}/state")
    public ResponseEntity<GameStateDTO> getGameState(@PathVariable Long matchId) {
        return ResponseEntity.ok(gameService.getGameState(matchId));
    }

    @PostMapping("{matchId}/questions/{mqId}/answer")
    public ResponseEntity<Void> submitAnswer(
            @PathVariable Long matchId,
            @PathVariable Long mqId,
            @RequestBody SubmitAnswerDTO dto) {
        gameService.submitAnswer(matchId, mqId, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("{matchId}/questions/{mqId}/select")
    public ResponseEntity<Void> selectAnswer(
            @PathVariable Long matchId,
            @PathVariable Long mqId,
            @RequestBody SelectAnswerDTO dto) {
        gameService.selectAnswer(matchId, mqId, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("{matchId}/leaderboard")
    public ResponseEntity<List<PlayerScoreDTO>> getLeaderboard(@PathVariable Long matchId) {
        return ResponseEntity.ok(gameService.getLeaderboard(matchId));
    }
}
