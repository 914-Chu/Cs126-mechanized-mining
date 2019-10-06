package mineopoly.competition; // This is the "competition" package

import mineopoly.game.Economy;
import mineopoly.game.TurnAction;
import mineopoly.item.InventoryItem;
import mineopoly.strategy.PlayerBoardView;
import mineopoly.strategy.MinePlayerStrategy;
// ^ These classes were provided to you, they do not need to be in the competition package

import java.awt.*;
import java.util.Random;
// ^ These classes are a part of Java, they also do not need to be in the competition package

/**
 * Because this class is in the competition package, it will be compiled and run in the competition.
 * You cannot put more than one MinePlayerStrategy implementation in the competition package, so you must
 *  either delete or modify this class in order to submit your strategy implementation
 */
public class ExampleCompetitionStrategy implements MinePlayerStrategy {
    @Override
    public void initialize(int boardSize, int maxInventorySize, int winningScore, Point startTileLocation,
                           boolean isRedPlayer, Random random) {

    }

    @Override
    public TurnAction getTurnAction(PlayerBoardView boardView, Economy economy, boolean isRedTurn) {
        return null; // This strategy is just an example, it actually does nothing
    }

    @Override
    public void onReceiveItem(InventoryItem itemReceived) {
        // System.out.println("imgur.com/a/ur8UYP7")
        /* ^ As mentioned in the assignment documentation, do not print things in your competition strategy
           If you do print things in your strategy, you need to comment or remove these, otherwise
            your strategy will not be run in the competition */
    }

    @Override
    public void onSoldInventory(int totalSellPrice) {

    }

    @Override
    public String getName() {
        return ExampleOtherClass.exampleOtherMethod();
    }

    @Override
    public void endRound(int pointsScored, int opponentPointsScored) {

    }
}
