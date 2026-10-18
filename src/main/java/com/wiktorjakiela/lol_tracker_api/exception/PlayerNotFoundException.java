package com.wiktorjakiela.lol_tracker_api.exception;

/**
 * Sygnalizuje brak konta Riot dla podanej nazwy i tagu.
 * GlobalExceptionHandler zamienia ten wyjątek na odpowiedź HTTP 404.
 */
public class PlayerNotFoundException extends RuntimeException {
    public PlayerNotFoundException(String message){
        // Komunikat zapisany w RuntimeException jest później dostępny przez getMessage().
        super(message);
    }
}
