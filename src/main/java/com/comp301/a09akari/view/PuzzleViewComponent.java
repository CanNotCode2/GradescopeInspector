package com.comp301.a09akari.view;

import com.comp301.a09akari.controller.ClassicMvcController;
import com.comp301.a09akari.model.CellType;
import com.comp301.a09akari.model.Model;
import com.comp301.a09akari.model.Puzzle;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class PuzzleViewComponent implements FXComponent {

  Model model;
  ClassicMvcController controller;

  PuzzleViewComponent(Model model, ClassicMvcController classicMvcController) {
    this.model = model;
    this.controller = classicMvcController;
  }
  @Override
  public Parent render() {
    GridPane gridPane = createGrid();

    Puzzle puzzle = model.getActivePuzzle();
    for (int i = 0; i < puzzle.getHeight(); i++) {
      for (int j = 0; j < puzzle.getWidth(); j++) {
        CellType cell = model.getActivePuzzle().getCellType(i, j);
        switch (cell) {
          case CORRIDOR:
            gridPane.add(createCorridorCell(i ,j), j, i);
            break;
          case WALL:
            gridPane.add(createWallCell(), j, i);
            break;
          case CLUE:
            gridPane.add(createClueCell(i, j), j, i);
            break;
        }
      }
    }
    return gridPane;
  }

  @Override
  public Pos getAlignment() {
    return null;
  }

  @Override
  public boolean show() {
    return true;
  }

  private GridPane createGrid() {
    GridPane gridPane = new GridPane();
    gridPane.setPickOnBounds(false);
    gridPane.setPrefSize(400, 400);
    gridPane.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
    for (int i = 0; i < model.getActivePuzzle().getHeight(); i++) {
      RowConstraints rc = new RowConstraints();
      rc.setPercentHeight(model.getActivePuzzle().getHeight());
      gridPane.getRowConstraints().add(rc);
    }
    for (int j = 0; j < model.getActivePuzzle().getWidth(); j++) {
      ColumnConstraints cc = new ColumnConstraints();
      cc.setPercentWidth(model.getActivePuzzle().getWidth());
      gridPane.getColumnConstraints().add(cc);
    }
    gridPane.setAlignment(Pos.CENTER);
    gridPane.setStyle("-fx-background-fill: black, white;\n" +
        "-fx-background-insets: 0, 2 2 2 2 ;" +
        "    -fx-padding: 5; \n" +
        "    -fx-hgap: 5; \n" +
        "    -fx-vgap: 5; ");
    gridPane.setMaxSize(.5, .5); // Makes entries always square
    return gridPane;
  }

  private Node createCorridorCell(int i, int j) {
    Button corridor = new Button();
    corridor.setMaxSize(Double.MAX_VALUE, Double.MIN_VALUE);
    StringBuilder stringBuilder = new StringBuilder();
    if (model.isLamp(i, j)) {
      corridor.setText("\uD83E\uDE94");
      if (model.isLampIllegal(i, j)) {
          stringBuilder.append("-fx-background-color: orange\n;");
      } else {
        stringBuilder.append("-fx-background-color: yellow\n;");
      }
    } else if (model.isLit(i ,j)) {
      stringBuilder.append("-fx-background-color: yellow\n;");
    } else {
      stringBuilder.append("-fx-background-color: white\n;");
    }
    corridor.setStyle(
        stringBuilder.toString() +
            "-fx-max-width: infinity;\n" +
            "-fx-max-height: infinity;"
    );
    corridor.setOnAction(e -> controller.clickCell(i, j));
    return corridor;
  }

  private Node createWallCell() {
    Button wall = new Button();
    wall.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    wall.setStyle("-fx-background-color: black");
    return wall;
  }

  private Node createClueCell(int i, int j) {
    Button number = new Button(Integer.toString(model.getActivePuzzle().getClue(i, j)));
    number.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    Font font = Font.font("Courier New", FontWeight.BOLD, 20);
    number.setFont(font);
    if (model.isClueSatisfied(i, j)) {
      number.setStyle("-fx-background-color: green;");
    } else {
      number.setStyle("-fx-background-color: red;");
    }
    return number;
  }
}
