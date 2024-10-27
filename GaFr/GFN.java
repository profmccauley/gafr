package GaFr;

/** Internal use.
  *
  * These are functions implemented by the native layer (e.g., JavaScript).
  * They are generally not meant to be called directly.
  */
public class GFN
{
  public static native void initEarly ();
  public static native void begin (Object o, int width, int height);
  public static native void consoleLogObj (Object o);
  public static native void debugSet (Object o);

  public static native void setCanvasSize (int w, int h);
  public static native void getCanvasWidth ();
  public static native void getCanvasHeight ();

  public static native void gl_createTexture (GFTexture t);
  public static native void gl_texParameteri (int a, int b, int c);
  public static native void gl_activeTexture (int i);
  public static native void gl_bindTexture (int i, GFTexture t);
  public static native void gl_texImage2D (int level, int internalFormat, int w, int h, int srcFormat, int pix[], int offset);
  public static native void gl_generateMipmap (int i);

  public static native int gl_getUniformLocation (Gl.Program p, String name);
  public static native int gl_getAttributeLocation (Gl.Program p, String name);
  public static native int gl_getParameter (int pname);
  public static native void gl_uniform2f (int loc, float f1, float f2);
  public static native void gl_uniform1i (int loc, int i);
  public static native void gl_useProgram (Gl.Program p);
  public static native void gl_createProgramFromSources (Gl.Program p, String vs, String fs);

  public static native void gl_clearColor (float r, float g, float b, float a);
  public static native void gl_clear (int f);

  public static native void gl_viewport (int x, int y, int w, int height);
  public static native void gl_viewportDefault ();

  public static native void stampSetup (int maxStamps, int maxTextures, float[] vinfo, float[] tcs, byte[] tex, int[] tints);
  public static native void stampDraw (int numStamps, int numTextures);

  public static native void loadSound (GaFr.GFSound sound, byte[] data, String mime);
  public static native void playSound (GaFr.GFSound sound);
  public static native void setVolume (GaFr.GFSound sound, float volume);
  public static native void pauseSound (GaFr.GFSound sound);
  public static native void stopSound (GaFr.GFSound sound);

  public static native boolean getGamepadData (int index, float[] axes, boolean[] buttons, int[] counts);

  public static native void setTitle (String s);
  public static native void setCursorStyle (String s);

  public static native int eventToBrowser (int n0, int n1, String s0, String s1);
  public static native String eventToBrowserStr (int n0, int n1, String s0, String s1);
}
