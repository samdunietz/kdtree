package personal.samdunietz.kdtree;

import java.util.Comparator;

/**  Comparator for finding ranking how close KDDatum are from an origin.
 * @author samdunietz
 *
 * @param <R> The type of the origin, which the elements are being compared to.
 * @param <Q> The type of the elements in the tree being compared.
 */
public class ClosestComparator<R extends KDData, Q extends KDData> implements Comparator<Q> {

  private R origin;

  /** Constructs a comparator that finds the KDData closest to origin.
   * @param origin The datum from which to find the closest other datum.
   */
  public ClosestComparator(R origin) {
    this.origin = origin;
  }

  @Override
  public int compare(Q o1, Q o2) {
    if (o1.getDims() != o2.getDims() || o1.getDims() != origin.getDims()) {
      throw new IllegalArgumentException(
          "Origin and both datum to compare must have same"
          + " number of dimensions");
    }

    return Double.compare(
        o1.euclidianDist(origin),
        o2.euclidianDist(origin));
  }
}
