package com.wiktorjakiela.lol_tracker_api.exception;

/**
 * Sygnalizuje, że nie można pobrać danych z Riot z powodu błędu usługi,
 * problemu z połączeniem lub autoryzacją.
 * GlobalExceptionHandler zamienia ten wyjątek na odpowiedź HTTP 503.
 */
public class RiotApiUnavailableException extends RuntimeException{
     public RiotApiUnavailableException(String message){
        super(message);
    }
}
