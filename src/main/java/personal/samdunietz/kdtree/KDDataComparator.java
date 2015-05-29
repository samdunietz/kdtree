package personal.samdunietz.kdtree;

import java.util.Comparator;

/** Comparator for comparing KDData in a KDTree.
 * @author samdunietz
 *
 * @param <Q> The type of KDData to compare.
 */
public final class KDDataComparator<Q extends KDData> implements Comparator<Q> {

  private final int numDim;
  private final int currDim;

  /** Constructs a new KDDataComparator given a number of dimensions.
   * @param numDim
   *            The number of dimensions the KDData has The current dimension
   *            to compare on is initialized to 0
   */
  protected KDDataComparator(int numDim) {
    this(numDim, 0);
  }

  /** Constructs a new KDDataComparator given a total number of dimensions
   * and a dimension to start on.
   * @param numDim The number of dimensions the KDData has.
   * @param currDim The current dimension to compare on.
   */
  protected KDDataComparator(int numDim, int currDim) {
    this.numDim = numDim;
    this.currDim = currDim;
  }

  /** Returns a new comparator with currDim incremented by 1.
   * @return A new comparator with currDim incremented by 1.
   */
  protected KDDataComparator<Q> incrementDim() {
    return new KDDataComparator<Q>(numDim, (currDim + 1) % numDim);
  }

  /** Gets the comparator's current dimension.
   * @return The current dimension the comparator is comparing on.
   */
  public int getCurrDim() {
    return currDim;
  }

  /** Get the total number of dimensions the KDData being compared has.
   * @return The total number of dimensions the KDData being compared has.
   */
  public int getNumDim() {
    return numDim;
  }

  @Override
  public int compare(Q kd1, Q kd2) {
    return Double.compare(
        kd1.getLocData()[currDim],
        kd2.getLocData()[currDim]);
  }
}
