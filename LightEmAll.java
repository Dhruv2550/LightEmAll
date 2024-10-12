import javalib.impworld.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import javalib.worldimages.*;
import tester.Tester;
import java.awt.Color;

// represents a GamePiece 
class GamePiece {
  int row;
  int col;
  boolean left;
  boolean right;
  boolean top;
  boolean bottom;
  boolean powerStation;
  boolean powered;

  // the constructor
  GamePiece(int row, int col) {
    this.row = row;
    this.col = col;
    this.left = false;
    this.right = false;
    this.top = false;
    this.bottom = false;
    this.powerStation = false;
    this.powered = false;
  }
  
  // the constructor for testing
  GamePiece(int row, int col, boolean left, boolean right, boolean top, boolean bottom,
      boolean isPowered) {
    this.row = row;
    this.col = col;
    this.left = left;
    this.right = right;
    this.top = top;
    this.bottom = bottom;
    this.powerStation = false;
    this.powered = isPowered;
  }

  // the constructor for testing
  GamePiece(int row, int col, boolean left, boolean right, boolean top, boolean bottom) {
    this.row = row;
    this.col = col;
    this.left = left;
    this.right = right;
    this.top = top;
    this.bottom = bottom;
    this.powerStation = false;
    this.powered = false;
  }

  // EFFECT: rotates this GamePiece clockwise by 45 degrees
  void rotate() {
    boolean temp = this.top;
    this.top = this.left;
    this.left = this.bottom;
    this.bottom = this.right;
    this.right = temp;
  }

  // draws this cell
  WorldImage draw() {
    int size = 39;
    WorldImage base = new RectangleImage(size, size, OutlineMode.SOLID, Color.DARK_GRAY);
    Color lineColor = this.powered ? Color.YELLOW : Color.LIGHT_GRAY;
    WorldImage upBorder = this.top ? new LineImage(new Posn(0, -size), lineColor)
        : new EmptyImage();
    WorldImage downBorder = this.bottom ? new LineImage(new Posn(0, size), lineColor)
        : new EmptyImage();
    WorldImage leftBorder = this.left ? new LineImage(new Posn(-size, 0), lineColor)
        : new EmptyImage();
    WorldImage rightBorder = this.right ? new LineImage(new Posn(size, 0), lineColor)
        : new EmptyImage();
    WorldImage pieceImage = new OverlayImage(upBorder, new OverlayImage(downBorder,
        new OverlayImage(leftBorder, new OverlayImage(rightBorder, base))));

    if (this.powerStation) {
      WorldImage star = new StarImage(size / 2.5, OutlineMode.SOLID, Color.ORANGE);
      pieceImage = new OverlayImage(star, pieceImage);
    }

    WorldImage border = new RectangleImage(size, size, OutlineMode.OUTLINE, Color.BLACK);
    pieceImage = new OverlayImage(border, pieceImage);
    return pieceImage;
  }
}

// represents the base of the game
class Board {
  int rows;
  int cols;
  int powerRow;
  int powerCol;
  ArrayList<ArrayList<GamePiece>> board;

  // the constructor
  Board(int rows, int cols) {
    this.rows = rows;
    this.cols = cols;
    this.powerRow = rows / 2;
    this.powerCol = cols / 2;
    this.board = new ArrayList<>();

    for (int row = 0; row < this.rows; row++) {
      ArrayList<GamePiece> current = new ArrayList<>();

      for (int col = 0; col < this.cols; col++) {
        GamePiece piece = new GamePiece(row, col);

        if (row == this.powerRow) {
          piece.left = piece.right = piece.top = piece.bottom = true;
        }
        else {
          piece.left = piece.right = true;
        }
        current.add(piece);
      }
      this.board.add(current);
    }

    GamePiece powerStation = this.board.get(this.powerRow).get(this.powerCol);
    powerStation.powerStation = true;
  }

  // the constructor for testing
  Board(int rows, int cols, ArrayList<ArrayList<GamePiece>> board) {
    this.rows = rows;
    this.cols = cols;
    this.powerRow = 0;
    this.powerCol = 0;
    this.board = board;
  }

  // EFFECT: rotates this GamePiece a random amount
  void randomRotate(Random seed) {
    for (ArrayList<GamePiece> row : this.board) {
      for (GamePiece piece : row) {
        int rotations = seed.nextInt(4);

        for (int i = 0; i < rotations; i++) {
          piece.rotate();
        }
      }
    }
  }
}

// represents the LightEmAll Game that draws the scene
class LightEmAll extends World {
  int width;
  int height;
  int powerRow;
  int powerCol;
  Random random;
  boolean won;
  Board board;

  // the constructor
  LightEmAll(int width, int height) {
    this.width = width;
    this.height = height;
    this.powerRow = height / 2;
    this.powerCol = width / 2;
    this.random = new Random();
    this.won = false;
    this.board = new Board(this.height, this.width);
    this.board.randomRotate(this.random);
  }
  
  // the constructor for a blank board
  LightEmAll(int width, int height, Board board) {
    this.width = width;
    this.height = height;
    this.powerRow = 0;
    this.powerCol = 0;
    this.random = new Random();
    this.won = false;
    this.board = board;
  }

  // the constructor for Random
  LightEmAll(int width, int height, Random seed) {
    this.width = width;
    this.height = height;
    this.powerRow = height / 2;
    this.powerCol = width / 2;
    this.random = seed;
    this.won = false;
    this.board = new Board(this.height, this.width);
    this.board.randomRotate(this.random);
  }

  // EFFECT: the power station is moved by one cell based on the key pressed
  public void onKeyEvent(String key) {
    if (this.won) {
      return;
    }
    int updatedPowerRow = this.powerRow;
    int updatedPowerCol = this.powerCol;

    if (key.equals("right")) {
      updatedPowerCol++;
    }
    else if (key.equals("left")) {
      updatedPowerCol--;
    }
    else if (key.equals("up")) {
      updatedPowerRow--;
    }
    else if (key.equals("down")) {
      updatedPowerRow++;
    }

    if (this.allowedToMove(this.powerRow, this.powerCol, updatedPowerRow, updatedPowerCol)) {
      this.board.board.get(this.powerRow).get(this.powerCol).powerStation = false;
      this.powerCol = updatedPowerCol;
      this.powerRow = updatedPowerRow;
      this.board.board.get(this.powerRow).get(this.powerCol).powerStation = true;
      this.changePower();
      this.checkGameWon();
    }
  }

  // checks whether the power station is allowed to move the desired coordinates
  boolean allowedToMove(int cRow, int cCol, int desiredRow, int desiredCol) {
    if (desiredRow < 0 || desiredRow >= this.height || desiredCol < 0 || desiredCol >= this.width) {
      return false;
    }

    GamePiece cPiece = this.board.board.get(cRow).get(cCol);
    GamePiece desiredPiece = this.board.board.get(desiredRow).get(desiredCol);

    if (cRow < desiredRow) {
      return cPiece.bottom && desiredPiece.top;
    }
    else if (cRow > desiredRow) {
      return cPiece.top && desiredPiece.bottom;
    }
    else if (cCol < desiredCol) {
      return cPiece.right && desiredPiece.left;
    }
    else if (cCol > desiredCol) {
      return cPiece.left && desiredPiece.right;
    }
    return false;
  }

  // EFFECT: rotates the specific GamePiece
  public void onMouseClicked(Posn pos, String button) {
    if (this.won) {
      return;
    }
    int col = pos.x / 39;
    int row = pos.y / 39;

    if (row >= 0 && row < this.height && col >= 0 && col < this.width
        && button.equals("LeftButton")) {
      GamePiece piece = this.board.board.get(row).get(col);

      piece.rotate();

      this.checkGameWon();
      this.changePower();
    }
  }

  // draws the LightEmAll game by placing the GamePieces on the Board
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(this.width * 39, this.height * 39);

    for (int row = 0; row < this.height; row++) {
      for (int col = 0; col < this.width; col++) {

        GamePiece piece = this.board.board.get(row).get(col);
        scene.placeImageXY(piece.draw(), (col * 39) + 20, (row * 39) + 20);
      }
    }

    if (this.won) {
      WorldImage winnerMessage = new TextImage("You Win!", 39, FontStyle.BOLD, Color.GREEN);
      scene.placeImageXY(winnerMessage, (this.width * 39) / 2, (this.height * 39) / 2);
    }
    return scene;
  }

  // EFFECT: changes the power connections between GamePieces
  void changePower() {
    for (ArrayList<GamePiece> row : this.board.board) {
      for (GamePiece piece : row) {
        piece.powered = false;
      }
    }

    ArrayList<GamePiece> queue = new ArrayList<>();

    GamePiece powerStation = this.board.board.get(this.powerRow).get(this.powerCol);
    powerStation.powered = true;
    queue.add(powerStation);

    while (!queue.isEmpty()) {
      GamePiece current = queue.remove(0);

      if (current.top && current.row > 0
          && this.board.board.get(current.row - 1).get(current.col).bottom
          && !this.board.board.get(current.row - 1).get(current.col).powered) {
        queue.add(this.board.board.get(current.row - 1).get(current.col));
        this.board.board.get(current.row - 1).get(current.col).powered = true;
      }
      if (current.bottom && current.row < height - 1
          && this.board.board.get(current.row + 1).get(current.col).top
          && !this.board.board.get(current.row + 1).get(current.col).powered) {
        queue.add(this.board.board.get(current.row + 1).get(current.col));
        this.board.board.get(current.row + 1).get(current.col).powered = true;
      }
      if (current.left && current.col > 0
          && this.board.board.get(current.row).get(current.col - 1).right
          && !this.board.board.get(current.row).get(current.col - 1).powered) {
        queue.add(this.board.board.get(current.row).get(current.col - 1));
        this.board.board.get(current.row).get(current.col - 1).powered = true;
      }
      if (current.right && current.col < width - 1
          && this.board.board.get(current.row).get(current.col + 1).left
          && !this.board.board.get(current.row).get(current.col + 1).powered) {
        queue.add(this.board.board.get(current.row).get(current.col + 1));
        this.board.board.get(current.row).get(current.col + 1).powered = true;
      }
    }
  }

  // EFFECT: checks if the user has finished the game
  void checkGameWon() {
    for (ArrayList<GamePiece> row : this.board.board) {
      for (GamePiece piece : row) {
        if (!piece.powered) {
          return;
        }
      }
    }
    this.won = true;
  }
}

// examples of the LightEmAll game
class ExamplesLightEmAll {

  // the constructor
  ExamplesLightEmAll() {
  }

  // single
  GamePiece left;
  GamePiece right;
  GamePiece top;
  GamePiece bottom;

  // single w/ power
  GamePiece leftPowered;
  GamePiece rightPowered;
  GamePiece topPowered;
  GamePiece bottomPowered;

  // double
  GamePiece topBottom;
  GamePiece leftRight;
  GamePiece leftTop;
  GamePiece topRight;
  GamePiece bottomRight;
  GamePiece leftBottom;

  // double w/ power
  GamePiece topBottomPowered;
  GamePiece leftRightPowered;
  GamePiece leftTopPowered;
  GamePiece topRightPowered;
  GamePiece bottomRightPowered;
  GamePiece leftBottomPowered;

  // triple
  GamePiece topBottomRight;
  GamePiece leftBottomRight;
  GamePiece leftTopBottom;
  GamePiece leftTopRight;

  // triple w/ power
  GamePiece topBottomRightPowered;
  GamePiece leftBottomRightPowered;
  GamePiece leftTopBottomPowered;
  GamePiece leftTopRightPowered;

  // quad
  GamePiece leftTopBottomRight;
  GamePiece leftTopBottomRightStation;

  // quad w/ power
  GamePiece leftTopBottomRightPowered;
  GamePiece leftTopBottomRightStationPowered;

  int cellSize;
  WorldImage cellBase;

  Color poweredColor;
  Color unpoweredColor;

  // cells
  WorldImage emptyImage;
  WorldImage border;
  WorldImage powerStation;

  WorldImage leftBorderPowered;
  WorldImage rightBorderPowered;
  WorldImage upBorderPowered;
  WorldImage downBorderPowered;

  WorldImage leftBorderUnPowered;
  WorldImage rightBorderUnPowered;
  WorldImage upBorderUnPowered;
  WorldImage downBorderUnPowered;

  WorldImage leftCell;
  WorldImage rightCell;
  WorldImage topCell;
  WorldImage bottomCell;

  WorldImage topBottomCell;
  WorldImage leftRightCell;
  WorldImage leftTopCell;
  WorldImage topRightCell;
  WorldImage bottomRightCell;
  WorldImage leftBottomCell;

  WorldImage topBottomRightCell;
  WorldImage leftBottomRightCell;
  WorldImage leftTopBottomCell;
  WorldImage leftTopRightCell;

  WorldImage leftCellPowered;
  WorldImage rightCellPowered;
  WorldImage topCellPowered;
  WorldImage bottomCellPowered;

  WorldImage topBottomCellPowered;
  WorldImage leftRightCellPowered;
  WorldImage leftTopCellPowered;
  WorldImage topRightCellPowered;
  WorldImage bottomRightCellPowered;
  WorldImage leftBottomCellPowered;

  WorldImage topBottomRightCellPowered;
  WorldImage leftBottomRightCellPowered;
  WorldImage leftTopBottomCellPowered;
  WorldImage leftTopRightCellPowered;

  WorldImage leftTopBottomRightCellPowered;
  WorldImage leftTopBottomRightCellStationCell;
  WorldImage leftTopBottomRightCell;

  // game pieces
  GamePiece p0;
  GamePiece p1;
  GamePiece p2;
  GamePiece p3;
  GamePiece p4;
  GamePiece p5;
  GamePiece p6;
  GamePiece p7;

  GamePiece p10;
  GamePiece p11;
  GamePiece p12;
  GamePiece p13;
  GamePiece p14;
  GamePiece p15;
  GamePiece p16;
  GamePiece p17;

  GamePiece p20;
  GamePiece p21;
  GamePiece p22;
  GamePiece p23;
  GamePiece p24;
  GamePiece p25;
  GamePiece p26;
  GamePiece p27;

  GamePiece p30;
  GamePiece p31;
  GamePiece p32;
  GamePiece p33;
  GamePiece p34;
  GamePiece p35;
  GamePiece p36;
  GamePiece p37;

  GamePiece p39;
  GamePiece p41;
  GamePiece p42;
  GamePiece p43;
  GamePiece p44;
  GamePiece p45;
  GamePiece p46;
  GamePiece p47;

  GamePiece p50;
  GamePiece p51;
  GamePiece p52;
  GamePiece p53;
  GamePiece p54;
  GamePiece p55;
  GamePiece p56;
  GamePiece p57;

  GamePiece p60;
  GamePiece p61;
  GamePiece p62;
  GamePiece p63;
  GamePiece p64;
  GamePiece p65;
  GamePiece p66;
  GamePiece p67;

  GamePiece p70;
  GamePiece p71;
  GamePiece p72;
  GamePiece p73;
  GamePiece p74;
  GamePiece p75;
  GamePiece p76;
  GamePiece p77;

  GamePiece p80;
  GamePiece p81;
  GamePiece p82;
  GamePiece p83;
  GamePiece p84;
  GamePiece p85;
  GamePiece p86;
  GamePiece p87;

  ArrayList<GamePiece> row0Initial;
  ArrayList<GamePiece> row1Initial;
  ArrayList<GamePiece> row2Initial;
  ArrayList<GamePiece> row3Initial;
  ArrayList<GamePiece> row4Initial;
  ArrayList<GamePiece> row5Initial;
  ArrayList<GamePiece> row6Initial;
  ArrayList<GamePiece> row7Initial;
  ArrayList<GamePiece> row8Initial;

  ArrayList<ArrayList<GamePiece>> gridInitial;

  Board boardInitial;

  LightEmAll gameInitial;

  // game piece examples w/ power
  GamePiece p0P;
  GamePiece p1P;
  GamePiece p2P;
  GamePiece p3P;
  GamePiece p4P;
  GamePiece p5P;
  GamePiece p6P;
  GamePiece p7P;

  GamePiece p10P;
  GamePiece p11P;
  GamePiece p12P;
  GamePiece p13P;
  GamePiece p14P;
  GamePiece p15P;
  GamePiece p16P;
  GamePiece p17P;

  GamePiece p20P;
  GamePiece p21P;
  GamePiece p22P;
  GamePiece p23P;
  GamePiece p24P;
  GamePiece p25P;
  GamePiece p26P;
  GamePiece p27P;

  GamePiece p30P;
  GamePiece p31P;
  GamePiece p32P;
  GamePiece p33P;
  GamePiece p34P;
  GamePiece p35P;
  GamePiece p36P;
  GamePiece p37P;

  GamePiece p39P;
  GamePiece p41P;
  GamePiece p42P;
  GamePiece p43P;
  GamePiece p44P;
  GamePiece p45P;
  GamePiece p46P;
  GamePiece p47P;

  GamePiece p50P;
  GamePiece p51P;
  GamePiece p52P;
  GamePiece p53P;
  GamePiece p54P;
  GamePiece p55P;
  GamePiece p56P;
  GamePiece p57P;

  GamePiece p60P;
  GamePiece p61P;
  GamePiece p62P;
  GamePiece p63P;
  GamePiece p64P;
  GamePiece p65P;
  GamePiece p66P;
  GamePiece p67P;

  GamePiece p70P;
  GamePiece p71P;
  GamePiece p72P;
  GamePiece p73P;
  GamePiece p74P;
  GamePiece p75P;
  GamePiece p76P;
  GamePiece p77P;

  GamePiece p80P;
  GamePiece p81P;
  GamePiece p82P;
  GamePiece p83P;
  GamePiece p84P;
  GamePiece p85P;
  GamePiece p86P;
  GamePiece p87P;

  ArrayList<GamePiece> row0Powered;
  ArrayList<GamePiece> row1Powered;
  ArrayList<GamePiece> row2Powered;
  ArrayList<GamePiece> row3Powered;
  ArrayList<GamePiece> row4Powered;
  ArrayList<GamePiece> row5Powered;
  ArrayList<GamePiece> row6Powered;
  ArrayList<GamePiece> row7Powered;
  ArrayList<GamePiece> row8Powered;

  ArrayList<ArrayList<GamePiece>> gridPowered;

  Board boardPowered;

  LightEmAll gamePowered;

  // Examples for the game scene
  WorldScene baseScene;
  WorldImage winnerMessage;

  WorldScene initialScene;
  WorldScene wonScene;

  // 1x1 Board
  GamePiece piece1x1;
  ArrayList<ArrayList<GamePiece>> grid1x1;
  ArrayList<GamePiece> row1x1;
  Board board1x1;
  GamePiece piece1x1Result;
  ArrayList<ArrayList<GamePiece>> grid1x1Result;
  ArrayList<GamePiece> row1x1Result;
  Board board1x1Result;

  // 1x1 Board w/ power
  GamePiece piece1x1Powered;
  ArrayList<ArrayList<GamePiece>> grid1x1Powered;
  ArrayList<GamePiece> row1x1Powered;
  Board board1x1Powered;
  GamePiece piece1x1ResultPowered;
  ArrayList<ArrayList<GamePiece>> grid1x1ResultPowered;
  ArrayList<GamePiece> row1x1ResultPowered;
  Board board1x1ResultPowered;

  // 2x2 Board
  GamePiece piece2x2w11;
  GamePiece piece2x2w12;
  GamePiece piece2x2w21;
  GamePiece piece2x2w22;
  ArrayList<GamePiece> row2x2w1;
  ArrayList<GamePiece> row2x2w2;
  ArrayList<ArrayList<GamePiece>> grid2x2;
  Board board2x2;

  // 2x2 Board with Random
  GamePiece piece2x2w11Rand;
  GamePiece piece2x2w12Rand;
  GamePiece piece2x2w21Rand;
  GamePiece piece2x2w22Rand;
  ArrayList<GamePiece> row2x2w1Rand;
  ArrayList<GamePiece> row2x2w2Rand;
  ArrayList<ArrayList<GamePiece>> grid2x2Rand;
  Board board2x2Rand;

  Random rand1;
  Random rand2;

  WorldScene initialScene1x1;
  WorldScene initialScene1x1Final;

  LightEmAll game1x1;
  LightEmAll game1x1P;
  LightEmAll game1x1Powered;

  // initialize game pieces
  void initCond() {

    left = new GamePiece(0, 0, true, false, false, false);
    right = new GamePiece(0, 1, false, true, false, false);
    top = new GamePiece(0, 2, false, false, true, false);
    bottom = new GamePiece(0, 3, false, false, false, true);

    leftPowered = new GamePiece(0, 0, true, false, false, false);
    rightPowered = new GamePiece(0, 1, false, true, false, false);
    topPowered = new GamePiece(0, 2, false, false, true, false);
    bottomPowered = new GamePiece(0, 3, false, false, false, true);
    leftPowered.powered = true;
    rightPowered.powered = true;
    topPowered.powered = true;
    bottomPowered.powered = true;

    topBottom = new GamePiece(1, 0, false, false, true, true);
    leftRight = new GamePiece(1, 1, true, true, false, false);
    leftTop = new GamePiece(1, 2, true, false, true, false);
    topRight = new GamePiece(1, 3, false, true, true, false);
    bottomRight = new GamePiece(2, 0, false, true, false, true);
    leftBottom = new GamePiece(2, 1, true, false, false, true);

    topBottomPowered = new GamePiece(1, 0, false, false, true, true);
    leftRightPowered = new GamePiece(1, 1, true, true, false, false);
    leftTopPowered = new GamePiece(1, 2, true, false, true, false);
    topRightPowered = new GamePiece(1, 3, false, true, true, false);
    bottomRightPowered = new GamePiece(2, 0, false, true, false, true);
    leftBottomPowered = new GamePiece(2, 1, true, false, false, true);
    topBottomPowered.powered = true;
    leftRightPowered.powered = true;
    leftTopPowered.powered = true;
    topRightPowered.powered = true;
    bottomRightPowered.powered = true;
    leftBottomPowered.powered = true;

    topBottomRight = new GamePiece(2, 2, false, true, true, true);
    leftBottomRight = new GamePiece(2, 3, true, true, false, true);
    leftTopBottom = new GamePiece(3, 0, true, false, true, true);
    leftTopRight = new GamePiece(3, 1, true, true, true, false);

    topBottomRightPowered = new GamePiece(2, 2, false, true, true, true);
    leftBottomRightPowered = new GamePiece(2, 3, true, true, false, true);
    leftTopBottomPowered = new GamePiece(3, 0, true, false, true, true);
    leftTopRightPowered = new GamePiece(3, 1, true, true, true, false);
    topBottomRightPowered.powered = true;
    leftBottomRightPowered.powered = true;
    leftTopBottomPowered.powered = true;
    leftTopRightPowered.powered = true;

    leftTopBottomRight = new GamePiece(3, 2, true, true, true, true);
    leftTopBottomRightPowered = new GamePiece(3, 2, true, true, true, true);
    leftTopBottomRightPowered.powered = true;

    // with power station
    leftTopBottomRightStation = new GamePiece(3, 3, true, true, true, true);
    leftTopBottomRightStation.powered = true;
    leftTopBottomRightStation.powerStation = true;

    cellSize = 39;

    cellBase = new RectangleImage(cellSize, cellSize, OutlineMode.SOLID, Color.DARK_GRAY);

    poweredColor = Color.YELLOW;
    unpoweredColor = Color.LIGHT_GRAY;

    leftBorderPowered = new LineImage(new Posn(-cellSize, 0), poweredColor);
    rightBorderPowered = new LineImage(new Posn(cellSize, 0), poweredColor);
    upBorderPowered = new LineImage(new Posn(0, -cellSize), poweredColor);
    downBorderPowered = new LineImage(new Posn(0, cellSize), poweredColor);

    leftBorderUnPowered = new LineImage(new Posn(-cellSize, 0), unpoweredColor);
    rightBorderUnPowered = new LineImage(new Posn(cellSize, 0), unpoweredColor);
    upBorderUnPowered = new LineImage(new Posn(0, -cellSize), unpoweredColor);
    downBorderUnPowered = new LineImage(new Posn(0, cellSize), unpoweredColor);

    emptyImage = new EmptyImage();

    leftCell = new OverlayImage(emptyImage, new OverlayImage(emptyImage,
        new OverlayImage(leftBorderUnPowered, new OverlayImage(emptyImage, cellBase))));
    rightCell = new OverlayImage(emptyImage, new OverlayImage(emptyImage,
        new OverlayImage(emptyImage, new OverlayImage(rightBorderUnPowered, cellBase))));
    topCell = new OverlayImage(upBorderUnPowered, new OverlayImage(emptyImage,
        new OverlayImage(emptyImage, new OverlayImage(emptyImage, cellBase))));
    bottomCell = new OverlayImage(emptyImage, new OverlayImage(downBorderUnPowered,
        new OverlayImage(emptyImage, new OverlayImage(emptyImage, cellBase))));

    topBottomCell = new OverlayImage(upBorderUnPowered, new OverlayImage(downBorderUnPowered,
        new OverlayImage(emptyImage, new OverlayImage(emptyImage, cellBase))));
    leftRightCell = new OverlayImage(emptyImage, new OverlayImage(emptyImage,
        new OverlayImage(leftBorderUnPowered, new OverlayImage(rightBorderUnPowered, cellBase))));
    leftTopCell = new OverlayImage(upBorderUnPowered, new OverlayImage(emptyImage,
        new OverlayImage(leftBorderUnPowered, new OverlayImage(emptyImage, cellBase))));
    topRightCell = new OverlayImage(upBorderUnPowered, new OverlayImage(emptyImage,
        new OverlayImage(emptyImage, new OverlayImage(rightBorderUnPowered, cellBase))));
    bottomRightCell = new OverlayImage(emptyImage, new OverlayImage(downBorderUnPowered,
        new OverlayImage(emptyImage, new OverlayImage(rightBorderUnPowered, cellBase))));
    leftBottomCell = new OverlayImage(emptyImage, new OverlayImage(downBorderUnPowered,
        new OverlayImage(leftBorderUnPowered, new OverlayImage(emptyImage, cellBase))));

    topBottomRightCell = new OverlayImage(upBorderUnPowered, new OverlayImage(downBorderUnPowered,
        new OverlayImage(emptyImage, new OverlayImage(rightBorderUnPowered, cellBase))));
    leftBottomRightCell = new OverlayImage(emptyImage, new OverlayImage(downBorderUnPowered,
        new OverlayImage(leftBorderUnPowered, new OverlayImage(rightBorderUnPowered, cellBase))));
    leftTopBottomCell = new OverlayImage(upBorderUnPowered, new OverlayImage(downBorderUnPowered,
        new OverlayImage(leftBorderUnPowered, new OverlayImage(emptyImage, cellBase))));
    leftTopRightCell = new OverlayImage(upBorderUnPowered, new OverlayImage(emptyImage,
        new OverlayImage(leftBorderUnPowered, new OverlayImage(rightBorderUnPowered, cellBase))));

    leftTopBottomRightCell = new OverlayImage(upBorderUnPowered, new OverlayImage(
        downBorderUnPowered,
        new OverlayImage(leftBorderUnPowered, new OverlayImage(rightBorderUnPowered, cellBase))));

    leftCellPowered = new OverlayImage(emptyImage, new OverlayImage(emptyImage,
        new OverlayImage(leftBorderPowered, new OverlayImage(emptyImage, cellBase))));
    rightCellPowered = new OverlayImage(emptyImage, new OverlayImage(emptyImage,
        new OverlayImage(emptyImage, new OverlayImage(rightBorderPowered, cellBase))));
    topCellPowered = new OverlayImage(upBorderPowered, new OverlayImage(emptyImage,
        new OverlayImage(emptyImage, new OverlayImage(emptyImage, cellBase))));
    bottomCellPowered = new OverlayImage(emptyImage, new OverlayImage(downBorderPowered,
        new OverlayImage(emptyImage, new OverlayImage(emptyImage, cellBase))));

    topBottomCellPowered = new OverlayImage(upBorderPowered, new OverlayImage(downBorderPowered,
        new OverlayImage(emptyImage, new OverlayImage(emptyImage, cellBase))));
    leftRightCellPowered = new OverlayImage(emptyImage, new OverlayImage(emptyImage,
        new OverlayImage(leftBorderPowered, new OverlayImage(rightBorderPowered, cellBase))));
    leftTopCellPowered = new OverlayImage(upBorderPowered, new OverlayImage(emptyImage,
        new OverlayImage(leftBorderPowered, new OverlayImage(emptyImage, cellBase))));
    topRightCellPowered = new OverlayImage(upBorderPowered, new OverlayImage(emptyImage,
        new OverlayImage(emptyImage, new OverlayImage(rightBorderPowered, cellBase))));
    bottomRightCellPowered = new OverlayImage(emptyImage, new OverlayImage(downBorderPowered,
        new OverlayImage(emptyImage, new OverlayImage(rightBorderPowered, cellBase))));
    leftBottomCellPowered = new OverlayImage(emptyImage, new OverlayImage(downBorderPowered,
        new OverlayImage(leftBorderPowered, new OverlayImage(emptyImage, cellBase))));

    topBottomRightCellPowered = new OverlayImage(upBorderPowered,
        new OverlayImage(downBorderPowered,
            new OverlayImage(emptyImage, new OverlayImage(rightBorderPowered, cellBase))));
    leftBottomRightCellPowered = new OverlayImage(emptyImage, new OverlayImage(downBorderPowered,
        new OverlayImage(leftBorderPowered, new OverlayImage(rightBorderPowered, cellBase))));
    leftTopBottomCellPowered = new OverlayImage(upBorderPowered, new OverlayImage(downBorderPowered,
        new OverlayImage(leftBorderPowered, new OverlayImage(emptyImage, cellBase))));
    leftTopRightCellPowered = new OverlayImage(upBorderPowered, new OverlayImage(emptyImage,
        new OverlayImage(leftBorderPowered, new OverlayImage(rightBorderPowered, cellBase))));

    leftTopBottomRightCellPowered = new OverlayImage(upBorderPowered,
        new OverlayImage(downBorderPowered,
            new OverlayImage(leftBorderPowered, new OverlayImage(rightBorderPowered, cellBase))));

    leftTopBottomRightCellStationCell = new OverlayImage(
        new StarImage(cellSize / 2.5, OutlineMode.SOLID, Color.ORANGE),
        new OverlayImage(upBorderPowered, new OverlayImage(downBorderPowered,
            new OverlayImage(leftBorderPowered, new OverlayImage(rightBorderPowered, cellBase)))));

    powerStation = new StarImage(cellSize / 2.5, OutlineMode.SOLID, Color.ORANGE);

    border = new RectangleImage(cellSize, cellSize, OutlineMode.OUTLINE, Color.BLACK);

    p0 = new GamePiece(0, 0, false, false, false, true);
    p0.powerStation = true;
    p0.powered = true;
    p1 = new GamePiece(0, 1, true, false, true, false);
    p2 = new GamePiece(0, 2, true, false, true, true);
    p3 = new GamePiece(0, 3, true, false, false, false);
    p4 = new GamePiece(0, 4, true, false, true, false);
    p5 = new GamePiece(0, 5, false, true, false, false);
    p6 = new GamePiece(0, 6, false, true, false, true);
    p7 = new GamePiece(0, 7, true, false, true, false);

    p10 = new GamePiece(1, 0, true, true, false, true);
    p11 = new GamePiece(1, 1, false, true, true, true);
    p12 = new GamePiece(1, 2, false, true, true, false);
    p13 = new GamePiece(1, 3, true, false, true, true);
    p14 = new GamePiece(1, 4, true, true, true, false);
    p15 = new GamePiece(1, 5, true, false, true, false);
    p16 = new GamePiece(1, 6, false, true, true, true);
    p17 = new GamePiece(1, 7, false, false, false, true);

    p20 = new GamePiece(2, 0, true, true, false, false);
    p21 = new GamePiece(2, 1, false, false, true, true);
    p22 = new GamePiece(2, 2, true, false, true, false);
    p23 = new GamePiece(2, 3, false, true, false, true);
    p24 = new GamePiece(2, 4, false, true, true, true);
    p25 = new GamePiece(2, 5, true, false, true, false);
    p26 = new GamePiece(2, 6, false, true, true, true);
    p27 = new GamePiece(2, 7, true, false, true, false);

    p30 = new GamePiece(3, 0, false, false, true, true);
    p31 = new GamePiece(3, 1, false, false, true, true);
    p32 = new GamePiece(3, 2, false, true, true, true);
    p33 = new GamePiece(3, 3, true, false, false, false);
    p34 = new GamePiece(3, 4, false, false, true, false);
    p35 = new GamePiece(3, 5, true, false, false, false);
    p36 = new GamePiece(3, 6, false, false, true, true);
    p37 = new GamePiece(3, 7, false, false, true, true);

    p39 = new GamePiece(4, 0, false, false, true, true);
    p41 = new GamePiece(4, 1, false, true, false, false);
    p42 = new GamePiece(4, 2, true, false, true, true);
    p43 = new GamePiece(4, 3, false, false, true, true);
    p44 = new GamePiece(4, 4, false, false, true, false);
    p45 = new GamePiece(4, 5, false, true, true, true);
    p46 = new GamePiece(4, 6, false, true, true, true);
    p47 = new GamePiece(4, 7, false, false, false, true);

    p50 = new GamePiece(5, 0, false, false, false, true);
    p51 = new GamePiece(5, 1, false, true, false, false);
    p52 = new GamePiece(5, 2, false, true, true, true);
    p53 = new GamePiece(5, 3, false, false, true, false);
    p54 = new GamePiece(5, 4, false, false, true, false);
    p55 = new GamePiece(5, 5, false, true, false, false);
    p56 = new GamePiece(5, 6, false, true, false, true);
    p57 = new GamePiece(5, 7, true, false, true, false);

    p60 = new GamePiece(6, 0, false, true, false, true);
    p61 = new GamePiece(6, 1, false, true, false, false);
    p62 = new GamePiece(6, 2, true, false, true, true);
    p63 = new GamePiece(6, 3, true, true, false, true);
    p64 = new GamePiece(6, 4, false, true, true, true);
    p65 = new GamePiece(6, 5, false, true, false, false);
    p66 = new GamePiece(6, 6, true, false, true, false);
    p67 = new GamePiece(6, 7, false, true, false, true);

    p70 = new GamePiece(7, 0, false, true, false, true);
    p71 = new GamePiece(7, 1, true, false, false, true);
    p72 = new GamePiece(7, 2, true, true, false, true);
    p73 = new GamePiece(7, 3, false, false, true, true);
    p74 = new GamePiece(7, 4, true, false, true, true);
    p75 = new GamePiece(7, 5, false, true, false, false);
    p76 = new GamePiece(7, 6, true, true, false, false);
    p77 = new GamePiece(7, 7, false, false, true, false);

    p80 = new GamePiece(8, 0, true, false, false, false);
    p81 = new GamePiece(8, 1, true, true, false, true);
    p82 = new GamePiece(8, 2, true, true, false, true);
    p83 = new GamePiece(8, 3, false, false, true, false);
    p84 = new GamePiece(8, 4, false, false, true, false);
    p85 = new GamePiece(8, 5, true, false, false, false);
    p86 = new GamePiece(8, 6, true, true, false, true);
    p87 = new GamePiece(8, 7, true, false, true, false);

    row0Initial = new ArrayList<>(Arrays.asList(p0, p1, p2, p3, p4, p5, p6, p7));
    row1Initial = new ArrayList<>(Arrays.asList(p10, p11, p12, p13, p14, p15, p16, p17));
    row2Initial = new ArrayList<>(Arrays.asList(p20, p21, p22, p23, p24, p25, p26, p27));
    row3Initial = new ArrayList<>(Arrays.asList(p30, p31, p32, p33, p34, p35, p36, p37));
    row4Initial = new ArrayList<>(Arrays.asList(p39, p41, p42, p43, p44, p45, p46, p47));
    row5Initial = new ArrayList<>(Arrays.asList(p50, p51, p52, p53, p54, p55, p56, p57));
    row6Initial = new ArrayList<>(Arrays.asList(p60, p61, p62, p63, p64, p65, p66, p67));
    row7Initial = new ArrayList<>(Arrays.asList(p70, p71, p72, p73, p74, p75, p76, p77));
    row8Initial = new ArrayList<>(Arrays.asList(p80, p81, p82, p83, p84, p85, p86, p87));

    gridInitial = new ArrayList<>(Arrays.asList(row0Initial, row1Initial, row2Initial, row3Initial,
        row4Initial, row5Initial, row6Initial, row7Initial, row8Initial));

    boardInitial = new Board(10,10, gridInitial);

    gameInitial = new LightEmAll(10, 10, boardInitial);

    ///// Examples for the game when won

    p0P = new GamePiece(0, 0, false, false, false, true, true);
    p0P.powerStation = true;
    p0P.powered = true;
    p1P = new GamePiece(0, 1, false, true, false, true, true);
    p2P = new GamePiece(0, 2, true, true, false, true, true);
    p3P = new GamePiece(0, 3, true, false, false, false, true);
    p4P = new GamePiece(0, 4, false, true, false, true, true);
    p5P = new GamePiece(0, 5, true, false, false, false, true);
    p6P = new GamePiece(0, 6, false, true, false, true, true);
    p7P = new GamePiece(0, 7, true, false, false, true, true);

    p10P = new GamePiece(1, 0, false, true, true, true, true);
    p11P = new GamePiece(1, 1, true, false, true, true, true);
    p12P = new GamePiece(1, 2, false, true, true, false, true);
    p13P = new GamePiece(1, 3, true, true, false, true, true);
    p14P = new GamePiece(1, 4, true, false, true, true, true);
    p15P = new GamePiece(1, 5, false, true, false, true, true);
    p16P = new GamePiece(1, 6, true, false, true, true, true);
    p17P = new GamePiece(1, 7, false, false, true, false, true);

    p20P = new GamePiece(2, 0, false, false, true, true, true);
    p21P = new GamePiece(2, 1, false, false, true, true, true);
    p22P = new GamePiece(2, 2, false, true, false, true, true);
    p23P = new GamePiece(2, 3, true, false, true, false, true);
    p24P = new GamePiece(2, 4, false, true, true, true, true);
    p25P = new GamePiece(2, 5, true, false, true, false, true);
    p26P = new GamePiece(2, 6, false, true, true, true, true);
    p27P = new GamePiece(2, 7, true, false, false, true, true);

    p30P = new GamePiece(3, 0, false, false, true, true, true);
    p31P = new GamePiece(3, 1, false, false, true, true, true);
    p32P = new GamePiece(3, 2, false, true, true, true, true);
    p33P = new GamePiece(3, 3, true, false, false, false, true);
    p34P = new GamePiece(3, 4, false, false, true, false, true);
    p35P = new GamePiece(3, 5, false, false, false, true, true);
    p36P = new GamePiece(3, 6, false, false, true, true, true);
    p37P = new GamePiece(3, 7, false, false, true, true, true);

    p39P = new GamePiece(4, 0, false, false, true, true, true);
    p41P = new GamePiece(4, 1, false, false, true, false, true);
    p42P = new GamePiece(4, 2, false, true, true, true, true);
    p43P = new GamePiece(4, 3, true, true, false, false, true);
    p44P = new GamePiece(4, 4, true, false, false, false, true);
    p45P = new GamePiece(4, 5, false, true, true, true, true);
    p46P = new GamePiece(4, 6, true, false, true, true, true);
    p47P = new GamePiece(4, 7, false, false, true, false, true);

    p50P = new GamePiece(5, 0, false, false, true, false, true);
    p51P = new GamePiece(5, 1, false, true, false, false, true);
    p52P = new GamePiece(5, 2, true, false, true, true, true);
    p53P = new GamePiece(5, 3, false, false, false, true, true);
    p54P = new GamePiece(5, 4, false, false, false, true, true);
    p55P = new GamePiece(5, 5, false, false, true, false, true);
    p56P = new GamePiece(5, 6, false, true, true, false, true);
    p57P = new GamePiece(5, 7, true, false, false, true, true);

    p60P = new GamePiece(6, 0, false, true, false, true, true);
    p61P = new GamePiece(6, 1, true, false, false, false, true);
    p62P = new GamePiece(6, 2, false, true, true, true, true);
    p63P = new GamePiece(6, 3, true, true, true, false, true);
    p64P = new GamePiece(6, 4, true, true, true, false, true);
    p65P = new GamePiece(6, 5, true, false, false, false, true);
    p66P = new GamePiece(6, 6, false, true, false, true, true);
    p67P = new GamePiece(6, 7, true, false, true, false, true);

    p70P = new GamePiece(7, 0, false, true, true, false, true);
    p71P = new GamePiece(7, 1, true, false, false, true, true);
    p72P = new GamePiece(7, 2, false, true, true, true, true);
    p73P = new GamePiece(7, 3, true, true, false, false, true);
    p74P = new GamePiece(7, 4, true, true, false, true, true);
    p75P = new GamePiece(7, 5, true, false, false, false, true);
    p76P = new GamePiece(7, 6, false, false, true, true, true);
    p77P = new GamePiece(7, 7, false, false, false, true, true);

    p80P = new GamePiece(8, 0, false, true, false, false, true);
    p81P = new GamePiece(8, 1, true, true, true, false, true);
    p82P = new GamePiece(8, 2, true, true, true, false, true);
    p83P = new GamePiece(8, 3, true, false, false, false, true);
    p84P = new GamePiece(8, 4, false, false, true, false, true);
    p85P = new GamePiece(8, 5, false, true, false, false, true);
    p86P = new GamePiece(8, 6, true, true, false, true, true);
    p87P = new GamePiece(8, 7, true, false, true, false, true);

    row0Powered = new ArrayList<>(Arrays.asList(p0P, p1P, p2P, p3P, p4P, p5P, p6P, p7P));
    row1Powered = new ArrayList<>(Arrays.asList(p10P, p11P, p12P, p13P, p14P, p15P, p16P, p17P));
    row2Powered = new ArrayList<>(Arrays.asList(p20P, p21P, p22P, p23P, p24P, p25P, p26P, p27P));
    row3Powered = new ArrayList<>(Arrays.asList(p30P, p31P, p32P, p33P, p34P, p35P, p36P, p37P));
    row4Powered = new ArrayList<>(Arrays.asList(p39P, p41P, p42P, p43P, p44P, p45P, p46P, p47P));
    row5Powered = new ArrayList<>(Arrays.asList(p50P, p51P, p52P, p53P, p54P, p55P, p56P, p57P));
    row6Powered = new ArrayList<>(Arrays.asList(p60P, p61P, p62P, p63P, p64P, p65P, p66P, p67P));
    row7Powered = new ArrayList<>(Arrays.asList(p70P, p71P, p72P, p73P, p74P, p75P, p76P, p77P));
    row8Powered = new ArrayList<>(Arrays.asList(p80P, p81P, p82P, p83P, p84P, p85P, p86P, p87P));

    gridPowered = new ArrayList<>(Arrays.asList(row0Powered, row1Powered, row2Powered, row3Powered,
        row4Powered, row5Powered, row6Powered, row7Powered, row8Powered));

    boardPowered = new Board(9, 8, gridPowered);

    gamePowered = new LightEmAll(9, 8, boardPowered);

    // examples for the makeScene

    winnerMessage = new TextImage("You Win!", 39, FontStyle.BOLD, Color.GREEN);

    // initializes the game scene
    initialScene = new WorldScene(8 * 39, 9 * 39);

    initialScene.placeImageXY(this.gameInitial.board.board.get(0).get(0).draw(),
        (0 * cellSize) + 20, (0 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(0).get(1).draw(),
        (1 * cellSize) + 20, (0 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(0).get(2).draw(),
        (2 * cellSize) + 20, (0 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(0).get(3).draw(),
        (3 * cellSize) + 20, (0 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(0).get(4).draw(),
        (4 * cellSize) + 20, (0 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(0).get(5).draw(),
        (5 * cellSize) + 20, (0 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(0).get(6).draw(),
        (6 * cellSize) + 20, (0 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(0).get(7).draw(),
        (7 * cellSize) + 20, (0 * cellSize) + 20);

    initialScene.placeImageXY(this.gameInitial.board.board.get(1).get(0).draw(),
        (0 * cellSize) + 20, (1 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(1).get(1).draw(),
        (1 * cellSize) + 20, (1 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(1).get(2).draw(),
        (2 * cellSize) + 20, (1 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(1).get(3).draw(),
        (3 * cellSize) + 20, (1 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(1).get(4).draw(),
        (4 * cellSize) + 20, (1 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(1).get(5).draw(),
        (5 * cellSize) + 20, (1 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(1).get(6).draw(),
        (6 * cellSize) + 20, (1 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(1).get(7).draw(),
        (7 * cellSize) + 20, (1 * cellSize) + 20);

    initialScene.placeImageXY(this.gameInitial.board.board.get(2).get(0).draw(),
        (0 * cellSize) + 20, (2 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(2).get(1).draw(),
        (1 * cellSize) + 20, (2 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(2).get(2).draw(),
        (2 * cellSize) + 20, (2 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(2).get(3).draw(),
        (3 * cellSize) + 20, (2 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(2).get(4).draw(),
        (4 * cellSize) + 20, (2 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(2).get(5).draw(),
        (5 * cellSize) + 20, (2 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(2).get(6).draw(),
        (6 * cellSize) + 20, (2 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(2).get(7).draw(),
        (7 * cellSize) + 20, (2 * cellSize) + 20);

    initialScene.placeImageXY(this.gameInitial.board.board.get(3).get(0).draw(),
        (0 * cellSize) + 20, (3 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(3).get(1).draw(),
        (1 * cellSize) + 20, (3 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(3).get(2).draw(),
        (2 * cellSize) + 20, (3 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(3).get(3).draw(),
        (3 * cellSize) + 20, (3 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(3).get(4).draw(),
        (4 * cellSize) + 20, (3 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(3).get(5).draw(),
        (5 * cellSize) + 20, (3 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(3).get(6).draw(),
        (6 * cellSize) + 20, (3 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(3).get(7).draw(),
        (7 * cellSize) + 20, (3 * cellSize) + 20);

    initialScene.placeImageXY(this.gameInitial.board.board.get(4).get(0).draw(),
        (0 * cellSize) + 20, (4 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(4).get(1).draw(),
        (1 * cellSize) + 20, (4 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(4).get(2).draw(),
        (2 * cellSize) + 20, (4 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(4).get(3).draw(),
        (3 * cellSize) + 20, (4 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(4).get(4).draw(),
        (4 * cellSize) + 20, (4 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(4).get(5).draw(),
        (5 * cellSize) + 20, (4 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(4).get(6).draw(),
        (6 * cellSize) + 20, (4 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(4).get(7).draw(),
        (7 * cellSize) + 20, (4 * cellSize) + 20);

    initialScene.placeImageXY(this.gameInitial.board.board.get(5).get(0).draw(),
        (0 * cellSize) + 20, (5 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(5).get(1).draw(),
        (1 * cellSize) + 20, (5 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(5).get(2).draw(),
        (2 * cellSize) + 20, (5 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(5).get(3).draw(),
        (3 * cellSize) + 20, (5 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(5).get(4).draw(),
        (4 * cellSize) + 20, (5 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(5).get(5).draw(),
        (5 * cellSize) + 20, (5 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(5).get(6).draw(),
        (6 * cellSize) + 20, (5 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(5).get(7).draw(),
        (7 * cellSize) + 20, (5 * cellSize) + 20);

    initialScene.placeImageXY(this.gameInitial.board.board.get(6).get(0).draw(),
        (0 * cellSize) + 20, (6 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(6).get(1).draw(),
        (1 * cellSize) + 20, (6 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(6).get(2).draw(),
        (2 * cellSize) + 20, (6 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(6).get(3).draw(),
        (3 * cellSize) + 20, (6 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(6).get(4).draw(),
        (4 * cellSize) + 20, (6 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(6).get(5).draw(),
        (5 * cellSize) + 20, (6 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(6).get(6).draw(),
        (6 * cellSize) + 20, (6 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(6).get(7).draw(),
        (7 * cellSize) + 20, (6 * cellSize) + 20);

    initialScene.placeImageXY(this.gameInitial.board.board.get(7).get(0).draw(),
        (0 * cellSize) + 20, (7 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(7).get(1).draw(),
        (1 * cellSize) + 20, (7 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(7).get(2).draw(),
        (2 * cellSize) + 20, (7 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(7).get(3).draw(),
        (3 * cellSize) + 20, (7 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(7).get(4).draw(),
        (4 * cellSize) + 20, (7 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(7).get(5).draw(),
        (5 * cellSize) + 20, (7 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(7).get(6).draw(),
        (6 * cellSize) + 20, (7 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(7).get(7).draw(),
        (7 * cellSize) + 20, (7 * cellSize) + 20);

    initialScene.placeImageXY(this.gameInitial.board.board.get(8).get(0).draw(),
        (0 * cellSize) + 20, (8 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(8).get(1).draw(),
        (1 * cellSize) + 20, (8 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(8).get(2).draw(),
        (2 * cellSize) + 20, (8 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(8).get(3).draw(),
        (3 * cellSize) + 20, (8 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(8).get(4).draw(),
        (4 * cellSize) + 20, (8 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(8).get(5).draw(),
        (5 * cellSize) + 20, (8 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(8).get(6).draw(),
        (6 * cellSize) + 20, (8 * cellSize) + 20);
    initialScene.placeImageXY(this.gameInitial.board.board.get(8).get(7).draw(),
        (7 * cellSize) + 20, (8 * cellSize) + 20);

    // winning scene
    wonScene = new WorldScene(8 * 39, 9 * 39);

    wonScene.placeImageXY(this.gamePowered.board.board.get(0).get(0).draw(), (0 * cellSize) + 20,
        (0 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(0).get(1).draw(), (1 * cellSize) + 20,
        (0 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(0).get(2).draw(), (2 * cellSize) + 20,
        (0 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(0).get(3).draw(), (3 * cellSize) + 20,
        (0 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(0).get(4).draw(), (4 * cellSize) + 20,
        (0 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(0).get(5).draw(), (5 * cellSize) + 20,
        (0 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(0).get(6).draw(), (6 * cellSize) + 20,
        (0 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(0).get(7).draw(), (7 * cellSize) + 20,
        (0 * cellSize) + 20);

    wonScene.placeImageXY(this.gamePowered.board.board.get(1).get(0).draw(), (0 * cellSize) + 20,
        (1 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(1).get(1).draw(), (1 * cellSize) + 20,
        (1 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(1).get(2).draw(), (2 * cellSize) + 20,
        (1 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(1).get(3).draw(), (3 * cellSize) + 20,
        (1 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(1).get(4).draw(), (4 * cellSize) + 20,
        (1 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(1).get(5).draw(), (5 * cellSize) + 20,
        (1 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(1).get(6).draw(), (6 * cellSize) + 20,
        (1 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(1).get(7).draw(), (7 * cellSize) + 20,
        (1 * cellSize) + 20);

    wonScene.placeImageXY(this.gamePowered.board.board.get(2).get(0).draw(), (0 * cellSize) + 20,
        (2 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(2).get(1).draw(), (1 * cellSize) + 20,
        (2 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(2).get(2).draw(), (2 * cellSize) + 20,
        (2 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(2).get(3).draw(), (3 * cellSize) + 20,
        (2 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(2).get(4).draw(), (4 * cellSize) + 20,
        (2 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(2).get(5).draw(), (5 * cellSize) + 20,
        (2 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(2).get(6).draw(), (6 * cellSize) + 20,
        (2 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(2).get(7).draw(), (7 * cellSize) + 20,
        (2 * cellSize) + 20);

    wonScene.placeImageXY(this.gamePowered.board.board.get(3).get(0).draw(), (0 * cellSize) + 20,
        (3 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(3).get(1).draw(), (1 * cellSize) + 20,
        (3 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(3).get(2).draw(), (2 * cellSize) + 20,
        (3 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(3).get(3).draw(), (3 * cellSize) + 20,
        (3 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(3).get(4).draw(), (4 * cellSize) + 20,
        (3 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(3).get(5).draw(), (5 * cellSize) + 20,
        (3 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(3).get(6).draw(), (6 * cellSize) + 20,
        (3 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(3).get(7).draw(), (7 * cellSize) + 20,
        (3 * cellSize) + 20);

    wonScene.placeImageXY(this.gamePowered.board.board.get(4).get(0).draw(), (0 * cellSize) + 20,
        (4 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(4).get(1).draw(), (1 * cellSize) + 20,
        (4 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(4).get(2).draw(), (2 * cellSize) + 20,
        (4 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(4).get(3).draw(), (3 * cellSize) + 20,
        (4 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(4).get(4).draw(), (4 * cellSize) + 20,
        (4 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(4).get(5).draw(), (5 * cellSize) + 20,
        (4 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(4).get(6).draw(), (6 * cellSize) + 20,
        (4 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(4).get(7).draw(), (7 * cellSize) + 20,
        (4 * cellSize) + 20);

    wonScene.placeImageXY(this.gamePowered.board.board.get(5).get(0).draw(), (0 * cellSize) + 20,
        (5 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(5).get(1).draw(), (1 * cellSize) + 20,
        (5 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(5).get(2).draw(), (2 * cellSize) + 20,
        (5 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(5).get(3).draw(), (3 * cellSize) + 20,
        (5 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(5).get(4).draw(), (4 * cellSize) + 20,
        (5 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(5).get(5).draw(), (5 * cellSize) + 20,
        (5 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(5).get(6).draw(), (6 * cellSize) + 20,
        (5 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(5).get(7).draw(), (7 * cellSize) + 20,
        (5 * cellSize) + 20);

    wonScene.placeImageXY(this.gamePowered.board.board.get(6).get(0).draw(), (0 * cellSize) + 20,
        (6 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(6).get(1).draw(), (1 * cellSize) + 20,
        (6 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(6).get(2).draw(), (2 * cellSize) + 20,
        (6 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(6).get(3).draw(), (3 * cellSize) + 20,
        (6 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(6).get(4).draw(), (4 * cellSize) + 20,
        (6 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(6).get(5).draw(), (5 * cellSize) + 20,
        (6 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(6).get(6).draw(), (6 * cellSize) + 20,
        (6 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(6).get(7).draw(), (7 * cellSize) + 20,
        (6 * cellSize) + 20);

    wonScene.placeImageXY(this.gamePowered.board.board.get(7).get(0).draw(), (0 * cellSize) + 20,
        (7 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(7).get(1).draw(), (1 * cellSize) + 20,
        (7 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(7).get(2).draw(), (2 * cellSize) + 20,
        (7 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(7).get(3).draw(), (3 * cellSize) + 20,
        (7 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(7).get(4).draw(), (4 * cellSize) + 20,
        (7 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(7).get(5).draw(), (5 * cellSize) + 20,
        (7 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(7).get(6).draw(), (6 * cellSize) + 20,
        (7 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(7).get(7).draw(), (7 * cellSize) + 20,
        (7 * cellSize) + 20);

    wonScene.placeImageXY(this.gamePowered.board.board.get(8).get(0).draw(), (0 * cellSize) + 20,
        (8 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(8).get(1).draw(), (1 * cellSize) + 20,
        (8 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(8).get(2).draw(), (2 * cellSize) + 20,
        (8 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(8).get(3).draw(), (3 * cellSize) + 20,
        (8 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(8).get(4).draw(), (4 * cellSize) + 20,
        (8 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(8).get(5).draw(), (5 * cellSize) + 20,
        (8 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(8).get(6).draw(), (6 * cellSize) + 20,
        (8 * cellSize) + 20);
    wonScene.placeImageXY(this.gamePowered.board.board.get(8).get(7).draw(), (7 * cellSize) + 20,
        (8 * cellSize) + 20);

    // Examples for the game
    // 1x1 Board
    piece1x1 = new GamePiece(0, 0, true, false, false, false);
    row1x1 = new ArrayList<>(Arrays.asList(piece1x1));
    grid1x1 = new ArrayList<ArrayList<GamePiece>>(Arrays.asList(row1x1));
    board1x1 = new Board(1, 1, grid1x1);
    piece1x1Result = new GamePiece(0, 0, false, true, false, false);
    row1x1Result = new ArrayList<>(Arrays.asList(piece1x1Result));
    grid1x1Result = new ArrayList<ArrayList<GamePiece>>(Arrays.asList(row1x1Result));
    board1x1Result = new Board(1, 1, grid1x1Result);

    game1x1 = new LightEmAll(1, 1, board1x1);

    // 1x1 Board powered
    piece1x1Powered = new GamePiece(0, 0, true, false, false, false);
    piece1x1Powered.powered = true;
    row1x1Powered = new ArrayList<>(Arrays.asList(piece1x1Powered));
    grid1x1Powered = new ArrayList<ArrayList<GamePiece>>(Arrays.asList(row1x1Powered));
    board1x1Powered = new Board(1, 1, grid1x1Powered);
    piece1x1ResultPowered = new GamePiece(0, 0, false, true, false, false);
    row1x1ResultPowered = new ArrayList<>(Arrays.asList(piece1x1ResultPowered));
    grid1x1ResultPowered = new ArrayList<ArrayList<GamePiece>>(Arrays.asList(row1x1ResultPowered));
    board1x1ResultPowered = new Board(1, 1, grid1x1ResultPowered);

    game1x1Powered = new LightEmAll(1, 1, board1x1Powered);
    game1x1Powered.won = true;

    // 2x2 Board
    piece2x2w11 = new GamePiece(0, 0, true, false, false, false);
    piece2x2w12 = new GamePiece(0, 1, true, false, false, false);
    piece2x2w21 = new GamePiece(1, 0, true, false, true, false);
    piece2x2w22 = new GamePiece(1, 1, true, true, true, true);
    row2x2w1 = new ArrayList<>(Arrays.asList(piece2x2w11, piece2x2w12));
    row2x2w2 = new ArrayList<>(Arrays.asList(piece2x2w21, piece2x2w22));
    grid2x2 = new ArrayList<ArrayList<GamePiece>>(Arrays.asList(row2x2w1, row2x2w2));
    board2x2 = new Board(2, 2, grid2x2);

    piece2x2w11Rand = new GamePiece(0, 0, false, true, false, false);
    piece2x2w12Rand = new GamePiece(0, 1, false, true, false, false);
    piece2x2w21Rand = new GamePiece(1, 0, true, false, true, false);
    piece2x2w22Rand = new GamePiece(1, 1, true, true, true, true);
    row2x2w1Rand = new ArrayList<>(Arrays.asList(piece2x2w11Rand, piece2x2w12Rand));
    row2x2w2Rand = new ArrayList<>(Arrays.asList(piece2x2w21Rand, piece2x2w22Rand));
    grid2x2Rand = new ArrayList<ArrayList<GamePiece>>(Arrays.asList(row2x2w1Rand, row2x2w2Rand));
    board2x2Rand = new Board(2, 2, grid2x2Rand);

    rand1 = new Random(4);
    rand2 = new Random(100);

    initialScene1x1 = new WorldScene(1 * 39, 1 * 39);

    initialScene1x1.placeImageXY(this.game1x1.board.board.get(0).get(0).draw(), (0 * cellSize) + 20,
        (0 * cellSize) + 20);

    initialScene1x1Final = new WorldScene(1 * 39, 1 * 39);

    initialScene1x1Final.placeImageXY(this.game1x1Powered.board.board.get(0).get(0).draw(),
        (0 * cellSize) + 20, (0 * cellSize) + 20);

    initialScene1x1Final.placeImageXY(this.winnerMessage, 20, 20);
  }

  // tests the rotate method in the GamePiece class
  void testRotate(Tester t) {
    this.initCond();

    this.right.rotate();
    t.checkExpect(this.right.left, false);
    t.checkExpect(this.right.right, false);
    t.checkExpect(this.right.top, false);
    t.checkExpect(this.right.bottom, true);

    this.left.rotate();
    t.checkExpect(this.left.left, false);
    t.checkExpect(this.left.bottom, false);
    t.checkExpect(this.left.right, false);
    t.checkExpect(this.left.top, true);

    this.top.rotate();
    t.checkExpect(this.top.bottom, false);
    t.checkExpect(this.top.left, false);
    t.checkExpect(this.top.right, true);
    t.checkExpect(this.top.top, false);

    this.bottom.rotate();
    t.checkExpect(this.bottom.left, true);
    t.checkExpect(this.bottom.right, false);
    t.checkExpect(this.bottom.top, false);
    t.checkExpect(this.bottom.bottom, false);

    this.topBottom.rotate();
    t.checkExpect(this.topBottom.left, true);
    t.checkExpect(this.topBottom.right, true);
    t.checkExpect(this.topBottom.top, false);
    t.checkExpect(this.topBottom.bottom, false);

    this.leftRight.rotate();
    t.checkExpect(this.leftRight.left, false);
    t.checkExpect(this.leftRight.right, false);
    t.checkExpect(this.leftRight.top, true);
    t.checkExpect(this.leftRight.bottom, true);

    this.leftTop.rotate();
    t.checkExpect(this.leftTop.left, false);
    t.checkExpect(this.leftTop.right, true);
    t.checkExpect(this.leftTop.top, true);
    t.checkExpect(this.leftTop.bottom, false);

    this.topRight.rotate();
    t.checkExpect(this.topRight.left, false);
    t.checkExpect(this.topRight.right, true);
    t.checkExpect(this.topRight.top, false);
    t.checkExpect(this.topRight.bottom, true);

    this.bottomRight.rotate();
    t.checkExpect(this.bottomRight.left, true);
    t.checkExpect(this.bottomRight.right, false);
    t.checkExpect(this.bottomRight.top, false);
    t.checkExpect(this.bottomRight.bottom, true);

    this.leftBottom.rotate();
    t.checkExpect(this.leftBottom.top, true);
    t.checkExpect(this.leftBottom.bottom, false);
    t.checkExpect(this.leftBottom.left, true);
    t.checkExpect(this.leftBottom.right, false);

    this.topBottomRight.rotate();
    t.checkExpect(this.topBottomRight.left, true);
    t.checkExpect(this.topBottomRight.right, true);
    t.checkExpect(this.topBottomRight.top, false);
    t.checkExpect(this.topBottomRight.bottom, true);

    this.leftBottomRight.rotate();
    t.checkExpect(this.leftBottomRight.left, true);
    t.checkExpect(this.leftBottomRight.right, false);
    t.checkExpect(this.leftBottomRight.top, true);
    t.checkExpect(this.leftBottomRight.bottom, true);

    this.leftTopRight.rotate();
    t.checkExpect(this.leftTopRight.left, false);
    t.checkExpect(this.leftTopRight.right, true);
    t.checkExpect(this.leftTopRight.top, true);
    t.checkExpect(this.leftTopRight.bottom, true);

    this.leftTopBottom.rotate();
    t.checkExpect(this.leftTopBottom.left, true);
    t.checkExpect(this.leftTopBottom.right, true);
    t.checkExpect(this.leftTopBottom.top, true);
    t.checkExpect(this.leftTopBottom.bottom, false);

    this.leftTopBottomRight.rotate();
    t.checkExpect(this.leftTopBottomRight.left, true);
    t.checkExpect(this.leftTopBottomRight.right, true);
    t.checkExpect(this.leftTopBottomRight.top, true);
    t.checkExpect(this.leftTopBottomRight.bottom, true);
  }

  // tests the draw method in the GamePiece class
  void testDraw(Tester t) {
    this.initCond();

    t.checkExpect(this.left.draw(), new OverlayImage(
        new RectangleImage(cellSize, cellSize, OutlineMode.OUTLINE, Color.BLACK), this.leftCell));
    t.checkExpect(this.right.draw(), new OverlayImage(
        new RectangleImage(cellSize, cellSize, OutlineMode.OUTLINE, Color.BLACK), this.rightCell));
  }

  // tests the randomRotate method in the Board class
  void testRandomRotate(Tester t) {
    this.initCond();

    this.board1x1.randomRotate(rand1);
    this.board2x2.randomRotate(rand2);

    t.checkExpect(this.board1x1, board1x1Result);
  }

  // tests the oneKeyEvent method in the LightEmAll class
  void testOnKeyEvent(Tester t) {
    this.initCond();

    // tests when the power station is allowed to move through a powered line
    this.gameInitial.onMouseClicked(new Posn(0, 41), "LeftButton");
    this.gameInitial.onKeyEvent("down");
    t.checkExpect(this.gameInitial.board.board.get(0).get(0).powerStation, false);
    t.checkExpect(this.gameInitial.board.board.get(1).get(0).powered, true);

    // tests when the power station cannot be moved because it will be off the board
    this.gameInitial.onKeyEvent("left");
    t.checkExpect(this.gameInitial.board.board.get(0).get(0).powered, true);
    t.checkExpect(this.gameInitial.board.board.get(0).get(0).powerStation, true);

    // tests when the power station cannot be moved because there is no powered line
    this.gameInitial.onKeyEvent("down");
    t.checkExpect(this.gameInitial.board.board.get(0).get(0).powered, true);
    t.checkExpect(this.gameInitial.board.board.get(0).get(0).powerStation, true);
  }

  // tests the allowedToMove method in the LightEmAll class
  void testAllowedToMove(Tester t) {
    this.initCond();

    // tests when the power station cannot be moved because it it will be off the
    // board
    t.checkExpect(this.gameInitial.allowedToMove(0, 0, 0, -1), false);

    // tests when the power station cannot be moved because there is no powered line
    t.checkExpect(this.gameInitial.allowedToMove(0, 0, 1, 0), false);

    // tests when the power station is allowed to move through a powered line
    this.gameInitial.onMouseClicked(new Posn(0, 39), "LeftButton");
    this.gameInitial.onMouseClicked(new Posn(0, 39), "LeftButton");
    this.gameInitial.onMouseClicked(new Posn(0, 39), "LeftButton");

    t.checkExpect(this.gameInitial.allowedToMove(0, 0, 1, 0), true);
    t.checkExpect(this.gameInitial.allowedToMove(0, 0, 0, 0), false);
  }

  // tests the onMouseClicked method in the LightEmAll class
  void testOnMouseClicked(Tester t) {
    this.initCond();

    this.gameInitial.onMouseClicked(new Posn(0, 0), "RightButton");
    t.checkExpect(this.gameInitial.board.board.get(0).get(0).left, false);
    t.checkExpect(this.gameInitial.board.board.get(0).get(0).right, false);
    t.checkExpect(this.gameInitial.board.board.get(0).get(0).top, false);
    t.checkExpect(this.gameInitial.board.board.get(0).get(0).bottom, true);
    t.checkExpect(this.gameInitial.board.board.get(0).get(0).powerStation, true);

    this.gameInitial.onMouseClicked(new Posn(45, 45), "LeftButton");
    t.checkExpect(this.gameInitial.board.board.get(1).get(1).left, true);
    t.checkExpect(this.gameInitial.board.board.get(1).get(1).right, true);
    t.checkExpect(this.gameInitial.board.board.get(1).get(1).top, false);
    t.checkExpect(this.gameInitial.board.board.get(1).get(1).bottom, true);
    t.checkExpect(this.gameInitial.board.board.get(0).get(0).powerStation, true);

    this.gameInitial.onMouseClicked(new Posn(0, 41), "LeftButton");
    t.checkExpect(this.gameInitial.board.board.get(1).get(0).left, false);
    t.checkExpect(this.gameInitial.board.board.get(1).get(0).right, true);
    t.checkExpect(this.gameInitial.board.board.get(1).get(0).top, true);
    t.checkExpect(this.gameInitial.board.board.get(1).get(0).bottom, true);
    t.checkExpect(this.gameInitial.board.board.get(1).get(0).powered, true);

  }

  // tests the makeScene method in the LightEmAll class
  void testMakeScene(Tester t) {
    this.initCond();

    t.checkExpect(this.game1x1.makeScene(), this.initialScene1x1);
    t.checkExpect(this.game1x1Powered.makeScene(), this.initialScene1x1Final);
  }

  // tests the changePower method in the LightEmAll class
  void testChangePower(Tester t) {
    this.initCond();

    t.checkExpect(this.gameInitial.board.board.get(0).get(0).powered, true);
    t.checkExpect(this.gameInitial.board.board.get(1).get(0).powered, false);

    this.gameInitial.board.board.get(1).get(0).rotate();
    this.gameInitial.board.board.get(1).get(0).rotate();
    this.gameInitial.board.board.get(1).get(0).rotate();
    t.checkExpect(this.gameInitial.board.board.get(1).get(0).powered, false);

    this.gameInitial.changePower();
    t.checkExpect(this.gameInitial.board.board.get(0).get(0).powered, true);
    t.checkExpect(this.gameInitial.board.board.get(0).get(0).powerStation, true);
    t.checkExpect(this.gameInitial.board.board.get(1).get(0).powered, true);

    // TEST CASE: Cell should become powered if connection is established across
    // multiple cell
    this.gameInitial.board.board.get(1).get(1).rotate();
    t.checkExpect(this.gameInitial.board.board.get(1).get(1).powered, false);

    this.gameInitial.changePower();
    t.checkExpect(this.gameInitial.board.board.get(0).get(0).powered, true);
    t.checkExpect(this.gameInitial.board.board.get(0).get(0).powerStation, true);
    t.checkExpect(this.gameInitial.board.board.get(1).get(0).powered, true);
    t.checkExpect(this.gameInitial.board.board.get(1).get(1).powered, true);

    // TEST CASE: Cell should become un-powered when the connection is broken
    this.gameInitial.board.board.get(1).get(0).rotate();

    this.gameInitial.changePower();
    t.checkExpect(this.gameInitial.board.board.get(0).get(0).powered, true);
    t.checkExpect(this.gameInitial.board.board.get(0).get(0).powerStation, true);
    t.checkExpect(this.gameInitial.board.board.get(1).get(0).powered, false);
    t.checkExpect(this.gameInitial.board.board.get(1).get(1).powered, false);
  }

  // tests the checkGameWon method in the LightEmAll class
  void testCheckWinCondition(Tester t) {
    this.initCond();

    this.gameInitial.checkGameWon();
    t.checkExpect(this.gameInitial.won, false);

    this.gamePowered.checkGameWon();
    t.checkExpect(this.gamePowered.won, true);
  }

  // creates the scene and displays the LightEmAll game
  void testBigBang(Tester t) {
    int boardWidth = 10;
    int boardHeight = 10;
    LightEmAll game = new LightEmAll(boardWidth, boardHeight);

    int gameWidth = boardWidth * 39;
    int gameHeight = boardHeight * 39;

    game.bigBang(gameWidth, gameHeight);
  }
}
