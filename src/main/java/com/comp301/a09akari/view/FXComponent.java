package com.comp301.a09akari.view;

import javafx.geometry.Pos;
import javafx.scene.Parent;

public interface FXComponent {
  /** Render the component and return the resulting Parent object */
  Parent render();

  Pos getAlignment();

  boolean show();
}
