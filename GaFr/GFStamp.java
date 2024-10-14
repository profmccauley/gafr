package GaFr;
import static GaFr.GFU.*;
import static GaFr.GFM.*;

/** An image which can be drawn to the screen.
  *
  * A texture is actually memory set aside for pixel data in your graphics
  * hardware.  But the question is... how and where do you want to draw it?
  * That's what this class is about.  It refers to a texture, but actually
  * lets you specify rotations, positions, and so on -- and then lets you
  * actually draw it.
  *
  * The stamp has a "pin" in it.  That is, if the image were on a little
  * piece of paper, the pin is where it's tacked onto the screen.  When
  * you position the image, it's relative to this pin.  When you rotate it,
  * it rotates around the pin.
  *
  * While the colors you see are mostly from the texture, you can specify
  * a color on the stamp; each channel (red, green, blue, and alpha) of
  * the given color are multiplied against the texture color.  This lets
  * you tint images to a degree.  Perhaps more interestingly, you can
  * set the alpha channel to make the image partially transparent.
  */
public class GFStamp
{
  public GFTexture texture; ///< Texture to use to draw this stamp.

  /** Texture coordinates in normalized [0,1] form -- *not* pixels. */
  public float u0, v0, u1, v1;

  public float pinX; ///< The location of the pin, with 0.5 being in the center.
  public float pinY; ///< The location of the pin, with 0.5 being in the center.

  public float origWidth; ///< The original image dimensions, before scaling/resizing.
  public float origHeight; ///< The original image dimensions, before scaling/resizing.

  public float width; ///< The current image dimensions in pixels.
  public float height; ///< The current image dimensions in pixels.

  public float x; ///< The current image location.
  public float y; ///< The current image location.

  protected boolean flippedX; ///< Whether image is currently flipped along X.
  protected boolean flippedY; ///< Whether image is currently flipped along Y.

  /** The current rotation in radians. */
  public float angle;

  public int color = Gfx.Color.WHITE; ///< The current color tint.

  /** Change just the alpha component of the color tint.
    *
    * @param v The alpha value (0 is fully transparent, 1 is fully opaque.
    */
  public GFStamp setAlpha (double v)
  {
    color = Gfx.replaceAlpha(color, clamp255(v*255));
    return this;
  }

  /** Initialze from an existing texture. */
  public GFStamp (GFTexture t)
  {
    texture = t;
    u0 = t.u0 / t.width;
    v0 = t.v0 / t.height;
    u1 = t.u1 / t.width;
    v1 = t.v1 / t.height;
    width = t.width;
    height = t.height;
    origWidth = t.width;
    origHeight = t.height;
  }

  /** Initialize from pixel data. */
  public GFStamp (GFPixels pix)
  {
    this(new GFTexture(pix));
  }

  /** Load image from file. */
  public GFStamp (String fileName)
  {
    this(new GFTexture(fileName));
  }

  /** Change the scale of the image.
    *
    * This set the scale relative to its initial size.  That is,
    * doing rescale(2,0.5) would make the image twice as wide and
    * half as tall as when it was initialized.
    */
  public GFStamp rescale (double x, double y)
  {
    width = (float)(origWidth * x);
    height = (float)(origHeight * y);
    return this;
  }
  /** \overload */
  public GFStamp rescale (float x, float y)
  {
    width =  origWidth * x;
    height = origHeight * y;
    return this;
  }
  /** Change the scale of the image.
    *
    * This is scales both X and Y dimensions equally.
    *
    * @see rescale(double, double)
    */
  public GFStamp rescale (double s)
  {
    rescale(s, s);
    return this;
  }
  /** \overload */
  public GFStamp rescale (float s)
  {
    rescale(s, s);
    return this;
  }

  /** Resizes an image.
    *
    * The end result of this is the same as rescale(), but you give it the
    * actual size you want the result to be in pixels, instead of relative
    * to the initial size.
    */
  public GFStamp resize (double x, double y)
  {
    width = (float)x;
    height = (float)y;
    return this;
  }
  /** \overload */
  public GFStamp resize (double s)
  {
    resize(s, s);
    return this;
  }

  /** Adjusts the color.
    *
    * See the class description for more.
    */
  public GFStamp recolor (int c)
  {
    this.color = c;
    return this;
  }

  /** Adjusts the color.
    *
    * See the class description for more.
    */
  public GFStamp recolor (double r, double g, double b, double a)
  {
    return recolor(Gfx.makeColor(r,g,b,a));
  }

  /** Set the position.
    */
  public GFStamp moveTo (float x, float y)
  {
    this.x = x; this.y = y;
    return this;
  }

  /** Set the position relative to the current position. */
  public GFStamp moveRelative (float x, float y)
  {
    this.x += x;
    this.y += y;
    return this;
  }

  /** Rotate the image relative to the current rotation. */
  public GFStamp rotateRelative (float angle)
  {
    this.angle += angle;
    return this;
  }

  /** Fix the angle range to -/+ PI.
    *
    * After rotating an image, you may end up with an angle like
    * 72*PI.  Sometimes that's confusing.  This method puts it
    * into the range [-PI,+PI).
    *
    * @see GFStamp.fixAngle()
    */
  public GFStamp fixAngleN ()
  {
    this.angle = wrapf(this.angle, -PIf, PIf);
    return this;
  }
  /** Fix the angle range to [0,2PI).
    *
    * After rotating an image, you may end up with an angle like
    * 72*PI.  Sometimes that's confusing.  This method puts it
    * into the range [0, 2PI).
    *
    * @see GFStamp.fixAngleN()
    */
  public GFStamp fixAngle ()
  {
    this.angle = wrapf(this.angle, 0, 2*PIf);
    return this;
  }

  /** Set the rotation in radians. */
  public GFStamp rotate (float angle)
  {
    this.angle = angle;
    return this;
  }

  /** Place the pin in the center of the image. */
  public GFStamp centerPin ()
  {
    pinX = 0.5f;
    pinY = 0.5f;
    return this;
  }
  /** Set the pin position in pixel coordinates. */
  public GFStamp movePinTo (float x, float y)
  {
    pinX = x/width;
    pinY = y/height;
    return this;
  }

  /** Flip the image. */
  public GFStamp flip (boolean x, boolean y)
  {
    if (x) flipX();
    if (y) flipY();
    return this;
  }

  /** Set the flippedness of the image. */
  public GFStamp setFlipX (boolean x)
  {
    if (flippedX != x) flipX();
    return this;
  }

  /** Set the flippedness of the image. */
  public GFStamp setFlipY (boolean y)
  {
    if (flippedY != y) flipY();
    return this;
  }

  /** Checks whether we are flipping along X. */
  public boolean getFlipX ()
  {
    return flippedX;
  }

  /** Checks whether we are flipping along Y. */
  public boolean getFlipY ()
  {
    return flippedY;
  }

  /** Set the flippedness of the image. */
  public GFStamp setFlip (boolean x, boolean y)
  {
    setFlipX(x);
    setFlipY(y);
    return this;
  }

  /** Flip the on the X axis. */
  public GFStamp flipX ()
  {
    float t = u0;
    u0 = u1;
    u1 = t;
    flippedX = !flippedX;
    return this;
  }

  /** Flip the on the Y axis. */
  public GFStamp flipY ()
  {
    float t = v0;
    v0 = v1;
    v1 = t;
    flippedY = !flippedY;
    return this;
  }

  /** Draw the stamp.
    *
    * This should be called from within GFGame.onDraw().
    */
  public void stamp ()
  {
    GFBoot.game._stampManager.stamp(this, x, y);
  }
}
