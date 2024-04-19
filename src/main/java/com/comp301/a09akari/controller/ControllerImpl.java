package com.comp301.a09akari.controller;

import com.comp301.a09akari.model.Model;

public class ControllerImpl implements ClassicMvcController {

  Model model;
  public ControllerImpl(Model model) {
    this.model = model;
  }
  @Override
  public void clickNextPuzzle() {
    model.setActivePuzzleIndex((model.getActivePuzzleIndex() + 1) % model.getPuzzleLibrarySize());
  }

  @Override
  public void clickPrevPuzzle() {
    int index = (model.getActivePuzzleIndex() - 1) % model.getPuzzleLibrarySize();
    model.setActivePuzzleIndex(index < 0 ? index + model.getPuzzleLibrarySize() : index);
  }

  @Override
  public void clickRandPuzzle() {
    int newPuzzleIndex = (int) (Math.random() * model.getPuzzleLibrarySize() - 1);
    if (newPuzzleIndex != model.getActivePuzzleIndex()) {
      model.setActivePuzzleIndex(newPuzzleIndex);
    } else {
      clickRandPuzzle();
    }
  }

  @Override
  public void clickResetPuzzle() {
    model.resetPuzzle();
  }

  @Override
  public void clickCell(int r, int c) {
    if (model.isLamp(r, c)) {
      model.removeLamp(r, c);
    } else {
      model.addLamp(r, c);
    }
  }
}
