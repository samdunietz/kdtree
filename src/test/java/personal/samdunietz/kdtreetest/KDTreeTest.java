package personal.samdunietz.kdtreetest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import personal.samdunietz.kdtree.ClosestComparator;
import personal.samdunietz.kdtree.KDData;
import personal.samdunietz.kdtree.KDTree;

import com.google.common.collect.Lists;

public class KDTreeTest {

  @Test
  public void nullInput() {
    try {
      KDTree<LatLng> locs = new KDTree<>(null);
      fail();
    } catch (IllegalArgumentException e) { }
  }

  @Test
  public void emptyInput() {
    KDTree<LatLng> emptyTree = new KDTree<>(new ArrayList<LatLng>());
    assertTrue(emptyTree.isEmpty());
    assertEquals(emptyTree.depth(), 0);
    assertEquals(emptyTree.size(), 0);
    assertEquals(emptyTree.getRoot(), null);
    assertEquals(emptyTree, new KDTree<>(new ArrayList<LatLng>()));

    assertEquals(
        emptyTree.kNearestNeighbor(randLatLng(), 10),
        new ArrayList<LatLng>());
    assertEquals(
        emptyTree.findWithinRadius(randLatLng(), 10),
        new ArrayList<LatLng>());

    try {
      emptyTree.nearestNeighbor(randLatLng());
      fail();
    } catch (NoSuchElementException e) { }

    try {
      emptyTree.getLeft();
      fail();
    } catch (NoSuchElementException e) { }

    try {
      emptyTree.getRight();
      fail();
    } catch (NoSuchElementException e) { }

    try {
      emptyTree.getCurrDim();
      fail();
    } catch (NoSuchElementException e) { }

    try {
      emptyTree.getNumDims();
      fail();
    } catch (NoSuchElementException e) { }
  }

  @Test
  public void equalsTest() {
    KDTree<LatLng> emptyTree1 = new KDTree<>(new ArrayList<LatLng>());
    KDTree<LatLng> emptyTree2 = new KDTree<>(new ArrayList<LatLng>());
    assertEquals(emptyTree1, emptyTree2);

    List<LatLng> locs = randLatLngs(20);
    KDTree<LatLng> t1 = new KDTree<>(locs);
    Collections.shuffle(locs);
    KDTree<LatLng> t2 = new KDTree<>(locs);
    assertEquals(t1, t2);
    KDTree<LatLng> t3 = new KDTree<>(new HashSet<>(locs));
    assertEquals(t1, t3);
  }

  @Test
  public void sizeAndDepthTests() {
    Set<LatLng> locs = new HashSet<>(randLatLngs(50));
    KDTree<LatLng> kdt = new KDTree<>(locs);
    assertEquals(kdt.size(), 50);
    assertEquals(kdt.depth(), 6);

    Set<LatLng> expectedLeft = getExpectedLeft(locs, kdt);
    KDTree<LatLng> leftTree = kdt.getLeft();
    assertEquals(expectedLeft.size(), leftTree.size());
    assertEquals(leftTree.depth(), 5);
  }

  @Test
  public void sizeOne() {
    LatLng loc = new LatLng(40.0, 50.0);
    List<LatLng> locList = Lists.newArrayList(loc);
    KDTree<LatLng> t = new KDTree<>(locList);
    assertEquals(t.size(), 1);
    assertEquals(t.depth(), 1);
    LatLng o = new LatLng(20.0, 60.0);
    assertEquals(t.nearestNeighbor(o), loc);
    assertEquals(t.kNearestNeighbor(o, 10), Lists.newArrayList(loc));
    assertEquals(t.findWithinRadius(o, 10), new ArrayList<>());
    assertEquals(t.findWithinRadius(o, 50), Lists.newArrayList(loc));
    assertEquals(t.toList(), locList);
  }

  @Test
  public void closestComparatorTest() {
    List<LatLng> locs = randLatLngs(40);
    LatLng origin = randLatLng();
    ClosestComparator<LatLng, LatLng> comp = new ClosestComparator<>(origin);
    assertEquals(locs.stream().min(comp).get(), findClosest(locs, origin));
  }

  private <Q extends KDData> Q findClosest(List<Q> locs, Q origin) {
    Q closest = null;
    double minDistance = Double.MAX_VALUE;
    for (Q loc : locs) {
      double dist = loc.euclidianDist(origin);
      if (dist < minDistance) {
        closest = loc;
        minDistance = dist;
      }
    }

    return closest;
  }

  class Foo implements KDData {
    double x;
    double y;

    public Foo(double x, double y) {
      this.x = x;
      this.y = y;
    }

    @Override
    public double[] getLocData() {
      // TODO Auto-generated method stub
      return new double[]{x, y};
    }

    @Override
    public int getDims() {
      // TODO Auto-generated method stub
      return 2;
    }

    @Override
    public String toString() {
      return "Foo [x=" + x + ", y=" + y + "]";
    }
  }

  @Test
  public void differentTypeSameDimension() {
    List<LatLng> locs = randLatLngs(50);

    KDTree<LatLng> kdt = new KDTree<>(locs);
    double radius = 20.0;
    Foo random = new Foo(40.0, 40.0);
    assertEquals(
        kdt.findWithinRadius(random, radius),
        KDTreeTestUtils.findWithinRadius(locs, random, radius));

    assertEquals(
        kdt.nearestNeighbor(random),
        KDTreeTestUtils.nearestNeighbor(locs, random));

    assertEquals(
        kdt.kNearestNeighbor(random, 10),
        KDTreeTestUtils.kNearestNeighbor(locs, random, 10));
  }

  class Bar implements KDData {
    double x, y, z;

    public Bar(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }

    @Override
    public double[] getLocData() {
      return new double[]{x, y, z};
    }
  }

  @Test
  public void badComparison() {
    try {
      List<LatLng> locs = randLatLngs(50);

      KDTree<LatLng> kdt = new KDTree<LatLng>(locs);
      Bar bar = new Bar(5.2, 3.6, 9.0);
      kdt.nearestNeighbor(bar);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals(
          e.getMessage(),
          "Given datum doesn't have same number of dimensions as KDTree");
    }
  }

  @Test
  public void bruteForce() {
    for (int i = 0; i < 5; i++) {
      for (int kdtSize = 1; kdtSize < 50; kdtSize++) {
        singleBruteForceTest(kdtSize);
      }
    }

   singleBruteForceTest(1000);
  }

  private void singleBruteForceTest(int kdtSize) {
    List<LatLng> locs = randLatLngs(kdtSize);

    KDTree<LatLng> kdt = new KDTree<LatLng>(locs);
    structureIsCorrectHelper(kdt);

    Random r = new Random();
    LatLng origin = locs.get(r.nextInt(locs.size()));
    double radius = 30.0;
    assertEquals(
        kdt.findWithinRadius(origin, radius),
        KDTreeTestUtils.findWithinRadius(locs, origin, radius));
    assertEquals(
        kdt.nearestNeighbor(origin),
        KDTreeTestUtils.nearestNeighbor(locs, origin));

    int numNeighbors = 10;
    assertEquals(
        kdt.kNearestNeighbor(origin, numNeighbors),
        KDTreeTestUtils.kNearestNeighbor(locs, origin, numNeighbors));
  }

  private LatLng randLatLng() {
    Random r = new Random();
    double lat = r.nextDouble() + r.nextInt(90);
    double lng = r.nextDouble() + r.nextInt(90);
    return new LatLng(lat, lng);
  }

  private List<LatLng> randLatLngs(int k) {
    List<LatLng> locs = new ArrayList<>();
    for (int i = 0; i < k; i++) {
      locs.add(randLatLng());
    }
    return locs;
  }

  @Test
  public void toListTest() {
    Set<LatLng> locs = new HashSet<>(randLatLngs(50));
    KDTree<LatLng> kdt = new KDTree<LatLng>(locs);

    Set<LatLng> kdtLocs = new HashSet<>(kdt.toList());
    assertEquals(locs, kdtLocs);

    Set<LatLng> expectedLeft = locs.stream()
        .filter(loc -> !loc.equals(kdt.getRoot()))
        .filter(loc -> loc.getComponent(0) < kdt.getRoot().getComponent(0))
        .collect(Collectors.toSet());
    Set<LatLng> actual = new HashSet<>(kdt.getLeft().toList());
    assertEquals(expectedLeft, actual);
  }

  @Test
  public void getLeftAndGetRight() {
    Set<LatLng> locs = new HashSet<>(randLatLngs(50));
    KDTree<LatLng> kdt = new KDTree<>(locs);

    Set<LatLng> expectedLeft = getExpectedLeft(locs, kdt);
    Set<LatLng> actual = new HashSet<>(kdt.getLeft().toList());
    assertEquals(expectedLeft, actual);

    Set<LatLng> expectedRight = getExpectedRight(locs, kdt);
    actual = new HashSet<>(kdt.getRight().toList());
    assertEquals(expectedRight, actual);
  }

  @Test
  public void structureIsCorrect() {
    for (int i = 0; i < 5; i++) {
      for (int kdtSize = 1; kdtSize < 50; kdtSize++) {
        KDTree<LatLng >kdt = new KDTree<>(randLatLngs(kdtSize));
        structureIsCorrectHelper(kdt);
      }
    }
  }

  private <Q extends KDData> Set<Q> getExpectedLeft(Set<Q> locs, KDTree<Q> kdt) {
    assert locs.size() == kdt.size();
    return locs.stream()
        .filter(loc -> !loc.equals(kdt.getRoot()))
        .filter(loc -> loc.getComponent(0) < kdt.getRoot().getComponent(0))
        .collect(Collectors.toSet());
  }

  private <Q extends KDData> Set<Q> getExpectedRight(Set<Q> locs, KDTree<Q> kdt) {
    assert locs.size() == kdt.size();
    return locs.stream()
        .filter(loc -> !loc.equals(kdt.getRoot()))
        .filter(loc -> loc.getComponent(0) > kdt.getRoot().getComponent(0))
        .collect(Collectors.toSet());
  }

  private <Q extends KDData> void structureIsCorrectHelper(KDTree<Q> kdt) {
    if (kdt != null) {
      assertTrue(isLeftLess(kdt));
      assertTrue(isRightMore(kdt));
      assertTrue(isDimIncremented(kdt));
      assertTrue(isBalanced(kdt));
      structureIsCorrectHelper(kdt.getLeft());
      structureIsCorrectHelper(kdt.getRight());
    }
  }

  private <Q extends KDData> boolean isDimIncremented(KDTree<Q> kdt) {
    if (kdt == null) {
      return true;
    }

    int currDim = kdt.getCurrDim();
    int numDim = kdt.getNumDims();
    int expectedNextDim = (currDim + 1) % numDim;
    if (kdt.getRight() != null) {
      if (kdt.getRight().getCurrDim() != expectedNextDim) {
        return false;
      }
    }
    if (kdt.getLeft() != null) {
      if (kdt.getLeft().getCurrDim() != expectedNextDim) {
        return false;
      }
    }

    return true;
  }

  private <Q extends KDData> boolean isLeftLess(KDTree<Q> kdt) {
    if (kdt == null || kdt.getLeft() == null) {
      return true;
    } else {
      int currDim = kdt.getCurrDim();
      return kdt.getLeft().toList().stream()
          .allMatch(d -> d.getComponent(currDim)
              < kdt.getRoot().getComponent(currDim));
    }
  }

  private <Q extends KDData> boolean isRightMore(KDTree<Q> kdt) {
    if (kdt == null || kdt.getRight() == null) {
      return true;
    } else {
      int currDim = kdt.getCurrDim();
      return kdt.getRight().toList().stream()
          .allMatch(d -> d.getComponent(currDim)
              > kdt.getRoot().getComponent(currDim));
    }
  }

  private <Q extends KDData> boolean isBalanced(KDTree<Q> kdt) {
    if (kdt == null) {
      return true;
    }
    if (kdt.getLeft() == null) {
      return kdt.getRight() == null || kdt.getRight().size() == 1;
    }
    if (kdt.getRight() == null) {
      return kdt.getLeft() == null || kdt.getLeft().size() == 1;
    }

    return Math.abs(kdt.getLeft().depth() - kdt.getRight().depth()) <= 1;
  }
}
