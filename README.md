# Tournament Manager

Tournament Manager is a Java desktop application for running Swiss and knockout tournaments. The project includes domain logic, automatic pairing generation, result tracking, player rankings, JSON persistence, and a JavaFX user interface.

## Screenshot

[Tournament Manager - main view](docs/images/app_screenshot.png)

## Features

- create tournaments with a custom player list,
- support for `SWISS` and `KNOCKOUT` tournament formats,
- automatic round generation,
- `BYE` handling for an odd number of players,
- entering and editing match results,
- tie-break winner selection for knockout draws,
- current round simulation and full knockout tournament simulation,
- ranking table with points, wins, draws, losses, played matches, and BYE count,
- JSON-based tournament save/load,
- unit tests for the model, pairing strategies, service layer, repository, and serialization format.

## Technologies

- Java 25,
- JavaFX 25,
- Maven,
- JUnit 5,
- Jackson Databind.

## Requirements

Before running the project, make sure you have installed:

- JDK 25,
- Maven 3.9 or newer.

Check your versions:

```bash
java --version
mvn --version
```

## Running The Application

JavaFX graphical interface:

```bash
mvn javafx:run
```

The project also contains an older console mode:

```bash
mvn exec:java -Dexec.mainClass="com.tournament.Main"
```

If the console command does not work out of the box, add `exec-maven-plugin` to `pom.xml` or run `com.tournament.Main` directly from your IDE.

## Tests

Run the full test suite:

```bash
mvn test
```

The current tests cover, among other things:

- validation for `Player`, `Match`, `Round`, and `Tournament`,
- Swiss and Knockout pairing generation,
- `TournamentService` behavior,
- save/load behavior in `TournamentRepository`,
- JSON serialization format.

## Project Structure

```text
src/main/java/com/tournament
├── controller      # older console interface
├── model           # tournament domain model
├── pairing         # pairing generation strategies
├── persistence     # tournament save/load logic
├── service         # tournament operations
└── ui              # JavaFX application
```

JavaFX resources:

```text
src/main/resources
├── css             # application styles
└── fxml            # JavaFX views
```

Application data is saved by default in:

```text
data/tournaments
```

## Tournament Rules

### Swiss

In a Swiss tournament, players are paired according to their current point totals. The strategy avoids repeated pairings whenever possible. If the number of players is odd, one player receives a `BYE`.

Scoring:

- win: 2 points,
- draw: 1 point,
- loss: 0 points,
- `BYE`: 2 points.

### Knockout

In a knockout tournament, winners advance to the next round. If a match ends in a draw, a tie-break winner must be selected. If the number of players is not a power of two, the application adds `BYE` matches.

## Data Persistence

Tournaments are saved as JSON files. Each file contains only the data needed to restore tournament state, including:

- tournament ID,
- name,
- player list,
- rounds,
- matches,
- points,
- result,
- tie-break winner,
- tournament type and state.

Derived fields, such as current ranking, match winner, or formatted score text, are recalculated by the application and do not need to be stored as separate data.

## Useful Commands

Compile:

```bash
mvn compile
```

Run tests:

```bash
mvn test
```

Clean build output:

```bash
mvn clean
```

Run the UI:

```bash
mvn javafx:run
```
