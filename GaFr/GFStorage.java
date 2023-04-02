package GaFr;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/** Simple client-side storage API.
  *
  * This class allows for the easy storage of (relatively small amounts of)
  * "local data" in the client (i.e., the browser).
  *
  * It follows a simple model: you can store data using a key, and then
  * retrieve that data using the same key.
  *
  * For example, you can do GFStorage.store("name", "Alice"), and later
  * retrieve "Alice" with GFStorage.load("name").
  */
public class GFStorage
{
  /// Internal use.
  protected static String fixKey (String key)
  {
    if (key.contains("/"))
      throw new RuntimeException("Slashes not allowed in storage keys");

    return "/files/" + key;
  }

  /// Store data to be retrieved later.
  public static void store (String key, byte[] data)
  {
    try
    {
      Files.write(Paths.get(fixKey(key)), data, StandardOpenOption.CREATE);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  /// Store data to be retrieved later.
  public static void store (String key, String data)
  {
    store(key, data.getBytes());
  }

  /// Load data previously stored using the same key.
  public static String load (String key, String defaultValue)
  {
    try
    {
      return GFU.loadTextFile(fixKey(key));
    }
    catch (Throwable e)
    {
      return defaultValue;
    }
  }

  /// Load data previously stored using the same key.
  public static String load (String key)
  {
    return load(key, null);
  }

  /// Load data previously stored using the same key.
  public static byte[] loadBytes (String key)
  {
    try
    {
      return GFU.loadDataFile(fixKey(key));
    }
    catch (Throwable e)
    {
      return new byte[0];
    }
  }
}
