package lbs.wifiparticlefilter.data;


/**
 * Represents the coordinate of a particle. 
 * 
 * @author Andreas Attenberger
 * @version 1.0
 */
public class Point {

	
	private final int x;
	private final int y;
	
	
	/**
	 * C'tor 
	 * 
	 * @param x x-coordinate
	 * @param y y-coordinate
	 */
	public Point(int x, int y){
		
		this.x = x;
		this.y = y;
	}


	/**
	 * Gets the x-coordinate of a point-object.
	 * 
	 * @return x-coordinate
	 */
	public int getX() {
		return x;
	}


	/**
	 * Gets the y-coordinate of a point-object.
	 * 
	 * @return y-coordinate
	 */
	public int getY() {
		return y;
	}	
	
	
	/**
	 * Compares two Point-object. 
	 * 
	 * @return true, if equal
	 */
	public boolean equals(Object that){
		
		if(that == null)
			return false;
		
		if (getClass() != that.getClass())
			return false;
		
		if (!super.equals(that))
			return false;
		
		return hashCode() == ((Point) that).hashCode();
	}
	
	
	/**
	 * Method checks, if Point is in the first quadrant.
	 *  
	 * @param p Point to check
	 * @return true, if p is in first quadrant
	 */
	public boolean quad1st(Point p){
		return ((this.x <= p.getX()) && (this.y <= p.getY()));
	}
	
	
	/**
	 * Method checks, if Point is in the second quadrant.
	 * 
	 * @param p Point to check
	 * @return true, if p is in second quadrant
	 */
	public boolean quad2nd(Point p){
		return ((this.x >= p.getX()) && (this.y <= p.getY()));
	}
	
	
	/**
	 * Method checks, if Point is in the third quadrant.
	 * 
	 * @param p Point to check
	 * @return true, if p is in third quadrant
	 */
	public boolean quad3rd(Point p){
		return ((this.x >= p.getX()) && (this.y >= p.getY()));
	}
	
	
	/**
	 * Method checks, if Point is in the fourth quadrant.
	 * 
	 * @param p Point to check
	 * @return true, if p is in fourth quadrant
	 */
	public boolean quad4th(Point p){
		return ((this.x >= p.getX()) && (this.y <= p.getY()));
	}
}
