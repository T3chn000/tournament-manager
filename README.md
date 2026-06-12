# Tournament Manager

A modern Java desktop application for organizing and running Swiss and knockout tournaments. Features automatic pairing generation, match result tracking, player rankings, and persistent JSON-based tournament storage.

## Features

- **Tournament Formats:**
  - Swiss tournaments with intelligent pairing based on player ratings
  - Single-elimination knockout tournaments with bracket advancement
  - BYE handling for odd number of participants
  - Tie-break winner selection for knockout draws
  
- **Smart Pairing & Generation:**
  - Automatic round generation
  - Repeated pairing avoidance in Swiss format
  - Configurable tournament simulation (current round and full tournament preview)

- **Match Management:**
  - Enter and edit match results
  - Track wins, draws, losses, and BYE rounds
  - Dynamic ranking table with comprehensive statistics

- **Data & Persistence:**
  - JSON-based tournament save/load
  - Only essential state is persisted (derived fields are recalculated)
  - Comprehensive unit tests for all core modules

## Quick Start

### Requirements

- **JDK 25** or newer
- **Maven 3.9** or newer

Verify your installation:

```bash
java --version
mvn --version
```

### Running the Application

Launch the JavaFX UI:

```bash
mvn javafx:run
```

Or use the legacy console interface:

```bash
mvn exec:java -Dexec.mainClass="com.tournament.Main"
```

## Testing

Run the complete test suite:

```bash
mvn test
```

**Test Coverage Includes:**
- Domain model validation (`Player`, `Match`, `Round`, `Tournament`)
- Swiss and knockout pairing strategies
- Service layer behavior
- Tournament persistence and serialization
- JSON format integrity

## Technologies

| Component | Version |
|-----------|---------|
| **Java** | 25 |
| **JavaFX** | 25.0.1 |
| **JUnit** | 5.10.2 |
| **Jackson** | 2.17.0 |
| **Build Tool** | Maven 3.9+ |

## Project Structure

### Java Source

```
src/main/java/com/tournament/
├── controller/    # Legacy console interface
├── model/         # Core tournament domain logic
├── pairing/       # Pairing generation strategies (Swiss & Knockout)
├── persistence/   # JSON serialization and file I/O
├── service/       # Tournament operations and business logic
└── ui/            # JavaFX GUI components
```

### UI Resources

```
src/main/resources/
├── css/           # JavaFX stylesheets
└── fxml/          # FXML layout files
```

### Data Storage

Tournaments are saved to:

```
data/tournaments/
```

## Tournament Rules

### Swiss Format

Players are paired based on current standings, with strategies to minimize repeated matchups:

**Scoring:**
- Win: 2 points
- Draw: 1 point
- Loss: 0 points
- BYE: 2 points

**Key Mechanics:**
- Pairing avoids repeated opponents when possible
- Odd participant receives automatic BYE
- Multiple rounds until determining clear winner

### Knockout Format

Single-elimination bracket where winners advance to the next round:

**Advancement Rules:**
- Win: advance to next round
- Loss: eliminated
- Draw: requires tie-break winner selection
- Odd bracket size: leading participant(s) receive BYE

**Scoring:**
- Win: 2 points
- Tie-break win: 2 points
- Loss: 0 points
- BYE: 2 points

## Development

### Build Commands

```bash
# Compile
mvn compile

# Run tests
mvn test

# Clean build artifacts
mvn clean

# Launch UI
mvn javafx:run

# Package application
mvn package
```

## Data Format

Tournaments persist as JSON files containing:
- Tournament metadata (ID, name, type, state)
- Player roster
- Round and match records
- Points and results
- Tie-break selections

Derived data (rankings, match winners, formatted scores) is regenerated on load.

