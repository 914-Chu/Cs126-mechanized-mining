package mineopoly.strategy;

import mineopoly.game.TurnAction;
import mineopoly.item.InventoryItem;
import mineopoly.tiles.TileType;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Utility {

    private static final int ADJACENT_TILES_AMOUNT = 4;
    private static List<TileType> resourceTileList = new ArrayList<>(Arrays.asList(TileType.RESOURCE_DIAMOND,
            TileType.RESOURCE_EMERALD,TileType.RESOURCE_RUBY));
    private static Point redLowerMarketPoint;
    private static Point redUpperMarketPoint;
    private static Point blueLowerMarketPoint;
    private static Point blueUpperMarketPoint;


    public static List<TileType> getAdjacentTileTypes(PlayerBoardView boardView, Point point) {

        List<TileType> adjacentTilesTypeList = new ArrayList<>(ADJACENT_TILES_AMOUNT);
        List<Point> adjacentPointList = getAdjacentPoints(point);

        for(int i = 0; i < ADJACENT_TILES_AMOUNT; i++) {
            adjacentTilesTypeList.add(boardView.getTileTypeAtLocation(adjacentPointList.get(i)));
        }

        return adjacentTilesTypeList;
    }

    public static List<Point> getAdjacentPoints(Point point) {

        List<Point> adjacentPointList = new ArrayList<>(ADJACENT_TILES_AMOUNT);
        adjacentPointList.add(new Point(point.x + 1, point.y)); //UP
        adjacentPointList.add(new Point(point.x - 1, point.y)); //DOWN
        adjacentPointList.add(new Point(point.x,point.y - 1));  //LEFT
        adjacentPointList.add(new Point(point.x, point.y + 1)); //RIGHT

        return adjacentPointList;
    }

    public static List<TurnAction> getPathToDestination(Point start, Point destination) {

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

    public static Point findClosestMarket(Point point, Boolean isRedPlayer, int halfBoardSize) {

        setMarketLocation(halfBoardSize);

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

    public static boolean canPick(Point location, Map<InventoryItem, Point> itemOnGround) {
        return itemOnGround.containsValue(location);
    }

    public static boolean canMine(TileType tileType) {
        return resourceTileList.contains(tileType);
    }

    public static Point getUnavailableAdjacentTile (List<Point> adjacentPointList, Point otherPlayerLocation) {

        if(adjacentPointList.contains(otherPlayerLocation)){
            return otherPlayerLocation;
        }
        return null;
    }

    public static List<Point> pointsSortByDistance(List<Point> pointList, Point start) {

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

    public static boolean hasAdjacentItem(List<Point> adjacentPointList, List<Point> itemsOnGroundPoints){

        if(itemsOnGroundPoints != null && !itemsOnGroundPoints.isEmpty()) {
            for (Point point : adjacentPointList) {
                if (itemsOnGroundPoints.contains(point)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static TurnAction moveToItem(Map<Point, TurnAction> actionAtPoint, List<Point> itemsOnGroundPoints){

        List<Point> points = new ArrayList<>(actionAtPoint.keySet());
        Collections.shuffle(points);
        for(Point itemPoint : itemsOnGroundPoints){
            for(Point adjacentPoint : points){
                if(itemPoint.equals(adjacentPoint)){
                    return actionAtPoint.get(adjacentPoint);
                }
            }
        }
        return null; // should never happen
    }

    public static boolean hasAdjacentResource(List<TileType> tileTypes){
        return tileTypes.contains(TileType.RESOURCE_DIAMOND) || tileTypes.contains(TileType.RESOURCE_EMERALD)
                ||tileTypes.contains(TileType.RESOURCE_RUBY);
    }

    public static TurnAction moveToResource(Map<TurnAction, TileType> adjacentTiles) {

        List<TileType> types = new ArrayList<>(adjacentTiles.values());
        Collections.shuffle(types);
        List<TileType> tileTypes = new ArrayList<>(Arrays.asList(TileType.RESOURCE_DIAMOND,
                TileType.RESOURCE_EMERALD, TileType.RESOURCE_RUBY));

        for(TurnAction action : adjacentTiles.keySet()){
            if(tileTypes.contains(adjacentTiles.get(action))){
                return action;
            }
        }
        return null; // should never happen
    }

    private static void setMarketLocation(int halfBoardSize){

        redLowerMarketPoint = new Point(halfBoardSize - 1, halfBoardSize - 1);
        redUpperMarketPoint = new Point(halfBoardSize, halfBoardSize);
        blueLowerMarketPoint = new Point(halfBoardSize, halfBoardSize - 1);
        blueUpperMarketPoint = new Point(halfBoardSize - 1, halfBoardSize);
    }
}
