package com.fincore.shared.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.validation.ConstraintViolationException;

import com.fincore.shared.exception.ConflictException;
import com.fincore.shared.exception.OperationNotAllowedException;
import com.fincore.shared.exception.ResourceNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduce excepciones de dominio y validación al formato estándar Problem Details. */
@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado", exception.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    ProblemDetail handleConflict(ConflictException exception) {
        return problem(HttpStatus.CONFLICT, "Conflicto", exception.getMessage());
    }

    @ExceptionHandler(OperationNotAllowedException.class)
    ProblemDetail handleOperationNotAllowed(OperationNotAllowedException exception) {
        return problem(HttpStatus.UNPROCESSABLE_CONTENT, "Operación no permitida", exception.getMessage());
    }

    /** Devuelve los errores por campo sin exponer detalles internos de Spring. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleInvalidBody(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        ProblemDetail detail = problem(
                HttpStatus.BAD_REQUEST,
                "Solicitud inválida",
                "Uno o más campos no cumplen el contrato.");
        detail.setProperty("errors", errors);
        return detail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail handleConstraintViolation(ConstraintViolationException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Solicitud inválida", exception.getMessage());
    }

    /** Traduce carreras en restricciones únicas sin revelar SQL ni nombres internos. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleDataIntegrity(DataIntegrityViolationException exception) {
        return problem(
                HttpStatus.CONFLICT,
                "Conflicto de datos",
                "La operación entra en conflicto con un recurso existente.");
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
