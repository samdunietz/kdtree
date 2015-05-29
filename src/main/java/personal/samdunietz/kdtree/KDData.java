package personal.samdunietz.kdtree;

/** Interface required for using KDTree.
 * @author samdunietz
 *
 */
public interface KDData {

  /** Gets the euclidian distance from another KDData.
   * @param other The other KDData. The the other KDData has a different
   * number of dimensions, throws IllegalArgumentException.
   * @return The euclidian distance from other, obtained by taking the
   * square root of the squares of the distances of each component.
   */
  default double euclidianDist(KDData other) {
    if (other.getDims() != getLocData().length) {
      throw new IllegalArgumentException(
          "Two datum must have same number of dimensions");
    }

    double distanceSquared = 0;
    double[] otherLoc = other.getLocData();
    double[] thisLoc = getLocData();
    for (int i = 0; i < thisLoc.length; i++) {
      distanceSquared += Math.pow(otherLoc[i] - thisLoc[i], 2);
    }
    return Math.sqrt(distanceSquared);
  }

  /** Gets a given component.
   * @param dim The component to get. For example, for a point (x, y),
   * 0 would be for x and 1 for y. If dim is too high for the dimensionality
   * of the data or below 0, will throw IllegalArgumentException.
   * @return The component at dim. For example, for a point (x, y),
   * getComponent(0) would return the x component, and getComponent(1) will
   * return the y component.
   */
  default double getComponent(int dim) {
    if (dim < 0) {
      throw new IllegalArgumentException("dim cannot be below 0");
    } else if (dim > getDims() - 1) {
      throw new IllegalArgumentException(
          "dim is too great for the dimensionality of this KDData");
    }

    return getLocData()[dim];
  }

  /** Gets an array representing the KDData's location.
   * @return An array of doubles representing the KDData's location
   * (e.g. [3, 4, 2] for x = 3, y = 4, x = 2).
   */
  public double[] getLocData();

  /** Gets the number of dimensions the KDData has.
   * @return The number of dimensions the KDData has, which will be
   * the same as getLocData().length.
   */
  default int getDims() {
    return getLocData().length;
  }
}
