package ru.abramov.FinFlow.FinFlow.dto;

import jakarta.validation.constraints.NotBlank;

public class RefreshTokenDTO {
    @NotBlank
    private String refreshToken;

    public RefreshTokenDTO(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}