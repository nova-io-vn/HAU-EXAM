package com.questionservice.presentation.advice;
import com.questionservice.domain.exception.*; import com.questionservice.presentation.response.ApiResponse; import java.util.*; import org.springframework.http.*; import org.springframework.security.access.AccessDeniedException; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*;
import com.questionservice.application.exception.ImageUploadException;
@RestControllerAdvice public class GlobalExceptionHandler {
 @ExceptionHandler(NotFoundException.class)@ResponseStatus(HttpStatus.NOT_FOUND)ApiResponse<Void> notFound(NotFoundException e){return error(e.code(),e.getMessage());}
 @ExceptionHandler(ForbiddenException.class)@ResponseStatus(HttpStatus.FORBIDDEN)ApiResponse<Void> forbidden(ForbiddenException e){return error(e.code(),e.getMessage());}
 @ExceptionHandler(AccessDeniedException.class)@ResponseStatus(HttpStatus.FORBIDDEN)ApiResponse<Void> accessDenied(AccessDeniedException e){return error("SUBJECT_ACCESS_DENIED","Subject operation is not allowed for the authenticated role");}
 @ExceptionHandler(com.questionservice.application.exception.UserDirectoryException.class)@ResponseStatus(HttpStatus.BAD_GATEWAY)ApiResponse<Void> directory(com.questionservice.application.exception.UserDirectoryException e){return error("USER_DIRECTORY_UNAVAILABLE","Lecturer directory is temporarily unavailable");}
 @ExceptionHandler(InvalidTransitionException.class)@ResponseStatus(HttpStatus.CONFLICT)ApiResponse<Void> conflict(InvalidTransitionException e){return error("INVALID_STATUS_TRANSITION",e.getMessage());}
 @ExceptionHandler({IllegalArgumentException.class,MethodArgumentNotValidException.class})@ResponseStatus(HttpStatus.BAD_REQUEST)ApiResponse<Map<String,String>> validation(Exception e){Map<String,String> errors=new LinkedHashMap<>();if(e instanceof MethodArgumentNotValidException m)m.getBindingResult().getFieldErrors().forEach(x->errors.put(x.getField(),x.getDefaultMessage()));else errors.put("request",e.getMessage());return new ApiResponse<>(false,"VALIDATION_ERROR","Request validation failed",errors);}
 @ExceptionHandler(ImageUploadException.class)@ResponseStatus(HttpStatus.BAD_REQUEST)ApiResponse<Void> image(ImageUploadException e){return error("IMAGE_UPLOAD_FAILED",e.getMessage());}
 @ExceptionHandler(Exception.class)@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)ApiResponse<Void> unexpected(Exception e){return error("INTERNAL_ERROR","Unexpected server error");}
 private static ApiResponse<Void> error(String c,String m){return new ApiResponse<>(false,c,m,null);}
}
