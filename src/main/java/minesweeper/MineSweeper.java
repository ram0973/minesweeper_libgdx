package minesweeper;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Random;


public class MineSweeper extends ApplicationAdapter {
    public static final int WIDTH = 860;
    public static final int HEIGHT = 650;
    private static final Random RANDOM = new Random();

    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private final int width = 40;
    private final int height = 30;
    private final boolean[][] map = new boolean[width][height];
    private final boolean[][] clicked = new boolean[width][height];
    private final boolean[][] flagged = new boolean[width][height];
    private final int[][] surroundingMines = new int[width][height];
    private final int numMines = 125;
    private final int tileDim = 20;
    private long mouseLastClicked;
    private boolean gameOver;
    private long gameOverTime;

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Mine Sweeper");
        config.setWindowedMode(WIDTH, HEIGHT);
        config.setResizable(false);
        config.setHdpiMode(HdpiMode.Logical);
        new Lwjgl3Application(new MineSweeper(), config);
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont(Gdx.files.internal("Consolas.fnt"));
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        newGameMap();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (button == 0) {
                    handlePrimaryClick(screenX, HEIGHT - screenY);
                } else if (button == 1) {
                    handleSecondaryClick(screenX, HEIGHT - screenY);
                }
                return true;
            }
        });
    }

    @Override
    public void render() {
        if (!gameOver) {
            handleKeyboard();
            draw();
            if (isWin() || isLose()) {
                gameOver = true;
                gameOverTime = TimeUtils.millis();
            }
        } else {
            drawEndScreen();
            if (TimeUtils.timeSinceMillis(gameOverTime) > 2000) {
                create();
            }
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }

    public void handlePrimaryClick(final int mx, final int my) {
        if (gameOver || TimeUtils.timeSinceMillis(mouseLastClicked) < 150) {
            return;
        }
        mouseLastClicked = TimeUtils.millis();

        final int x = (int) ((mx - 10) / (double) (tileDim + 1));
        final int y = (int) ((my - 10) / (double) (tileDim + 1));
        if (x >= 0 && x < width && y >= 0 && y < height) {
            click(x, y, 0);
        }
    }

    public void handleSecondaryClick(final int mx, final int my) {
        if (gameOver || TimeUtils.timeSinceMillis(mouseLastClicked) < 150) {
            return;
        }
        mouseLastClicked = TimeUtils.millis();

        final int x = (int) ((mx - 10) / (double) (tileDim + 1));
        final int y = (int) ((my - 10) / (double) (tileDim + 1));
        if (x >= 0 && x < width && y >= 0 && y < height) {
            flagged[x][y] = !flagged[x][y];
        }
    }

    public void handleKeyboard() {
        if (Gdx.input.isKeyPressed(Input.Keys.H)) {
            help();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.C)) {
            clickAll();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.R)) {
            newGameMap();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            //System.exit(0);
            Gdx.app.exit();
        }
    }

    private void click(int x, int y, int depth) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return;
        }
        if (clicked[x][y]) {
            return;
        }
        clicked[x][y] = true;
        if (map[x][y]) {
            return;
        }
        if (surroundingMines[x][y] > 0) {
            return;
        }

        click(x - 1, y - 1, depth + 1);
        click(x, y - 1, depth + 1);
        click(x + 1, y - 1, depth + 1);
        click(x - 1, y, depth + 1);
        click(x + 1, y, depth + 1);
        click(x - 1, y + 1, depth + 1);
        click(x, y + 1, depth + 1);
        click(x + 1, y + 1, depth + 1);
    }

    private void help() {
        if (!isWin() && !isLose()) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (!map[x][y] && !clicked[x][y]) {
                        flagged[x][y] = false;
                        click(x, y, 0);
                        return;
                    }
                }
            }
        }
    }

    private void drawRect(final Color color, final int x, final int y) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x, y, tileDim, tileDim);
        shapeRenderer.end();
    }

    public void draw() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (clicked[x][y]) {
                    if (map[x][y]) { // if is mine
                        drawRect(Color.RED,
                                x * (tileDim + 1) + 10,
                                y * (tileDim + 1) + 10);
                    } else { // draw empty square;
                        drawRect(Color.WHITE,
                                x * (tileDim + 1) + 10,
                                y * (tileDim + 1) + 10);

                        if (surroundingMines[x][y] > 0) {
                            batch.begin();
                            font.getData().setScale(1.2f, 1.2f);
                            font.setColor(getMineCountColor(surroundingMines[x][y]));
                            font.draw(batch,
                                    String.valueOf(surroundingMines[x][y]),
                                    x * (tileDim + 1) + 15,
                                    y * (tileDim + 1) + 27);
                            batch.end();
                        }
                    }
                } else {
                    drawRect(Color.GRAY,
                            x * (tileDim + 1) + 10,
                            y * (tileDim + 1) + 10);

                    if (flagged[x][y]) {
                        drawRect(Color.GREEN,
                                x * (tileDim + 1) + 10,
                                y * (tileDim + 1) + 10);
                    }
                }

            }
        }
    }

    private void drawEndScreen() {
        draw();
        batch.begin();
        font.setColor(Color.RED);

        if (isWin()) {
            font.getData().setScale(1.0f, 1.0f);
            font.draw(batch, "You Win", 20, 120);
        } else if (isLose()) {
            font.getData().setScale(1.0f, 1.0f);
            font.draw(batch, "You Lose", 20, 120);
        }
        batch.end();
    }

    private Color getMineCountColor(int num) {
        return switch (num) {
            case 1 -> Color.BLUE;
            case 2 -> Color.FOREST;
            case 3 -> Color.RED;
            case 4 -> Color.NAVY;
            case 5 -> Color.MAGENTA;
            case 6 -> Color.CORAL;
            default -> Color.BLACK;
        };
    }


    private void newGameMap() {
        gameOver = false;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                clicked[x][y] = false;
                flagged[x][y] = false;
                map[x][y] = false;
            }
        }

        int minesPlaced = 0;
        while (minesPlaced < numMines) {
            int x = RANDOM.nextInt(width);
            int y = RANDOM.nextInt(height);
            if (!map[x][y]) {
                map[x][y] = true; // place mine
                minesPlaced++;
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                surroundingMines[x][y] = adjacentMines(x, y);
            }
        }
    }

    private int adjacentMines(int x, int y) {
        int total = 0;
        if (isMine(x - 1, y - 1)) {
            total++;
        }
        if (isMine(x, y - 1)) {
            total++;
        }
        if (isMine(x + 1, y - 1)) {
            total++;
        }
        if (isMine(x - 1, y)) {
            total++;
        }
        if (isMine(x + 1, y)) {
            total++;
        }
        if (isMine(x - 1, y + 1)) {
            total++;
        }
        if (isMine(x, y + 1)) {
            total++;
        }
        if (isMine(x + 1, y + 1)) {
            total++;
        }
        return total;
    }

    private boolean isMine(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return map[x][y];
        }
        return false;
    }

    private void clickAll() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                clicked[x][y] = true;
            }
        }
    }

    private boolean isWin() {
        boolean win = true;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                win &= ((clicked[x][y] && !map[x][y]) || (!clicked[x][y] && map[x][y]));
            }
        }
        return win;
    }

    private boolean isLose() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (clicked[x][y] && map[x][y]) {
                    return true;
                }
            }
        }
        return false;
    }
}
