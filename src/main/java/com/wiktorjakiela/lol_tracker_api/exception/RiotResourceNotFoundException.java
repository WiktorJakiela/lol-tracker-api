package com.wiktorjakiela.lol_tracker_api.exception;

/**
 * Sygnalizuje brak konkretnego zasobu w Riot API, np. profilu LoL lub meczu.
 * W odróżnieniu od PlayerNotFoundException nie dotyczy wyszukiwania konta po Riot ID.
 * GlobalExceptionHandler zamienia ten wyjątek na odpowiedź HTTP 404.
 */
public class RiotResourceNotFoundException extends RuntimeException{
    public RiotResourceNotFoundException(String message){
        super(message);
    }
}
