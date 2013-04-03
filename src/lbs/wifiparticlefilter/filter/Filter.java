package lbs.wifiparticlefilter.filter;


import java.util.ArrayList;
import java.util.List;

import lbs.wifiparticlefilter.data.Measure;
import lbs.wifiparticlefilter.data.Particle;
import lbs.wifiparticlefilter.data.Point;


/**
 * Class contains filter-core for localisation by wifi-measurements.
 * 
 * @author Andreas Attenberger
 * @version 1.9
 */
public class Filter {

	
	// List with wifi-measurements from datebase
	private List<Particle> db_list;
	// Array for room-dilation; [xmin xmax ymin ymax]
	private int[] border = new int[4];
	// Number of particles
	private int num_part;
	// List for "moving"-particles
	private List<Particle> part_list;	
	// Tuning-factor for degeneration-threshold
	double threshold = 0.7;	
	// List for actual wifi-measurement
	private List<Measure> current = null;
	
	
	/**
	 * C'tor
	 * 
	 * @param particles Number of particles for creation
	 * @param database List with all measured particles from database
	 */
	public Filter(int particles, List<Particle> database){
		
		num_part = particles;
		part_list = new ArrayList<Particle>();	
		db_list = database;
		
		// calc dilation of room
		border = this.calcBorder();		
	}
	
	
	/**
	 * Method creates and initializes all particles. 
	 */
	public void initFilter(){
		
		// calc initial weight
		double initial_weight = 1.0 / num_part;
		
		for (int i=0; i<num_part; i++){
			
			// create random point
			Point p = createRandomPoint();
			
			// new particle on point p
			Particle part = new Particle(p);
			
			// interpolate particle part by his neighbors from database
			InterpolateParticle ip = new InterpolateParticle(part, db_list);
			Particle clean = ip.interpolatedParticle();
			
			// set intial weight
			clean.setWeight(initial_weight);
			
			// add particle to list
			part_list.add(clean);	
		}	
	}
	
	
	/**
	 * Method considers actual measure and calcs new weight.
	 * 
	 */
	public void estimate(){
		
		for (int i=0; i<part_list.size(); i++){
			
			// get particle from list
			Particle tmp = part_list.get(i);
		
			calcWeight(tmp);
		}
		
		// normalize weights
		part_list = normWeight(part_list);		
	}
	
	
	/**
	 * Creates particles on their probability-density-function. More probable
	 * particles are propagated. Less probable particles will be rejected and  
	 * replaced by new more probable particles. 
	 * 
	 */
	public void propagate(){
		
		// calcs sum of all weights
		double sum = sumWeight(part_list);
		
		// mean weight overall particles
		double mean_weight = sum / num_part;
		
		// List for particles
		List<Particle> tmp_list = new ArrayList<Particle>();
		
		double mean_x = 0;
		double mean_y = 0;

		for (int i=0; i<part_list.size(); i++){
			
			// get particle from list
			Particle p = part_list.get(i);
			// weight of particle
			double weight = p.getWeight();
			
			// if particle has more weight than average particle, write to list
			if (weight >= mean_weight){
				
				tmp_list.add(p);		
				
				// calc sum of coordinates
				mean_x = mean_x + p.getX();
				mean_y = mean_y + p.getY();				
			}
		}
		
		// calc mean coordinate
		mean_x = Math.round(mean_x / tmp_list.size());
		mean_y = Math.round(mean_y / tmp_list.size());
		int x = (int) mean_x;
		int y = (int) mean_y;
		
		
		// difference between all particles and more probable particles
		int diff = num_part - tmp_list.size();
		
		// there must be a difference, except for completely degeneration 
		if (diff > 0){
			
			// initial weight
			double initial_weight = 1.0 / num_part;
			
			// create new particles according to difference 
			for (int j=0; j<diff; j++){
				
				Point new_coord;
				Particle part;
				
				// only if there is a mean position of predicted particle
				if ((x != 0) && (y != 0)){
					
					// new particle on pseudo-random point near predicted point
					new_coord = createPseudoRandomPoint(x,y);
					part = new Particle(new_coord);
					
				} else {
					
					// new particle on random point
					new_coord = createRandomPoint();
					part = new Particle(new_coord);					
				}
				
				// new particle on random point
				//Point new_coord = createRandomPoint();
				//Particle part = new Particle(new_coord);
				
				// interpolate from neigborhood
				InterpolateParticle ip = new InterpolateParticle(part, db_list);
				Particle clean = ip.interpolatedParticle();

				// set new weight 
				clean.setWeight(initial_weight);

				tmp_list.add(clean);					
			}			
		}
		
		// update particlelist
		part_list = tmp_list;		
	}
	
	
	/**
	 * Method creates a random point-object.
	 *  
	 * @return Point-Object with coordinates
	 */
	public Point createRandomPoint(){
		
		// generate random numbers
		double x = Math.random() * (border[1] - border[0]) + border[0];
		double y = Math.random() * (border[3] - border[2]) + border[2];

		// typecast necessary
		int tx = (int)Math.round(x); 	
		int ty = (int)Math.round(y); 

		// new point-object for coordinates
		Point point = new Point(tx, ty);
		
		return point;
	}
	
	
	/**
	 * Searches for min/max-values in the database and saves them on a array.
	 * 
	 * @return Array with border-values; [xmin xmax ymin ymax]
	 */
	private int[] calcBorder(){		
		
		// array with border-values; [xmin xmax ymin ymax]
		int[] border = new int[]{Integer.MAX_VALUE, 0, Integer.MAX_VALUE, 0};
		
		for (int i=0; i<db_list.size(); i++){
			
			// get values from list
			int x = db_list.get(i).getX();
			int y = db_list.get(i).getY();
			
			// xmin
			if (x < border[0])				
				border[0] = x;
			// xmax
			if (x > border[1])
				border[1] = x;
			// ymin
			if (y < border[2])				
				border[2] = y;
			// ymax
			if (y > border[3])
				border[3] = y;			
		}
		
		return border;
	}
	
	
	/**
	 * returns the border
	 * 
	 * @return the border array
	 */
	public int[] getBorder()
	{
		return this.border;
	}
	
	
	/**
	 * Method updates a wifi-measurement on actual position.
	 * 
	 * @param m Measure-List with all actual measurements
	 */
	public void setCurrentMeasure(List<Measure> m){
		this.current = m;
	}
	
	
	/**
	 * Method gives back all particle as list.
	 * 
	 * @return List of type Particle
	 */
	public List<Particle> getParticles(){
		return part_list;
	}
	
	
	/**
	 * Random particles get interpolated measurements from their neighborhood.  
	 * 
	 * @param part List with random particles
	 * @param db List with particles from database
	 * @return List with interpolated particles
	 */
	public List<Particle> interpolate(List<Particle> part, List<Particle> db){		
		
		// List with interpolated particles
		List<Particle> plist = new ArrayList<Particle>();
		
		for (int i=0; i<part.size(); i++){

			// particle from list
			Particle p = part.get(i);

			// interpolate them
			InterpolateParticle ip = new InterpolateParticle(p, db);
			Particle mean = ip.interpolatedParticle();

			// add to list
			plist.add(mean);
		}

		return plist;				
	}


	/**
	 * Calculates new weight for a particle. 
	 * 
	 * @param p Particle whose weight will be calculated
	 */
	private void calcWeight(Particle p){
		
		double weight = 0;
	
		// list with new weights 
		List<Double> weight_list = new ArrayList<Double>();		
		List<Measure> mlist = p.getMeasure();
			
		// measures at this location
		for (int i=0; i<current.size(); i++){
			
			String ssid = current.get(i).getBSSID();
			
			// all measures from generated particle
			for (int j=0; j<mlist.size(); j++){
				
				String ssid_part = mlist.get(j).getBSSID();
				
				// same ssids --> I can calculate a new weight 
				if (ssid.equals(ssid_part)){
				
					// rssi from actual location
					int rssi_curr = current.get(i).getRssi();
					// rssi from particle
					int rssi_part = mlist.get(j).getRssi();
					
					// calc new weight; typecast is necessary
					weight = ((double) rssi_curr) / ((double) rssi_part);
					
					// get percentage
					if (weight > 1.0)
						weight = 1.0 / weight;
				
					// add to list
					weight_list.add(weight);
				}				
			}
		}
	
		double sum = 0;
		int number_weights = weight_list.size();
			
		// calc sum of all weights
		for (int k=0; k<number_weights; k++)
			sum = sum + weight_list.get(k);
		
		//double mean_weight = sum / number_weights;
		double mean_weight = sum / current.size();

		// set new weight 
		p.setWeight(mean_weight);
	}

	
	/**
	 * Calculates the sum of all particle-weights.
	 * 
	 * @param list List with all particles
	 * @return Sum of weights
	 */
	private double sumWeight(List<Particle> list){
		
		double sum = 0;
		
		for (int i=0; i<list.size(); i++){
			
			// get weights & calc sum
			sum = sum + list.get(i).getWeight();
		}
		
		return sum;
	}	
	
	
	/**
	 * Normalizes all particle-weights.
	 * 
	 * @param list List with all particles
	 * @return Particlelist with normalized weights
	 */
	private List<Particle> normWeight(List<Particle> list){
		
		// sum all weights 
		double sum = sumWeight(list);
		
		for (int i=0; i<list.size(); i++){
			
			// calc normalized weights
			double new_weight = list.get(i).getWeight() / sum;
			
			// set new weights
			list.get(i).setWeight(new_weight);
		}
		
		return list;
	}	

	
	/**
	 * Calculates a pseudo-random Point-object with coordinates near input
	 * arguments.
	 * 
	 * @param x_mean x-coordinate; center-point of new area
	 * @param y_mean y-coordinate; center-point of new area
	 * @return New Point-object with pseudo-random coordinates
	 */
	private Point createPseudoRandomPoint(int x_mean, int y_mean){
		
		// calcs a smaller roi in room 
		int high_x = (int) (x_mean + ((border[1] - border[0]) + border[0]) * 0.2);
		int low_x = (int) (x_mean - ((border[1] - border[0]) + border[0]) * 0.2);
		int high_y = (int) (y_mean + ((border[3] - border[2]) + border[2]) * 0.2);
		int low_y = (int) (y_mean - ((border[3] - border[2]) + border[2]) * 0.2);
		
		//System.out.println(low_x + " " + high_x);
		//System.out.println(low_y + " " + high_y);

		// calcs random numbers
		double x = Math.random() * (high_x - low_x) + low_x;
		double y = Math.random() * (high_y - low_y) + low_y;		
		
		// typecast necessary
		int tx = (int)Math.round(x); 	
		int ty = (int)Math.round(y); 

		//System.out.println("pseudo "+tx + " " + ty);
		
		// new point-object for coordinates
		Point point = new Point(tx, ty);

		return point;
	}
}
