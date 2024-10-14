package GaFr;
import static GaFr.GFM.*;

/** Internal use.
  *
  * This is responsible for actually drawing stamps.
  */
class GFStampManager
{
  private static final int MAX_ITEMS = 512;

  float[] vinfo = new float[MAX_ITEMS*8];
  // stamp info is:
  // pinx,piny
  // sizex,sizey
  // posx,posy
  // sin,cos

  float[] tcs = new float[MAX_ITEMS*4]; // two u and two v coords

  byte[] tex = new byte[MAX_ITEMS];

  int[] colors = new int[MAX_ITEMS];

  GFTexture[] texmap = new GFTexture[MAX_TEXTURES];

  private static final int MAX_TEXTURES = 6;

  int nextTexture = 0;
  int nextIndex = 0;


  GFStampManager ()
  {
    GFN.stampSetup(MAX_ITEMS, MAX_TEXTURES, vinfo, tcs, tex, colors);
  }

  boolean isEmpty ()
  {
    return nextIndex == 0;
  }

  void flush ()
  {
    if (nextIndex == 0) return;
    //GFU.log("flushing ", nextTexture, " textures, and ", nextIndex, " stamps");
    for (int i = 0; i < nextTexture; ++i)
    {
      texmap[i].activateTexture();
    }
    GFN.stampDraw(nextIndex, nextTexture);
    nextIndex = 0;
    for (int i = 0; i < nextTexture; ++i)
    {
      texmap[i].currentIndex = -1;
      texmap[i] = null; // Probably not necessary
    }
    nextTexture = 0;
  }

  void stamp (GFStamp stamp, float x, float y)
  {
    if (nextIndex == MAX_ITEMS) flush();

    if (stamp.texture.currentIndex == -1)
    {
      if (nextTexture >= MAX_TEXTURES) flush();
      texmap[nextTexture] = stamp.texture;
      stamp.texture.currentIndex = nextTexture;
      ++nextTexture;
    }
    tex[nextIndex] = (byte)stamp.texture.currentIndex;

    int off = nextIndex * 8; // vinfo offset
    vinfo[off + 0] = stamp.pinX * stamp.width; //TODO: Make the shader use relative coordinates
    vinfo[off + 1] = stamp.pinY * stamp.height;//      so that we can skip scaling it here?

    vinfo[off + 2] = stamp.width;
    vinfo[off + 3] = stamp.height;
    //GFU.log(stamp.width, " ", stamp.height);

    vinfo[off + 4] = x;
    vinfo[off + 5] = y;

    if (stamp.angle == 0)
    {
      vinfo[off + 6] = 0;
      vinfo[off + 7] = 1;
    }
    else
    {
      vinfo[off + 6] = sinf(stamp.angle);
      vinfo[off + 7] = cosf(stamp.angle);
    }

    tcs[nextIndex * 4 + 0] = stamp.u0;
    tcs[nextIndex * 4 + 1] = stamp.v0;
    tcs[nextIndex * 4 + 2] = stamp.u1;
    tcs[nextIndex * 4 + 3] = stamp.v1;

    colors[nextIndex] = stamp.color;

    ++nextIndex;
  }
}
