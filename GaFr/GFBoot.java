package GaFr;
import java.io.IOException;

/** Internal use. */
public class GFBoot
{
  public static String basePath;
  public static GFGame game;

  public static void main (String[] args) throws IOException
  {
    GFN.initEarly();

    basePath = args[0];
    if (!basePath.endsWith("/")) basePath += "/";

    String clsname = args.length > 1 ? args[1] : null;
    if (clsname == null) clsname = "Game";

    // If you load an image this way here, then you can load multiple images
    // the same way later.  If you *don't* do it here, then you can't do it
    // later either (it causes a CheerpJ error).  No idea what's up there.
    // For the moment, we work around this by loading images a different way.
    //ImageIO.read(new File( resolvePath("minimal/blue_marble1.jpg") ));
    try
    {
      Class<?> cls = Class.forName(clsname);
      game = (GFGame)cls.getDeclaredConstructor().newInstance();
      game.onStartup();
    }
    catch (Exception e)
    {
      GFST.printStackTrace(e, "Exception instantiating/initializing main class " + clsname);
      throw new RuntimeException(e);
    }

    GFN.begin(game, game.WIDTH, game.HEIGHT);
  }

  public static String resolvePath (String fileName)
  {
    if (!fileName.startsWith("/")) fileName = basePath + fileName;
    return fileName;
  }

  public static void onFocusChange (boolean hasFocus)
  {
    GFKeyboard.releaseAll(); // Maybe only do this on hasFocus==false?
  }

  public static void onKeyUp (String key, int code, int flags)
  {
    GFKeyboard.setKey(code, false);
    if (game!=null) game.onKeyUp(key, code, flags);
    //System.out.print("Java: onKeyUp(" + key + ", code:" + code + ")");
  }

  public static void onKeyDown (String key, int code, int flags)
  {
    GFKeyboard.setKey(code, true);
    if (game!=null) game.onKeyDown(key, code, flags);
  }

  public static void onMouseMove (int x, int y, int buttons, int flags)
  {
    GFMouse.x = x;
    GFMouse.y = y;
    GFMouse.buttons = buttons;
    if (game!=null) game.onMouseMove(x,y,buttons,flags);
    //System.out.println("Java: onMouseMove("+x+","+y+","+buttons+","+flags+")");
  }
  public static void onMouseDown (int x, int y, int buttons, int flags, int button)
  {
    GFMouse.x = x;
    GFMouse.y = y;
    GFMouse.buttons = buttons;
    if (game!=null) game.onMouseDown(x,y,buttons,flags,button);
  }
  public static void onMouseUp (int x, int y, int buttons, int flags, int button)
  {
    GFMouse.x = x;
    GFMouse.y = y;
    GFMouse.buttons = buttons;
    if (game!=null) game.onMouseUp(x,y,buttons,flags,button);
  }

  public static int onEventFromBrowser (int i0, int i1, float f0, float f1,
                                         String s0, String s1)
  {
    if (game == null) return 0;
    return game.onEventFromBrowser(i0, i1, f0, f1, s0, s1);
  }

  public static void onDraw ()
  {
    //System.out.println("Java: draw()");
    ++GFCore.updateCount;
    if (game!=null) game.onUpdate();
    ++GFCore.frameCount;
    if (game!=null) game.onDrawBegin(GFCore.frameCount);
  }
}
