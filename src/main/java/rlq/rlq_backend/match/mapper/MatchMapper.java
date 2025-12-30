package rlq.rlq_backend.match.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import rlq.rlq_backend.match.dtos.MatchDTO;
import rlq.rlq_backend.match.entity.Match;
import rlq.rlq_backend.match.entity.MatchPlayer;
import rlq.rlq_backend.match.entity.MatchQuestion;
import rlq.rlq_backend.match.dtos.MatchList;
import rlq.rlq_backend.match.dtos.MatchQuestionDTO;
import rlq.rlq_backend.user.dtos.UserDto;
import rlq.rlq_backend.user.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = { UserMapper.class })
public abstract class MatchMapper {

    @Autowired
    protected UserMapper userMapper;

    @Mapping(target = "players", source = "players", qualifiedByName = "toUserDtoList")
    public abstract MatchDTO toDTO(Match match);

    @Mapping(target = "players", source = "players", qualifiedByName = "toUserDtoList")
    public abstract MatchList toMatchList(Match match);

    @Mapping(target = "questionText", source = "question.text")
    @Mapping(target = "matchQuestionId", source = "id")
    public abstract MatchQuestionDTO toMatchQuestionDTO(MatchQuestion matchQuestion);

    @Named("toUserDtoList")
    protected List<UserDto> toUserDtoList(List<MatchPlayer> players) {
        if (players == null) {
            return null;
        }
        return players.stream()
                .map(MatchPlayer::getUser)
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }
}