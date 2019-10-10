package mineopoly.strategy;

import javafx.scene.control.RadioMenuItem;
import mineopoly.game.Economy;
import mineopoly.game.TurnAction;
import mineopoly.item.InventoryItem;
import mineopoly.item.ResourceType;
import mineopoly.tiles.ResourceTile;

import mineopoly.tiles.TileType;

import java.awt.*;
import java.util.*;
import java.util.List;


public class PlayerStrategy implements MinePlayerStrategy {

    private int boardSize;
    private int halfBoardSize;
    private int maxInventorySize;
    private int winningScore;
    private int currentScore;
    private Point startTileLocation;
    private Point currentPlayerLocation;
    private Point otherPlayerLocation;
    private Point lastPoint;
    private TurnAction lastMove;
    private boolean isRedPlayer;
    private boolean goingToMarket;
    private Random random;
    private List<TurnAction> allPossibleActions;
    private List<InventoryItem> inventoryItemList;
    private List<TurnAction> pathToMarket;
    private List<TurnAction> movementActions;
    private List<Point> itemsOnGroundPoints;
    private List<Point> adjacentPointList;
    private List<TileType> adjacentTileTypeList;
    private Map<TurnAction, TileType> actionAtTiles = new HashMap<>();
    private Map<InventoryItem, Point> itemsOnGround;
    private Map<Point, TurnAction> actionAtPoint = new HashMap<>();
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
        halfBoardSize = boardSize / 2;
        currentPlayerLocation = startTileLocation;
        actionAtPoint = new HashMap<>();
        inventoryItemList = new ArrayList<>(maxInventorySize);
        movementActions = new ArrayList<>(Arrays.asList(TurnAction.MOVE_UP, TurnAction.MOVE_DOWN, TurnAction.MOVE_LEFT, TurnAction.MOVE_RIGHT));

        goingToMarket = false;
        currentScore = 0;
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

        currentPlayerLocation = boardView.getYourLocation();
        otherPlayerLocation = boardView.getOtherPlayerLocation();
        adjacentPointList = Utility.getAdjacentPoints(currentPlayerLocation);
        adjacentTileTypeList = Utility.getAdjacentTileTypes(boardView, currentPlayerLocation);
        TileType selfTileType = boardView.getTileTypeAtLocation(currentPlayerLocation);
        Point unavailableAdjacentTilePoint = Utility.getUnavailableAdjacentTile(adjacentPointList, otherPlayerLocation);
        Point toGo = null;
        TurnAction action = null;
        itemsOnGround = boardView.getItemsOnGround();
        itemsOnGroundPoints = new ArrayList<>(itemsOnGround.values());

        for(int i = 0; i < ADJACENT_TILES_AMOUNT; i++) {
            actionAtPoint.put(adjacentPointList.get(i), movementActions.get(i));
            actionAtTiles.put(movementActions.get(i), adjacentTileTypeList.get(i));
        }


        if(inventoryItemList.size() == maxInventorySize){
            if(!goingToMarket){
                Point closestMarket = Utility.findClosestMarket(currentPlayerLocation, isRedPlayer, halfBoardSize);
                pathToMarket = Utility.getPathToDestination(currentPlayerLocation, closestMarket);
                goingToMarket = true;
            }
            if(!pathToMarket.isEmpty()) {
                action = pathToMarket.get(NEXT_MOVE_TO_MARKET);
                pathToMarket.remove(NEXT_MOVE_TO_MARKET);
            }
        }else if(Utility.canPick(currentPlayerLocation, itemsOnGround)){
            action = TurnAction.PICK_UP;
        }else if(Utility.canMine(selfTileType)){
            action = TurnAction.MINE;
//        }else if(hasAdjacentItem(adjacentPointList, itemsOnGroundPoints)) {
//            action = moveToItem(actionAtPoint, itemsOnGroundPoints);
//        }else if(hasAdjacentResource(adjacentTileTypeList)){
//            action = moveToResource(actionAtTiles);
        } else {
            int randomActionIndex = random.nextInt(movementActions.size());
            action = allPossibleActions.get(randomActionIndex);
        }

        for(Point point : actionAtPoint.keySet()){
            assert toGo != null;
            if(toGo.equals(point)){
                action = actionAtPoint.get(point);
            }
        }

        endTurn(action, currentPlayerLocation);
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

    private void endTurn(TurnAction action, Point point){

        actionAtPoint.clear();
        actionAtTiles.clear();
        lastMove = action;
        lastPoint = point;
    }

}
