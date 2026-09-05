package com.hau.question.presentation.advice;
import com.hau.question.domain.exception.*; import com.hau.question.presentation.response.ApiResponse; import java.util.*; import org.springframework.http.*; import org.springframework.security.access.AccessDeniedException; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*;
@RestControllerAdvice public class GlobalExceptionHandler {
 @ExceptionHandler(NotFoundException.class)@ResponseStatus(HttpStatus.NOT_FOUND)ApiResponse<Void> notFound(NotFoundException e){return error("RESOURCE_NOT_FOUND",e.getMessage());}
 @ExceptionHandler({ForbiddenException.class,AccessDeniedException.class})@ResponseStatus(HttpStatus.FORBIDDEN)ApiResponse<Void> forbidden(Exception e){return error("FORBIDDEN",e.getMessage());}
 @ExceptionHandler(InvalidTransitionException.class)@ResponseStatus(HttpStatus.CONFLICT)ApiResponse<Void> conflict(InvalidTransitionException e){return error("INVALID_STATUS_TRANSITION",e.getMessage());}
 @ExceptionHandler({IllegalArgumentException.class,MethodArgumentNotValidException.class})@ResponseStatus(HttpStatus.BAD_REQUEST)ApiResponse<Map<String,String>> validation(Exception e){Map<String,String> errors=new LinkedHashMap<>();if(e instanceof MethodArgumentNotValidException m)m.getBindingResult().getFieldErrors().forEach(x->errors.put(x.getField(),x.getDefaultMessage()));else errors.put("request",e.getMessage());return new ApiResponse<>(false,"VALIDATION_ERROR","Request validation failed",errors);}
 @ExceptionHandler(Exception.class)@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)ApiResponse<Void> unexpected(Exception e){return error("INTERNAL_ERROR","Unexpected server error");}
 private static ApiResponse<Void> error(String c,String m){return new ApiResponse<>(false,c,m,null);}
}
