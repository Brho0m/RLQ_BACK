package rlq.rlq_backend.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import rlq.rlq_backend.match.dtos.UserSelectionDTO;
import rlq.rlq_backend.match.entity.UserSelection;
import rlq.rlq_backend.user.dtos.UserDto;
import rlq.rlq_backend.user.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    UserDto toDto(User user);
    User toUser(UserDto userDto);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    UserDto toUserDto(UserSelection userSelection);
}
