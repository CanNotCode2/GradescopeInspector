package com.comp301.a09akari.view;

import com.comp301.a09akari.SamplePuzzles;
import com.comp301.a09akari.controller.ClassicMvcController;
import com.comp301.a09akari.controller.ControllerImpl;
import com.comp301.a09akari.model.Model;
import com.comp301.a09akari.model.ModelImpl;
import com.comp301.a09akari.model.ModelObserver;
import com.comp301.a09akari.model.PuzzleImpl;
import com.comp301.a09akari.model.PuzzleLibrary;
import com.comp301.a09akari.model.PuzzleLibraryImpl;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class AppLauncher extends Application {
  @Override
  public void start(Stage stage) {
    // TODO: Create your Model, View, and Controller instances and launch your GUI
    PuzzleLibrary puzzleLibrary = new PuzzleLibraryImpl();
    puzzleLibrary.addPuzzle(new PuzzleImpl(SamplePuzzles.PUZZLE_01));
    puzzleLibrary.addPuzzle(new PuzzleImpl(SamplePuzzles.PUZZLE_02));
    puzzleLibrary.addPuzzle(new PuzzleImpl(SamplePuzzles.PUZZLE_03));
    puzzleLibrary.addPuzzle(new PuzzleImpl(SamplePuzzles.PUZZLE_04));
    puzzleLibrary.addPuzzle(new PuzzleImpl(SamplePuzzles.PUZZLE_05));

    Model model = new ModelImpl(puzzleLibrary);
    ClassicMvcController controller = new ControllerImpl(model);

    List<FXComponent> fxComponents = new ArrayList<>();
    fxComponents.add(new ControlsViewComponent(model, controller));
    fxComponents.add(new PuzzleViewComponent(model, controller));
    fxComponents.add(new WinMessageComponent(model));

    ModelObserver modelObserver = getModelObserver(stage, model, fxComponents);

    ObservableList<Screen> screens = Screen.getScreens();

    // Change stage properties
    Rectangle2D bounds = screens.get(0).getVisualBounds();
    stage.setX(bounds.getMinX());
    stage.setY(bounds.getMinY());
    stage.setWidth(800);
    stage.setHeight(600); // Bounds.getHeight is bugged on some platforms, using hardcoded defaults and scaling
    stage.setResizable(false);
    stage.getIcons().add(new Image("icon.png"));

    model.addObserver(modelObserver);
    modelObserver.update(model);
  }

  private static ModelObserver getModelObserver(Stage stage, Model model, List<FXComponent> components) {

    return modelObserver -> {
      StackPane root = new StackPane();
      for (FXComponent component: components) {
        if (component.show()) {
          Parent node = component.render();
          root.getChildren().add(node);
          StackPane.setAlignment(node, component.getAlignment());
        }
      }

      Scene scene = new Scene(root);

//    Scaling on arbitrary display resolution
//    Scale scale = new Scale(screens.get(0).getOutputScaleX(), screens.get(0).getOutputScaleY());
//    scale.setPivotX(400);
//    scale.setPivotY(300);
//    scene.getRoot().getTransforms().setAll(scale);

//    messageView.getTransforms().setAll(scale);
//    controlView.getTransforms().setAll(scale);
//    puzzleView.getTransforms().setAll(scale);

      stage.setTitle("Akari: Puzzle " + (model.getActivePuzzleIndex() + 1) + " of " + model.getPuzzleLibrarySize());
      stage.setScene(scene);
      stage.show();
    };
  }
}
