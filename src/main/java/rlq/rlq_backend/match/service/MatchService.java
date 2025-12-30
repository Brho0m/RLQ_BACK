package rlq.rlq_backend.match.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import rlq.rlq_backend.exception.BusinessException;
import rlq.rlq_backend.match.dtos.MatchDTO;
import rlq.rlq_backend.match.dtos.MatchEvent;
import rlq.rlq_backend.match.dtos.MatchList;
import rlq.rlq_backend.match.entity.Match;
import rlq.rlq_backend.match.entity.MatchPlayer;
import rlq.rlq_backend.match.enums.GameStatus;
import rlq.rlq_backend.match.enums.MatchStatus;
import rlq.rlq_backend.match.mapper.MatchMapper;
import rlq.rlq_backend.match.repository.MatchPlayerRepository;
import rlq.rlq_backend.match.repository.MatchRepository;
import rlq.rlq_backend.user.entity.User;
import rlq.rlq_backend.user.enums.Roles;
import rlq.rlq_backend.user.util.UserUtil;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchPlayerRepository matchPlayerRepository;
    private final MatchWsPublisher matchWsPublisher;
    private final MatchMapper matchMapper;
    private final UserUtil userUtil;

    public List<MatchList> getAllMatches() {
        return matchRepository.findAll().stream().map(m -> matchMapper.toMatchList(m)).toList();
    }

    public MatchDTO getMatch(Long matchId) {
        Match match = matchRepository.findById(matchId).orElseThrow(() -> new BusinessException("Match not found"));
        return matchMapper.toDTO(match);
    }

    public MatchDTO joinMatch(Long matchId) {
        User user = userUtil.getCurrentUser();
        Match match = matchRepository.findById(matchId).orElseThrow(() -> new BusinessException("Match not found"));

        MatchDTO matchDTO = matchMapper.toDTO(match);
        if (user.getRole() == Roles.ROLE_ADMIN || match.getStatus() != MatchStatus.WAITING) {
            return matchDTO;
        }

        MatchPlayer player = matchPlayerRepository.findByMatchIdAndUserId(matchId, user.getId())
                .orElse(null);

        if (null == player) {
            match.getPlayers().add(
                    MatchPlayer.builder()
                            .match(match)
                            .user(user)
                            .joinedAt(LocalDateTime.now())
                            .score(0L)
                            .build());

            matchRepository.save(match);

            MatchEvent<MatchDTO> event = MatchEvent.<MatchDTO>builder()
                    .type(GameStatus.PLAYER_JOINED)
                    .data(matchMapper.toDTO(match))
                    .matchId(matchId)
                    .build();

            matchWsPublisher.publishMatchLobby(event);
        }

        return matchMapper.toDTO(match);
    }

}
