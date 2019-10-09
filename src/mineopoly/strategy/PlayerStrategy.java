package mineopoly.strategy;

import mineopoly.game.Economy;
import mineopoly.game.TurnAction;
import mineopoly.item.InventoryItem;
import mineopoly.tiles.Tile;
import mineopoly.tiles.TileType;

import java.awt.*;
import java.awt.image.AffineTransformOp;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlayerStrategy implements MinePlayerStrategy {

    private int boardSize;
    private int halfBoardSize;
    private int maxInventorySize;
    private int winningScore;
    private int currentScore;
    private Point startTileLocation;
    private Point currentTileLocation;
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
    private List<TurnAction> movementActions;
    private final int ADJACENT_TILES_AMOUNT = 4;
    private final int NEXT_MOVE_TO_MARKET = 0;
    private final int UP_TILE = 0;
    private final int DOWN_TILE = 1;
    private final int LEFT_TILE = 2;
    private final int RIGHT_TILE = 3;

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
        currentTileLocation = startTileLocation;
        inventoryItemList = new ArrayList<>(maxInventorySize);
        movementActions = new ArrayList<>(Arrays.asList(TurnAction.MOVE_UP, TurnAction.MOVE_DOWN, TurnAction.MOVE_LEFT, TurnAction.MOVE_RIGHT));
        goingToMarket = false;
        currentScore = 0;
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
        Point otherPlayerLocation = boardView.getOtherPlayerLocation();
        List<Point> adjacentPointList = getAdjacentPoints(selfLocation);
        List<TileType> adjacentTileTypeList = getAdjacentTileTypes(boardView, selfLocation);
        TileType selfTileType = boardView.getTileTypeAtLocation(selfLocation);
        Point unavailableAdjacentTilePoint = getUnavailableAdjacentTile(adjacentPointList, otherPlayerLocation);
        TurnAction action = null;
        Map<InventoryItem, Point> itemsOnGround = boardView.getItemsOnGround();
        Map<Point, TurnAction> actionAtPoint = new HashMap<>();
        actionAtPoint.put(adjacentPointList.get(UP_TILE), movementActions.get(UP_TILE));
        actionAtPoint.put(adjacentPointList.get(DOWN_TILE), movementActions.get(DOWN_TILE));
        actionAtPoint.put(adjacentPointList.get(LEFT_TILE), movementActions.get(LEFT_TILE));
        actionAtPoint.put(adjacentPointList.get(RIGHT_TILE), movementActions.get(RIGHT_TILE));

        if(inventoryItemList.size() == maxInventorySize){
            if(!goingToMarket){
                Point closestMarket = findClosestMarket(selfLocation);
                pathToMarket = getPathToDestination(selfLocation, closestMarket);
                goingToMarket = true;
            }
            if(!pathToMarket.isEmpty()) {
                action = pathToMarket.get(NEXT_MOVE_TO_MARKET);
                pathToMarket.remove(NEXT_MOVE_TO_MARKET);
            }
        }else if(canPick(selfLocation, itemsOnGround)){
            action = TurnAction.PICK_UP;
        }else if(canMine(selfTileType)){
            action = TurnAction.MINE;
        }else {

            int randomActionIndex = random.nextInt(allPossibleActions.size() - 2);
            action = allPossibleActions.get(randomActionIndex);
        }

        return action;
    }

    /**
     * Called when the player receives an item from performing a TurnAction that gives an item.
     * At the moment this is only from using PICK_UP on top of a mined resource
     *
     * @param itemReceived The item received from the player's TurnAction on their last turn
     */
    @Override
    public void onReceiveItem(InventoryItem itemReceived) {
        inventoryItemList.add(itemReceived);
    }

    /**
     * Called when the player steps on a market tile with items to sell. Tells your strategy how much all
     * of the items sold for.
     *
     * @param totalSellPrice The combined sell price for all items in your strategy's inventory
     */
    @Override
    public void onSoldInventory(int totalSellPrice) {

        inventoryItemList.clear();
        goingToMarket = false;
        currentScore += totalSellPrice;
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

    public List<TileType> getAdjacentTileTypes(PlayerBoardView boardView, Point point) {

        List<TileType> adjacentTilesTypeList = new ArrayList<>(ADJACENT_TILES_AMOUNT);
        List<Point> adjacentPointList = getAdjacentPoints(point);

        for(int i = 0; i < ADJACENT_TILES_AMOUNT; i++) {
            adjacentTilesTypeList.add(boardView.getTileTypeAtLocation(adjacentPointList.get(i)));
        }

        return adjacentTilesTypeList;
    }

    public List<Point> getAdjacentPoints(Point point) {

        List<Point> adjacentPointList = new ArrayList<>(ADJACENT_TILES_AMOUNT);
        adjacentPointList.add(new Point(point.x + 1, point.y)); //UP
        adjacentPointList.add(new Point(point.x - 1, point.y)); //DOWN
        adjacentPointList.add(new Point(point.x,point.y - 1));  //LEFT
        adjacentPointList.add(new Point(point.x, point.y + 1)); //RIGHT

        return adjacentPointList;
    }

    public List<TurnAction> getPathToDestination(Point start, Point destination) {

        List<TurnAction> pathToDestination = new ArrayList<>();
        int horizontalMove = destination.x - start.x;
        int verticalMove = destination.y - start.y;
        if(horizontalMove < 0) {
            for(int i = 0; i < Math.abs(horizontalMove); i++) {
                pathToDestination.add(TurnAction.MOVE_LEFT);
            }
        }else {
            for(int i = 0; i < Math.abs(horizontalMove); i++) {
                pathToDestination.add(TurnAction.MOVE_RIGHT);
            }
        }

        if(verticalMove < 0) {
            for(int i = 0; i < Math.abs(verticalMove); i++) {
                pathToDestination.add(TurnAction.MOVE_DOWN);
            }
        }else {
            for(int i = 0; i < Math.abs(verticalMove); i++) {
                pathToDestination.add(TurnAction.MOVE_UP);
            }
        }
        return pathToDestination;
    }

    public Point findClosestMarket(Point point) {

        if(isRedPlayer) {
            if(point.x < redUpperMarketPoint.x && point.y < redUpperMarketPoint.y){
                return redLowerMarketPoint;
            }else {
                return redUpperMarketPoint;
            }
        }else {
            if(point.x <= blueUpperMarketPoint.x && point.y >= blueUpperMarketPoint.y){
                return blueUpperMarketPoint;
            }else {
                return blueLowerMarketPoint;
            }
        }
    }

    public boolean canPick(Point location, Map<InventoryItem, Point> itemOnGround) {

        return itemOnGround.containsValue(location);
    }

    public boolean canMine(TileType tileType) {
        return Arrays.asList(TileType.RESOURCE_DIAMOND, TileType.RESOURCE_EMERALD, TileType.RESOURCE_RUBY).contains(tileType);
    }

    public Point getUnavailableAdjacentTile (List<Point> adjacentPointList, Point otherPlayerLocation) {

        if(adjacentPointList.contains(otherPlayerLocation)){
            return otherPlayerLocation;
        }
        return null;
    }

    public List<Point> pointsSortByDistance(List<Point> pointList, Point start) {

        Map<Integer, List<Point>> distanceOfPoint = new TreeMap<>();
        List<Point> sortedPointList = new ArrayList<>();

        for(Point destination : pointList){
            int length = getPathToDestination(start, destination).size();
            if(distanceOfPoint.containsKey(length)){
                distanceOfPoint.get(length).add(destination);
            }else {
                distanceOfPoint.put(length, new ArrayList<>(Arrays.asList(destination)));
            }
        }

        for(Map.Entry<Integer, List<Point>> entry : distanceOfPoint.entrySet()){
            sortedPointList.addAll(entry.getValue());
        }
        return sortedPointList;
    }

}
