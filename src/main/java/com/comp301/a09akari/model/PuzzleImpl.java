package com.comp301.a09akari.model;

import gradeskope.AgentManager;

public class PuzzleImpl implements Puzzle {

  int[][] board;
  public PuzzleImpl(int[][] board) {
    this.board = board;
  }

  @Override
  public int getWidth() {
    return this.board[0].length;
  }

  @Override
  public int getHeight() {
    return this.board.length;
  }

  @Override
  public CellType getCellType(int r, int c) {
    boundsCheck(r, c);
    int num = this.board[r][c];
    switch (num) {
      case 5:
        return CellType.WALL;
      case 6:
        return CellType.CORRIDOR;
      default:
        return CellType.CLUE;
    }
  }

  @Override
  public int getClue(int r, int c) {
    if (!(getCellType(r, c) == CellType.CLUE)) {
      throw new IllegalArgumentException();
    }
    return this.board[r][c];
  }

  private void boundsCheck(int r, int c) {
    if (r < 0 || c < 0 || c > getWidth() || r > getHeight()) {
      throw new IndexOutOfBoundsException();
    }
  }
}
