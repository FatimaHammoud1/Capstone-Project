package com.capstone.personalityTest.mapper;

import com.capstone.personalityTest.dto.RequestDTO.UserInfoRequest;
import com.capstone.personalityTest.dto.RequestDTO.UserUpdateRequest;
import com.capstone.personalityTest.dto.ResponseDTO.UserInfoResponse;
import com.capstone.personalityTest.model.UserInfo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserInfo toEntity(UserInfoRequest userInfoRequest);

    UserInfoResponse toResponse(UserInfo userInfo);

    //For updating an existing User
    void updateUserFromDTO(UserUpdateRequest userUpdateRequest, @MappingTarget UserInfo userInfo);


}
