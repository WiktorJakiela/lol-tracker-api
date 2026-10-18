package com.wiktorjakiela.lol_tracker_api.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.wiktorjakiela.lol_tracker_api.response.ApiErrorResponse;

/**
 * klasa zamieniająca wyjątki powstałe w aplikacji na odpowiedzi HTTP.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Niepoprawne argumenty żądania, np. ujemny start historii. Zwraca HTTP 400.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
        IllegalArgumentException exception) {
        ApiErrorResponse response = new ApiErrorResponse(exception.getMessage());
        return  ResponseEntity.badRequest().body(response);
    }
    // Nie znaleziono konta Riot dla podanej nazwy i tagu. Zwraca HTTP 404.
    @ExceptionHandler(PlayerNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePlayerNotFoundException(PlayerNotFoundException exception){
        ApiErrorResponse response = new ApiErrorResponse(exception.getMessage());
        return  ResponseEntity.status(404).body(response);
    }
    // Nie można pobrać danych z Riot, np. przez awarię, brak połączenia
    // lub problem z kluczem API. Zwraca HTTP 503.
    @ExceptionHandler(RiotApiUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleRiotApiUnavaibleException(RiotApiUnavailableException exception){
        ApiErrorResponse response = new ApiErrorResponse(exception.getMessage());
        return  ResponseEntity.status(503).body(response);
    }
    // Riot ograniczył liczbę zapytań. Nasze API zwraca HTTP 503,
    // a jeśli znamy czas oczekiwania, przekazuje go w nagłówku Retry-After.
    @ExceptionHandler(RiotRateLimitException.class)
    public ResponseEntity<ApiErrorResponse> handleRiotRateLimitException(RiotRateLimitException exception){
        ApiErrorResponse response = new ApiErrorResponse(exception.getMessage());
        Long retryAfterSeconds = exception.getRetryAfterSeconds();
        if (retryAfterSeconds != null) {
            return ResponseEntity.status(503).header("Retry-After", retryAfterSeconds.toString()).body(response);
        }
        return  ResponseEntity.status(503).body(response);
    }

    // Nie znaleziono zasobu w Riot API, np. profilu LoL lub meczu, zwraca HTTP 404.
    @ExceptionHandler(RiotResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleRiotResourceNotFoundException(RiotResourceNotFoundException exception){
        ApiErrorResponse response = new ApiErrorResponse(exception.getMessage());
        return ResponseEntity.status(404).body(response);
    }
}
