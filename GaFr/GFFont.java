package GaFr;
import java.util.Arrays;

/** A class for drawing image-based fonts
  *
  * This class can draw image-based fonts to the screen.  See the constructors
  * for further information.
  *
  * @see GFFont(GFStamp[], String)
  * @see GFFont(String)
  *
  */
public class GFFont
{
  public GFStamp[] glyphMap;
  protected GFStamp replacementGlyph = null;

  public float lineHeight; ///< Space between lines
  public float deltaX = 0; ///< <0 to make characters closer together, >0 to make farther apart

  public int color = Gfx.Color.WHITE; ///< Color tint

  /** Loads the font from font file(s).
    *
    * GaFr has its own font file support.  Currently, these only support
    * fixed-width fonts.  The files come as a pair.  One contains meta-
    * data about the font (e.g., myfont.ffont.json).  The other is an
    * image with the actual font glyphs in it (e.g., myfont.ffont.png).
    *
    * Presently, these fonts can be easily generated from .psf font file
    * used by, e.g., Linux machines.  GaFr comes with a psf_to_ffont.py
    * utility to do the conversion.  For example:
    *
    *     python3 psf_to_png.py --out-dir=. /usr/share/consolefonts/Lat2-TerminusBold20x10.psf.gz
    *
    * @param fileName the name of the ffont.json file.
    *
    */
  public GFFont (String fileName)
  {
    if (!fileName.endsWith(".ffont.json"))
      throw new RuntimeException("Unsupported font format");

    String imgName = GFU.beforeLast(fileName, ".json") + ".png";
    GFTexture t = new GFTexture(imgName);

    GFJSON.Value json = new GFJSON(GFU.loadTextFile(fileName)).root;

    int charwidth = json.get("width").asInt();
    int charheight = json.get("height").asInt();
    GFStamp[] glyphs = t.splitIntoTilesBySize(charwidth, charheight);

    lineHeight = json.get("y_spacing", GFJSON.create(t.height)).asFloat();

    if (json.has("x_spacing"))
    {
      deltaX = json.get("x_spacing").asInt() - charwidth;
    }

    if (!json.has("charmap") || json.get("charmap").isNull())
    {
      glyphMap = new GFStamp[json.get("count", GFJSON.create(256)).asInt()];
      for (int i = 0; i < glyphMap.length; ++i)
      {
        glyphMap[i] = glyphs[i];
      }
    }
    else
    {
      int max = -1;
      for (GFJSON.Value v : json.get("charmap").asArray())
      {
        int ch = v.get(0).asInt();
        if (ch > max) max = ch;
      }
      glyphMap = new GFStamp[max+1];
      for (GFJSON.Value v : json.get("charmap").asArray())
      {
        int ch = v.get(0).asInt();
        int gl = v.get(1).asInt();
        glyphMap[ch] = glyphs[gl];
      }
    }
  }

  /** Constructs the font from a set of glyph stamps.
    *
    * Especially in the old "demo scene", lots of people made lots of
    * image-based fonts.  You can find a lot of them on the web, e.g.,:
    * https://github.com/ianhan/BitmapFonts
    *
    * While we could create .ffont.json files for these, you can often
    * get away without it, especially for fixed-width fonts, which
    * many of them are.  A basic recipe is:
    * 1. Load the image into a GFTexture
    * 2. Use tex.splitIntoTilesBySize() to split the texture into
    *    a stamp for each character.  For example, if each character
    *    glyph was 20 by 30 pixels, you'd pass in 20,30.
    * 3. Construct a GFFont, passing in the stamps and a string which
    *    telling the GFFont which character corresponds to each glyph
    *    in reading order (left to right, top to bottom).
    *
    * For example, bb_font.png (available at the link above), can be
    * loaded as so:
    *
    *     GFTexture tex = new GFTexture("fonts/bb_font.png", 0xff000000, 0);
    *     GFStamp[] stamps = tex.splitIntoTilesBySize(32,26);
    *     GFFont font = new GFFont(stamps,
    *        "ABCDEFGHIJKLMNOPQRSTUVWXYZ! _;0123456789\"(),-.'?:" );
    *     font.collapseCase();
    *
    * Note that we make use of the texture constructor's color remapping
    * feature to make black transparent.  Additionally, we make use of
    * GFFont's collapseCase() feature so that we can print strings
    * containing both upper and lower case characters even though the
    * font actually only has glyphs for one case.
    *
    */
  public GFFont (GFStamp[] glyphs, String chars)
  {
    char maxchar = chars.charAt(0);
    for (int i = 0; i < chars.length(); ++i)
    {
      if (chars.charAt(i) > maxchar) maxchar = chars.charAt(i);
    }
    glyphMap = new GFStamp[maxchar+1];
    for (int i = 0; i < chars.length(); ++i)
    {
      glyphMap[chars.charAt(i)] = glyphs[i];
      if (glyphs[i].height > lineHeight) lineHeight = glyphs[i].height;
    }
    initReplacement();
  }

  /** Fills in missing characters in upper/lower case.
    *
    * If your font doesn't actually have both upper and lower case defined,
    * printing strings with mixed case will have lots of empty characters,
    * which is annoying.  This fills in missing lower case characters using
    * upper case characters and vice versa to get around the issue.
    *
    */
  public GFFont collapseCase ()
  {
    if (glyphMap.length <= 'z')
      glyphMap = Arrays.copyOf(glyphMap, 'z'+1);

    for (int l = 'A'; l < 'Z'; ++l)
    {
      int u = l + 32;
      if (glyphMap[l] == null) glyphMap[l] = glyphMap[u];
      else if (glyphMap[u] == null) glyphMap[u] = glyphMap[l];
    }

    return this;
  }

  /** Tries to set up a replacement glyph for missing chracters. */
  protected boolean tryReplacement (int c)
  {
    if (c >= glyphMap.length) return false;
    GFStamp s = glyphMap[c];
    if (s == null) return false;
    replacementGlyph = s;
    return true;
  }

  /** Tries to set up a replacement glyph for missing chracters. */
  protected void initReplacement ()
  {
    if (tryReplacement('\u25a1')) return;
    if (tryReplacement('_')) return;
    if (tryReplacement(' ')) return;
    if (tryReplacement('?')) return;
  }

  /** Draws a string of text. */
  public void draw (float x, float y, String s)
  {
    //TODO: an easy way to draw at an angle

    float xx = x;
    for (int i = 0; i < s.length(); ++i)
    {
      int c = s.charAt(i);
      if (c == '\n')
      {
        y += lineHeight;
        x = xx;
        continue;
      }
      GFStamp g;
      if (c >= glyphMap.length)
      {
        g = replacementGlyph;
      }
      else
      {
        g = glyphMap[c];
        if (g == null) g = replacementGlyph;
      }
      if (g == null) continue;

      g.color = color;
      g.moveTo(x,y).stamp();
      x += g.width + deltaX;
    }
  }
}
