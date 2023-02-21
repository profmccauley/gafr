package GaFr;

import java.util.Objects;

/** A pair of two ints.
  */
public class GFPairI
{
  public final int e1;
  public final int e2;

  public GFPairI (int e1, int e2)
  {
    this.e1 = e1;
    this.e2 = e2;
  }

  @Override
  public boolean equals (Object other)
  {
    if (other == null) return false;
    if (!(other instanceof GFPairI)) return false;
    GFPairI o = (GFPairI)other;
    return o.e1 == e1 && o.e2 == e2;
  }

  @Override
  public int hashCode ()
  {
    return Objects.hash(e1, e2);
  }
}
