package com.capstone.personalityTest.dto.ResponseDTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {

    private String accessToken;

    private String refreshToken;



}