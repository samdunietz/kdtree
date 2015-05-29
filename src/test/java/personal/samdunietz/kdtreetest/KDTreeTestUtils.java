package personal.samdunietz.kdtreetest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import personal.samdunietz.kdtree.ClosestComparator;
import personal.samdunietz.kdtree.KDData;

public class KDTreeTestUtils {

	public static <Q extends KDData, R extends KDData>  Q nearestNeighbor(
			Collection<Q> l, R origin) {
	 return l.stream()
	     .min(new ClosestComparator<>(origin))
	     .get();
	}

	public static <Q extends KDData, R extends KDData> List<Q> kNearestNeighbor(
			Collection<Q> l, R origin, int k) {
		return l.stream()
		    .sorted(new ClosestComparator<>(origin))
		    .limit(k)
		    .collect(Collectors.toList());
	}

	// Hack hack hack
	private static ArrayList<KDData> castObjects(Object[] l) {
		ArrayList<KDData> newList = new ArrayList<>();
		for (Object o : l) {
			newList.add((KDData)(o));
		}
		return newList;
	}

	public static <Q extends KDData, R extends KDData> List<Q> findWithinRadius(
			Collection<Q> l, R origin, double radius) {
		return l.stream()
		    .filter(d -> d.euclidianDist(origin) < radius)
		    .sorted(new ClosestComparator<>(origin))
		    .collect(Collectors.toList());

	}


}
