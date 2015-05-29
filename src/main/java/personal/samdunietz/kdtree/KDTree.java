package personal.samdunietz.kdtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.collect.Multiset;

/** KDTree for nearest neighbor and k nearest neighbor searches,
 * as well as finding datum within a given radius.
 *
 * Two KDTrees are equal if they contain the same elements, regardless of
 * how they are structured in the tree.
 * @author samdunietz
 *
 * @param <Q> The type of KDData in the tree.
 */
public final class KDTree<Q extends KDData> {

  private final int numDims;
  private final KDNode root;

  private int size = -1;
  private int depth = -1;

  private final static String emptyMessage = "Tree is empty";

  /** Constructs a KDDTree out of a Collection of KDData.
   * @param treeData A list with all the data for the KDTree.
   */
  public KDTree(Collection<Q> treeData) {
    if (treeData == null) {
      throw new IllegalArgumentException("treeData cannot be null");
    }

    if (treeData.isEmpty()) {
      root = null;
      numDims = -1;
      size = 0;
      depth = 0;
    } else {
      // Check all datum have same number of dimensions
      int firstNumDims = treeData.stream().findAny().get().getDims();
      boolean allSameNumDims = treeData.stream()
          .allMatch(d -> d.getDims() == firstNumDims);
      if (!allSameNumDims) {
        throw new IllegalArgumentException(
            "All datum must have same number of dimensions");
      }

      if (firstNumDims == 0) {
        throw new IllegalArgumentException("Datum cannot have 0 dimensions");
      }

      numDims = firstNumDims;

      // Make copy of treeData so sorts in buildSubtree don't affect clients.
      List<Q> treeDataCopy = new ArrayList<Q>(treeData);

      root = buildSubtree(treeDataCopy, new KDDataComparator<Q>(numDims));

      size = treeData.size();
      depth = root.depth();
    }
  }

  private KDTree(KDNode node) {
    numDims = node.data.getDims();
    root = node;

  }

  private KDNode buildSubtree(List<Q> treeData, KDDataComparator<Q> comp) {
    if (treeData.size() == 0) {
      return null;
    }
    if (treeData.size() == 1) {
      return new KDNode(treeData.get(0), comp.getCurrDim(), null, null);
    } else {
      Collections.sort(treeData, comp);

      int middle = treeData.size() / 2;
      Q middleData = treeData.get(middle);
      List<Q> leftTreeData = treeData.subList(0, middle);
      List<Q> rightTreeData = treeData.subList(middle + 1, treeData.size());

      // Get currDim so the KDNode will have correct current dimension
      // info, then increment comp to pass down correct dimension info
      // to subtrees.
      int currDim = comp.getCurrDim();
      comp = comp.incrementDim();

      return new KDNode(middleData, currDim,
          buildSubtree(leftTreeData, comp),
          buildSubtree(rightTreeData, comp));
    }
  }

  /** Gets the number of dimensions of the data in the tree.
   * If the tree is empty, throws NoSuchElementException.
   * @return The number of dimension of the data in the tree.
   */
  public int getNumDims() {
    if (isEmpty()) {
      throw new NoSuchElementException(emptyMessage);
    } else {
      return numDims;
    }
  }

  /** Gets the current dimension the tree is split on.
   * If the tree is empty, throws NoSuchElementException.
   * @return The current dimension the tree is split on.
   * All elements to the left have a lower value for this dimension,
   * while all elements to the right have a higher value.
   */
  public int getCurrDim() {
    if (isEmpty()) {
      throw new NoSuchElementException(emptyMessage);
    } else {
      return root.currDim;
    }
  }

  /** Gives an unordered list of all the elements in the tree.
   * @return An unordered list of the all the elements in the tree.
   */
  public List<Q> toList() {
    if (isEmpty()) {
      return new ArrayList<>();
    } else {
      return root.toList();
    }
  }

  /** Determines if the tree is empty.
   * @return True if the tree has no elements, and false otherwise.
   */
  public boolean isEmpty() {
    return root == null;
  }

  /** Gets the number of elements in the tree.
   * @return The number of elements in the KDTree.
   */
  public int size() {
    // If size == -1, then size has not been previously computed.
    if (size == -1) {
      size = isEmpty() ? 0 : root.size();
    }
    return size;
  }

  /** Gets the maximum depth of the KDTree.
   * @return The maximum depth of the KDTree.
   */
  public int depth() {
 // If depth == -1, then depth has not been previously computed.
    if (depth == -1) {
      depth = isEmpty() ? 0 : root.depth();
    }
    return depth;
  }

  /** Gets the element at the root of the tree.
   * @return The element at the root of the tree. If the tree is empty,
   * returns null.
   */
  public Q getRoot() {
    if (isEmpty()) {
      return null;
    } else {
      return root.data;
    }
  }

  /** Gets the left subtree.
   * @return All elements to the left will have a lower value
   * for the dimension this level of the subtree is dividing on. This
   * dimension is accessible by calling getCurrDim(). If the current root
   * has no children, returns null. If the tree is empty,
   * throws NoSuchElementException.
   */
  public KDTree<Q> getLeft() {
    if (isEmpty()) {
      throw new NoSuchElementException(emptyMessage);
    } else if (root.left == null) {
      return null;
    } else {
      return new KDTree<>(root.left);
    }
  }

  /** Gets the right subtree.
   * @return All elements to the right will have a greater value
   * for the dimension this level of the subtree is dividing on. This
   * dimension is accessible by calling getCurrDim(). If the current root
   * has no children, returns null. If the tree is empty,
   * throws NoSuchElementException.
   */
  public KDTree<Q> getRight() {
    if (isEmpty()) {
      throw new NoSuchElementException(emptyMessage);
    } else if (root.right == null) {
      return null;
    } else {
      return new KDTree<>(root.right);
    }
  }


  /** Gets the nearest neighbor from a given point
   * @param origin The datum from which to find the nearest neighbor.
   * Can be of a different type than Q, so long as the other type
   * has the same dimension. If not, throws IllegalArgumentException.
   * @return The datum nearest to origin in the KDTree. If the tree is empty,
   * throws a NoSuchElementException
   */
  public Q nearestNeighbor(KDData origin) {
    if (isEmpty()) {
      throw new NoSuchElementException(emptyMessage);
    } else if (origin.getDims() != numDims) {
        throw new IllegalArgumentException(
            "Given datum doesn't have same number of dimensions as KDTree");
    }

    return nnHelper(origin, root, root).data;
  }

  private KDNode nnHelper(
      KDData origin, KDNode bestGuess, KDNode curr) {
    // Adapted from pseudocode from
    // http://web.stanford.edu/class/cs106l/handouts/assignment-3-kdtree.pdf

    if (curr == null) {
      return bestGuess;
    }

    bestGuess = closest(origin, bestGuess, curr);

    double currComponentDifference = origin.getComponent(curr.currDim)
        - curr.getComponent(curr.currDim);

    // So we know where we looked if we have to search the other subtree
    boolean searchedLeft;
    if (currComponentDifference < 0) {
      bestGuess = nnHelper(origin, bestGuess, curr.getLeft());
      searchedLeft = true;
    } else {
      bestGuess = nnHelper(origin, bestGuess, curr.getRight());
      searchedLeft = false;
    }

    // Search other subtree if bestGuess isn't good enough
    // (when |currComponentDifference| < distance for bestGuess)
    if (Math.abs(currComponentDifference)
        < bestGuess.data.euclidianDist(origin)) {
      if (searchedLeft) {
        bestGuess = nnHelper(origin, bestGuess, curr.getRight());
      } else {
        bestGuess = nnHelper(origin, bestGuess, curr.getLeft());
      }
    }
    return bestGuess;
  }

  /** Gets the k nearestest neighbors to a given point.
   * @param origin The datum from which to find the nearest neighbors.
   * Can be of a different type than Q, so long as the other type
   * has the same dimension. If not, throws IllegalArgumentException.
   * @param k The number of nearest neighbors to find.
   * @return The k nearest datum to origin in the KDTree.
   * If the tree is empty, returns an empty list.
   */
  public <R extends KDData> List<Q> kNearestNeighbor(R origin, int k) {
    if (isEmpty()) {
      return new ArrayList<>();
    } else if (origin.getDims() != numDims) {
      throw new IllegalArgumentException(
          "Given datum doesn't have same number of dimensions as KDTree");
    }

    if (k <= 0) {
      throw new IllegalArgumentException(
          "ERROR: k must be an integer greater than zero.");
    } else {
      ClosestComparator<R, Q> comp = new ClosestComparator<>(origin);
      MinMaxPriorityQueue<Q> queue =
          MinMaxPriorityQueue.orderedBy(comp)
          .maximumSize(k).create();
      MinMaxPriorityQueue<Q> bestGuesses =
          knnHelper(origin, queue, root, k);

      List<Q> kNearestNeighbors =
          castObjects(bestGuesses.toArray());

      // Before following, list is unsorted by distance
      Collections.sort(kNearestNeighbors, comp);
      return kNearestNeighbors;
    }
  }

  private MinMaxPriorityQueue<Q> knnHelper(KDData origin,
      MinMaxPriorityQueue<Q> bestGuesses, KDNode curr, int k) {

    if (curr == null) {
      return bestGuesses;
    }

    // Adds current node's data to bestGuess. This will do nothing
    // if bestGuesses is full and the current node's data is worse
    // than the worst datum in bestGuesses.
    bestGuesses.add(curr.data);

    double currComponentDifference =
        origin.getComponent(curr.currDim)
        - curr.getComponent(curr.currDim);

    // So we know where we looked if we have to search the other subtree
    boolean searchedLeft;
    if (currComponentDifference < 0) {
      bestGuesses = knnHelper(origin, bestGuesses, curr.getLeft(), k);
      searchedLeft = true;
    } else {
      bestGuesses = knnHelper(origin, bestGuesses, curr.getRight(), k);
      searchedLeft = false;
    }

    // Search other subtree if bestGuesses doesn't have k elements
    // or if its guesses aren't good enough
    // (when |currComponentDifference| < dist for worst element of bestGuesses)
    boolean bestGuessesIsFull = bestGuesses.size() == k;
    boolean guessesNotGoodEnough =
        Math.abs(currComponentDifference)
        // Distance between the farthest element in bestGuesses
        < bestGuesses.peekLast().euclidianDist(origin);

    if (!bestGuessesIsFull || guessesNotGoodEnough) {
      if (searchedLeft) {
        bestGuesses = knnHelper(
            origin, bestGuesses, curr.getRight(), k);
      } else {
        bestGuesses = knnHelper(
            origin, bestGuesses, curr.getLeft(), k);
      }
    }

    return bestGuesses;
  }

  /** Gets all elements within a given radius of a given point.
   * @param <R> The type of the origin. Must be a KDData with the same
   * dimension as the KDTree.
   * @param origin The datum from which to find data within a given radius.
   * Can be of a different type than Q, so long as the other type
   * has the same dimension. If not, throws IllegalArgumentException.
   * @param radius The euclidian radius within which to give results. If less than 0,
   * throws IllegalArgumentException.
   * @return All the data within the euclidian distance of origin. If the tree is empty,
   * returns an empty list.
   */
  public <R extends KDData> List<Q> findWithinRadius(R origin, double radius) {
    if (isEmpty()) {
      return new ArrayList<>();
    }
    if (origin.getDims() != numDims) {
      throw new IllegalArgumentException(
          "Given datum doesn't have same number of dimensions as KDTree");
    }

    if (radius < 0) {
      throw new IllegalArgumentException("Radius must be 0 or greater");
    }

    else {
      List<Q> withinRadius = fwrHelper(
          new ArrayList<Q>(), origin, radius, root);
      ClosestComparator<R, Q> comp = new ClosestComparator<>(origin);
      Collections.sort(withinRadius, comp);
      return withinRadius;
    }
  }

  private List<Q> fwrHelper(
      List<Q> withinRadius, KDData origin, double radius, KDNode curr) {

    if (curr == null) {
      return withinRadius;
    }

    if (origin.euclidianDist(curr.data) < radius) {
      withinRadius.add(curr.data);
    }

    double currComponentDifference = origin.getComponent(curr.currDim)
        - curr.getComponent(curr.currDim);

    // So we know where we looked if we have to search the other subtree
    boolean searchedLeft;
    if (currComponentDifference < 0) {
      fwrHelper(withinRadius, origin, radius, curr.getLeft());
      searchedLeft = true;
    } else {
      fwrHelper(withinRadius, origin, radius, curr.getRight());
      searchedLeft = false;
    }

    // Search other subtree if it's possible it has points within radius
    // (when |currComponentDifference| < radius)
    if (Math.abs(currComponentDifference) < radius) {
      if (searchedLeft) {
        fwrHelper(withinRadius, origin, radius, curr.getRight());
      } else {

        fwrHelper(withinRadius, origin, radius, curr.getLeft());
      }
    }

    return withinRadius;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    KDTree other = (KDTree) obj;
    Multiset<Q> elements = HashMultiset.create(toList());
    Multiset otherElements = HashMultiset.create(other.toList());
    if (!elements.equals(otherElements)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    Multiset<Q> elements = HashMultiset.create(toList());
    final int prime = 31;
    int result = 1;
    result = prime * result + ((elements == null) ? 0 : elements.hashCode());
    return result;
  }

  private KDNode closest(KDData origin, KDNode a, KDNode b) {
    if (origin.euclidianDist(a.data) < origin.euclidianDist(b.data)) {
      return a;
    } else {
      return b;
    }
  }

  private List<Q> castObjects(Object[] l) {
    ArrayList<Q> newList = new ArrayList<Q>();
    for (Object o : l) {
      newList.add((Q) (o));
    }
    return newList;
  }

  /** Internal KDNode class, for nodes of KDTree.
   * @author samdunietz
   *
   */
  private final class KDNode {

    private final Q data;
    private final int currDim;
    private final KDNode left;
    private final KDNode right;

    private KDNode(Q data, int currDim, KDNode left, KDNode right) {
      this.data = data;
      this.currDim = currDim;
      this.left = left;
      this.right = right;
    }

    private List<Q> toList() {
      List<Q> list = Lists.newArrayList(data);
      toListHelper(list, left);
      toListHelper(list, right);
      return list;
    }

    // Mutates list along left and right subtrees.
    private void toListHelper(List<Q> list, KDNode node) {
      if (node != null) {
        list.add(node.data);
        toListHelper(list, node.left);
        toListHelper(list, node.right);
      }
    }

    private double getComponent(int dim) {
      return data.getComponent(dim);
    }

    private KDNode getLeft() {
      return left;
    }

    private KDNode getRight() {
      return right;
    }

    private int size() {
      if (left == null && right == null) {
        return 1;
      } else if (left != null && right == null) {
        return 1 + left.size();
      } else if (left == null && right != null) {
        return 1 + right.size();
      } else {
        return 1 + left.size() + right.size();
      }
    }

    private int depth() {
      if (left == null && right == null) {
        return 1;
      } else if (left != null && right == null) {
        return 1 + left.depth();
      } else if (left == null && right != null) {
        return 1 + right.depth();
      } else {
        return 1 + Math.max(left.depth(), right.depth());
      }
    }
  }
}
