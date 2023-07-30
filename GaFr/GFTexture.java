package GaFr;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Graphics;

/**
  * A texture.
  *
  * This is basically the pixel data that the graphics hardware can use to
  * draw an image.  However, it's not especially useful by iself.  You
  * are probably more interested in GFStamp, which can actually draw
  * textures.
  *
  * Note that if you change wrap and filter settings, you should probably
  * do it before drawing this texture at all.  If that's a problem, you
  * can 1) flush output between changes, 2) make a duplicate texture with
  * different settings, or 3) create a github issue asking for this
  * limitation to be removed.
  *
  */
public class GFTexture
{
  public static final int MAX_TEXTURE_SIZE = GFN.gl_getParameter(Gl.MAX_TEXTURE_SIZE);

  // Texture coordinates in pixel coordinates
  public int u0, v0, u1, v1;

  public int width, height;

  public int wrapS = Gl.CLAMP_TO_EDGE;
  public int wrapT = Gl.CLAMP_TO_EDGE;
  public int minFilter = Gl.NEAREST;
  public int magFilter = Gl.NEAREST;

  /** Internal use */
  public int currentIndex = -1;

  /** Loads a texture.
    *
    * The file can probably be a png, jpg, gif, or webm.  png is a solid
    * choice since it's widely supported and it has transparency.  jpg
    * is good for large stuff with no transparency (like backgrounds).
    *
    * If inColor and outColor are not the same, it will change all pixels of
    * inColor to outColor.  This is largely intended to be used for adding
    * tranparency.  For example, you could turn magic pink into transparent
    * black with Gfx.Color.MAGIC_PINK 0xFFFF00FF and 0x00000000.  This
    * feature should probably be migrated to a GFPixels class eventually.
    */
  public GFTexture (String fileName, int inColor, int outColor)
  {
    loadTexture(fileName, inColor, outColor);
  }

  /// \overload
  public GFTexture (String fileName)
  {
    this(fileName, 0, 0);
  }

  /// \overload
  public GFTexture (GFPixels img)
  {
    loadTexture(img, 0, 0);
  }

  /// Internal use.
  protected void loadTexture (GFPixels img, int inColor, int outColor)
  {
    if (inColor != outColor)
      img = img.replaceColor(inColor, outColor);

    u0 = 0; v0 = 0;
    u1 = img.width; v1 = img.height;

    width = img.width;
    height = img.height;

    if (width > MAX_TEXTURE_SIZE || height > MAX_TEXTURE_SIZE)
    {
      // This will result in a loss of quality, but oh well!
      int ww = Math.min(img.width, MAX_TEXTURE_SIZE);
      int hh = Math.min(img.height, MAX_TEXTURE_SIZE);
      GFU.log("Warning: Texture size ",img.width,"x",img.height,
              " is too large; rescaling to ",ww,"x",hh,".");
      //TODO: The above should probably be GFU.warn().
      BufferedImage src = img.toBufferedImage();
      BufferedImage scaled = new BufferedImage(ww, hh, src.getType());
      Graphics g = scaled.createGraphics();
      g.drawImage(src, 0, 0, ww, hh, null);
      g.dispose();
      img = new GFPixels(scaled);
    }

    GFN.gl_createTexture(this);

    // We always use unit 7 for loading/etc.
    //FIXME: We should probably be using 0, if anything
    GFN.gl_activeTexture(Gl.TEXTURE0 + 7);
    GFN.gl_bindTexture(Gl.TEXTURE_2D, this);

    GFN.gl_texImage2D(0, Gl.RGBA, img.width, img.height, Gl.RGBA, img.pix, 0);
  }

  /** Generate mipmaps for this texture.
    *
    * This is required if you want to use mipmap min/mag filters.
    */
  public void generateMipmap ()
  {
    GFN.gl_activeTexture(Gl.TEXTURE0 + 7);
    GFN.gl_bindTexture(Gl.TEXTURE_2D, this);

    GFN.gl_generateMipmap(Gl.TEXTURE_2D);
  }

  /// Internal use.
  protected void loadTexture (String fileName, int inColor, int outColor)
  {
    loadTexture( new GFPixels(fileName), inColor, outColor );
    // The code that used to be here is somewhat more efficient in cases
    // where a texture needs to be resized to be loaded.  It has been
    // removed to avoid (mostly) duplicate code.  It could be brought
    // back if performance demanded it.
  }

  /** Split texture into tiles.
    *
    * This splits the texture into numX by numY tiles.
    * If there's padding around the edges, you can specify it.
    * It returns a two dimensional array of GFStamps.
    *
    * Note: This function ignores the original texture coordinates.
    */
  public GFStamp[][] splitIntoTiles2D (int numX, int numY, int padL, int padT, int padR, int padB)
  {
    int sx = (width-padL-padR) / numX;
    int sy = (height-padT-padB) / numY;
    return splitIntoTilesBySize2D( sx, sy,
                                   padL, padT, padR, padB );
  }
  /// \overload
  public GFStamp[][] splitIntoTiles2D (int numX, int numY)
  {
    return splitIntoTiles2D(numX, numY, 0, 0, 0, 0);
  }

  /** Split a texture into tiles.
    *
    * This is similar to splitIntoTiles2D(), except that instead of giving it
    * the number of tiles to split into,  you give it the size of the tiles.
    *
    * @see GFTexture#splitIntoTiles2D(int, int, int, int, int, int)
    *
    */
  public GFStamp[][] splitIntoTilesBySize2D (int sizeX, int sizeY, int padL, int padT, int padR, int padB)
  {
    int w = width - padL - padR;
    int h = height - padT - padB;
    int cw = sizeX;
    int ch = sizeY;
    int numX = w/cw;
    int numY = h/ch;
    GFStamp[][] r = new GFStamp[numX][numY];
    for (int y = 0; y < numY; ++y)
    {
      int yy = padT + y * ch;
      for (int x = 0; x < numX; ++x)
      {
        int xx = padL + x * cw;

        GFStamp s = new GFStamp(this);
        r[x][y] = s;
        s.u0 = xx / (float)width;
        s.v0 = yy / (float)height;
        s.u1 = s.u0 + cw / (float)width;
        s.v1 = s.v0 + ch / (float)height;
        s.width = cw;
        s.height = ch;
        s.origWidth = cw;
        s.origHeight = ch;
      }
    }
    return r;
  }
  /// \overload
  public GFStamp[][] splitIntoTilesBySize2D (int sizeX, int sizeY)
  {
    return splitIntoTilesBySize2D(sizeX, sizeY, 0, 0, 0, 0);
  }

  /** Split texture into tiles.
    *
    * It's the same as splitIntoTiles2D() except it returns a 1D array.
    */
  public GFStamp[] splitIntoTiles (int numX, int numY, int padL, int padT, int padR, int padB)
  {
    int sx = (width-padL-padR) / numX;
    int sy = (height-padT-padB) / numY;
    return splitIntoTilesBySize( sx, sy,
                                 padL, padT, padR, padB );
  }
  /// \overload
  public GFStamp[] splitIntoTiles (int numX, int numY)
  {
    return splitIntoTiles(numX, numY, 0, 0, 0, 0);
  }

  /** Split texture into tiles.
    *
    * It's the same as splitIntoTilesBySize2D() except it returns a 1D array.
    */
  public GFStamp[] splitIntoTilesBySize (int sizeX, int sizeY, int padL, int padT, int padR, int padB)
  {
    GFStamp[][] t = splitIntoTilesBySize2D(sizeX, sizeY, padL, padT, padR, padB);
    int numX = t.length;
    int numY = t[0].length;
    GFStamp[] r = new GFStamp[numX * numY];
    for (int y = 0; y < numY; ++y)
      for (int x = 0; x < numX; ++x)
        r[x+y*numX] = t[x][y];
    return r;
  }
  /// \overload
  public GFStamp[] splitIntoTilesBySize (int sizeX, int sizeY)
  {
    return splitIntoTilesBySize(sizeX, sizeY, 0, 0, 0, 0);
  }

  /** Creates a stamp based on a region of this texture.
    */
  public GFStamp subStamp (int x, int y, int w, int h)
  {
    GFStamp s = new GFStamp(this);
    s.u0 = x / (float)width;
    s.v0 = y / (float)height;
    s.u1 = s.u0 + w / (float)width;
    s.v1 = s.v0 + h / (float)height;
    s.width = w;
    s.height = h;
    s.origWidth = w;
    s.origHeight = h;
    return s;
  }

  /** Internal use */
  public void activateTexture ()
  {
    assert currentIndex >= 0;
    GFN.gl_activeTexture(Gl.TEXTURE0 + currentIndex);
    GFN.gl_bindTexture(Gl.TEXTURE_2D, this);

    GFN.gl_texParameteri(Gl.TEXTURE_2D, Gl.TEXTURE_WRAP_S, wrapS);
    GFN.gl_texParameteri(Gl.TEXTURE_2D, Gl.TEXTURE_WRAP_T, wrapT);
    GFN.gl_texParameteri(Gl.TEXTURE_2D, Gl.TEXTURE_MIN_FILTER, minFilter);
    GFN.gl_texParameteri(Gl.TEXTURE_2D, Gl.TEXTURE_MAG_FILTER, magFilter);
    //GFU.log("activated texture ", currentIndex);
  }
}
