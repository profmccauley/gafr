package GaFr;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import java.text.NumberFormat;
import java.text.DecimalFormat;


/** Allows access to JSON-formatted data.
  *
  * JSON is a common, human-and-machine-readable format for data interchange.
  * It is fairly well suited for ad hoc metadata storage, e.g., for
  * for configuration files, attributes in video game levels, etc.
  *
  * Example input file:
  *     { "name": "Alice", "age": 7 }
  *
  * Example code:
  *     Value v = GFJSON.parseFile("myfile.json");
  *     System.out.println("Hello, " + v.get("name").asString());
  *
  * JSON objects have string key names, which refer to values which may be
  * booleans, numbers, strings, the special value null, arrays, or other
  * objects.
  *
  * @link https://en.wikipedia.org/wiki/JSON
  */
public class GFJSON
{
  protected String json;
  protected int cur = 0;

  public Value root;

  /** Parse a JSON string. */
  public static Value parse (String json)
  {
    return new GFJSON(json).root;
  }

  /** Parse JSON from a file. */
  public static Value parseFile (String fileName)
  {
    return parse(GFU.loadTextFile(fileName));
  }

  /** Creates and runs a JSON parser.
    *
    * @param json The input JSON string.
    * @param offset The first chracter to parse from.
    * @param requireFull Whether the entire input must be parsed.
    *
    * By passing requireFull=false, one can make use of the offset parameter
    * and the getCurrentOffset() function to parse multiple JSON objects from
    * a single input string.
    */
  public GFJSON (String json, int offset, boolean requireFull)
  {
    this.json = json;
    cur = 0;
    root = parseObject();
    maybeWhitespace();
    if (requireFull)
    {
      if (cur != json.length())
        throw new RuntimeException("Did not consume entire input");
    }
  }

  /** The current parse position.
    *
    * If your input was entirely consumed (e.g, it started with { and ended
    * with }), this will be the input's length.  If the JSON object stopped
    * before the end, this points just after the last part that was read.
  */
  public int getCurrentOffset ()
  {
    return cur;
  }

  /** Creates and runs a JSON parser.
    *
    * This is used for the common case where you want to parse a buffer that
    * is entirely dedicated to a single JSON object.
    */
  public GFJSON (String json)
  {
    this(json, 0, true);
  }

  /** Superclass for all JSON values. */
  public static class Value
  {
    /** Is this Value a null? */
    public boolean isNull () { return false; }
    /** Is this Value an object? */
    public boolean isObject () { return false; }
    /** Is this Value an array? */
    public boolean isArray () { return false; }
    /** Is this Value a string? */
    public boolean isString () { return false; }
    /** Is this Value a number? */
    public boolean isNumber () { return false; }

    /** Get the length of the value.
      *
      * This is meaningful for, e.g., strings and arrays.
      */
    public int length () { return 0; } // Or exception?

    protected RuntimeException badType (String typeName)
    {
      return new RuntimeException("Bad type: Wanted to treat " + getClass().getName() + " as " + typeName);
    }

    /** Return this value as a Java int if possible. */
    public int asInt () { throw badType("int"); }
    /** Return this value as a Java double if possible. */
    public double asDouble () { throw badType("Number"); }
    /** Return this value as a Java float if possible. */
    public float asFloat () { return (float)asDouble(); }
    /** Return this value as a Java boolean if possible. */
    public boolean asBool () { return false; }
    /** Get an element from an object type value. */
    public Value get (String key) { throw badType("object"); }
    /** Get an element from an array type value. */
    public Value get (int index) { throw badType("array"); }
    /** Return this value as a Java String if possible. */
    public String asString () { throw badType("String"); }
    /** String conversion. */
    public String toString () { return "[JSON:"+asString()+"]"; }

    public ArrayValue asArray () { throw new RuntimeException("Not an array"); }
    public ObjectValue asObject () { throw new RuntimeException("Not an object"); }

    /** Checks whether a key exists in an object value. */
    public boolean has (String key) { return false; }

    /** Gets all elements of an ArrayValue as a Java ArrayList. */
    public ArrayList<Value> getElements () { throw new RuntimeException("Bad type"); }
    /** Gets all elements of an ObjectValue as a Java HashMap. */
    public HashMap<String,Value> getItems () { throw new RuntimeException("Bad type"); }

    /** Return an element from an object or a default value.
      *
      * If the key does not exist, returns the default.
      */
    public Value get (String key, Value defaultValue) { return has(key) ? get(key) : defaultValue; }

    /** Return this value as a Java int or a default value.
      *
      * If the object can't be read as an int, return the default.
      */
    public int asInt (int defaultValue)
    {
      try
      {
        return asInt();
      }
      catch (NumberFormatException e)
      {
        return defaultValue;
      }
    }
    /** Return this value as a Java float or a default value.
      *
      * If the object can't be read as an float, return the default.
      */
    public float asFloat (float defaultValue)
    {
      try
      {
        return asFloat();
      }
      catch (NumberFormatException e)
      {
        return defaultValue;
      }
    }
    /** Return this value as a Java double or a default value.
      *
      * If the object can't be read as an double, return the default.
      */
    public double asDouble (double defaultValue)
    {
      try
      {
        return asDouble();
      }
      catch (NumberFormatException e)
      {
        return defaultValue;
      }
    }
  }

  public static Value create (double v) { return new NumberValue(v); }
  public static Value create (float v) { return new NumberValue(v); }
  public static Value create (boolean v) { return v ? jstrue : jsfalse; };
  public static Value create (String v) { return new StringValue(v); }
  public static Value create (Object v)
  {
    if (v == null) return jsnull;
    throw new RuntimeException("Can't wrap this type of value"); //TODO
  }


  /** Superclass for boolean values. */
  static class BoolValue extends Value
  {
    public boolean isBool () { return true; }
  }

  /** A Value type used strictly for true values.
    *
    * There is one instance of this -- jstrue.
    */
  static class TrueValue extends BoolValue
  {
    public String asString () { return "true"; }
    public boolean asBool () { return true; }
  }

  /** A Value type used strictly for false values.
    *
    * There is one instance of this -- jsfalse.
    */
  static class FalseValue extends BoolValue
  {
    public String asString () { return "false"; }
    public boolean asBool () { return false; }
  }

  /** A Value type used strictly for the null value.
    *
    * There is one instance of this -- jsnull.
    */
  static class NullValue extends Value
  {
    public String asString () { return "null"; }
    public boolean isNull () { return true; }
  }

  /** A Value type used strictly for string values. */
  static class StringValue extends Value
  {
    protected String v;
    StringValue (String v)
    {
      this.v = v;
    }
    public String asString ()
    {
      return v;
    }
    public boolean isString () { return true; }
    public boolean asBool ()
    {
      String vv = v.toLowerCase();
      if (vv.equals("false")) return false;
      if (vv.equals("disabled")) return false;
      if (vv.equals("off")) return false;
      if (vv.equals("no")) return false;
      return asDouble(1) != 0;
    }

    public int asInt ()
    {
      if (v.startsWith("0x")) return Integer.parseInt(v.substring(2), 16);
      if (v.startsWith("0b")) return Integer.parseInt(v.substring(2), 2);
      return Integer.parseInt(v);
    }

    public double asDouble  ()
    {
      return Double.parseDouble(v);
    }
  }

  /** A Value type used strictly for numeric values. */
  static protected NumberFormat numberFormat = new DecimalFormat("#.########");

  static class NumberValue extends Value
  {
    public boolean isNumber () { return true; }
    protected double v;
    NumberValue (double v) { this.v = v; }
    public boolean asBool () { return v != 0; }
    public int asInt () { return (int)v; }
    public String asString ()
    {
      return numberFormat.format(v);
    }
    public double asDouble () { return v; }
  }

  /** A Value type used strictly for array values. */
  static class ArrayValue extends Value implements Iterable<Value>
  {
    public Iterator<Value> iterator ()
    {
      return values.iterator();
    }

    protected ArrayList<Value> values = new ArrayList<Value>();

    public boolean isArray () { return true; }

    void add (Value v) { values.add(v); }

    public boolean asBool ()
    {
      return values.size() > 0;
    }

    public String asString ()
    {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      for (int i = 0; i < values.size(); ++i)
      {
        if (i != 0) sb.append(", ");
        sb.append(values.get(i).asString());
      }
      sb.append("]");
      return sb.toString();
    }

    public Value get (int index)
    {
      return values.get(index);
    }

    public int length ()
    {
      return values.size();
    }

    public ArrayList<Value> getElements () { return values; }

    public ArrayValue asArray () { return this; }
  }

  /** A Value type used strictly for objects. */
  static class ObjectValue extends Value implements Iterable<Map.Entry<String,Value>>
  {
    public ObjectValue asObject () { return this; }
    public Iterator<Map.Entry<String,Value>> iterator ()
    {
      return values.entrySet().iterator();
    }

    public boolean has (String key) { return values.containsKey(key); }

    protected HashMap<String,Value> values = new HashMap<String,Value>();

    public boolean isObject () { return true; }

    void put (String k, Value v) { values.put(k, v); }

    public HashMap<String,Value> getItems () { return values; }

    public boolean asBool ()
    {
      return values.size() > 0;
    }

    public String asString ()
    {
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      boolean first = true;
      for (Map.Entry<String,Value> kv : values.entrySet())
      {
        if (!first) sb.append(", ");
        first = false;
        sb.append('"');
        sb.append(kv.getKey());
        sb.append('"');
        sb.append(" : ");
        sb.append(kv.getValue().asString());
      }
      sb.append("}");
      return sb.toString();
    }

    public Value get (String key)
    {
      return values.get(key);
    }

    public int length ()
    {
      return values.size();
    }
  }

  /** Singleton for all true values. */
  static protected Value jstrue = new TrueValue();
  /** Singleton for all false values. */
  static protected Value jsfalse = new FalseValue();
  /** Singleton for all null values. */
  static protected Value jsnull = new NullValue();

  /** Parse a JSON object. */
  protected Value parseObject ()
  {
    maybeWhitespace();
    eat("{");

    ObjectValue v = new ObjectValue();

    while (true)
    {
      maybeWhitespace();
      if (maybeEat("}")) return v;
      eat("\"");
      String key = eatString();
      maybeWhitespace();
      eat(":");
      Value value = eatValue();

      //System.out.println(key+"="+value);
      v.put(key, value);

      maybeWhitespace();

      if (!maybeEat(","))
      {
        eat("}");
        return v;
      }
    }
  }

  /** Returns the next character to be parsed. */
  protected int front ()
  {
    if (cur >= json.length()) return -1;
    return json.charAt(cur);
  }

  /** Consumes any whitespace. */
  protected void maybeWhitespace ()
  {
    while (true)
    {
      switch (front())
      {
        case ' ':
        case '\t':
        case 0x0a:
        case 0x0d:
          ++cur;
          continue;
        default:
          return;
      }
    }
  }

  /** Consumes the given string, or errors. */
  protected void eat (String s)
  {
    if (!maybeEat(s))
      throw new RuntimeException("Expected '" + s + "' at " + cur);
  }

  /** Possibly consumes the given string.
    *
    * @return true if the string was consumed.
    */
  protected boolean maybeEat (String s)
  {
    int rewind = cur;
    for (int i = 0; i < s.length(); ++i)
    {
      if (front() != s.charAt(i))
      {
        cur = rewind;
        return false;
      }
      ++cur;
    }

    return true;
  }

  /** Parses a vaue. */
  protected Value eatValue ()
  {
    maybeWhitespace();
    switch (front())
    {
      case '{':
        //++cur;
        return parseObject();
      case '[':
        ++cur;
        return parseArray();
      case '"':
        ++cur;
        return parseString();
    }
    if (maybeEat("null"))
      return jsnull;
    if (maybeEat("true"))
      return jstrue;
    if (maybeEat("false"))
      return jsfalse;

    return parseNumber();
  }

  /** Parses a number. */
  protected Value parseNumber ()
  {
    // This is not quite to the JSON spec.
    StringBuilder sb = new StringBuilder();

    if (maybeEat("-")) sb.append("-");

    while (true)
    {
      int d = front();
      if (d < '0' || d > '9') break;
      sb.appendCodePoint(d);
      ++cur;
      //intpart *= 10;
      //intpart += (d - '0');
    }

    if (maybeEat("."))
    {
      sb.append(".");
      while (true)
      {
        int d = front();
        if (d < '0' || d > '9') break;
        sb.appendCodePoint(d);
        ++cur;
      }
    }

    if (maybeEat("e") || maybeEat("E"))
    {
      sb.append("e");
      if (maybeEat("-")) sb.append("-");
      else if (maybeEat("+")) sb.append("+");
      while (true)
      {
        int d = front();
        if (d < '0' || d > '9') break;
        sb.appendCodePoint(d);
        ++cur;
      }
    }
    return new NumberValue(Double.parseDouble(sb.toString()));
  }

  /** Parse a string.
    *
    * The current position should be just after the opening quote mark.
    */
  protected Value parseString ()
  {
    return new StringValue(eatString());
  }

  /** Consumes a string.
    *
    * The current position should be just after the opening quote mark.
    */
  protected String eatString ()
  {
    StringBuilder sb = new StringBuilder();

    while (true)
    {
      if (maybeEat("\"")) return sb.toString();
      if (maybeEat("\\"))
      {
        int f = front();
        ++cur;
        switch (f)
        {
          case '"':
            sb.append('"');
            break;
          case '\\':
            sb.append('\\');
            break;
          case '/':
            sb.append('/');
            break;
          case 'b':
            sb.append('\b');
            break;
          case 'f':
            sb.append('\f');
            break;
          case 'n':
            sb.append('\n');
            break;
          case 'r':
            sb.append('\r');
            break;
          case 't':
            sb.append('\t');
            break;
          case 'u':
            String cp = json.substring(cur, cur+4);
            int hv = Integer.parseInt(cp, 16);
            sb.appendCodePoint(hv);
            break;
          default:
            --cur; // Back up.
            throw new RuntimeException("Unexpected backslash-quoted character at " + cur);
        }
      }
      else
      {
        sb.appendCodePoint(front());
        ++cur;
      }
    }
  }

  /** Parse an array.
    *
    * The current position should be just after the opening brace.
    */
  protected Value parseArray ()
  {
    ArrayValue v = new ArrayValue();
    if (maybeEat("]")) return v;
    while (true)
    {
      v.add(eatValue());
      maybeWhitespace();
      if (!maybeEat(","))
      {
        eat("]");
        return v;
      }
      maybeWhitespace();
    }
  }
}
