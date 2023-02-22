package GaFr;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

/** Internal use.
  *
  * This is used to provide stack traces with line numbers.
  */
public class GFST
{
  private static final int STACKTRACE_MAX = 20;
  private static int stSize = 0;

  private static String[] stMethods = new String[STACKTRACE_MAX];
  private static int[] stLines = new int[STACKTRACE_MAX];

  /** Internal use for generating stack traces in CheerpJified code. */
  public static int stLine = -1;
  private static String stMethod = "?";

  /** Internal use for generating stack traces in CheerpJified code. */
  public static boolean setLine (int lineNum)
  {
    stLine = lineNum;
    return false;
  }

  /** Internal use for generating stack traces in CheerpJified code. */
  public static void pushStack (String methodName)
  {
    lastExceptionDepth = 0;
    lastException = null;
    //lastExceptionLine = -1;
    //lastExceptionMethod = null;
    if (stSize < STACKTRACE_MAX)
    {
      stMethods[stSize] = stMethod;
      stLines[stSize] = stLine;
    }
    stMethod = methodName;
    ++stSize;
  }

  private static int lastExceptionDepth = -1;
  //private static int lastExceptionLine = -1;
  //private static int lastExceptionMethod = null;
  private static Throwable lastException;

  /** Internal use for generating stack traces in CheerpJified code. */
  public static void doException (Throwable t)
  {
    if (t == lastException) return;
    pushStack("EXCEPTION");
    --stSize;

    lastException = t;
    lastExceptionDepth = stSize;
    //lastExceptionLine = stLine;
    //lastExceptionMethod = stMethod;
  }

  /** Internal use for generating stack traces in CheerpJified code. */
  public static void popStack ()
  {
    --stSize;
    if (stSize < STACKTRACE_MAX)
    {
      stMethod = stMethods[stSize];
      stLine = stLines[stSize];
    }
    else
    {
      stMethod = "Unknown";
      stLine = -1;
    }
  }

  /** Get a stack trace for an exception as a String. */
  public static String getStackTrace (Throwable e)
  {
    return getStackTrace(e, null);
  }

  /** Get a stack trace for an exception as a String. */
  public static String getStackTrace (Throwable e, String whileDoing)
  {
    if (stSize == 0)
    {
      // No Numberizer stack trace info -- just do a normal stack trace
      StringWriter sw = new StringWriter();
      if (whileDoing != null) sw.write(whileDoing + ": ");
      e.printStackTrace(new PrintWriter(sw));
      return sw.toString();
    }

    // This is not exact, but it will work okay at least some of the time.
    ArrayList<String> methods = new ArrayList<>();
    ArrayList<String> rests = new ArrayList<>();

    for (int i = lastExceptionDepth; i >= 0; --i)
    {
      if (i == 0 && stLines[i] == -1) break;
      String m = stMethods[i];
      String rest = ":" + stLines[i];
      if (m.indexOf("(") > 0)
      {
        rest = m.substring(m.indexOf("(")) + ":" + stLines[i];
        m = m.substring(0, m.indexOf("("));
      }
      methods.add(m);
      rests.add(rest);
    }

    Throwable t = e;
    while (t.getCause() != null)
      t = t.getCause();

    ArrayList<String> trace = new ArrayList<>();

    int cur = 0;
    boolean lostTrack = false;

    for (StackTraceElement el : t.getStackTrace())
    {
      String n = el.getClassName() + "." + el.getMethodName();
      if (cur < methods.size() && n.equals(methods.get(cur)))
      {
        trace.add( "  at " + methods.get(cur) + rests.get(cur) );
        ++cur;
      }
      else
      {
        //if (!lostTrack) trace.add( "  ?" );
        lostTrack = true;
        trace.add( "  at " + n + ":?" );
      }
    }

    if (cur < methods.size()) trace.add("  ?");
    for (; cur < methods.size(); ++cur)
    {
      trace.add( "  at " + methods.get(cur) + rests.get(cur) );
    }

    String desc = "";
    if (whileDoing != null) desc = whileDoing + ": ";
    trace.add(0, desc + t.toString());

    if (e != t)
    {
      int trim = trace.size() - 1;

      trace.add("  Eventually causing: " + e.toString());
      int trimStart = trace.size();
      for (StackTraceElement el : t.getStackTrace())
      {
        String n = el.getClassName() + "." + el.getMethodName();
        trace.add( "  at " + n + "" );
      }

      int trimmed = 0;
      while (trace.size() >= trimStart && trim > 0)
      {
        String n = trace.get(trace.size() - 1);
        String r = trace.get(trim);
        if (r.indexOf("(") > 0) r = r.substring(0, r.indexOf("("));
        if (r.indexOf(":") > 0) r = r.substring(0, r.indexOf(":"));
        if (!r.equals(n)) break;
        trace.remove(trace.size() - 1);
        --trim;
        ++trimmed;
      }
      if (trimmed > 0 && trace.size() != trimStart)
      {
        trace.add("  ... " + trimmed + " more");
      }
    }

    String out = "";

    for (int i = 0; i < trace.size(); ++i)
    {
      if (i != 0) out += "\n";
      out += trace.get(i);
    }

    return out;
  }

  /** Prints a stack trace. */
  public static void printStackTrace (Throwable e, String whileDoing)
  {
    System.err.print(getStackTrace(e, whileDoing));
  }

  /** Prints a stack trace. */
  public static void printStackTrace (Throwable e)
  {
    printStackTrace(e, null);
  }
}
