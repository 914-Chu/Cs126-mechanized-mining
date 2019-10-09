package mineopoly.strategy;

import mineopoly.game.TurnAction;
import mineopoly.item.InventoryItem;
import mineopoly.item.ResourceType;
import mineopoly.tiles.Tile;
import mineopoly.tiles.TileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.awt.*;
import java.util.List;

import static org.junit.Assert.*;

class PlayerStrategyTest {

    private PlayerStrategy playerStrategy;

    @BeforeEach
    void setUp() {
        playerStrategy = new PlayerStrategy();
        playerStrategy.initialize(20, 5, 12000, new Point(0,0), true, new Random());
    }

    @Test
    void testGetAdjacentTileTypes() {

    }

    @Test
    void testGetAdjacentPoints() {

        Point point = new Point(3,5);
        List<Point> adjacentPointList = playerStrategy.getAdjacentPoints(point);
        List<Point> expectPointList = new ArrayList<>(Arrays.asList(new Point(4,5), new Point(2,5), new Point(3,4), new Point(3,6)));
        assertEquals(expectPointList, adjacentPointList);
    }

    @Test
    void testGetPathToDestination() {

        Point start = new Point(4,7);
        Point destination = new Point(2,6);
        List<TurnAction> pathToDestination = playerStrategy.getPathToDestination(start, destination);
        List<TurnAction> expectPath = new ArrayList<>(Arrays.asList(TurnAction.MOVE_LEFT, TurnAction.MOVE_LEFT, TurnAction.MOVE_DOWN));
        assertEquals(expectPath, pathToDestination);
    }

    @Test
    void testFindClosestMarket() {

        Point closerToRedUpper = new Point(11,11);
        Point closerToRedLower = new Point(9,7);
        assertEquals(new Point(10,10), playerStrategy.findClosestMarket(closerToRedUpper));
        assertEquals(new Point(9,9), playerStrategy.findClosestMarket(closerToRedLower));
    }

    @Test
    void testCanPick() {

        Map<InventoryItem, Point> itemOnGround = new HashMap<>();
        itemOnGround.put(new InventoryItem(ResourceType.DIAMOND), new Point(1,3));
        itemOnGround.put(new InventoryItem(ResourceType.EMERALD), new Point(3,5));
        Point canPick = new Point(1,3);
        Point cannotPick = new Point(2,4);
        assertTrue(playerStrategy.canPick(canPick, itemOnGround));
        assertFalse(playerStrategy.canPick(cannotPick, itemOnGround));
    }

    @Test
    void testCanMine() {

        TileType canMine = TileType.RESOURCE_RUBY;
        TileType cannotMine = TileType.MARKET;
        assertTrue(playerStrategy.canMine(canMine));
        assertFalse(playerStrategy.canMine(cannotMine));
    }

    @Test
    void getUnavailableAdjacentTile() {

        Point player1 = new Point(3,3);
        Point player2 = new Point(4,1);
        Point other = new Point(3,4);
        List<Point> hasUnavailableTile = playerStrategy.getAdjacentPoints(player1);
        List<Point> noUnavailableTile = playerStrategy.getAdjacentPoints(player2);
        assertEquals(other, playerStrategy.getUnavailableAdjacentTile(hasUnavailableTile,other));
        assertNull(playerStrategy.getUnavailableAdjacentTile(noUnavailableTile, other));
    }

    @Test
    void testPointsSortByDistance() {

        Point start = new Point(0,0);
        List<Point> pointList = new ArrayList<>(Arrays.asList(new Point(1,3),
                new Point(5,1), new Point(3,2), new Point(2,3)));
        List<Point> sorted = new ArrayList<>(Arrays.asList(new Point(1,3),
                new Point(3,2), new Point(2,3), new Point(5,1)));
        assertEquals(sorted, playerStrategy.pointsSortByDistance(pointList,start));
    }
}