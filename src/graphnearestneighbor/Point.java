
package graphnearestneighbor;

/**
 *
 * @author Nil
 */
class Point {
	double x, y;

	Point(double x, double y) {
		this.x = x; this.y = y;
	}
	
	static double dist(Point first, Point second) {
		double xDiff = first.x - second.x;
		double yDiff = first.y - second.y;
		return Math.sqrt(xDiff*xDiff + yDiff*yDiff);
	}
}
