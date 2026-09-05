package com.hau.user.presentation.response;

public record ApiResponse<T>(boolean success,String code,String message,T data) {
    public static <T> ApiResponse<T> success(T data){return new ApiResponse<>(true,"SUCCESS","Operation successful",data);}
}
