package com.wiktorjakiela.lol_tracker_api.service;


import org.springframework.stereotype.Service;

import com.wiktorjakiela.lol_tracker_api.client.RiotApiClient;
import com.wiktorjakiela.lol_tracker_api.response.PlayerResponse;
import com.wiktorjakiela.lol_tracker_api.response.RankedEntryResponse;
import com.wiktorjakiela.lol_tracker_api.response.SummonerV4Response;
import com.wiktorjakiela.lol_tracker_api.response.MatchV5Response.PerkSelection;
import com.wiktorjakiela.lol_tracker_api.response.MatchV5Response.PerkStyle;
import com.wiktorjakiela.lol_tracker_api.response.AccountV1Response;
import com.wiktorjakiela.lol_tracker_api.response.LeagueEntryResponse;
import com.wiktorjakiela.lol_tracker_api.response.MatchDetailResponse;
import com.wiktorjakiela.lol_tracker_api.response.MatchParticipantResponse;
import com.wiktorjakiela.lol_tracker_api.response.MatchRunesResponse;
import com.wiktorjakiela.lol_tracker_api.response.MatchTeamResponse;
import com.wiktorjakiela.lol_tracker_api.response.MatchV5Response;

import java.util.List;
import java.util.stream.*;

// Koordynuje pobieranie danych z Riot i przygotowuje odpowiedzi naszego API.

@Service
public class PlayerService {
    private final RiotApiClient riotApiClient;

    // Spring przekazuje klienta Riot przez konstruktor.
    public PlayerService(RiotApiClient riotApiClient) {
        this.riotApiClient = riotApiClient;
    }

    // Łączy dane konta Riot, profilu LoL i rankingu w jeden profil dla frontendu.
    public PlayerResponse getPlayerByRiotId(String gameName, String tagLine,String region) {
        validatePlayerInput(gameName, tagLine, region);

        // Account używa trasy regionalnej, a Summoner i League konkretnego serwera gry.
        String platform = getPlatform(region);
        String regionalRoute = getRegionalRoute(platform);

        // PUUID z konta pozwala pobrać pozostałe dane bez ponownego wyszukiwania po nazwie.
        AccountV1Response account = riotApiClient.getAccountByRiotId(gameName, tagLine, regionalRoute);
        SummonerV4Response summoner = riotApiClient.getSummonerByPuuid(account.puuid(), platform);
        List<LeagueEntryResponse> leagueEntries = riotApiClient.getLeagueEntriesByPuuid(account.puuid(), platform);
        List<RankedEntryResponse> rankedEntries = leagueEntries.stream().map(entry -> toRankedEntryResponse(entry)).toList();
        return new PlayerResponse(summoner.puuid(),account.gameName(),account.tagLine(),region, summoner.summonerLevel(),summoner.profileIconId(),rankedEntries);
    }

    // Pobiera stronę historii i przygotowuje szczegóły wszystkich meczów.
    public List<MatchDetailResponse> getMatchHistoryByRiotId(String gameName, String tagLine, String region, int start, int count)
     {
        // Limit strony ogranicza liczbę osobnych zapytań o szczegóły meczów.
        if (start < 0) {
            throw new IllegalArgumentException("The start parameter must not be negative.");
        }
        if (count < 1 || count > 10) {
            throw new IllegalArgumentException("The count parameter must be between 1 and 10.");
        }
        validatePlayerInput(gameName, tagLine, region);

        String platform = getPlatform(region);
        String regionalRoute = getRegionalRoute(platform);

        AccountV1Response account = riotApiClient.getAccountByRiotId(gameName, tagLine, regionalRoute);

        // Pierwszy request zwraca tylko ID meczów, start określa przesunięcie, a count liczbę meczów.
        List<String> matchIds = riotApiClient.getMatchIdsByPuuid(account.puuid(), regionalRoute, start, count);

        // Dla każdego ID pobieramy szczegóły.
        List<MatchV5Response> matches = matchIds.stream().map(matchId -> riotApiClient.getMatchById(matchId, regionalRoute)).toList();

        // Surowe odpowiedzi Riot zamieniamy na modele zwracane przez nasze API.
        return matches.stream().map(match -> toMatchDetailResponse(match)).toList();
    }


    // Mapuje uczestnika jednego meczu i wylicza statystyki w meczu
     private MatchParticipantResponse toMatchParticipantResponse(MatchV5Response.Participant participant, int teamKills, long gameDuration) {
        int cs = participant.totalMinionsKilled() + participant.neutralMinionsKilled();
        MatchRunesResponse runes = toMatchRunesResponse(participant.perks());
        // Bez zgonów KDA pozostaje null, co frontend prezentuje jako Perfect KDA.
        Double kda = null;
        Double killParticipation = 0.0;
        Double csPerMinute = null;
        if (participant.deaths() != 0) {
            kda = Math.round((double) (participant.kills() + participant.assists()) / participant.deaths() * 100) / 100.0;
        }
        if (teamKills > 0) {
            double participationPercent = (participant.kills() + participant.assists()) / (double) teamKills * 100.0;
            killParticipation = Math.round(participationPercent * 100) / 100.0;
        }
        if (gameDuration > 0) {
            double durationMinutes = gameDuration / 60.0;
            csPerMinute = Math.round(cs / durationMinutes * 100) / 100.0;
        }

        List<Integer> itemIds = List.of(
        participant.item0(),
        participant.item1(),
        participant.item2(),
        participant.item3(),
        participant.item4(),
        participant.item5(),
        participant.item6());

        return new MatchParticipantResponse(
        participant.puuid(),
        participant.riotIdGameName(),
        participant.riotIdTagline(),
        participant.teamId(),
        participant.win(),
        participant.championId(),
        participant.championName(),
        participant.champLevel(),
        participant.teamPosition(),
        participant.kills(),
        participant.deaths(),
        participant.assists(),
        kda,
        killParticipation,
        cs,
        csPerMinute,
        participant.goldEarned(),
        participant.visionScore(),
        itemIds,
        participant.summoner1Id(),
        participant.summoner2Id(),
        participant.totalDamageDealtToChampions(),
        participant.totalDamageTaken(),
        runes
        );
    }

    // Upraszcza dane drużyny do wyniku, listy banów i liczników objectivów.
    private MatchTeamResponse toMatchTeamResponse(MatchV5Response.Team team) {
        List<Integer> bannedChampionIds = team.bans().stream().map(ban -> ban.championId()).toList();

        return new MatchTeamResponse(
        team.teamId(),
        team.win(),
        objectiveKills(team.objectives().champion()),
        bannedChampionIds,
        objectiveKills(team.objectives().baron()),
        objectiveKills(team.objectives().dragon()),
        objectiveKills(team.objectives().horde()),
        objectiveKills(team.objectives().riftHerald()),
        objectiveKills(team.objectives().tower()),
        objectiveKills(team.objectives().inhibitor())
        );

    }

    // Składa dane meczu z osobno przygotowanych drużyn i uczestników.
    private MatchDetailResponse toMatchDetailResponse(MatchV5Response match) {
        List<MatchTeamResponse> teams = match.info().teams().stream().map(team -> toMatchTeamResponse(team)).toList();
        List<MatchParticipantResponse> participants = match.info().participants().stream()
            .map(participant -> {
                // Dopasowujemy uczestnika do drużyny po teamId.
                MatchTeamResponse team = teams.stream()
                    .filter(candidate -> candidate.teamId() == participant.teamId())
                    .findFirst()
                    // Brak pasującej drużyny oznacza niespójne dane meczu.
                    .orElseThrow(() -> new IllegalStateException(
                        "Missing team " + participant.teamId() + " in match " + match.metadata().matchId()));

                return toMatchParticipantResponse(participant, team.kills(), match.info().gameDuration());
            })
            .toList();

        return new MatchDetailResponse(
        match.metadata().matchId(),
        match.info().gameStartTimestamp(),
        match.info().gameDuration(),
        match.info().queueId(),
        match.info().gameVersion(),
        participants,
        teams
        );
    }

    // Spłaszcza zagnieżdżone dane run Riot do ID drzewek, run i odłamków dla frontendu.
    private MatchRunesResponse toMatchRunesResponse(MatchV5Response.Perks perks) {
        // Niekompletna główna struktura oznacza brak danych run.
        if (perks == null || perks.statPerks() == null || perks.styles() == null) {return null;}
        int defenseShardId = perks.statPerks().defense();
        int offenseShardId = perks.statPerks().offense();
        int flexShardId = perks.statPerks().flex();
        // Brak rozpoznanego drzewka pozostawia ID równe 0 i pustą listę jego run.
        int primaryStyleId = 0;
        int secondaryStyleId = 0;
        List<Integer> primaryRuneIds = List.of();
        List<Integer> secondaryRuneIds = List.of();
        for (PerkStyle perkStyle : perks.styles()) {

            List<PerkSelection> selections = perkStyle.selections();
            // Pomijamy drzewko bez listy wyborów, zachowując pozostałe dostępne dane.
            if(selections == null) {
                continue;
            }

            // description opisuje, czy drzewko jest główne czy poboczne.
            if("primaryStyle".equals(perkStyle.description())) {
                primaryStyleId = perkStyle.style();
                primaryRuneIds = selections.stream().map(selection -> selection.perk()).toList();
            }
            if("subStyle".equals(perkStyle.description())) {
                secondaryStyleId = perkStyle.style();
                secondaryRuneIds = selections.stream().map(selection -> selection.perk()).toList();
            }
        }
        return new MatchRunesResponse(
            primaryStyleId,
            secondaryStyleId,
            primaryRuneIds,
            secondaryRuneIds,
            offenseShardId,
            flexShardId,
            defenseShardId
        );
    }

    // Rozszerza wpis z Riot API o liczbę gier i procent wygranych zaokrąglony do dwóch miejsc.
    private RankedEntryResponse toRankedEntryResponse(LeagueEntryResponse lolEntryResponse) {
        int gamesPlayed = lolEntryResponse.wins() + lolEntryResponse.losses();
        // Przy zerowej liczbie gier zwracamy 0%.
        Double winrate = 0.0;
        if (gamesPlayed != 0){
            Double winratePercent = lolEntryResponse.wins() / (double) gamesPlayed * 100.0;
            winrate = Math.round(winratePercent * 100) / 100.0;
        }
        return new RankedEntryResponse(
            lolEntryResponse.queueType(),
            lolEntryResponse.tier(),
            lolEntryResponse.rank(),
            lolEntryResponse.leaguePoints(),
            lolEntryResponse.wins(),
            lolEntryResponse.losses(),
            gamesPlayed,
            winrate
        );

    }
    // Brak danego celu w odpowiedzi Riot traktujemy jako licznik równy 0.
    private int objectiveKills(MatchV5Response.Objective objective) {
        if (objective == null) {
            return 0;
        }
        return objective.kills();
    }


    // Przypisuje serwer gry do szerszej trasy (Regional Route) używanej przez Account-V1 i Match-V5.
    private String getRegionalRoute(String platform) {
        return switch (platform) {
            case "eun1", "euw1" -> "europe";
            case "kr", "jp1" -> "asia";
            case "na1", "br1", "la1", "la2" -> "americas";
            default -> throw new IllegalArgumentException("Unsupported platform: " + platform);
        };
    }

    // Zamienia region aplikacji, np. EUNE, na identyfikator serwera Riot, np. eun1.
    private String getPlatform(String region) {
    return switch (region.toUpperCase()) {
        case "EUNE" -> "eun1";
        case "EUW" -> "euw1";
        case "KR" -> "kr";
        case "NA" -> "na1";
        default -> throw new IllegalArgumentException(
                "Unsupported region: " + region
            );
        };
    }

    // Odrzuca brakujące dane do poprawnego wyszukania przed requestami, GlobalExceptionHandler zamienia wyjątek na HTTP 400.
    private void validatePlayerInput(String gameName, String tagLine, String region) {
        if (gameName == null || gameName.isBlank() ) {
            throw new IllegalArgumentException("Player name must not be blank.");
        }
        if (tagLine == null || tagLine.isBlank()) {
            throw new IllegalArgumentException("Tag must not be blank.");
        }
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("Region must not be blank.");
        }
    }
}
