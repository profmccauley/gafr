package GaFr;

import java.util.Objects;

/** A pair of two references.
  */
public class GFPair <E1, E2>
{
  public final E1 e1;
  public final E2 e2;

  public GFPair (E1 e1, E2 e2)
  {
    this.e1 = e1;
    this.e2 = e2;
  }

  @Override
  public boolean equals (Object other)
  {
    if (other == null) return false;
    if (!(other instanceof GFPair)) return false;
    GFPair o = (GFPair)other;
    return o.e1 == e1 && o.e2 == e2;
  }

  @Override
  public int hashCode ()
  {
    return Objects.hash(e1, e2);
  }
}
