package mineopoly.game;

import mineopoly.item.ResourceType;
import mineopoly.tiles.EmptyTile;
import mineopoly.tiles.MarketTile;
import mineopoly.tiles.ResourceTile;
import mineopoly.tiles.Tile;
import mineopoly.tiles.TileType;

import java.awt.Point;
import java.util.Random;

/**
 * A class to generate a random GameBoard based on an initial seed value. Giving a random number generator
 *  a seed value means that it will always generate the same "random" sequence whenever it is provided
 *  with the same seed value later. This allows us to generate an unpredictable GameBoard, but also
 *  generate that same exact GameBoard if necessary for replaying a match / reproducing bugs / other purposes
 */
public class WorldGenerator {
    private static final float RANDOM_RESOURCE_CHANCE = 0.2f;
    private static final int MAX_EMPTY_TILE_SEARCHES = 50;
    private final Random randomGenerator;

    public WorldGenerator(long rngSeedValue) {
        this.randomGenerator = new Random(rngSeedValue);
    }

    /**
     * Generates a GameBoard by filling it with empty tiles, adding market tiles, and then generating the resources
     *
     * @param boardSize The size of the board to generate
     * @return A GameBoard object ready for use in a round of Mine-opoly
     */
    protected GameBoard generateBoard(int boardSize) {
        // Fill board with empty tiles to begin with
        Tile[][] tilesOnBoard = new Tile[boardSize][boardSize];
        for (int i = 0; i < tilesOnBoard.length; i++) {
            for (int j = 0; j < tilesOnBoard[i].length; j++) {
                // The top left corner is index (0, 0) but location (0, maxY)
                // This is so MOVE_UP actually moves up relative to the bottom of the screen
                Point tileLocation = new Point(j, (boardSize - 1) - i);
                tilesOnBoard[i][j] = new EmptyTile(tileLocation);
            }
        }

        GameBoard board = new GameBoard(tilesOnBoard);
        this.addMarketTiles(board);
        this.generateResources(board);
        return board;
    }

    private void addMarketTiles(GameBoard board) {
        int halfBoardSize = board.getSize() / 2;
        Point redLowerMarketPoint = new Point(halfBoardSize - 1, halfBoardSize - 1);
        Point redUpperMarketPoint = new Point(halfBoardSize, halfBoardSize);
        Point blueLowerMarketPoint = new Point(halfBoardSize, halfBoardSize - 1);
        Point blueUpperMarketPoint = new Point(halfBoardSize - 1, halfBoardSize);

        board.setTileAtTileLocation(new MarketTile(redLowerMarketPoint, true));
        board.setTileAtTileLocation(new MarketTile(redUpperMarketPoint, true));
        board.setTileAtTileLocation(new MarketTile(blueLowerMarketPoint, false));
        board.setTileAtTileLocation(new MarketTile(blueUpperMarketPoint, false));

        // Set the start points for both players to be their lower market
        board.setRedStartLocation(redLowerMarketPoint);
        board.setBlueStartLocation(blueLowerMarketPoint);
    }

    private void generateResources(GameBoard board) {
        assert board.getSize() >= 10;
        final int numTilesOnBoard = board.getSize() * board.getSize();
        final int halfBoardSize = board.getSize() / 2;
        final ResourceType[] resourceTypes = ResourceType.values();

        // Spawn rings of resources centered on the markets
        for (ResourceType currentResourceType : resourceTypes) {
            int numResourceTilesToSpawn = (int) (numTilesOnBoard * currentResourceType.getSpawnCountRatio());
            double minRadius = halfBoardSize * currentResourceType.getMinSpawnDistanceRatio();
            double maxRadius = halfBoardSize * currentResourceType.getMaxSpawnDistanceRatio();

            for (int i = 0; i < numResourceTilesToSpawn; i++) {
                // Get a random empty tile location, unless we can't find one
                int randomX;
                int randomY;
                boolean tileEmpty;
                int numAttempts = 0;
                do {
                    double randomAngle = randomGenerator.nextDouble() * (2 * Math.PI);
                    double randomRadius = randomGenerator.nextDouble() * (maxRadius - minRadius) + minRadius;
                    randomX = (int) (randomRadius * Math.cos(randomAngle)) + halfBoardSize;
                    randomY = (int) (randomRadius * Math.sin(randomAngle)) + halfBoardSize;

                    numAttempts++;
                    tileEmpty = (board.getTileAtLocation(randomX, randomY).getType() == TileType.EMPTY);
                } while (!tileEmpty && (numAttempts <= MAX_EMPTY_TILE_SEARCHES));

                if (!tileEmpty) {
                    // Could not find an empty tile in a lot of random attempts, skip this iteration
                    continue;
                }

                // Rarely spawn a resource of a different type
                ResourceType typeToSpawn = currentResourceType;
                if (randomGenerator.nextFloat() <= RANDOM_RESOURCE_CHANCE) {
                    int randomIndex = randomGenerator.nextInt(resourceTypes.length);
                    typeToSpawn = resourceTypes[randomIndex];
                }

                // Plop this resource down at the random empty tile
                Point randomLocation = new Point(randomX, randomY);
                Tile resourceTile = new ResourceTile(randomLocation, typeToSpawn);
                board.setTileAtTileLocation(resourceTile);
            }
        }
    }
}
