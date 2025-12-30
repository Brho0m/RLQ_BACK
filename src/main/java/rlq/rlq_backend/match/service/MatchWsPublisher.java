package rlq.rlq_backend.match.service;

import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import rlq.rlq_backend.match.dtos.GameEvent;
import rlq.rlq_backend.match.dtos.MatchDTO;
import rlq.rlq_backend.match.dtos.MatchEvent;
import rlq.rlq_backend.match.dtos.MatchList;
import rlq.rlq_backend.match.enums.GameStatus;

@Service
@AllArgsConstructor
public class MatchWsPublisher {
    private final SimpMessagingTemplate simpMessagingTemplate;

    public void publishMatchLobby(MatchEvent<MatchDTO> matchEvent) {
        simpMessagingTemplate.convertAndSend("/topic/match/" + matchEvent.getMatchId() + "/lobby", matchEvent);
    }

    public void publishMatches() {
        simpMessagingTemplate.convertAndSend("/topic/matches", MatchEvent.<MatchDTO>builder()
                .type(GameStatus.MATCH_CREATED)
                .build());
    }

    public <T> void publishGameEvent(Long matchId, GameStatus eventType, T data) {
        GameEvent<T> event = GameEvent.<T>builder()
                .type(eventType)
                .matchId(matchId)
                .data(data)
                .build();
        simpMessagingTemplate.convertAndSend("/topic/match/" + matchId + "/game", event);
    }
}
