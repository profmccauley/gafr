package GaFr;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.Random;
import java.util.Iterator;

/** A utility library for odds and ends.
  *
  * It's possible some of this stuff should be elsewhere.  File an issue or
  * create a pull request!
  */
public class GFU
{
  /** Default random number generator. */
  protected static Random random = new Random();

  /** Makes the given path relative to the project base directory. */
  public static String resolvePath (String fileName)
  {
    return GFBoot.resolvePath(fileName);
  }

  /** Read a file's contents into a String. */
  public static String loadTextFile (String fileName)
  {
    return new String(loadDataFile(fileName));
  }

  /** Read a file's contents into a byte array. */
  public static byte[] loadDataFile (String fileName)
  {
    try
    {
      return Files.readAllBytes(Paths.get(resolvePath(fileName)));
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  /** Get a Stream for a given file. */
  public static ByteArrayInputStream loadAsStream (String fileName)
  {
    return new ByteArrayInputStream(loadDataFile(fileName));
  }

  /** Log a message for debugging purposes. */
  public static void log (Object... args)
  {
    String s = "";
    for (Object o : args)
    {
      //if (s.length() != 0) s += " ";
      s += o.toString();
    }
    System.out.print(s);
  }

  /** Float version of cos(). */
  public static float cosf (double a) { return (float)Math.cos(a); }
  /** Float version of sin(). */
  public static float sinf (double a) { return (float)Math.sin(a); }
  /** Float version of cos(). */
  public static float cosf (float a) { return (float)Math.cos(a); }
  /** Float version of sin(). */
  public static float sinf (float a) { return (float)Math.sin(a); }

  /** Wraps value into a range.
    * lo should be less than hi.
    * If lo is 0 and value is not negative, this is just modulus.
    * The output is in the range [lo,hi).
    */
  public static double wrapf (double v, double lo, double hi)
  {
    v = (v-lo) % (hi-lo);
    if (v < 0) return v + hi;
    return v + lo;
  }
  /** Wraps value into a range.
    *
    * @see GFU#wrapf(double, double, double)
    */
  public static float wrapf (float v, float lo, float hi)
  {
    v = (v-lo) % (hi-lo);
    if (v < 0) return v + hi;
    return v + lo;
  }

  /** Wraps value into the range [0,1).
    *
    * @see GFU#wrapf(double, double, double)
    */
  public static double wrap1 (double v)
  {
    return wrapf(v, 0, 1);
  }
  /** Wraps value into the range [0,1).
    *
    * @see GFU#wrapf(double, double, double)
    */
  public static float wrap1 (float v)
  {
    return wrapf(v, 0, 1);
  }

  /** Wraps value into a range.
    *
    * @see GFU#wrapf(double, double, double)
    *
    * Unlike the floating point version, the output range is [lo,hi].
    */
  public static int wrap (int v, int lo, int hi)
  {
    v = (v-lo) % (hi-lo+1);
    if (v < 0) return v + hi + 1;
    return v + lo;
  }

  /** Clamps a value to [0,255].
    *
    * @see GFU#clamp(int, int, int).
    */
  public static int clamp255 (int n)
  {
    if (n < 0) return 0;
    if (n > 255) return 255;
    return n;
  }

  /// \overload
  public static int clamp255 (double n)
  {
    return clamp255((int)n);
  }

  /** Clamps an integer value.
    *
    * The output range is [lo,hi].  If the input is less than lo, the
    * return is lo.  If it's greater than hi, the return is hi.  Otherwise,
    * the return is the input.
    */
  public static int clamp (int n, int lo, int hi)
  {
    if (n < lo) return lo;
    if (n > hi) return hi;
    return n;
  }

  /** Clamps a floating point value.
    *
    * @see GFU#clamp(int, int, int)
    */
  public static double clamp (double n, double lo, double hi)
  {
    if (n < lo) return lo;
    if (n > hi) return hi;
    return n;
  }
  /** Clamps a floating point value.
    *
    * @see GFU#clamp(int, int, int)
    */
  public static float clamp (float n, float lo, float hi)
  {
    if (n < lo) return lo;
    if (n > hi) return hi;
    return n;
  }

  /** Clamps a floating point value to [0,1).
    */
  public static double clamp1 (double v)
  {
    return clamp(v, 0, 1);
  }
  /** Clamps a floating point value to [0,1).
    */
  public static float clamp1 (float v)
  {
    return clamp(v, 0, 1);
  }

  /** The value of PI. */
  public static final double PI = Math.PI;
  /** The value of PI as a float. */
  public static final float PIf = (float)Math.PI;

  /** The substring before the last instance of a delimiter.
    *
    *     beforeLast("foo.bar.baz", ".") == "foo.bar"
    *     beforeLast("foo", ".") == "foo"
    */
  public static String beforeLast (String string, String delim)
  {
    int l = string.lastIndexOf(delim);
    if (l == -1) return string;
    return string.substring(0, l);
  }

  /** The substring after the last instance of a delimiter.
    *
    *     afterLast("foo.bar.baz", ".") == "baz"
    *     afterLast("foo", ".") == ""
    */
  public static String afterLast (String string, String delim)
  {
    int l = string.lastIndexOf(delim);
    if (l == -1) return "";
    return string.substring(l+1);
  }

  /** Returns a random number.
   *
   * @param lo     The lower bound
   * @param hi     The upper bound
   *
   * The return value is between [lo,hi].
   */
  public static int randint (int lo, int hi)
  {
    return random.nextInt(hi-lo+1) + lo;
  }

  /** A generic iterator for 2D arrays.
    *
    * It can be nice to be able just iterate over each element of
    * a 2D array.  This lets us do so like...
    *
    *     Foo[][] foos = getFoos();
    *     for (Foo f : new Iter2D<>(foos))
    *        System.out.println( f.toString() );
    */
  public static class Iter2D<T> implements Iterable<T>
  {
    T[][] array;
    public Iter2D (T[][] array)
    {
      this.array = array;
    }

    public Iterator<T> iterator ()
    {
      return new Iterator2D(array);
    }
  }

  /** Iterable for 2D arrays.
    *
    * This is largely intended to be used with Iter2D.
    */
  public static class Iterator2D<T> implements Iterator<T>
  {
    int i, j;
    int count; // Total elements
    T[][] array;

    public Iterator2D (T[][] array)
    {
      this.array = array;

      // This isn't very smart, but the way we know whether we have any
      // elements left at present is to just count them all ahead of
      // time and subtract.  We could do better.
      for (int k = 0; k < this.array.length; ++k)
        count += this.array[k].length;
    }

    public T next ()
    {
      while (i >= this.array.length)
      {
        i = 0;
        ++j;
      }
      --count;
      return this.array[i++][j];
    }

    public boolean hasNext ()
    {
      return count > 0;
    }
  }
}
