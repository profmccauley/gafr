package GaFr;

/** Provides an interface to mouse/pointer related things.
  */
public class GFMouse
{
  public static final String CURSOR_AUTO = "auto";
  public static final String CURSOR_DEFAULT = "default";
  public static final String CURSOR_NONE = "none";
  public static final String CURSOR_CONTEXT_MENU = "context-menu";
  public static final String CURSOR_HELP = "help";
  public static final String CURSOR_POINTER = "pointer";
  public static final String CURSOR_PROGRESS = "progress";
  public static final String CURSOR_WAIT = "wait";
  public static final String CURSOR_CELL = "cell";
  public static final String CURSOR_CROSSHAIR = "crosshair";
  public static final String CURSOR_TEXT = "text";
  public static final String CURSOR_VERTICAL_TEXT = "vertical-text";
  public static final String CURSOR_ALIAS = "alias";
  public static final String CURSOR_COPY = "copy";
  public static final String CURSOR_MOVE = "move";
  public static final String CURSOR_NO_DROP = "no-drop";
  public static final String CURSOR_NOT_ALLOWED = "not-allowed";
  public static final String CURSOR_GRAB = "grab";
  public static final String CURSOR_GRABBING = "grabbing";
  public static final String CURSOR_ALL_SCROLL = "all-scroll";
  public static final String CURSOR_COL_RESIZE = "col-resize";
  public static final String CURSOR_ROW_RESIZE = "row-resize";
  public static final String CURSOR_E_RESIZE = "e-resize";
  public static final String CURSOR_S_RESIZE = "s-resize";
  public static final String CURSOR_W_RESIZE = "w-resize";
  public static final String CURSOR_NE_RESIZE = "ne-resize";
  public static final String CURSOR_NW_RESIZE = "nw-resize";
  public static final String CURSOR_SE_RESIZE = "se-resize";
  public static final String CURSOR_SW_RESIZE = "sw-resize";
  public static final String CURSOR_EW_RESIZE = "ew-resize";
  public static final String CURSOR_NS_RESIZE = "ns-resize";
  public static final String CURSOR_NESW_RESIZE = "nesw-resize";
  public static final String CURSOR_NWSE_RESIZE = "nwse-resize";
  public static final String CURSOR_ZOOM_IN = "zoom-in";
  public static final String CURSOR_ZOOM_OUT = "zoom-out";

  //TODO: Try to set these on startup (though it's not clear we always can).
  /// Current mouse X coordinate.
  public static float x;
  /// Current mouse Y coordinate.
  public static float y;
  /// Currently pressed mouse buttons.
  public static int buttons;

  /** Sets the pointer cursor.
    *
    * It's likely you want to use one of the "CURSOR" constants for the
    * argument.
    */
  public void setCursor (String s)
  {
    GFN.setCursorStyle(s);
  }
}
