package com.wiktorjakiela.lol_tracker_api.exception;

/**
 * Sygnalizuje odpowiedź HTTP 429 z Riot, czyli przekroczenie limitu zapytań.
 * Przenosi komunikat i opcjonalny czas oczekiwania do GlobalExceptionHandler,
 * który zwraca HTTP 503 oraz nagłówek Retry-After.
 */
public class RiotRateLimitException extends RuntimeException{
    // Liczba sekund z nagłówka Riot; null oznacza brak poprawnej informacji o czasie oczekiwania.
    private final Long retryAfterSeconds;

    public RiotRateLimitException(String message, Long retryAfterSeconds){
        this.retryAfterSeconds = retryAfterSeconds;
        super(message);
    }
     public RiotRateLimitException(String message){
        // Delegujemy do głównego konstruktora, gdy znamy tylko komunikat błędu.
        this(message, null);
    }
    public Long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

}
