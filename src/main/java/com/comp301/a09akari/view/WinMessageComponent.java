package com.comp301.a09akari.view;

import com.comp301.a09akari.model.Model;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;

public class WinMessageComponent implements FXComponent {

  Model model;

  WinMessageComponent(Model model) {
    this.model = model;
  }
  @Override
  public Parent render() {
    Label label = new Label("Congratulations, You Win!");
    label.setStyle("-fx-background-color: cornsilk;\n" +
        "    -fx-padding: 10;\n" +
        "    -fx-border-color: black; \n" +
        "    -fx-border-width: 5;\n" +
        "    -fx-font-size: 16");
    return label;
  }

  @Override
  public Pos getAlignment() {
    return Pos.CENTER;
  }

  @Override
  public boolean show() {
    return model.isSolved();
  }
}
