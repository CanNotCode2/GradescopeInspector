package com.comp301.a09akari.view;

import com.comp301.a09akari.controller.ClassicMvcController;
import com.comp301.a09akari.model.Model;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

public class ControlsViewComponent implements FXComponent {
  Model model;
  ClassicMvcController controller;
  ControlsViewComponent(Model model, ClassicMvcController controller) {
    this.model = model;
    this.controller = controller;
  }

  @Override
  public Parent render() {
    Button prev = new Button("Prev");
    Button reset = new Button("Reset");
    Button next = new Button("Next");
    Button random = new Button("Random");

    prev.setOnMouseClicked(e -> controller.clickPrevPuzzle());
    reset.setOnMouseClicked(e -> controller.clickResetPuzzle());
    next.setOnMouseClicked(e -> controller.clickNextPuzzle());
    random.setOnMouseClicked(e -> controller.clickRandPuzzle());

    BorderPane borderPane = new BorderPane();
    Insets buttonInsets = new Insets(10, 10, 10, 10);
    Label currentPuzzle = new Label(String.format("Puzzle %d of %d", model.getActivePuzzleIndex() + 1,
        model.getPuzzleLibrarySize()));
    HBox puzzleStringHB = new HBox(currentPuzzle);
    puzzleStringHB.setSpacing(10);
    puzzleStringHB.setPadding(buttonInsets);

    HBox leftHB = new HBox(prev);
    leftHB.setSpacing(10); //horizontal gap in pixels => that's what you are asking for
    leftHB.setPadding(buttonInsets);
    HBox rightHB = new HBox(next);
    rightHB.setSpacing(10);
    rightHB.setPadding(buttonInsets);

    borderPane.setLeft(leftHB);
    borderPane.setCenter(reset);
    borderPane.setRight(rightHB);

    FlowPane bottomButtons = new FlowPane();
    bottomButtons.getChildren().addAll(reset, random);
    HBox hbox = new HBox(bottomButtons);
    hbox.setPadding(buttonInsets);
    borderPane.setBottom(hbox);

    borderPane.setTop(currentPuzzle);

    return borderPane;
  }

  @Override
  public Pos getAlignment() {
    return Pos.BOTTOM_CENTER;
  }

  @Override
  public boolean show() {
    return true;
  }
}
