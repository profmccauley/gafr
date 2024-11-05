package GaFr;
import GaFr.util.KV;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;

/** A class for drawing image-based fonts.
  *
  * This class can draw image-based fonts to the screen.  These fonts can
  * be constructed "by hand" in code, or by loading one from a file.  GaFr
  * has support for the following image-based font formats:
  * * GaFr fixed fonts (.ffont.json).  These are simple fixed-width fonts.
  * * BMFont fonts.  These are supported by a variety of software.
  *
  * If your intest is primarily in creating GFFonts through code, you
  * might want to start your journey at the GFFont(GFStamp[], String)
  * constructor.  Otherwise, you may be interested in the GFFont(String)
  * constructor for loading from files.
  *
  * Getting Your Desired Fonts Into GaFr
  * ====================================
  * Today, most "normal" computer fonts are stored in the form of
  * mathematical formulas.  Font types like TrueType (.ttf), OpenType
  * (.otf), and PostScript fonts (.pfb, etc.) are all examples of this.
  * These are sometimes referred to as vector fonts or outline fonts,
  * and are generally not great for games.  In games, we usually want
  * to draw *images*.  We want the fonts in terms of bitmaps, the same
  * as sprites and stuff.  For this reason, we often don't use normal
  * font files directly.  Instead, it's common to convert from a normal
  * font file to an image-based font file.  In many cases, such a file
  * is actually two files: one is an image, which contains all the
  * characters of the font (sort of like a sprite sheet), and the other
  * is metadata saying where all the individual characters are on the
  * image, maybe how much space to put between characters; that sort
  * of thing.
  *
  * GaFr currently supports two such file formats.  One is a simple
  * home-grown file format, which currently only supports fixed-width
  * fonts (where each character is the same width).  This is the GaFr
  * Fixed Font format (stored in .ffont.json) files.  The other is
  * the BMFont format, which comes from the AngelCode Bitmap Font
  * Generator program.  See the relevant section below for more.
  *
  * GaFr Fixed Fonts
  * ================
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
  * BMFont Fonts
  * ============
  * BMFont comes from the AngelCode Bitmap Font Generator program.
  * Unsurprisingly, this program can generate BMFonts from a normal
  * TrueType font like many OSes use.  There are other similar tools
  * too.  For example, the libGDX community has a class and a simple
  * command-line tool for generating such fonts, as well as the more
  * fully-featured Hiero tool.
  *
  * A downside of the above tools is that they require you to install
  * software.  The SnowB Bitmap Font tool does more or less the same
  * stuff and runs in a web browser.  It has a lot of options for
  * selecting how large the output images should be (it will use more
  * than one if all the characters don't fit on one), lets you add
  * shadows/outlines, etc.  I've found it generates slightly nicer
  * images in Firefox than Chrome.  When you've got your font how
  * you want it, export it in the "BMFont TEXT" format with the ".fnt"
  * extension.  This will give you a zip file; unzip it and put the
  * files in your project somewhere.  You can then load it with the
  * single-string-parameter GFFont constructor.  See below for a link
  * to the SnowB tool.
  *
  * Futher Info
  * ===========
  * For further info, check the constructors:
  * * @see GFFont(GFStamp[], String)
  * * @see GFFont(String)
  *
  *
  * PSF font info/links
  * -------------------
  * * [Screenshots](https://adeverteuil.github.io/linux-console-fonts-screenshots/)
  *   of a number of different PSF fonts.
  * * [PSF Tools](https://www.seasip.info/Unix/PSF/) is a set of tools
  *   for converting a number of fixed-width font formats into PSF...
  *   which you can then turn into GaFr fixed width fonts.
  * * [Wikipedia's PC Screen Font](https://en.wikipedia.org/wiki/PC_Screen_Font)
  *   page has information about the PSF font format.
  * * [OSDev's PC Screen Font](https://wiki.osdev.org/PC_Screen_Font) page
  *   has more detailed information about the format.
  *
  *
  * BMFont info/links
  * -----------------
  * * The [AngelCode BMFont](https://www.angelcode.com/products/bmfont)
  *   tool is the origin of the BMFont format that GaFr supports.  As
  *   well as having the tool, their site has details of the BMFont
  *   format (which were used to write GaFr's support).
  * * The libGDX website has information and tools for working with bitmap
  *   fonts on their [Hiero](https://libgdx.com/wiki/tools/hiero) page.
  * * The [SnowB Bitmap Font tool](https://snowb.org/) is a pretty nice
  *   web-based tool for generating BMFonts from normal outline fonts.
  *
  */

public class GFFont
{
  /// Info needed to draw a glyph.
  protected static class GlyphInfo
  {
    GFStamp stamp; ///< Stamp of the glyph image.
    float deltaX; ///< Used in conjunction with global deltaX.
    GlyphInfo (GFStamp s)
    {
      stamp = s;
    }
  }

  /** Helper for parsing the plain-text variant of BMFont files.
    *
    * The text version of BMFont files are line-oriented.  Each line starts
    * with a "tag" describing what kind of info the line contains.  The rest
    * of the line are a bunch of key/value pairs.
    *
    * BMFont file format documentation:
    * https://www.angelcode.com/products/bmfont/doc/file_format.html
    */
  protected static class BMFontLoader implements Iterable<KV>
  {
    // The following are all parser states
    protected static final int S_TAG = 0;        ///< The tag
    protected static final int S_KEY = 1;        ///< Key part of KV pair
    protected static final int S_QVAL = 2;       ///< a "quoted" part of val
    protected static final int S_QVALSLASH = 3;  ///< slash-quoted part of val
    protected static final int S_VAL = 4;        ///< val of KV pair

    protected int state = S_TAG; ///< Current parser state
    protected String curKey;     ///< When parsing value, this is current key
    protected String s = "";     ///< Temporary string during parsing
    protected String curTag;     ///< Tag for current KV pairs
    protected KV kv;             ///< Current KV pairs

    public KV info = new KV();      ///< KVs for "info" tag
    public KV common = new KV();    ///< KVs for "common" tag
    public KV charsInfo = new KV(); ///< KVs for "chars" tag

    /// This contains KVs for each page, indexed by the page id.
    public HashMap<Integer, KV> pages = new HashMap<>();

    /// This contains KVs for each char, indexed by the character code.
    protected HashMap<Integer, KV> chars = new HashMap<>();

    protected void storeChar (int id, KV kv)
    {
      chars.put(id, kv);
    }

    public KV getChar (int id)
    {
      return chars.get(id);
    }

    protected void finishTag ()
    {
      if (curTag.equals("info"))
        info = kv;
      else if (curTag.equals("common"))
        common = kv;
      else if (curTag.equals("page"))
        pages.put( kv.getInt("id"), kv );
      else if (curTag.equals("char"))
        storeChar( kv.getInt("id"), kv );
      else if (curTag.equals("kerning"))
        GFU.log("kerning is not currently supported"); //TODO
      else if (curTag.equals("chars"))
        charsInfo = kv;
      else
        GFU.log("Font tag '" + curTag + "' unknown");

      kv = null;
      state = S_TAG;
    }

    protected void push (char c)
    {
      if (state == S_TAG)
      {
        if (c == ' ')
        {
          state = S_KEY;
          curTag = s;
          kv = new KV();
          s = "";
        }
        else if (c == '\n')
        {
          if (s.length() != 0)
            throw new RuntimeException("BMFont parse error near '" + s + "'");
          // .. otherwise just ignore blank lines
        }
        else
        {
          s += c;
        }
      }
      else if (state == S_KEY)
      {
        if (c == '=')
        {
          state = S_VAL;
          curKey = s;
          s = "";
        }
        else
        {
          s += c;
        }
      }
      else if (state == S_VAL)
      {
        if (c == '"')
        {
          state = S_QVAL;
        }
        else if (c == ' ' || c == '\n')
        {
          kv.kv.put(curKey, s);
          s = "";
          curKey = "";
          state = S_KEY;
          if (c == '\n') finishTag();
        }
        else
        {
          s += c;
        }
      }
      else if (state == S_QVAL)
      {
        if (c == '"')
        {
          state = S_VAL;
        }
        else if (c == '\\')
        {
          state = S_QVALSLASH;
        }
        else
        {
          s += c;
        }
      }
      else if (state == S_QVALSLASH)
      {
        switch (c)
        {
          //TODO: Handle \xXX and unicode?
          case 't':
            s += "\t";
            break;
          case 'b':
            s += "\b";
            break;
          case 'n':
            s += "\n";
            break;
          case 'x':
            throw new RuntimeException("\\x escapes are not supported");
          default:
            s += c;
        }
        state = S_QVAL;
      }
    }

    void load (String data)
    {
      String s = data;
      for (int i = 0; i < s.length(); ++i)
        push(s.charAt(i));
      push('\n');
      if (state != S_TAG)
      {
        throw new RuntimeException("Bad format: " + s);
      }

      if (charsInfo.getInt("count", chars.size()) != chars.size())
      {
        System.err.print("Got " + chars.size() + " characters, but expected "
                         + charsInfo.getInt("count"));
      }
    }

    public Iterator<KV> iterator ()
    {
      return chars.values().iterator();
    }
  }


  /** Maximum character value for direct-mapping glyphs.
    *
    * Internally, glyphs might be stored in two different ways: direct-mapped
    * and hash-mapped.  When in direct-mapped mode, a character value is
    * simply an index into glyphMap which is an array.  This allows very
    * cheap, constant-time lookup to find glyph data for a character.
    *
    * The direct-mapped approach works great when the data is dense.  That
    * is, when almost every character code is used.  This is the case if
    * you are only interested in ASCII characters, for example.  All the
    * character codes are in the range of like 0 through 127.  However,
    * when working with Unicode characters, you may have some very large
    * character codes, and there may be huge gaps of unused codes between
    * the used ones; this is potentially very wasteful in terms of memory.
    *
    * Thus, we have the hash-mapped approach, which maps from character
    * code to glyph data via a hashmap.  This is more expensive in terms
    * of runtime, but potentially much more space efficient.
    *
    * MAX_DIRECT determines which mode we end up using.  We default to
    * using direct mapping, but if we ever see a character code larger
    * than MAX_DIRECT, we switch to the hashed mode.
    */
  protected static final int MAX_DIRECT = 255;

  protected GlyphInfo[] glyphMap; ///< Direct-mapped glyphs (see MAX_DIRECT).
  protected HashMap<Character, GlyphInfo> glyphMap2; ///< See MAX_DIRECT.

  /// If the font is missing a character's glyph, this glyph is used.
  protected GlyphInfo replacementGlyph = null;

  public float lineHeight; ///< Space between lines.

  /** Adjustment to all character widths.
    *
    * Greater than zero will make characters farther apart.
    * Less than zero will make characters closer together.
    */
  public float deltaX = 0;

  public int color = Gfx.Color.WHITE; ///< Color tint.

  /** Loads the font from font file(s).
    *
    * The font can be a GaFr ffont, or an AngelCode BMFont in text format
    * with a .bmfont or .fnt extension.  See above.
    *
    * @param fileName the name of the ffont.json/bmfont/fnt file.
    *
    */
  public GFFont (String fileName)
  {
    if (fileName.endsWith(".ffont.json"))
      loadFixedFont(fileName);
    else if (fileName.endsWith(".bmfont") || fileName.endsWith(".fnt"))
      loadBMFont(fileName);
    else
      throw new RuntimeException("Unsupported font format");
  }

  /// Loads a texture; can be overridden to change how it's done.
  protected GFTexture loadTexture (String fileName)
  {
    return new GFTexture(fileName);
  }

  /// Loads a BMFont file.
  protected void loadBMFont (String fileName)
  {
    BMFontLoader loader = new BMFontLoader();
    loader.load(GFU.loadTextFile(fileName));

    HashMap<Integer, GFTexture> pages = new HashMap<>();

    for (KV page : loader.pages.values())
    {
      //System.out.print("PAGE " + page + " " + page.getStr("file"));

      String baseName = "";
      if (fileName.contains("/"))
        baseName = GFU.beforeLast(fileName, "/") + "/";
      pages.put( page.getInt("id"), loadTexture( baseName + page.getStr("file") ) );
    }

    int base = loader.common.getInt("base");

    for (KV ch : loader)
    {
      //System.out.print(ch.getInt("id") + ": " + ch);
      GFTexture tex = pages.get( ch.getInt("page") );

      int id = ch.getInt("id");
      int x = ch.getInt("x");
      int y = ch.getInt("y");
      int width = ch.getInt("width");
      int height = ch.getInt("height");

      int xoffset = ch.getInt("xoffset");
      int yoffset = ch.getInt("yoffset");
      int xadvance = ch.getInt("xadvance");

      int chnl = ch.getInt("chnl");

      if (chnl != 15)
        throw new RuntimeException("BMFont channel-packing not supported");

      GFStamp s = tex.subStamp(x, y, width, height);
      GlyphInfo gi = new GlyphInfo( s );
      gi.deltaX = xadvance - width;

      s.movePinTo(-xoffset,base - yoffset);

      setGlyph((char)id, gi);
    }
  }

  /// Loads a GaFr fixed with font.
  protected void loadFixedFont (String fileName)
  {
    String imgName = GFU.beforeLast(fileName, ".json") + ".png";
    GFTexture t = loadTexture(imgName);

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
      for (char i = 0; i < glyphs.length; ++i)
      {
        setGlyph(i, glyphs[i]);
      }
    }
    else
    {
      for (GFJSON.Value v : json.get("charmap").asArray())
      {
        int ch = v.get(0).asInt();
        int gl = v.get(1).asInt();
        setGlyph((char)ch, glyphs[gl]);
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
    for (int i = 0; i < chars.length(); ++i)
    {
      setGlyph(chars.charAt(i), glyphs[i]);
      if (glyphs[i].height > lineHeight) lineHeight = glyphs[i].height;
    }
    initReplacement();
  }

  /// Sets the stamp to use for a given character.
  public GlyphInfo setGlyph (char c, GFStamp g)
  {
    GlyphInfo gi = new GlyphInfo(g);
    return setGlyph(c, gi);
  }

  /// Sets the stamp to use for a given character.
  public GlyphInfo setGlyph (char c, GlyphInfo gi)
  {
    if (glyphMap2 == null)
    {
      if (c <= MAX_DIRECT)
      {
        // Easy
        if (glyphMap == null)
        {
          int count = c;
          if (count < 127) count = 127;
          glyphMap = new GlyphInfo[count+1];
        }
        else if (c >= glyphMap.length)
        {
          int count = ((int)c)+1;
          count = (count+31)/32*32;
          glyphMap = Arrays.copyOf(glyphMap, count);
        }

        glyphMap[c] = gi;
        return gi;
      }

      // Convert to glyphMap2.
      glyphMap2 = new HashMap<>();
      if (glyphMap != null)
      {
        for (char o = 0; o <= MAX_DIRECT; ++o)
        {
          if (glyphMap[o] == null) continue;
          glyphMap2.put(o, glyphMap[o]);
        }
        glyphMap = null;
        //System.out.println("Newly converted glyphMap2 has size " + glyphMap2.size());
        //System.out.println("(Converted due to character " + (int)c);
      }
    }

    glyphMap2.put(c, gi);
    return gi;
  }

  /// Gets a glyph or null if it doesn't exist.
  public GlyphInfo getGlyphNR (char c)
  {
    GlyphInfo s;
    if (glyphMap2 == null)
    {
      if (c >= glyphMap.length) return null;
      s = glyphMap[c];
    }
    else
    {
      s = glyphMap2.get(c);
    }

    return s;
  }

  /// Gets a glyph (or the replacement glyph).
  public GlyphInfo getGlyph (char c)
  {
    GlyphInfo s;
    if (glyphMap2 == null)
    {
      if (c >= glyphMap.length) return replacementGlyph;
      s = glyphMap[c];
    }
    else
    {
      s = glyphMap2.get(c);
    }

    if (s == null) return replacementGlyph;
    return s;
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
    for (char l = 'A'; l <= 'Z'; ++l)
    {
      char u = (char)(l + 32);
      if (getGlyphNR(l) == null) setGlyph(l, getGlyph(u));
      else if (getGlyphNR(u) == null) setGlyph(u, getGlyph(l));
    }

    return this;
  }

  /// Tries to set up a replacement glyph for missing chracters.
  protected boolean tryReplacement (int c)
  {
    GlyphInfo r = getGlyphNR((char)c);

    if (r == null) return false;

    replacementGlyph = r;
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
      char c = s.charAt(i);
      if (c == '\n')
      {
        y += lineHeight;
        x = xx;
        continue;
      }
      GlyphInfo gi = getGlyph(c);
      GFStamp g = gi.stamp;

      g.color = color;
      g.moveTo(x,y).stamp();
      x += g.width + gi.deltaX + deltaX;
    }
  }

  /** Gets the number of pixels a string would take. */
  public GFPairI measure (String s)
  {
    //TODO: We need a version which accounts for angles.
    //TODO: Refactor to share code with drawing.

    float xx = 0;
    float x = 0;
    float y = 0;
    float maxX = 0;
    if (s.length() > 0) y += lineHeight;
    for (int i = 0; i < s.length(); ++i)
    {
      char c = s.charAt(i);
      if (c == '\n')
      {
        y += lineHeight;
        x = 0;
        continue;
      }
      GlyphInfo gi = getGlyph(c);
      GFStamp g = gi.stamp;
      x += g.width + gi.deltaX + deltaX;
      if (x > maxX) maxX = x;
    }

    return new GFPairI((int)Math.ceil(maxX), (int)Math.ceil(y));
  }
}
