package com.tournament.persistence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tournament.model.Player;
import com.tournament.model.Round;
import com.tournament.model.Tournament;
import com.tournament.model.TournamentState;
import com.tournament.model.TournamentType;

import java.util.List;
import java.util.UUID;

/**
 * JSON representation of a tournament persisted by {@link TournamentRepository}.
 *
 * <p>The DTO keeps storage concerns out of the domain model and stores only the
 * fields needed to rebuild the aggregate.</p>
 *
 * @param tournamentId stable tournament identifier
 * @param name tournament display name
 * @param players registered tournament players
 * @param rounds generated rounds
 * @param type tournament format
 * @param state lifecycle state
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record TournamentData(
        UUID tournamentId,
        String name,
        List<Player> players,
        List<RoundData> rounds,
        TournamentType type,
        TournamentState state
) {
    TournamentData {
        players = players == null ? List.of() : List.copyOf(players);
        rounds = rounds == null ? List.of() : List.copyOf(rounds);
    }

    /**
     * Captures the persistent state of a domain tournament.
     */
    static TournamentData fromDomain(Tournament tournament) {
        return new TournamentData(
                tournament.getTournamentId(),
                tournament.getName(),
                tournament.getPlayers(),
                tournament.getRounds().stream()
                        .map(RoundData::fromDomain)
                        .toList(),
                tournament.getType(),
                tournament.getState()
        );
    }

    /**
     * Rebuilds a domain tournament from persisted data.
     */
    Tournament toDomain() {
        List<Round> domainRounds = rounds.stream()
                .map(RoundData::toDomain)
                .toList();
        return new Tournament(tournamentId, name, players, domainRounds, type, state);
    }
}
