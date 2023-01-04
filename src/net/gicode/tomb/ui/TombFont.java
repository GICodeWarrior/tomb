package net.gicode.tomb.ui;

import java.awt.Font;

public class TombFont {
  private static final String[] MONOSPACED_FONT_PRECEDENCE = new String[] {
      "DejaVu Sans Mono", // Linux
      "Cascadia Mono", // Windows 11
      "Menlo", // macOS
      "Consolas", // Windows 10
      Font.MONOSPACED,
  };

  private static Font bestMonospaced = null;

  public static Font getMonospaced(int style, int size) {
    if (bestMonospaced == null) {
      bestMonospaced = findMonospaced();
    }

    return bestMonospaced.deriveFont(style, size);
  }

  private static Font findMonospaced() {
    Font fontAttempt = null;
    for (String fontFamily : MONOSPACED_FONT_PRECEDENCE) {
      fontAttempt = Font.decode(fontFamily);

      // The family is Dialog if the font is not found
      if (!fontAttempt.getFamily().equals(Font.DIALOG)) {
        break;
      }
    }

    return fontAttempt;
  }

  private TombFont() {
    throw new UnsupportedOperationException(TombFont.class.getName() + " is not instantiable.");
  }
}
