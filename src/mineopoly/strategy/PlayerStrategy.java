package mineopoly.strategy;

import mineopoly.game.Economy;
import mineopoly.game.TurnAction;
import mineopoly.item.InventoryItem;
import mineopoly.tiles.Tile;
import mineopoly.tiles.TileType;

import java.awt.*;
import java.awt.image.AffineTransformOp;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class PlayerStrategy implements MinePlayerStrategy {

    private int boardSize;
    private int halfBoardSize;
    private int maxInventorySize;
    private int winningScore;
    private Point startTileLocation;
    private Point redLowerMarketPoint;
    private Point redUpperMarketPoint;
    private Point blueLowerMarketPoint;
    private Point blueUpperMarketPoint;
    private boolean isRedPlayer;
    private boolean goingToMarket;
    private Random random;
    private List<TurnAction> allPossibleActions;
    private List<InventoryItem> inventoryItemList;
    private List<TurnAction> pathToMarket;
    private final int ADJACENT_TILES_AMOUNT = 4;
    private final int NEXT_MOVE_TO_MARKET = 0;


    /**
     * Called at the start of every round
     *
     * @param boardSize         The length and width of the square game board
     * @param maxInventorySize  The maximum number of items that your player can carry at one time
     * @param winningScore      The first player to reach this score wins the round
     * @param startTileLocation A Point representing your starting location in (x, y) coordinates
     *                          (0, 0) is the bottom left and (boardSize - 1, boardSize - 1) is the top right
     * @param isRedPlayer       True if this strategy is the red player, false otherwise
     * @param random            A random number generator, if your strategy needs random numbers you should use this.
     */
    @Override
    public void initialize(int boardSize, int maxInventorySize, int winningScore, Point startTileLocation, boolean isRedPlayer, Random random) {

        this.boardSize = boardSize;
        this.maxInventorySize = maxInventorySize;
        this.winningScore = winningScore;
        this.startTileLocation = startTileLocation;
        this.isRedPlayer = isRedPlayer;
        this.random = random;
        this.allPossibleActions = new ArrayList<>(EnumSet.allOf(TurnAction.class));
        allPossibleActions.add(null);
        inventoryItemList = new ArrayList<>(maxInventorySize);
        goingToMarket = false;
        halfBoardSize = boardSize / 2;
        redLowerMarketPoint = new Point(halfBoardSize - 1, halfBoardSize - 1);
        redUpperMarketPoint = new Point(halfBoardSize, halfBoardSize);
        blueLowerMarketPoint = new Point(halfBoardSize, halfBoardSize - 1);
        blueUpperMarketPoint = new Point(halfBoardSize - 1, halfBoardSize);
    }

    /**
     * The main part of your strategy, this method returns what action your player should do on this turn
     *
     * @param boardView A PlayerBoardView object representing all the information about the board and the other player
     *                  that your strategy is allowed to access
     * @param economy   The GameEngine's economy object which holds current prices for resources
     * @param isRedTurn For use when two players attempt to move to the same spot on the same turn
     *                  If true: The red player will move to the spot, and the blue player will do nothing
     *                  If false: The blue player will move to the spot, and the red player will do nothing
     * @return The TurnAction that this strategy wants to perform on this game turn
     */
    @Override
    public TurnAction getTurnAction(PlayerBoardView boardView, Economy economy, boolean isRedTurn) {

        Point selfLocation = boardView.getYourLocation();
        Point otherLocation = boardView.getOtherPlayerLocation();
        Point market;
        List<TileType> adjacentTileTypeList = getAdjacentTileTypes(boardView, selfLocation);
        TileType selfTileType = boardView.getTileTypeAtLocation(selfLocation);
        TurnAction action;

        if(goingToMarket) {
            action = pathToMarket.get(NEXT_MOVE_TO_MARKET);
            pathToMarket.remove(NEXT_MOVE_TO_MARKET);
        }else if(inventoryItemList.size() == maxInventorySize){
            market = findClosestMarket(selfLocation);
            pathToMarket = getPathToMarket(selfLocation);

        }


        return null;
    }

    /**
     * Called when the player receives an item from performing a TurnAction that gives an item.
     * At the moment this is only from using PICK_UP on top of a mined resource
     *
     * @param itemReceived The item received from the player's TurnAction on their last turn
     */
    @Override
    public void onReceiveItem(InventoryItem itemReceived) {

    }

    /**
     * Called when the player steps on a market tile with items to sell. Tells your strategy how much all
     * of the items sold for.
     *
     * @param totalSellPrice The combined sell price for all items in your strategy's inventory
     */
    @Override
    public void onSoldInventory(int totalSellPrice) {

    }

    /**
     * Gets the name of this strategy. The amount of characters that can actually be displayed on a screen varies,
     * although by default at screen size 750 it's about 16-20 characters depending on character size
     *
     * @return The name of your strategy for use in the competition and rendering the scoreboard on the GUI
     */
    @Override
    public String getName() {
        return "PlayerStrategy";
    }

    /**
     * Called at the end of every round to let players reset, and tell them how they did if the strategy does not
     * track that for itself
     *
     * @param pointsScored         The total number of points this strategy scored
     * @param opponentPointsScored The total number of points the opponent's strategy scored
     */
    @Override
    public void endRound(int pointsScored, int opponentPointsScored) {

    }

    public List<TileType> getAdjacentTileTypes(PlayerBoardView boardView, Point location) {

        List<TileType> adjacentTilesTypeList = new ArrayList<>(ADJACENT_TILES_AMOUNT);
        List<Point> adjacentPointList = getAdjacentPoints(location);

        for(int i = 0; i < ADJACENT_TILES_AMOUNT; i++) {
            adjacentTilesTypeList.add(boardView.getTileTypeAtLocation(adjacentPointList.get(i)));
        }

        return adjacentTilesTypeList;
    }

    public List<Point> getAdjacentPoints(Point location) {

        List<Point> adjacentPointList = new ArrayList<>(ADJACENT_TILES_AMOUNT);
        adjacentPointList.add(new Point(location.x + 1, location.y)); //UP
        adjacentPointList.add(new Point(location.x - 1, location.y)); //DOWN
        adjacentPointList.add(new Point(location.x,location.y - 1));  //LEFT
        adjacentPointList.add(new Point(location.x, location.y + 1)); //RIGHT

        return adjacentPointList;
    }

    public List<TurnAction> getPathToMarket(Point location) {

        List<TurnAction> pathToMarket = new ArrayList<>();
        Point market = findClosestMarket(location);
        int horizontalMove = market.x - location.x;
        int verticalMove = market.y - location.y;
        if(horizontalMove < 0) {
            for(int i = 0; i < Math.abs(horizontalMove); i++) {
                pathToMarket.add(TurnAction.MOVE_LEFT);
            }
        }else {
            for(int i = 0; i < Math.abs(horizontalMove); i++) {
                pathToMarket.add(TurnAction.MOVE_RIGHT);
            }
        }

        if(verticalMove < 0) {
            for(int i = 0; i < Math.abs(verticalMove); i++) {
                pathToMarket.add(TurnAction.MOVE_DOWN);
            }
        }else {
            for(int i = 0; i < Math.abs(verticalMove); i++) {
                pathToMarket.add(TurnAction.MOVE_UP);
            }
        }
        return pathToMarket;
    }

    public Point findClosestMarket(Point location) {

        if(isRedPlayer) {
            if(location.x < redUpperMarketPoint.x && location.y < redUpperMarketPoint.y){
                return redLowerMarketPoint;
            }else {
                return redUpperMarketPoint;
            }
        }else {
            if(location.x <= blueUpperMarketPoint.x && location.y >= blueUpperMarketPoint.y){
                return blueUpperMarketPoint;
            }else {
                return blueLowerMarketPoint;
            }
        }
    }


}
