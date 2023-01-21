package GaFr;
import static GaFr.GFU.*;

/** Stuff for graphics. */
public class Gfx
{
  /** Constants for common colors.
    *
    * GaFr's usual color format are 32 bit ints, where the high 8 bits
    * are the alpha channel (transparency), then 8 blue bits, then 8
    * green bits, and the low 8 bits are red.
    *
    */
  public static class Color
  {
    public static final int BLACK = 0xff000000;
    public static final int WHITE = 0xffFFffFF;
    public static final int RED =   0xff0000FF;
    public static final int BLUE =  0xffFF0000;
    public static final int GREEN = 0xff00FF00;

    /** "Magic pink", often used for transparency.
      * Magic pink (which is probably better described as magenta), is just
      * maximum red and blue.  However, it has a history of being used as a
      * special color indicating that it should be transparent (this is
      * useful in image formats without an alpha channel, for example).
      */
    public static final int MAGIC_PINK = 0xFFFF00FF;
  }

  /** Set the color that will be used to clear the screen. */
  public static void clearColor (int c)
  {
    GFN.gl_clearColor( ((c >>  0) & 0xff) / 255.0f,
                       ((c >>  8) & 0xff) / 255.0f,
                       ((c >> 16) & 0xff) / 255.0f,
                       ((c >> 24) & 0xff) / 255.0f );
  }

  /** Make an int color from floating point r/g/b/a values.
    *
    * The inputs are in the range [0,1].
    */
  public static int makeColor (double r, double g, double b, double a)
  {
    int rr = GFU.clamp255((int)(r * 255));
    int gg = GFU.clamp255((int)(g * 255));
    int bb = GFU.clamp255((int)(b * 255));
    int aa = GFU.clamp255((int)(a * 255));
    return aa << 24 | bb << 16 | gg << 8 | rr;
  }
  /** Make an int color from individual r/g/b/a values.
    *
    * The inputs are in the range [0,255].
    */
  public static int makeColor (int r, int g, int b, int a)
  {
    r = GFU.clamp255(r);
    g = GFU.clamp255(g);
    b = GFU.clamp255(b);
    a = GFU.clamp255(a);
    return a << 24 | b << 16 | g << 8 | r;
  }

  /** Replace just the red portion of a color. */
  public static int replaceRed (int color, int red)
  {
    //int rr = (color >>  0) & 0xff;
    int gg = (color >>  0) & 0xff;
    int bb = (color >>  0) & 0xff;
    int aa = (color >>  0) & 0xff;
    int rr = clamp255(red);
    return aa << 24 | bb << 16 | gg << 8 | rr;
  }
  /** Replace just the green portion of a color. */
  public static int replaceGreen (int color, int green)
  {
    int rr = (color >>  0) & 0xff;
    //int gg = (color >>  0) & 0xff;
    int bb = (color >>  0) & 0xff;
    int aa = (color >>  0) & 0xff;
    int gg = clamp255(green);
    return aa << 24 | bb << 16 | gg << 8 | rr;
  }
  /** Replace just the blue portion of a color. */
  public static int replaceBlue (int color, int blue)
  {
    int rr = (color >>  0) & 0xff;
    int gg = (color >>  0) & 0xff;
    //int bb = (color >>  0) & 0xff;
    int aa = (color >>  0) & 0xff;
    int bb = clamp255(blue);
    return aa << 24 | bb << 16 | gg << 8 | rr;
  }
  /** Replace just the alpha portion of a color. */
  public static int replaceAlpha (int color, int alpha)
  {
    int rr = (color >>  0) & 0xff;
    int gg = (color >>  0) & 0xff;
    int bb = (color >>  0) & 0xff;
    //int aa = (color >>  0) & 0xff;
    int aa = clamp255(alpha);
    return aa << 24 | bb << 16 | gg << 8 | rr;
  }
}
