package lbs.wifiparticlefilter.data;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
* Represents a particle. A Particle-object contains coordinates, list of 
* measurements and weight.
* 
* @author Andreas Attenberger
* @version 1.0
*/
public class Particle {
	

	// Particle-weight
	private double weight;
	// Coordinates
	private Point point;
	// List with measurements
	private List<Measure> measure;
	
	
	/**
	 * C'tor; Particle-weight is infinity by default
	 * 
	 * @param point Object for coordinates
	 * @param measure List with measurements
	 */
	public Particle(Point point, List<Measure> measure){
		
		this.measure = measure; 						
		this.point = point;
		this.weight = Double.POSITIVE_INFINITY;	
	}
	
	
	/**
	 * C'tor
	 * 
	 * @param point Object for coordinates
	 * @param measure List with measurements
	 * @param weight Weight of a Particle
	 */
	public Particle(Point point, List<Measure> measure, double weight){
		
		this.measure = measure; 						
		this.point = point;
		this.weight = weight;	
	}

	
	/**
	 * C'tor; Only position of a Particle is known.
	 * 
	 * @param point Object for coordinates
	 */
	public Particle(Point point){
		
		// new list for measurements
		this.measure = new ArrayList<Measure>(); 						
		this.point = point;
		this.weight = Double.POSITIVE_INFINITY;	
	}
	

	/**
	 * Gets the x-coordinate of a particle.
	 * 
	 * @return x-coordinate
	 */
	public int getX() {
		return point.getX();
	}


	/**
	 * Gets the x-coordinate of a particle.
	 * 
	 * @return c-Koordinate
	 */
	public int getY() {
		return point.getY();
	}
	
	
	/**
	 * Gets the weight of a particle. If the particle hasn't a initial-weight
	 * the default-weight is infinity.
	 * 
	 * @return Gewicht als Double-Wert
	 */
	public double getWeight() {
		return weight;
	}


	/**
	 * Sets a new weight for the particle.
	 * 
	 * @param weight New Particle-weight
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}


	/**
	 * Gets the Point-object of a particle, which contains the coordinates.
	 * 
	 * @return Point-object with x- & y-coordinate
	 */
	public Point getPoint() {
		return point;
	}


	/**
	 * Gets a list with all measures on actual position.
	 * 
	 * @return List with type Measure
	 */
	public List<Measure> getMeasure() {
		return measure;
	}
	
	
	/**
	 * Adds a new Measure to the list.
	 * 
	 * @param m New measure 
	 * @return true, if add was successful
	 */
	public boolean addMeasure(Measure m){
		
		// add a measure to list
		return measure.add(m);
	}


	/**
	 * Gets a String representation of a particle.
	 * 
	 * @return String with all attributes of the particle
	 */
	public String toString(){
		
		String s1 = null;
		
		// Iterator ueber alle Messungen
		Iterator<Measure> miter = measure.iterator();
		
		while (miter.hasNext()){
			
			String s2;
			Measure tmp = miter.next();
			s2 = "MAC: " + tmp.getBSSID() + " Signal: " + tmp.getRssi() + "    ";
			
			if (s1 != null)
				s1 = s1 + s2;
			else
				s1 = s2;
		}
		
		return String.format("x: %d, y: %d,    %s \n", getX(), getY(), s1);
	}
	
}

