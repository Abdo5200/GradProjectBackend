package com.example.gradproject.mappers;

import com.example.gradproject.DTO.SignupRequest;
import com.example.gradproject.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SignupRequestUserMapper {
    User SignupRequestToUser(SignupRequest signupRequest);

}
