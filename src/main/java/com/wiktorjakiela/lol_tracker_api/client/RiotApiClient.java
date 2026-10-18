package com.wiktorjakiela.lol_tracker_api.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.ResourceAccessException;

import com.wiktorjakiela.lol_tracker_api.exception.PlayerNotFoundException;
import com.wiktorjakiela.lol_tracker_api.exception.RiotApiUnavailableException;
import com.wiktorjakiela.lol_tracker_api.exception.RiotRateLimitException;
import com.wiktorjakiela.lol_tracker_api.exception.RiotResourceNotFoundException;
import com.wiktorjakiela.lol_tracker_api.response.AccountV1Response;
import com.wiktorjakiela.lol_tracker_api.response.LeagueEntryResponse;
import com.wiktorjakiela.lol_tracker_api.response.SummonerV4Response;
import com.wiktorjakiela.lol_tracker_api.response.MatchV5Response;

import java.util.function.Supplier;
import java.util.List;

/**
 * Klasa odpowiada za komunikację z Riot API, wysyłanie requestów HTTP i odbieranie odpowiedzi.
 */
@Component
public class RiotApiClient {
    private final RestClient restClient; // narzędzie Springa do wysyłania requestów HTTP
    private final String apiKey;

    /**
     * Tworzy klienta HTTP i ustawia wspólną obsługę błędów Riot API:
     * niedostępności usługi, problemów z autoryzacją i przekroczenia limitu requestów.
     * Klucz API jest pobierany z konfiguracji aplikacji.
     */
    public RiotApiClient(RestClient.Builder restClientBuilder,
        @Value("${riot.api.key}") String apiKey) {
        this.restClient = restClientBuilder.defaultStatusHandler(
        status -> status.is5xxServerError(),
        (request, response) -> {
            throw new RiotApiUnavailableException("Riot services are temporarily unavailable.");})
        .defaultStatusHandler(
        status -> status.value() == 401 || status.value() == 403,
        (request, response) -> {
            throw new RiotApiUnavailableException("Data retrieval is currently unavailable.");})
        .defaultStatusHandler(
        status -> status.value() == 429,
        (request, response) -> {
            // Odczytujemy czas oczekiwania podany przez Riot, aby przekazać go klientowi API.
            Long retryAfterSeconds = parseRetryAfterSeconds(response.getHeaders().getFirst("Retry-After"));
            throw new RiotRateLimitException("Riot API rate limit reached. Please try again later.",retryAfterSeconds);})
        .build();
        this.apiKey = apiKey;
    }

    //Pobiera konto Riot po nazwie i tagu, w tym PUUID potrzebne do kolejnych zapytań.
    public AccountV1Response getAccountByRiotId(String gameName, String tagLine, String regionalRoute) {
        try {
            return executeRequest(() -> restClient.get().uri(
            "https://" + regionalRoute + ".api.riotgames.com/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}", gameName, tagLine)
            .header("X-Riot-Token",apiKey).retrieve()
            .body(AccountV1Response.class));
        } catch (HttpClientErrorException.NotFound exception) {
            throw new PlayerNotFoundException("Player not found.");
        }
    }

    // Pobiera profil LoL po PUUID z konkretnego serwera gry
    public SummonerV4Response getSummonerByPuuid(String puuid, String platform) {
        try{
            return executeRequest(() -> restClient.get().uri(
                "https://" + platform + ".api.riotgames.com/lol/summoner/v4/summoners/by-puuid/{puuid}", puuid)
                .header("X-Riot-Token",apiKey).retrieve()
                .body(SummonerV4Response.class));
        } catch (HttpClientErrorException.NotFound exception) {
            throw new RiotResourceNotFoundException("Player profile not found.");
        }
    }

    /**
     * Pobiera wpisy rankingowe gracza dla poszczególnych kolejek na danym serwerze.
     * Pusta lista oznacza brak wpisów rankingowych, nie błąd pobierania.
     */
    public List<LeagueEntryResponse> getLeagueEntriesByPuuid(String puuid, String platform) {
        return executeRequest(() -> restClient.get().uri(
            "https://" + platform + ".api.riotgames.com/lol/league/v4/entries/by-puuid/{puuid}", puuid)
            .header("X-Riot-Token",apiKey).retrieve()
            // Przekazujemy również typ elementów listy, aby JSON został odczytany jako recordy.
            .body(new ParameterizedTypeReference<List<LeagueEntryResponse>>(){}));
    }

    /**
     * Pobiera stronę identyfikatorów meczów gracza, bez ich szczegółów.
     * start to liczba pomijanych meczów, a count to liczba zwracanych ID.
     */
    public List<String> getMatchIdsByPuuid(String puuid, String regionalRoute, int start, int count) {
        return executeRequest(() -> restClient.get().uri(
            "https://" + regionalRoute + ".api.riotgames.com/lol/match/v5/matches/by-puuid/{puuid}/ids?start={start}&count={count}", puuid,start,count)
            .header("X-Riot-Token",apiKey).retrieve()
            .body(new ParameterizedTypeReference<List<String>>(){}));
    }


    //Pobiera dane jednego meczu, obejmujące uczestników i drużyny.
    public MatchV5Response getMatchById(String matchId, String regionalRoute) {
        try {
            return executeRequest(() -> restClient.get().uri(
                "https://" + regionalRoute + ".api.riotgames.com/lol/match/v5/matches/{matchId}",matchId)
                .header("X-Riot-Token", apiKey).retrieve()
                .body(MatchV5Response.class));
        } catch (HttpClientErrorException.NotFound exception) {
            throw new RiotResourceNotFoundException("Match not found.");
        }
    }

    /**
     * Wykonuje przekazane zapytanie i zamienia błędy połączenia, np. timeout,
     * na wyjątek niedostępności Riot. Typ T pozwala zwrócić różne rodzaje odpowiedzi.
     * Pozostałe wyjątki przekazuje wyżej, do lokalnych catch obsługujących 404.
     */
    private <T> T executeRequest(Supplier<T> request) {
        try {
            return request.get();
        }
        catch (ResourceAccessException exception){
            throw new RiotApiUnavailableException("Unable to connect to Riot services.");
        }
    }

    /**
     * Odczytuje Retry-After zapisane jako nieujemna liczba sekund.
     * Brak lub niepoprawna wartość daje null, aby błąd nagłówka nie przesłonił błędu limitu.
     */
    private Long parseRetryAfterSeconds(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            long seconds = Long.parseLong(value.trim());
            if (seconds >= 0) {
                return seconds;
            }
            return null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
