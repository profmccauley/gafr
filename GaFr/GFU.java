package GaFr;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.Random;
import java.util.Iterator;
import java.util.ArrayList;

/** A utility library for odds and ends.
  *
  * It's possible some of this stuff should be elsewhere.  File an issue or
  * create a pull request!
  */
public class GFU
{
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

  /** Log a message for debugging purposes.
    *
    * Unlike logs(), it does not put spaces between items.
    *
    * @see GFU#logs
    **/
  public static void log (Object... args)
  {
    String s = "";
    for (Object o : args)
    {
      //if (s.length() != 0) s += " ";
      if (o == null) o = "<null>";
      s += o.toString();
    }
    System.out.print(s);
  }

  /** Log a message for debugging purposes.
    *
    * Unlike log(), it puts spaces between items.
    *
    * @see GFU#log
    **/
  public static void logs (Object... args)
  {
    String s = "";
    for (Object o : args)
    {
      if (s.length() != 0) s += " ";
      if (o == null) o = "<null>";
      s += o.toString();
    }
    System.out.print(s);
  }


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
      return new Iterator2D<T>(array);
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

  /** Makes an ArrayList at least a given length.
    */
  public static void ensureSize (ArrayList<?> list, int size)
  {
    list.ensureCapacity(size);
    while (list.size() < size)
      list.add(null);
  }

  /** Makes an ArrayList large enough for the given index.
    */
  public static void ensureIndex (ArrayList<?> list, int index)
  {
    ensureSize(list, index+1);
  }


  /** Gets a substring using Python-like indices.
    */
  public static String substr (String s, int start, int end)
  {
    if (s == null) return "";

    int len = s.length();
    if (start < 0) start = len + start;
    if (end < 0) end = len + end;

    if (start < 0) start = 0;
    if (end < 0) end = 0;

    if (start >= len) start = len;
    if (end >= len) end = len;

    return s.substring(start, end);
  }

  /** Gets a substring using Python-like indices.
    */
  public static String substr (String s, int start)
  {
    return substr(s, start, -1);
  }

  /** Split a string at the first occurrence.
    *
    * If the delimiter does not appear in the string, the second item of the
    * pair is null.
    */
  public static GFPair<String, String> split (String string, String delim)
  {
    int n = string.indexOf(delim);
    if (n == -1) return new GFPair<>(string, null);
    return new GFPair<>(substr(string, 0,n), substr(string, n+1));
  }

  /** Split a string at the last occurrence.
    *
    * If the delimiter does not appear in the string, the first item of the
    * pair is null.
    */
  public static GFPair<String, String> rsplit (String string, String delim)
  {
    int n = string.lastIndexOf(delim);
    if (n == -1) return new GFPair<>(string, null);
    return new GFPair<>(substr(string, 0,n), substr(string, n+1));
  }
}
