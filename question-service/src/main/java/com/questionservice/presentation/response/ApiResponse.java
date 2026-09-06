package com.questionservice.presentation.response;
public record ApiResponse<T>(boolean success,String code,String message,T data){public static <T> ApiResponse<T> ok(T data){return new ApiResponse<>(true,"SUCCESS","Operation successful",data);}}
