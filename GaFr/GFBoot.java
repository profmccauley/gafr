package GaFr;
import GaFr.GFGame;
import GaFr.GFN;
import GaFr.GFKeyboard;

import java.io.IOException;

/** Internal use. */
public class GFBoot
{
  public static String basePath;
  public static int frameCount;
  public static GFGame game;

  public static void main (String[] args) throws IOException
  {
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
      Class cls = Class.forName(clsname);
      game = (GFGame)cls.newInstance();
    }
    catch (Exception e)
    {
      GFU.log("Exception while instantiating Game: " + e);
      GFU.log(GFU.getStackTrace(e));
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
    if (game!=null) game.onMouseMove(x,y,buttons,flags);
    //System.out.println("Java: onMouseMove("+x+","+y+","+buttons+","+flags+")");
  }
  public static void onMouseDown (int x, int y, int buttons, int flags, int button)
  {
    if (game!=null) game.onMouseDown(x,y,buttons,flags,button);
  }
  public static void onMouseUp (int x, int y, int buttons, int flags, int button)
  {
    if (game!=null) game.onMouseUp(x,y,buttons,flags,button);
  }

  public static void onDraw ()
  {
    //System.out.println("Java: draw()");
    if (game!=null) game.onUpdate();
    ++frameCount;
    if (game!=null) game.onDrawBegin(frameCount);
  }
}
