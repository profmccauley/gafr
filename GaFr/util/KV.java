package GaFr.util;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;


/** A dictionary class meant for configuration data.
  *
  * This is meant for working with hashmaps where all the values are
  * internally stored as strings, but you might want to access them as if
  * they were something else.  A common reason for this is when parsing
  * configuration files of arbitrary formats where the easiest thing to do
  * is just read it into strings and let the consumer specify the type.
  */
public class KV
{
  public final HashMap<String, String> kv = new HashMap<>();
  private static final int[] noData = new int[0];

  public String toString (String delim)
  {
    ArrayList<String> out = new ArrayList<>();
    for (Map.Entry<String,String> entry : kv.entrySet())
    {
      String s = entry.getValue();
      if (s.indexOf(" ") >= 0) s = "{" + s + "}";
      out.add(entry.getKey() + " : " + s);
    }
    Collections.sort(out);
    return String.join(delim, out);
  }

  public String toString ()
  {
    return toString(", ");
  }

  /// Read a key as a boolean value.
  public boolean getBool (String key)
  {
    return getBool(key, false);
  }

  /// Read a key as a boolean value.
  public boolean getBool (String key, boolean defaultVal)
  {
    String v = getStr(key, defaultVal ? "true" : "false").trim().toLowerCase();
    if (v.equals("true")) return true;
    if (v.equals("false")) return false;
    try
    {
      int i = Integer.parseInt(v);
      if (i != 0) return true;
      if (i == 0) return false;
    }
    catch (Exception e)
    {
    }
    return defaultVal;
  }

  /// Read a key as an array of integers.
  public int[] getInts (String key)
  {
    String vs = getStr(key);
    if (vs == null) return noData;
    String[] parts = vs.split(",");
    int[] r = new int[parts.length];
    for (int i = 0; i < r.length; ++i)
    {
      r[i] = Integer.parseInt(parts[i].trim());
    }
    return r;
  }

  /// Read a key as a string.
  public String getStr (String key)
  {
    return getStr(key, null);
  }

  /// Read a key as a string.
  public String getStr (String key, String defaultVal)
  {
    if (kv == null) return defaultVal;
    String r = kv.get(key);
    if (r != null) return r;
    return defaultVal;
  }

  /// Throw an exception for a bad key.
  protected void bad (String key)
  {
    throw new RuntimeException("Bad key '" + key + "'");
  }

  /// Read a key as an integer.
  public int getInt (String key)
  {
    if (kv == null) bad(key);
    if (!kv.containsKey(key)) bad(key);
    return Integer.parseInt(kv.get(key).trim());
  }

  /// Read a key as an integer.
  public int getInt (String key, int defaultVal)
  {
    if (kv == null) return defaultVal;
    if (!kv.containsKey(key)) return defaultVal;
    return Integer.parseInt(kv.get(key).trim());
  }
}
