package GaFr;
import java.util.ArrayList;
import java.util.HashMap;
import GaFr.util.KV;

/** A Texture atlas class.
  *
  * This class loads texture atlases which can be created with a number of
  * different tools.  Such atlases are essentially an image file (like a
  * .png) and a metadata file which contains info about the contents of the
  * image.  The actual contents are basically a bunch of smaller images which
  * have all been smashed together into one larger one.  The metadata
  * includes names for each little sub-image region, along with the
  * coordinates where it is in the big texture, and so on.  When loading
  * an atlas, we use this information to create GFStamps for each region.
  *
  * There are a number of different file formats for atlases, which are used
  * by different tools, game engines, and so on.  GaFr currently has support
  * for the one defined by Esoteric Software's Spine tool.  The same (or a
  * similar) format has support in libGDX and other tools as well.  It has
  * good support, generally, and has at least one nice feature that some
  * other formats do not.  GaFr's support may not (yet?) be perfect, but
  * the aim is for it to work pretty well.  Information on the file format
  * and various tools for creating atlases can be found below.
  *
  * Creating such texture atlases has several benefits.  Some of these are:
  * * They may take less code to write!  You can access all of the individual
  *   sub-images from the main atlas, without having to have created a bunch
  *   of separate GFStamps fields and loaded them all using their correct
  *   filenames.
  * * Loading can be more efficient.  It's generally more efficient to load
  *   a smaller number of larger files than a larger number of smaller files.
  *   by packing a bunch of small images into one big image, we do that.
  * * Sometimes sprite sheets are distributed this way.  Sometimes you are
  *   just given an image with regularly sized cells which you can use the
  *   spliting functions to break into separate stamps.  But sometimes you
  *   are also given a metadata file.  Sometimes this is just nice -- you
  *   often then get meaningful names for things instead of just tile
  *   coordinates.  Sometimes it's basically necessary: the individual
  *   sub-images are not the same size, so splitting into a regular grid
  *   does not help -- you need info about where things are.
  * * Texture atlases can make drawing faster.  Switching between images when
  *   drawing can actually take time.  But all the little sub-images on an
  *   atlas are technically the same image.  Switching which *part* you
  *   draw is faster.  If you are drawing a large number of items, it's
  *   possible there will actually be an appreciable performance improvement.
  *
  * As mentioned above, we support the same file format used by libGDX.
  * Therefore, we can make use of the libGDX Texture Packer tool.  This is
  * a command line tool written in Java which packs all the items in a
  * directory (and subdirectories).  You probably want to download the
  * "standalone nightly" version which doesn't require a full libGDX
  * installation to run.  A link to it and documentation can be found at:
  * https://libgdx.com/wiki/tools/texture-packer
  *
  * There is also a graphical version of the libGDX texture packer, which
  * can be found at: https://github.com/crashinvaders/gdx-texture-packer-gui
  *
  * Various other tools also support this format.  Free Texture Packer is
  * a nice one because it's just a web page -- there's nothing to install.
  * Before exporting, change the Format (just above the Export button) to
  * "Spine".  You can find this tool at:
  * https://free-tex-packer.com/app/
  *
  * A very nice feature of the Spine format (which Free Texture Packer does
  * not support) is indexing.  Essentially, you can have multiple sub-images
  * in your atlas with the same name, but different integer indices.  All the
  * sub-images with the same name are loaded into an ArrayList in GaFr, and
  * you can access them by their index.  This is really nice for doing things
  * like animations -- you can just cycle through the indices rather than
  * have to assemble a list of all the frames of the animation in code by
  * hand.  However, unfortunately, not all pack tools support this feature.
  * To account for this, GaFr's TextureAtlas class supports "autoindexing".
  * When this is turned on, sub-image names which end with a number use that
  * number as their index.  For example, if you have the files "idle1.png",
  * "idle2.png", and "idle3.png", and you use Free Texture Packer to put
  * them in an atlas, it will have regions named idle1, idle2, and idle3.
  * TextureAtlas can index these regions for you automatically.  Note,
  * that there is no index 0 in this example, which may be annoying if you
  * want to cycle through them.  You could fix the file or region names by
  * hand, but sometimes it will be sufficient to just call the
  * removeEmptyIndices() method to remove the empty ones and renumber the
  * remaining ones.
  *
  * Another nice feature of the Spine format is that it has native support
  * for "nine patches".  GaFr does not currently have support for this
  * (please add it!).
  *
  * The Spine atlas format is documented here:
  * http://en.esotericsoftware.com/spine-atlas-format
  */
public class TextureAtlas
{
  /// If true, infer index numbers from filenames.
  public boolean autoIndex = false;

  /// A path prefix to be used to help locate image files.
  public String prefix = null;

  protected boolean needToLoadStamps = false;

  /// All of the regions indexed by name.
  public final HashMap<String, Region> regions = new HashMap<>();

  // All of the pages indexed by filename (multiple regions per page).
  public final HashMap<String, Page> pages = new HashMap<>();

  protected static class FrameInfo
  {
    public String originalName;
    public String name;
    public int index;
  }

  /// Remove image filename extensions.
  protected String stripExt (String f)
  {
    if (f.indexOf(".") == -1) return f;
    int sp = f.lastIndexOf(".");
    String ext = f.substring(sp+1).toLowerCase();
    if (ext.equals("png") || ext.equals("gif") || ext.equals("jpg"))
      return f.substring(0, sp);
    return f;
  }

  /// Infer indices from filenames.
  protected FrameInfo getFrameInfo (String f)
  {
    String ff = f; // Should already be stripped stripExt(f);
    String ns = "";
    while (ff.length() > 0)
    {
      char c = ff.charAt(ff.length() - 1);
      if (c < '0' || c > '9')
      {
        break;
      }

      ff = ff.substring(0, ff.length() - 1);
      ns = c + ns;
    }

    while (ff.endsWith("/") || ff.endsWith("\\") || ff.endsWith(" "))
      ff = ff.substring(0, ff.length() - 1);
    while (ff.startsWith("/") || ff.startsWith("\\") || ff.startsWith(" "))
      ff = ff.substring(1);

    FrameInfo fi = new FrameInfo();
    fi.originalName = f;
    fi.name = ff;
    fi.index = -1;

    if (ns.length() > 0)
    {
      fi.index = Integer.parseInt(ns);
    }

    return fi;
  }

  /// Superclass for classes that store KV pairs.
  public class KVData extends KV
  {
    /// Get Left/Right/Top/Bottom.
    public LRTB getLRTB (String key)
    {
      int[] d = getInts(key);
      if (d == null || d.length == 0) return null;
      LRTB r = new LRTB();
      if (d.length > 0) r.left = d[0];
      if (d.length > 1) r.right = d[1];
      if (d.length > 2) r.top = d[2];
      if (d.length > 3) r.bottom = d[3];
      return r;
    }
  }

  /** Parse a Spine atlas scaling filter name.
    *
    * Given one of the spine minification/maxification filter names like
    * "linear", returns the numeric value that the graphics subsystem
    * actually uses to represent that filter.
    */
  protected int parseFilter (String s)
  {
    if (s == null) return Gl.NEAREST;
    s = s.trim().toLowerCase();
    if (s.equals("nearest")) return Gl.NEAREST;
    if (s.equals("linear")) return Gl.LINEAR;
    if (s.equals("mipmap")) return Gl.LINEAR_MIPMAP_LINEAR;
    if (s.equals("mipmapnearestnearest")) return Gl.NEAREST_MIPMAP_NEAREST;
    if (s.equals("mipmaplinearnearest")) return Gl.LINEAR_MIPMAP_NEAREST;
    if (s.equals("mipmapnearestlinear")) return Gl.NEAREST_MIPMAP_LINEAR;
    if (s.equals("mipmaplinearlinear")) return Gl.LINEAR_MIPMAP_LINEAR;
    if (s.equals("")) return Gl.NEAREST;
    throw new RuntimeException("Unknown filter '" + s + "'");
  }

  /** Information about a Page.
    *
    * A Page is a single image, and this type contains information about the
    * properties associated with that image.  Multiple Regions refer to pixel
    * data held by a single Page.
    *
    * See, e.g., the Spine atlas file format documentation for what many of
    * the fields mean.
    */
  public class Page extends KVData
  {
    protected boolean finished = false;

    public GFPixels pix;
    public GFTexture tex;
    public String name;
    public int w;
    public int h;

    public int minFilter, magFilter;
    public boolean repeatX;
    public boolean repeatY;

    public boolean pma; // pre-multiplied alpha

    public String pixelFormat;

    void finish ()
    {
      if (finished) return;
      finished = true;

      int[] size = getInts("size");
      w = size[0]; h = size[1];

      pixelFormat = getStr("format", "RGBA8888");

      String s = getStr("filter", "").toLowerCase().trim();
      if (s.length() > 0)
      {
        String[] filters = s.split(",");
        minFilter = parseFilter(filters[0]);
        magFilter = parseFilter(filters[filters.length-1]);
      }
      else
      {
        minFilter = Gl.NEAREST;
        magFilter = Gl.NEAREST;
      }

      s = getStr("repeat", "").toLowerCase();
      if (s.contains("x")) repeatX = true;
      if (s.contains("y")) repeatY = true;

      pma = getBool("pma", false);
    }
  }

  /** Loads a texture atlas file.
    *
    * This is the same as loadAtlas(filename, true), plus it automatically
    * tries to load the stamps after.
    *
    * @see TextureAtlas#loadAtlas(String, boolean)
    */
  public TextureAtlas loadAtlas (String filename)
  {
    loadAtlas(filename, true);
    loadStamps();
    return this;
  }

  /** Loads a texture atlas file.
    *
    * This loads an atlas file.  You can call this more than once passing in
    * different files, and they'll all get merged into this TextureAtlas
    * instance.  This is useful because many texture packing tools seem to
    * generate separate atlas files for each texture... even if the optimal
    * packing has resulted in related things being in different textures.
    */
  public TextureAtlas loadAtlas (String filename, boolean setPrefix)
  {
    int lastSlash = filename.lastIndexOf("/");

    if (setPrefix)
    {
      if (lastSlash != -1)
        prefix = filename.substring(0, lastSlash+1);
      else
        prefix = null;
    }

    String data = GFU.loadTextFile(filename);
    loadAtlasData(data);
    return this;
  }

  /// Parses an atlas from String data.
  public void loadAtlasData (String data)
  {
    String[] pages = data.trim().split("\n\n");
    for (String p : pages)
      parsePage(p);

    for (Page p : this.pages.values())
      p.finish();

    for (Region r : regions.values())
      r.finish();

    needToLoadStamps = true;
  }

  /** Sets up an indexed region.
    *
    * After we've read an entire region, we know if it has an index, so we
    * can take care of linking it with its siblings.  Do that here.
    */
  protected void applyRegionIndex (Region r)
  {
    if (r == null) return;

    int index2 = r.getInt("index", r.index);
    if (r.index == -1)
      r.index = index2;

    if (r.index == -1)
    {
      if (r.groupHead != r)
      {
        throw new RuntimeException("Region "+r.name+" has group but no index");
      }
      return;
    }

    if (index2 != r.index)
      throw new RuntimeException("Autoindex and index disagree!");

    Region head = r.groupHead;
    if (head.indexMap == null)
      head.indexMap = new ArrayList<>();
    r.indexMap = head.indexMap;

    GFU.ensureIndex(r.indexMap, r.index);
    r.indexMap.set(r.index, r);
  }

  /// Parse one page worth of an atlas file.
  protected void parsePage (String data)
  {
    String[] lines = data.trim().split("\n");
    Page p = null;
    Region r = null;
    for (int ln = 0; ln < lines.length; ++ln)
    {
      String l = lines[ln].trim();
      if (l.length() == 0) continue;
      int colon = l.indexOf(':');
      if (colon == -1)
      {
        if (p == null)
        {
          p = new Page();
          p.name = l;
          if (pages.containsKey(l))
            throw new RuntimeException("Page '" + l + "' already exists");
          pages.put(p.name, p);
        }
        else
        {
          applyRegionIndex(r);
          r = new Region();
          r.page = p;
          l = stripExt(l);
          if (autoIndex)
          {
            FrameInfo fi = getFrameInfo(l);
            l = fi.name; // Maybe only use this if there's >1 index?
            r.index = fi.index;
          }
          r.name = l;
          Region other = regions.get(l);
          if (other == null)
          {
            regions.put(r.name, r);
            r.groupHead = r;
          }
          else
          {
            r.groupHead = other;
          }
        }

        continue;
      }

      String k = l.substring(0, colon).trim();
      String v = l.substring(colon+1, l.length()).trim();

      KVData t = r;
      if (t == null) t = p;

      if (t.kv.containsKey(k))
        throw new RuntimeException("Key '" + k + "' already exists");
      t.kv.put(k, v);
    }

    applyRegionIndex(r);
  }

  /** Load image pixel data.
    *
    * This can be overridden to change loading behavior.
    */
  protected GFPixels loadPixels (String filenamePrefix, Page p)
  {
    GFPixels pix = new GFPixels(filenamePrefix + p.name);
    return pix;
  }

  /** Actually create a texture.
    *
    * This can be overridden to change loading behavior.
    */
  protected GFTexture loadTexture (GFPixels pix, Page p)
  {
    GFTexture tex = new GFTexture(pix);
    return tex;
  }

  /// Load the stamp to be used by the given region.
  protected GFStamp loadStamp (Region r)
  {
    GFStamp s = new GFStamp(r.page.tex);
    s.width = r.w;
    s.height = r.h;
    s.origWidth = r.w;
    s.origHeight = r.h;
    s.u0 = r.x         / (float)r.page.tex.width;
    s.u1 = (r.x + r.w) / (float)r.page.tex.width;
    s.v0 = r.y         / (float)r.page.tex.height;
    s.v1 = (r.y + r.h) / (float)r.page.tex.height;

    int offR = r.origW - r.w - r.offX;
    int offT = r.origH - r.h - r.offY;

    //s.movePinTo(r.offX, offT);
    //s.movePinTo(0, offT);
    s.movePinTo(0, 0);
    // Should probably actually rotate the pixels, and not allow
    // non-90-degree rotations?
    s.angle = (float)r.rotate;

    return s;
  }

  /** Loads textures/stamps after parsing.
    *
    * This is the same as loadStamps(null).
    *
    * @see TextureAtlas#loadStamps(String)
    */
  public TextureAtlas loadStamps ()
  {
    return loadStamps(null);
  }

  /** Loads textures/stamps after parsing.
    *
    * This loads textures based on what the atlas file said.  If you pass
    * a prefix, it is used to help locate the image files.  If you pass
    * null, and the instance's .prefix has been set (possibly automatically
    * when loading the atlas), it will be used.
    *
    * @see TextureAtlas#loadStamps()
    */
  public TextureAtlas loadStamps (String filenamePrefix)
  {
    if (! needToLoadStamps) return this;

    if (filenamePrefix == null)
      filenamePrefix = prefix;

    for (Page page : pages.values())
    {
      if (page.pix == null)
        page.pix = loadPixels(filenamePrefix, page);
      if (page.tex == null)
        page.tex = loadTexture(page.pix, page);
    }

    //System.out.print("ls regions " + regions.size());
    for (Region group : regions.values())
    {
      //System.out.print("process " + group.name);
      if (group.indexMap == null)
      {
        group.stamp = loadStamp(group);
        continue;
      }

      //System.out.print("group " + group.name);
      for (Region r : group.indexMap)
      {
        if (r.stamp != null) continue;
        //System.out.print("member " + r.index + ": " + r);
        GFStamp s = loadStamp(r);
        r.stamp = s;
      }
    }

    needToLoadStamps = false;
    postLoad();
    return this;
  }

  /** Removes empty entries from indexed regions.
    *
    * It's possible that you will end up with an indexed region where there
    * are unsed indices.  For example, perhaps you are using autoindexing, and
    * didn't like frame 3 of some animation, so you just deleted the
    * badguy_standing_03.png file -- but didn't renumber the rest of the
    * frames.  Now there's a gap between 2 and 4.  This function will just
    * renumber things so there's no gap.
    */
  public TextureAtlas removeEmptyIndices ()
  {
    for (Region r : regions.values())
      removeEmpty(r.indexMap);

    return this;
  }

  /// Helper for removeEmptyIndices().
  protected void removeEmpty (ArrayList<Region> list)
  {
    int j = 0;
    for (int i = 0; i < list.size(); ++i)
    {
      Region r = list.get(i);
      if (r == null) continue;

      r.index = j;
      list.set( j, r );
      ++j;
    }
    list.subList(j, list.size()).clear();
  }

  /** A do-nothing constructor.
    *
    * If you create an instance this way, you'll want to load the atlas
    * file(s) manually.
    */
  public TextureAtlas ()
  {
  }

  /// Called after loading.  Can be overridden to augment behavior.
  protected void postLoad ()
  {
  }

  /** Create an instance and initialize it using given atlas file.
    *
    * @see TextureAtlas#TextureAtlas(String, boolean)
    */
  public TextureAtlas (String filename)
  {
    loadAtlas(filename);
  }

  /** Create an instance and initialize it using given atlas file.
    *
    * This version of the constructor lets you set whether to enable
    * automatic indexing (based on the region name).
    */
  public TextureAtlas (String filename, boolean autoIndex)
  {
    this.autoIndex = autoIndex;
    loadAtlas(filename);
  }

  /// A combination of Left, Right, Top and Bottom.
  public static class LRTB
  {
    public int left;
    public int right;
    public int top;
    public int bottom;
  }

  /** An atlas Region (i.e., sub-image).
    *
    * See, e.g., the Spine file format documentation for what some of this
    * stuff means.  However, for many use cases it is sufficient to know
    * that you can use the .stamp property to get a GFStamp that you can use
    * to draw the region.  If the region is indexed, you can use .get(X) to
    * get the sibling Region with index X.  So you can do something like:
    * atlas.get("idle_animation").get(3).stamp.stamp();.
    */
  public class Region extends KVData
  {
    protected boolean finished = false;

    public Object opaque;
    public GFStamp stamp;
    public Page page;
    public String name;
    public int index = -1;
    public double rotate;
    public int x;
    public int y;
    public int w;
    public int h;
    public int offX, offY, origW, origH;
    public Region groupHead;

    // For ninepatches
    public LRTB split;
    public LRTB pad;

    public GFStamp[] nine; //TODO

    public ArrayList<Region> indexMap;

    /// Get part of a nine patch (x and y are 0 through 2).
    GFStamp get9 (int x, int y)
    {
      return nine[x + y * 3];
    }

    /* Get part of an indexed region.
     *
     * The index is actually wrapped, so you can request large values and
     * they'll get wrapped back around to small values.  This is convenient
     * for time-based animations based on the frame counter.
     */
    public Region get (int index)
    {
      return indexMap.get(index % indexMap.size());
    }

    public ArrayList<GFStamp> getStamps ()
    {
      ArrayList<GFStamp> r = new ArrayList<>();
      if (!isIndexed())
      {
        r.add(stamp);
      }
      else
      {
        for (int i = 0; i < indexMap.size(); ++i)
        {
          r.add( get(i).stamp );
        }
      }
      return r;
    }

    public boolean isIndexed ()
    {
      return indexMap != null;
    }

    public String toString ()
    {
      String s = "<Region " + name;
      if (index != -1) s += "#" + index;
      return s + ">";
    }

    void finish ()
    {
      if (groupHead == this && indexMap != null)
      {
        for (Region sub : indexMap)
        {
          if (sub == this) continue;
          sub.finish();
        }
      }

      if (finished) return;
      finished = true;

      int[] bounds = getInts("bounds");
      x=bounds[0]; y=bounds[1]; w=bounds[2]; h=bounds[3];
      int ox = x, oy = y, ow = w, oh = h;
      int[] offsets = getInts("offsets");
      if (offsets.length > 0) ox = offsets[0];
      if (offsets.length > 1) oy = offsets[1];
      if (offsets.length > 2) ow = offsets[2];
      if (offsets.length > 3) oh = offsets[3];
      offX = ox; offY = oy; origW = ow; origH = oh;

      String v = getStr("rotate", "false").toLowerCase();
      double r = 0;
      if (v.equals("true"))
      {
        r = Math.PI / 2;
      }
      else
      {
        try
        {
          r = Math.toRadians(Double.parseDouble(v));
        }
        catch (Exception e)
        {
        }
      }

      split = getLRTB("split");
      pad = getLRTB("pad");
    }
  }

  /// Get a Region by name.
  public Region get (String name)
  {
    return regions.get(name);
  }
}
