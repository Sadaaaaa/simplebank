package com.kitchentech.frontui.dto;

public class LoginResponseDto {
    private boolean success;
    private String error;

    public LoginResponseDto() {}

    public LoginResponseDto(boolean success) {
        this.success = success;
    }

    public LoginResponseDto(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
} 