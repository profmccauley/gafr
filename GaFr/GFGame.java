package GaFr;

/** Superclass for games.
  *
  * This is your main entryway into GaFr.  You create a subclass of this class
  * and override its various methods.
  */
public class GFGame
{
  /** The canvas / "screen" width. */
  public int WIDTH = 800;

  /** The canvas / "screen" height. */
  public int HEIGHT = 500;

  /** Internal use. */
  public GFStampManager _stampManager;

  public GFGame ()
  {
    _stampManager = new GFStampManager();
  }

  /** Called when the game is being initialized. */
  public void onStartup ()
  {
  }

  /** Called when a key is relased.
    *
    * @see GFGame#keyDown(String, int, int)
    */
  public void onKeyUp (String key, int code, int flags)
  {
  }

  /** Called when a key is pressed.
    *
    * @param key The key as a string.
    * @param code The key code for the key.
    * @param flags Additional information about the keypress.
    *
    * If the keypress corresponds to a printable character, it will show up in
    * the key parameter.  For example, if a user presses the key marked A, this
    * will be "a", unless they are holding shift, in which case it will be "A".
    *
    * The code paramter is more concrete.  It specifically refers to a key and
    * not a character.  Thus, it can refer to things that aren't characters,
    * like the control key.  There is no difference between a capital and
    * lower case from the perspective of the keycode, because it's the same
    * key either way.  The codes are defined as constants in GFKey.
    *
    * The flags parameter has other information about the keypress, for example
    * whether a modifier key (like Shift) is being held.  Flags is a bitfield
    * that contains some combinations of the bit flags in GFKey.Flag.  You can
    * check if Shift is being held by doing the following, for example:
    *     ((flags & GFKey.Flag.Shift) != 0)
    */
  public void onKeyDown (String key, int code, int flags)
  {
  }

  /** Called when the mouse is moved.
    *
    * The parameters are the same as onMouseDown(), except there is no button
    * parameter because this was not called in response to a button being
    * pressed.  However, the flags paramter still gives information about
    * modifier keys being held.
    *
    * @see GFGame#onMouseDown(int, int, int, int, int)
    */
  public void onMouseMove (int x, int y, int buttons, int flags)
  {
  }

  /** Called when a mouse button is pressed.
    *
    * @param x The mouse X coordinate (0 is the left).
    * @param y The mouse Y coordinate (0 is the top).
    * @param buttons Bitfield of buttons being held.
    * @param flags Additional information.
    * @param button The button that was pressed.
    *
    * To check if button n is down, you would do the following:
    *     (boolean)(buttons & (1 << (n-1)))
    *
    * The flags paramter is the same as used by, e.g., onKeyDown.
    */
  public void onMouseDown (int x, int y, int buttons, int flags, int button)
  {
  }
  /** Called when a mouse button is released.
    *
    * The parameters are the same as onMouseDown(), except button refers to
    * the button that was released.
    *
    * @see GFGame#onMouseDown(int, int, int, int, int)
    */
  public void onMouseUp (int x, int y, int buttons, int flags, int button)
  {
  }

  /** Called 60 times per second.
    *
    * Unlike onDraw(), we try very hard to call this 60 times per second.
    * (Note: actually, at present, there's literally no difference to
    * onDraw(), but there should be someday.)
    */
  public void onUpdate ()
  {
  }

  /** Mostly internal use.
    *
    * This calls onDraw().
    */
  public void onDrawBegin (int frameCount)
  {
    if (!_stampManager.isEmpty())
      GFU.log("StampManager not empty at start of frame");
    GFN.gl_clear(Gl.COLOR_BUFFER_BIT);
    try
    {
      onDraw(frameCount);
    }
    catch (Exception e)
    {
      GFST.printStackTrace(e, "In onDraw()");
    }
    _stampManager.flush();
  }

  /** Called when you should draw a new frame.
    *
    * The framework tries to call this 60 times per second, though it may skip
    * some if things are busy.
    */
  public void onDraw (int frameCount)
  {
  }

  /** Invoked by user-provided JavaScript.
    *
    * It can be useful to integrate functionality written in JavaScript with
    * GaFr.  As one example, one might wish to use HTML form elements (like a
    * text field) to provide input to GaFr.  While one could extend GaFr by
    * adding native methods specifically for a particular use, this method
    * is an attempt to provide a generic bridge.  From JavaScript, one can
    * just call gafrj_eventToGaFr() and pass some data; this method is then
    * called, and the game can interpret it however it desires -- without
    * needing to modify GaFr itself.
    *
    * With much the same rationale, GaFr developers can call either
    * GFN.eventToBrowser() or GFN.eventToBrowserStr() from Java, which will
    * call onEventFromGaFr() or onEventFromGaFrStr() JavaScript functions.
    */
  public int onEventFromBrowser (int i0, int i1, float f0, float f1,
                                  String s0, String s1)
  {
    GFU.logs("Unhandled onEventFromBrowser:", i0, i1, f0, f1, s0, s1);
    return -1;
  }
}
