package GaFr;
import java.util.Arrays;

/** This class contains information on which keys are currently down.
  */
public class GFKeyboard
{
  protected static boolean[] downKeys = new boolean[256];

  /** Get whether a key is down.
    * This version works if "key" is an uppercase letter, a digit, a character that
    * corresponds to the ASCII value of an un-shifted key on a US keyboard,
    * a linefeed, backspace, and probably not much else.
    */
  /*
  public static boolean isDown (char key)
  {
    return downKeys[key];
  }
  */
  /** Checks whether a given key is down.
    *
    * @param key One of the constants in GFKey.
    *
    * Note that for keys that correspond to ASCII values on a normal US
    * keyboard, the GFKey code is the same as the ASCII value.  Thus, you
    * can do things like .isDown('A').
    *
    */
  public static boolean isDown ( /*GFKey*/ int key)
  {
    return downKeys[key/*.code*/];
  }
  /** The opposite of isDown(). */
  public static boolean isUp ( /*GFKey*/ int key)
  {
    return !isDown(key);
  }

  /** Internal use. */
  public static void releaseAll ()
  {
    Arrays.fill(downKeys, false);
  }
  /** Internal use. */
  public static void setKey (int code, boolean value)
  {
    downKeys[code] = value;
  }
}
