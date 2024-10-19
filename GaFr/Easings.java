package GaFr;
import java.util.ArrayList;
import static GaFr.GFM.*;

/**
  * Easing functions.
  *
  * An easing function is just a function for which the input is typically
  * [0,1] and the output is slightly more interesting.  Usually ease(0) is
  * 0 and ease(1) is 1, but in the middle it might be something else.  A
  * typical use case for these functions is to map a linear input to
  * something more exciting.  For example, if you use time as the input
  * (linearly progressing as time passes), you can use the output to do
  * something like compute a position... which does *not* move linearly.
  * This is an easy way to add more interesting motion to a game, for
  * example.
  *
  * GaFr provides an Easing superclass which easings can be derived from.
  * As a consumer of an Easing, the functions you might be particularly
  * interested in are Easing::ease() and Easing::easef() (for doubles and
  * floats respectively).  You simply pass a value from 0 to 1 in, and get
  * the eased value out.  The inputs are clamped to [0,1] automatically.
  *
  * GaFr comes with some flexible easings, and some utility classes for
  * combining easings and such.  But perhaps most usefully, it comes with
  * a bunch of "standard" easings already built into classes.  You can
  * find nice demos of these easings on the website listed below; these
  * demos can help you pick the easing function that does what you want.
  * GaFr creates static instances of all of these "standard" easings for
  * you, so you don't even need to instantiate anything -- you can just
  * skip straight to using them.  For example:
  *
  * ```
  * double easedValue = Easings.easeInOutElastic.ease( linearValue );
  * ```
  *
  * See https://easings.net/ nice graphs of these functions.  (That's also
  * where many of the functions came from.)
  */
public class Easings
{
  /**
    * A concatenation of two easings.
    *
    * This creates a joint easing which does one followed by another.
    */
  public static class EasingPair extends Easing
  {
    public Easing e1;
    public Easing e2;
    public EasingPair (Easing e1, Easing e2)
    {
      this.e1 = e1;
      this.e2 = e2;
    }
    public float f (float x)
    {
      if (x < 0) x = 0;
      else if (x > 1) x = 1;
      if (x <= 0.5) return e1.f(x*2);
      return e2.f((x-0.5f)*2);
    }
  }

  /**
    * A concatenation of easings.
    *
    * This creates a joint easing which does one followed by another.
    */
  public static class EasingGroup extends Easing
  {
    public static class Pair
    {
      Easing e;
      double w;
      double a, b;
      public Pair (Easing e, double w)
      {
        this.e = e;
        this.w = w;
      }
    }
    ArrayList<Pair> easings = new ArrayList<>();
    protected boolean initialized;

    public EasingGroup add (Easing e, double w)
    {
      easings.add(new Pair(e, w) );
      return this;
    }

    public EasingGroup add (Easing e)
    {
      return add(e, 1);
    }

    protected void initialize ()
    {
      double total = 0;
      for (Pair p : easings)
        total += p.w;

      double a = 0;
      double b = 0;
      for (Pair p : easings)
      {
        p.a = a;
        b = b + p.w/total;
        a = b;
        p.b = b;
      }

      initialized = true;
    }

    public float f (float x)
    {
      if (!initialized) initialize();

      Pair e = easings.get(easings.size()-1);

      for (Pair p : easings)
      {
        if (x < p.b)
        {
          e = p;
          break;
        }
      }

      double xx = (x - e.a) / (e.b-e.a);
      return e.e.f((float)xx);
    }
  }

  /**
    * Inverts the output of an easing.
    */
  public static class Invert extends Easing
  {
    public Easing e;
    public Invert (Easing e)
    {
      this.e = e;
    }
    public float f (float x)
    {
      return 1-e.f(x);
    }
  }

  /**
    * Reverses the input of an easing.
    */
  public static class Reverse extends Easing
  {
    public Easing e;
    public Reverse (Easing e)
    {
      this.e = e;
    }
    public float f (float x)
    {
      //if (x < 0) x = 0;
      //else if (x > 1) x = 1;
      return e.f(1-x);
    }
  }

  /**
    * A superclass for easing functions.
    */
  public abstract static class Easing
  {
    /// Apply easing
    public double ease (double x)
    {
      if (x < 0) x = 0;
      else if (x > 1) x = 1;
      return f(x);
    }
    /// Apply easing (float version)
    public float easef (double x)
    {
      return easef((float)x);
    }
    /// Apply easing (float version)
    public float easef (float x)
    {
      if (x < 0) x = 0;
      else if (x > 1) x = 1;
      return f(x);
    }

    /// Perform easing
    public abstract float f (float x);

    /// Perform easing
    public double f (double x)
    {
      return f((float)x);
    }
  }

  public static class EaseLinear extends Easing
  {
    public float f (float x) { return x; }
  }

  public static class EaseInSine extends Easing
  {
    public float f (float x) { return 1 - cosf((x * PIf) / 2); }
  }

  public static class EaseOutSine extends Easing
  {
    public float f (float x) { return sinf((x * PIf) / 2); }
  }

  public static class EaseInOutSine extends Easing
  {
    public float f (float x) { return -(cosf(PIf * x) - 1) / 2; }
  }

  public static class EaseInQuad extends Easing
  {
    public float f (float x) { return x * x; }
  }

  public static class EaseOutQuad extends Easing
  {
    public float f (float x) { return 1 - (1 - x) * (1 - x); }
  }

  public static class EaseInOutQuad extends Easing
  {
    public float f (float x) { return x < 0.5 ? 2 * x * x : 1 - powf(-2 * x + 2, 2) / 2; }
  }

  public static class EaseInCubic extends Easing
  {
    public float f (float x) { return x * x * x; }
  }

  public static class EaseOutCubic extends Easing
  {
    public float f (float x) { return 1 - powf(1 - x, 3); }
  }

  public static class EaseInOutCubic extends Easing
  {
    public float f (float x) { return x < 0.5 ? 4 * x * x * x : 1 - powf(-2 * x + 2, 3) / 2; }
  }

  public static class EaseInQuart extends Easing
  {
    public float f (float x) { return x * x * x * x; }
  }

  public static class EaseOutQuart extends Easing
  {
    public float f (float x) { return 1 - powf(1 - x, 4); }
  }

  public static class EaseInOutQuart extends Easing
  {
    public float f (float x) { return x < 0.5 ? 8 * x * x * x * x : 1 - powf(-2 * x + 2, 4) / 2; }
  }

  public static class EaseInQuint extends Easing
  {
    public float f (float x) { return x * x * x * x * x; }
  }

  public static class EaseOutQuint extends Easing
  {
    public float f (float x) { return 1 - powf(1 - x, 5); }
  }

  public static class EaseInOutQuint extends Easing
  {
    public float f (float x) { return x < 0.5 ? 16 * x * x * x * x * x : 1 - powf(-2 * x + 2, 5) / 2; }
  }

  public static class EaseInExpo extends Easing
  {
    public float f (float x) { return x == 0 ? 0 : powf(2, 10 * x - 10); }
  }

  public static class EaseOutExpo extends Easing
  {
    public float f (float x) { return x == 1 ? 1 : 1 - powf(2, -10 * x); }
  }

  public static class EaseInOutExpo extends Easing
  {
    public float f (float x)
    {
      return x == 0
        ? 0
        : x == 1
        ? 1
        : x < 0.5 ? powf(2, 20 * x - 10) / 2
        : (2 - powf(2, -20 * x + 10)) / 2;
    }
  }

  public static class EaseInCirc extends Easing
  {
    public float f (float x) { return 1 - sqrtf(1 - powf(x, 2)); }
  }

  public static class EaseOutCirc extends Easing
  {
    public float f (float x) { return sqrtf(1 - powf(x - 1, 2)); }
  }

  public static class EaseInOutCirc extends Easing
  {
    public float f (float x)
    {
      return x < 0.5
        ? (1 - sqrtf(1 - powf(2 * x, 2))) / 2
        : (sqrtf(1 - powf(-2 * x + 2, 2)) + 1) / 2;
    }
  }

  public static class EaseInBack extends Easing
  {
    public float c1 = 1.70158f;
    public float c3 = c1 + 1;

    public float f (float x)
    {
      return c3 * x * x * x - c1 * x * x;
    }
  }

  public static class EaseOutBack extends Easing
  {
    public float c1 = 1.70158f;
    public float c3 = c1 + 1;

    public float f (float x)
    {
      return 1 + c3 * powf(x - 1, 3) + c1 * powf(x - 1, 2);
    }
  }

  public static class EaseInOutBack extends Easing
  {
    public float c1 = 1.70158f;
    public float c2 = c1 * 1.525f;

    public float f (float x)
    {
      return x < 0.5
        ? (powf(2 * x, 2) * ((c2 + 1) * 2 * x - c2)) / 2
        : (powf(2 * x - 2, 2) * ((c2 + 1) * (x * 2 - 2) + c2) + 2) / 2;
    }
  }

  public static class EaseInElastic extends Easing
  {
    public float c4 = (2 * PIf) / 3f;

    public float f (float x)
    {
      return x == 0
        ? 0
        : x == 1
        ? 1
        : -powf(2, 10 * x - 10) * sinf((x * 10 - 10.75) * c4);
    }
  }

  public static class EaseOutElastic extends Easing
  {
    public float c4 = (2 * PIf) / 3f;

    public float f (float x)
    {
      return x == 0
        ? 0
        : x == 1
        ? 1
        : powf(2, -10 * x) * sinf((x * 10 - 0.75) * c4) + 1;
    }
  }

  public static class EaseInOutElastic extends Easing
  {
    public float c5 = (2 * PIf) / 4.5f;

    public float f (float x)
    {
      return x == 0
        ? 0
        : x == 1
        ? 1
        : x < 0.5
        ? -(powf(2, 20 * x - 10) * sinf((20 * x - 11.125f) * c5)) / 2
        : (powf(2, -20 * x + 10) * sinf((20 * x - 11.125f) * c5)) / 2 + 1;
    }
  }

  public static class EaseInBounce extends Easing
  {
    public float f (float x) { return 1 - easeOutBounce.f(1 - x); }
  }

  public static class EaseOutBounce extends Easing
  {
    public static float n1 = 7.5625f;
    public static float d1 = 2.75f;

    public float f (float x)
    {
      if (x < 1 / d1) {
          return n1 * x * x;
      } else if (x < 2 / d1) {
          return n1 * (x -= 1.5f / d1) * x + 0.75f;
      } else if (x < 2.5f / d1) {
          return n1 * (x -= 2.25f / d1) * x + 0.9375f;
      } else {
          return n1 * (x -= 2.625f / d1) * x + 0.984375f;
      }
    }
  }

  public static class EaseInOutBounce extends Easing
  {
    public static float n1 = 7.5625f;
    public static float d1 = 2.75f;

    // This is just the EaseOutBounce code
    protected float f2 (float x)
    {
      if (x < 1 / d1) {
          return n1 * x * x;
      } else if (x < 2 / d1) {
          return n1 * (x -= 1.5f / d1) * x + 0.75f;
      } else if (x < 2.5f / d1) {
          return n1 * (x -= 2.25f / d1) * x + 0.9375f;
      } else {
          return n1 * (x -= 2.625f / d1) * x + 0.984375f;
      }
    }
    public float f (float x)
    {
      return x < 0.5
        ? (1 - f2(1 - 2 * x)) / 2
        : (1 + f2(2 * x - 1)) / 2;
    }
  }

  public static class Smoothstep extends Easing
  {
    public float f (float x)
    {
      return x * x * (3 - 2 * x);
    }
  }

  public static class Smootherstep extends Easing
  {
    public float f (float x)
    {
      return x * x * x * (x * (x * 6 - 15) + 10);
    }
  }

  public static class Constant0 extends Easing
  {
    public float f (float x)
    {
      return 0;
    }
  }

  public static class Constant1 extends Easing
  {
    public float f (float x)
    {
      return 1;
    }
  }

  /**
    * A square waveform "easing".
    *
    * This jumps from 0 to 1 half way through.
    */
  public static class Square extends Easing
  {
    public float f (float x)
    {
      if (x > 0.5) return 1;
      return 0;
    }
  }

  /**
    * A triangle waveform "easing".
    *
    * This goes from 0 to 1 and back to 0.
    */
  public static class Triangle extends Easing
  {
    public float f (float x)
    {
      return 1f - (Math.abs(x - 0.5f) * 2);
    }
  }

  /**
    * A sine wave "easing".
    *
    * A complete sinusoidal waveform from 0 to 1 to 0.
    */
  public static class Sine extends Easing
  {
    public float f (float x)
    {
      return (sinf(x * TAUf - PIf/2f) + 1) / 2;
    }
  }

  /**
    * A generic pulse wave "easing".
    *
    * A pulse wave is a generalization of a square wave.  When the duty cycle
    * is 0.5, it *is* a square wave -- half the time is spent at 0 and half
    * is spent at 1.  In general, the duty cycle is the fraction of time
    * which the output is 1.
    */
  public static class Pulse extends Easing
  {
    protected float anti_duty;
    public Pulse (double duty)
    {
      this( (float)duty );
    }
    public Pulse (float duty)
    {
      anti_duty = 1 - duty;
    }
    public float f (float x)
    {
      if (x >= anti_duty) return 1;
      return 0;
    }
  }

  public static EaseLinear easeLinear = new EaseLinear();
  public static EaseOutSine easeOutSine = new EaseOutSine();
  public static EaseInOutSine easeInOutSine = new EaseInOutSine();
  public static EaseInQuad easeInQuad = new EaseInQuad();
  public static EaseOutQuad easeOutQuad = new EaseOutQuad();
  public static EaseInOutQuad easeInOutQuad = new EaseInOutQuad();
  public static EaseInCubic easeInCubic = new EaseInCubic();
  public static EaseOutCubic easeOutCubic = new EaseOutCubic();
  public static EaseInOutCubic easeInOutCubic = new EaseInOutCubic();
  public static EaseInQuart easeInQuart = new EaseInQuart();
  public static EaseOutQuart easeOutQuart = new EaseOutQuart();
  public static EaseInOutQuart easeInOutQuart = new EaseInOutQuart();
  public static EaseInQuint easeInQuint = new EaseInQuint();
  public static EaseOutQuint easeOutQuint = new EaseOutQuint();
  public static EaseInOutQuint easeInOutQuint = new EaseInOutQuint();
  public static EaseInExpo easeInExpo = new EaseInExpo();
  public static EaseOutExpo easeOutExpo = new EaseOutExpo();
  public static EaseInOutExpo easeInOutExpo = new EaseInOutExpo();
  public static EaseInCirc easeInCirc = new EaseInCirc();
  public static EaseOutCirc easeOutCirc = new EaseOutCirc();
  public static EaseInOutCirc easeInOutCirc = new EaseInOutCirc();
  public static EaseInBack easeInBack = new EaseInBack();
  public static EaseOutBack easeOutBack = new EaseOutBack();
  public static EaseInOutBack easeInOutBack = new EaseInOutBack();
  public static EaseInElastic easeInElastic = new EaseInElastic();
  public static EaseOutElastic easeOutElastic = new EaseOutElastic();
  public static EaseInOutElastic easeInOutElastic = new EaseInOutElastic();
  public static EaseInBounce easeInBounce = new EaseInBounce();
  public static EaseOutBounce easeOutBounce = new EaseOutBounce();
  public static EaseInOutBounce easeInOutBounce = new EaseInOutBounce();
  public static Smoothstep smoothstep = new Smoothstep();
  public static Smootherstep smootherstep = new Smootherstep();
  public static Constant0 constant0 = new Constant0();
  public static Constant1 constant1 = new Constant1();
  public static Square square = new Square();

  public static Sine sineWave = new Sine();
  public static Triangle triangleWave = new Triangle();
  public static Square squareWave = square;
  public static EaseLinear sawWave = easeLinear;
}
