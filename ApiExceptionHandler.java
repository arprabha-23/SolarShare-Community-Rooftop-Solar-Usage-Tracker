package com.solarshare.exception;
import org.springframework.http.*; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*; import java.time.Instant; import java.util.stream.Collectors;
@RestControllerAdvice public class ApiExceptionHandler {
 private ResponseEntity<?> body(HttpStatus s,String m){return ResponseEntity.status(s).body(java.util.Map.of("timestamp",Instant.now(),"status",s.value(),"error",s.getReasonPhrase(),"message",m));}
 @ExceptionHandler(NotFoundException.class) ResponseEntity<?> notFound(NotFoundException e){return body(HttpStatus.NOT_FOUND,e.getMessage());}
 @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<?> bad(IllegalArgumentException e){return body(HttpStatus.BAD_REQUEST,e.getMessage());}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<?> validation(MethodArgumentNotValidException e){return body(HttpStatus.BAD_REQUEST,e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).collect(Collectors.joining("; ")));}
 @ExceptionHandler(Exception.class) ResponseEntity<?> general(Exception e){return body(HttpStatus.INTERNAL_SERVER_ERROR,"An unexpected server error occurred.");}
}
