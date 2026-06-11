package com.tournament.persistence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tournament.model.Match;
import com.tournament.model.Round;

import java.util.List;

/**
 * JSON representation of a tournament round.
 *
 * @param roundNumber one-based round number
 * @param matches matches scheduled in the round
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record RoundData(int roundNumber, List<MatchData> matches) {
    RoundData {
        matches = matches == null ? List.of() : List.copyOf(matches);
    }

    /**
     * Captures the persistent state of a domain round.
     */
    static RoundData fromDomain(Round round) {
        return new RoundData(
                round.getRoundNumber(),
                round.getMatches().stream()
                        .map(MatchData::fromDomain)
                        .toList()
        );
    }

    /**
     * Rebuilds a domain round from persisted data.
     */
    Round toDomain() {
        List<Match> domainMatches = matches.stream()
                .map(MatchData::toDomain)
                .toList();
        return new Round(roundNumber, domainMatches);
    }
}
