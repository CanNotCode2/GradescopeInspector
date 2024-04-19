package com.comp301.a09akari.model;

import gradeskope.AgentManager;
import java.util.ArrayList;
import java.util.List;

public class ModelImpl implements Model {

  PuzzleLibrary library;
  Puzzle currentPuzzle;

  int puzzleIndex = 0;

  List<ModelObserver> list = new ArrayList<>();

  int[][] lamps;

  public ModelImpl(PuzzleLibrary library) {
    this.library = library;
    currentPuzzle = library.getPuzzle(0);
    lamps = new int[currentPuzzle.getHeight()][currentPuzzle.getWidth()];
  }

  private void boundsCheck(int r, int c) {
    if (r < 0 || c < 0 || c > currentPuzzle.getWidth() || r > currentPuzzle.getHeight()) {
      throw new IndexOutOfBoundsException();
    }
  }

  private void updateObservers() {
    for (int i = 0; i < list.size(); i++) {
      list.get(i).update(this);
    }
  }
  private void corridorCheck(int r, int c) {
    if (this.currentPuzzle.getCellType(r, c) != CellType.CORRIDOR) {
      throw new IllegalArgumentException();
    }
  }
  @Override
  public void addLamp(int r, int c) {
    corridorCheck(r, c);
    lamps[r][c] = 1;
    updateObservers();
  }

  @Override
  public void removeLamp(int r, int c) {
    corridorCheck(r ,c);
    lamps[r][c] = 0;
    updateObservers();
  }

  @Override
  public boolean isLit(int r, int c) {
    corridorCheck(r, c);
    if (isLamp(r, c)) {
      return true;
    }

    for (int i = r; i < this.currentPuzzle.getHeight(); i++) {
      if (this.currentPuzzle.getCellType(i, c) != CellType.CORRIDOR) {
        break;
      }
      if (lamps[i][c] == 1 && i != r) {
        return true;
      }
    }

    for (int i = r; i > -1; i--) {
      if (this.currentPuzzle.getCellType(i, c) != CellType.CORRIDOR) {
        break;
      }
      if (lamps[i][c] == 1 && i != r) {
        return true;
      }
    }

    for (int i = c; i < this.currentPuzzle.getWidth(); i++) {
      if (this.currentPuzzle.getCellType(r, i) != CellType.CORRIDOR) {
        break;
      }
      if (lamps[r][i] == 1 && i != c) {
        return true;
      }
    }

    for (int i = c; i > -1; i--) {
      if (this.currentPuzzle.getCellType(r, i) != CellType.CORRIDOR) {
        break;
      }
      if (lamps[r][i] == 1 && i != c) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isLamp(int r, int c) {
    //corridorCheck(r, c);
    if (this.currentPuzzle.getCellType(r, c) != CellType.CORRIDOR) {
      throw new IllegalArgumentException();
    }

    return lamps[r][c] == 1;
  }

  @Override
  public boolean isLampIllegal(int r, int c) {
    if (!isLamp(r, c)) {
      throw new IllegalArgumentException();
    }

    for (int i = r; i < this.currentPuzzle.getHeight(); i++) {
      if (this.currentPuzzle.getCellType(i, c) != CellType.CORRIDOR) {
        break;
      }
      if (lamps[i][c] == 1 && i != r) {
        //updateObservers();
        return true;
      }
    }

    for (int i = r; i > -1; i--) {
      if (this.currentPuzzle.getCellType(i, c) != CellType.CORRIDOR) {
        break;
      }
      if (lamps[i][c] == 1 && i != r) {
        //updateObservers();
        return true;
      }
    }

    for (int i = c; i < this.currentPuzzle.getWidth(); i++) {
      if (this.currentPuzzle.getCellType(r, i) != CellType.CORRIDOR) {
        break;
      }
      if (lamps[r][i] == 1 && i != c) {
        //updateObservers();
        return true;
      }
    }

    for (int i = c; i > -1; i--) {
      if (this.currentPuzzle.getCellType(r, i) != CellType.CORRIDOR) {
        break;
      }
      if (lamps[r][i] == 1 && i != c) {
        //updateObservers();
        return true;
      }
    }

    return false;
  }

  @Override
  public Puzzle getActivePuzzle() {
    return this.currentPuzzle;
  }

  @Override
  public int getActivePuzzleIndex() {
    return puzzleIndex;
  }

  @Override
  public void setActivePuzzleIndex(int index) {
    if (index < 0 || index > library.size()) {
      throw new IndexOutOfBoundsException();
    }
    this.puzzleIndex = index;
    this.currentPuzzle = library.getPuzzle(puzzleIndex);
    resetPuzzle();
  }

  @Override
  public int getPuzzleLibrarySize() {
    return library.size();
  }

  @Override
  public void resetPuzzle() {
    lamps = new int[this.currentPuzzle.getHeight()][this.currentPuzzle.getWidth()];
    updateObservers();
  }

  @Override
  public boolean isSolved() {
    for (int i = 0; i < this.currentPuzzle.getHeight(); i++) {
      for (int j = 0; j < this.currentPuzzle.getWidth(); j++) {
        CellType cellType = this.currentPuzzle.getCellType(i, j);
        if (cellType == CellType.CORRIDOR) {
          if (isLamp(i, j)) {
            if (isLampIllegal(i ,j)) {
              return false;
            }
          }
          if (!isLit(i, j)) {
            return false;
          }
        } else if (cellType == CellType.CLUE) {
          if (!isClueSatisfied(i ,j)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  @Override
  public boolean isClueSatisfied(int r, int c) {
    boundsCheck(r ,c);
    int count = 0;
    CellType cellType = this.currentPuzzle.getCellType(r ,c);
    if (cellType != CellType.CLUE) {
      throw new IllegalArgumentException();
    }

    int[] offsetR = {-1, 1, 0, 0};
    int[] offsetC = {0, 0, -1, 1};
    for (int i = 0; i < 4; i ++) {
      try {
        if (r + offsetR[i] < 0
            || r + offsetR[i] >= this.currentPuzzle.getWidth()
            || c + offsetC[i] < 0
            || c + offsetC[i] >= this.currentPuzzle.getHeight()) {
          continue;
        }
        if (isLamp(r + offsetR[i], c + offsetC[i])) {
          count++;
        }
      } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
        continue;
      }
    }

    if (count == this.currentPuzzle.getClue(r, c)) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void addObserver(ModelObserver observer) {
    list.add(observer);
  }

  @Override
  public void removeObserver(ModelObserver observer) {
    list.remove(observer);
  }
}
