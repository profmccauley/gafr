package GaFr;
import java.util.Arrays;
import java.util.ArrayList;

/** Gamepad access.
  *
  * This class allows accessing gamepad information.  Gamepads are
  * considered to have some number of axes (e.g., a joystick has two
  * axes -- an X and a Y), and a number of buttons.  Each gamepad
  * should have a unique index.
  *
  * Perhaps the easiest way to use this is to simply construct as many
  * Gamepad instances as you want in your constructor, passing each
  * one a different index.  Then call refresh() once per update/frame,
  * and check the new button/axis values.
  *
  * A problematic aspect is that it remains to be seen how different
  * browsers present multiple gamepad.  Do the indices always start
  * at 0?  I couldn't immediately convince myself that was true.
  * In theory, the static getGamepad() method will provide a better
  * way, but it's not quite there yet (we need never-connected
  * instances to do deferred initialization, essentially).
  *
  * Note that browsers may not report anything / may report that a
  * game pad is unconnected until the user actually interacts with it.
  *
  */
public class GFGamepad
{
  public float[] axes;
  public boolean[] buttons;
  public boolean connected;
  public int index;
  protected int[] counts;

  private static ArrayList<GFGamepad> pads;
  public static GFGamepad getGamepad ()
  {
    return getGamepad(-1);
  }
  public static GFGamepad getGamepad (int which)
  {
    if (pads == null) pads = new ArrayList<GFGamepad>();
    if (which == -1) which = pads.size();
    if (which < pads.size()) return pads.get(which);
    int start = 0;
    if (pads != null) start = pads.get(pads.size()-1).index + 1;
    GFGamepad x = null;
    for (int i = start; i < start + 4; ++i)
    {
      x = new GFGamepad(i);
      if (x.connected)
      {
        pads.add(x);
        return x;
      }
    }
    return x;
  }

  public void reset ()
  {
    int numAxes = -1;
    int numButtons = -1;
    if (axes != null) numAxes = axes.length;
    if (buttons != null) numButtons = buttons.length;

    connected = GFN.getGamepadData(index, null, null, counts);
    if (connected)
    {
      if (numAxes == -1) numAxes = counts[0];
      if (numButtons == -1) numButtons = counts[1];
    }
    else
    {
      if (numAxes == -1) numAxes = 4;
      if (numButtons == -1) numButtons = 8;
    }

    axes = new float[numAxes];
    buttons = new boolean[numButtons];
    refresh();
  }

  public GFGamepad (int which, int numAxes, int numButtons)
  {
    counts = new int[2];
    index = which;
    reset();
  }
  public GFGamepad (int which)
  {
    this(which, -1, -1);
  }
  public GFGamepad ()
  {
    this(0);
  }
  public GFGamepad refresh ()
  {
    connected = GFN.getGamepadData(index, axes, buttons, counts);
    if (!connected)
    {
      Arrays.fill(axes, 0);
      Arrays.fill(buttons, false);
    }
    return this;
  }

  /*
  public double getAxis (int index)
  {
    if (index > axes.length) return 0;
    return axes[index];
  }
  */
  public float getAxisf (int index)
  {
    //return (float)getAxis(index);
    if (index > axes.length) return 0;
    return axes[index];
  }
  public boolean getButton (int index)
  {
    if (index > buttons.length) return false;
    return buttons[index];
  }

  public int getAxisCount ()
  {
    return counts[0];
  }
  public int getButtonCount ()
  {
    return counts[1];
  }

  public String toString ()
  {
    String s = "";
    for (int i = 0; i < axes.length; ++i)
    //for (int i = 0; i < counts[0]; ++i)
    {
      // For whatever reason, the obvious format string %+.2f isn't working
      // quite right (giving me +-0.00 sometimes).  So do it by hand.
      String x = String.format("%.2f ", axes[i]);
      if (!x.startsWith("-")) s += "+";
      s += x;
    }
    for (int i = 0; i < buttons.length; ++i)
    //for (int i = 0; i < counts[1]; ++i)
      s += buttons[i] ? "O" : ".";
    return s;
  }
}
