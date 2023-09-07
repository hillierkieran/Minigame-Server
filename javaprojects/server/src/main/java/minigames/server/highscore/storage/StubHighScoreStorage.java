package minigames.server.highscore;

import minigames.server.highscore.GameMetadata;
import minigames.server.highscore.ScoreRecord;

import java.util.Collections;
import java.util.List;

@Deprecated
public class StubHighScoreStorage implements HighScoreStorage {

    @Override
    public void registerGame(String gameName, Boolean isLowerBetter) {
        // Do nothing for now
    }

    @Override
    public void storeScore(ScoreRecord record) {
        // Do nothing for now
    }

    @Override
    public List<ScoreRecord> retrieveTopScores(String gameName) {
        return Collections.emptyList();  // Return an empty list
    }

    @Override
    public ScoreRecord retrievePersonalBest(String playerId, String gameName) {
        return null;  // Return null for now
    }

    @Override
    public List<ScoreRecord> retrieveAllScores() {
        return Collections.emptyList();  // Return an empty list
    }

    @Override
    public GameMetadata getGameMetadata(String gameName) {
        return new GameMetadata(gameName, true);  // Return a dummy game metadata for now
    }
}