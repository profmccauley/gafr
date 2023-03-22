package GaFr;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.Arrays;

/**
  * Raw pixel data.
  *
  * This class encapsulates raw pixel data.  Each pixel is an int -- 32 bits
  * containing red, green, blue, and alpha values.
  *
  * Many of the functions mutate pixels and return the same object in order
  * to allow chaining calls.
  */
public class GFPixels
{
  /// Image width; you probably don't want to set this directly.
  public int width;

  /// Image height; you probably don't want to set this directly.
  public int height;

  /// Pixel data.
  public int[] pix;

  /// Blank/empty constructor.
  public GFPixels (int w, int h)
  {
    pix = new int[w*h];
    width = w;
    height = h;
  }

  /// Cloning constructor.
  public GFPixels (GFPixels original)
  {
    width = original.width;
    height = original.height;
    pix = original.pix.clone();
  }

  /// Construct from image file.
  public GFPixels (String fileName)
  {
    fileName = GFBoot.resolvePath(fileName);
    //System.out.print("loading " + fileName);

    BufferedImage img;
    try
    {
      // See notes in GFBoot, but this doesn't work reliably...
      //img = ImageIO.read(new File(fileName));

      // .. so we do it this way instead.
      img = ImageIO.read(GFU.loadAsStream(fileName));
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }

    width = img.getWidth();
    height = img.getHeight();

    pix = img.getRGB(0,0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
  }

  /// Construct from a BufferedImage.
  public GFPixels (BufferedImage img)
  {
    width = img.getWidth();
    height = img.getHeight();
    pix = img.getRGB(0,0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
  }

  /**
    * Replaces one color with another.
    *
    * Return value is for chaining.
    */
  public GFPixels replaceColor (int replaceThis, int withThis)
  {
    if (replaceThis == withThis) return this;
    for (int i = 0; i < pix.length; ++i)
    {
      if (pix[i] == replaceThis) pix[i] = withThis;
    }
    return this;
  }

  /**
    * Clears entire image to given color.
    *
    * Return value is for chaining.
    */
  public GFPixels fill (int color)
  {
    for (int i = 0; i < pix.length; ++i)
    {
      pix[i] = color;
    }
    return this;
  }

  /**
    * Fills a rectangle with a given color
    *
    * Return value is for chaining.
    */
  public GFPixels fill (int x, int y, int w, int h, int color)
  {
    for (int yy = y; yy < (y+h); ++yy)
    {
      int start = getStride() * yy + x;
      Arrays.fill(pix, start, start+w, color);
    }
    return this;
  }

  /**
    * Replaces the alpha of all pixels.
    *
    * Return value is for chaining.
    */
  public GFPixels replaceAlpha (int alpha)
  {
    for (int i = 0; i < pix.length; ++i)
    {
      pix[i] = Gfx.replaceAlpha(pix[i], alpha);
    }
    return this;
  }

  /**
    * Converts to grayscale.
    *
    * Return value is for chaining.
    */
  public GFPixels toGrayscale ()
  {
    for (int i = 0; i < pix.length; ++i)
    {
      int col = pix[i];
      int r = Gfx.getRed(col);
      int g = Gfx.getGreen(col);
      int b = Gfx.getBlue(col);
      int a = Gfx.getAlpha(col);

      int av = ((r+g+b) / 3) & 0xff;

      col = (a << 24) | (av << 16) | (av << 8) | av;
      pix[i] = col;
    }
    return this;
  }

  /**
    * Sets saturation of image.
    *
    * Return value is for chaining.
    */
  public GFPixels setSaturation (double saturation)
  {
    double inv = 1-saturation;
    for (int i = 0; i < pix.length; ++i)
    {
      int col = pix[i];
      int r = Gfx.getRed(col);
      int g = Gfx.getGreen(col);
      int b = Gfx.getBlue(col);
      int a = Gfx.getAlpha(col);

      int av = ((r+g+b) / 3) & 0xff;

      r = (int)(r * saturation + av * inv);
      g = (int)(g * saturation + av * inv);
      b = (int)(b * saturation + av * inv);

      pix[i] = Gfx.makeColor(r,g,b,a);
    }
    return this;
  }

  /**
    * Get a single pixel.
    */
  public int get (int x, int y, int defaultColor)
  {
    if (x < 0 || y < 0 || x >= width || y >= height)
      return defaultColor;

    return pix[x+y*getStride()];
  }

  /**
    * Get a single pixel.
    */
  public int get (int x, int y)
  {
    return get(x,y,0);
  }

  /**
    * Set a single pixel.
    *
    * Return value is for chaining.
    */
  public GFPixels set (int x, int y, int value)
  {
    if (x < 0 || y < 0 || x >= width || y >= height)
      return this;

    pix[x+y*getStride()] = value;
    return this;
  }

  /**
    * Get the image stride (length between a pixel and the pixel below it).
    */
  private int getStride ()
  {
    return width;
  }

  /**
    * Convert these pixels to a BufferedImage.
    */
  public BufferedImage toBufferedImage ()
  {
    BufferedImage bi = new BufferedImage(width, height,
                                         BufferedImage.TYPE_INT_ARGB);
    bi.setRGB(0,0, width,height, pix, 0, getStride());
    return bi;
  }

  /**
    * Crop a portion out of this image.
    *
    * Returns a new image.
    */
  public GFPixels cropped (int x, int y, int w, int h)
  {
    GFPixels n = new GFPixels(w, h);
    n.pasteFrom(this, x, y, w, h, 0, 0);
    return n;
  }

  /**
    * Copy a portion of one GFPixels into another.
    *
    * Returns this object for chaining purposes.
    */
  public GFPixels pasteFrom (GFPixels src, int srcx, int srcy, int w, int h, int dstx, int dsty)
  {
    GFPixels dst = this;
    if (srcx < 0)
    {
      w += srcx;
      dstx -= srcx;
      srcx = 0;
    }
    if (srcx+w > src.width)
    {
      w = src.width - srcx;
    }

    if (srcy < 0)
    {
      h += srcy;
      dsty -= srcy;
      srcy = 0;
    }
    if (srcy+h > src.height)
    {
      h = src.height - srcy;
    }

    if (dstx < 0)
    {
      w += dstx;
      srcx += dstx;
      dstx = 0;
    }
    if (dstx+w > dst.width)
    {
      w = dst.width - dstx;
    }

    if (dsty < 0)
    {
      h += dsty;
      srcy += dsty;
      dsty = 0;
    }
    if (dsty+h > dst.height)
    {
      h = dst.height - dsty;
    }

    for (int yy = 0; yy < h; ++yy)
    {
      int sp = (yy+srcy) * src.getStride() + srcx;
      int dp = (yy+dsty) * dst.getStride() + dstx;
      System.arraycopy(src.pix, sp, dst.pix, dp, w);
    }

    return this;
  }

  /** Split image into tiles.
    *
    * This splits an image into numX by numY tiles.
    * If there's padding around the edges, you can specify it.
    * It returns a two dimensional array of new GFPixels.
    *
    * This is similar to a function in GFTexture, but that function just
    * makes a bunch of GFStamps which refer to different portions of the
    * same GFTexture.  This actually creates new little GFPixels objects
    * out of a larger GFPixels.
    *
    * @see GFTexture#splitIntoTiles2D(int, int, int, int, int, int)
    */
  public GFPixels[][] splitIntoTiles2D (int numX, int numY, int padL, int padT, int padR, int padB)
  {
    int sx = (width-padL-padR) / numX;
    int sy = (height-padT-padB) / numY;
    return splitIntoTilesBySize2D( sx, sy,
                                   padL, padT, padR, padB );
  }
  /// \overload
  public GFPixels[][] splitIntoTiles2D (int numX, int numY)
  {
    return splitIntoTiles2D(numX, numY, 0, 0, 0, 0);
  }

  /** Split an image into tiles.
    *
    * This is similar to splitIntoTiles2D(), except that instead of giving it
    * the number of tiles to split into,  you give it the size of the tiles.
    *
    * @see GFPixels#splitIntoTiles2D(int, int, int, int, int, int)
    *
    */
  public GFPixels[][] splitIntoTilesBySize2D (int sizeX, int sizeY, int padL, int padT, int padR, int padB)
  {
    int w = width - padL - padR;
    int h = height - padT - padB;
    int cw = sizeX;
    int ch = sizeY;
    int numX = w/cw;
    int numY = h/ch;
    GFPixels[][] r = new GFPixels[numX][numY];
    for (int y = 0; y < numY; ++y)
    {
      int yy = padT + y * ch;
      for (int x = 0; x < numX; ++x)
      {
        int xx = padL + x * cw;

        GFPixels s = cropped(xx, yy, cw, ch);
        r[x][y] = s;
      }
    }
    return r;
  }
  /// \overload
  public GFPixels[][] splitIntoTilesBySize2D (int sizeX, int sizeY)
  {
    return splitIntoTilesBySize2D(sizeX, sizeY, 0, 0, 0, 0);
  }

  /** Split image into tiles.
    *
    * It's the same as splitIntoTiles2D() except it returns a 1D array.
    */
  public GFPixels[] splitIntoTiles (int numX, int numY, int padL, int padT, int padR, int padB)
  {
    int sx = (width-padL-padR) / numX;
    int sy = (height-padT-padB) / numY;
    return splitIntoTilesBySize( sx, sy,
                                 padL, padT, padR, padB );
  }
  /// \overload
  public GFPixels[] splitIntoTiles (int numX, int numY)
  {
    return splitIntoTiles(numX, numY, 0, 0, 0, 0);
  }

  /** Split image into tiles.
    *
    * It's the same as splitIntoTilesBySize2D() except it returns a 1D array.
    */
  public GFPixels[] splitIntoTilesBySize (int sizeX, int sizeY, int padL, int padT, int padR, int padB)
  {
    GFPixels[][] t = splitIntoTilesBySize2D(sizeX, sizeY, padL, padT, padR, padB);
    int numX = t.length;
    int numY = t[0].length;
    GFPixels[] r = new GFPixels[numX * numY];
    for (int y = 0; y < numY; ++y)
      for (int x = 0; x < numX; ++x)
        r[x+y*numX] = t[x][y];
    return r;
  }
  /// \overload
  public GFPixels[] splitIntoTilesBySize (int sizeX, int sizeY)
  {
    return splitIntoTilesBySize(sizeX, sizeY, 0, 0, 0, 0);
  }
}
