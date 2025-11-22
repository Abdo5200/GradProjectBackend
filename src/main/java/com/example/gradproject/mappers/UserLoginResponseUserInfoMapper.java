package com.example.gradproject.mappers;

import com.example.gradproject.DTO.LoginResponse;
import com.example.gradproject.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserLoginResponseUserInfoMapper {
    LoginResponse.UserInfo UserToUserInfoMapper(User user);
}
