package lbs.wifiparticlefilter.filter;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lbs.wifiparticlefilter.data.Measure;
import lbs.wifiparticlefilter.data.Particle;
import lbs.wifiparticlefilter.data.Point;


/**
 * Compares a particle-position to the particle-position of the neighbors. A 
 * new measure-profile for the actual particle will be generated.
 * 
 * @author Andreas Attenberger
 * @version 1.4
 */
public class InterpolateParticle {

	
	// actual Particle 
	private Particle p;
	// Particle-list with measures, which contains the neighbors
	private List<Particle> db_list;
	
	
	/**
	 * C'tor
	 * 
	 * @param p Particle, whose neighbor we are looking for
	 * @param list Particle-list with measurements we want to compare 
	 */
	public InterpolateParticle(Particle p, List<Particle> list) {
		
		this.p = p;		
		db_list = list;		
	}

	
	/**
	 * Method gives back a mean measurement for a particle.
	 * 
	 * @return New Particle-object with mean-measures
	 */
	public Particle interpolatedParticle(){	
		
		// Map for Particle-objects sorted by quadrants 		
		Map<Integer,Point> q1_map = new HashMap<Integer,Point>();
		Map<Integer,Point> q2_map = new HashMap<Integer,Point>();
		Map<Integer,Point> q3_map = new HashMap<Integer,Point>();
		Map<Integer,Point> q4_map = new HashMap<Integer,Point>();		
		
		// Point-object for current particle
		Point currentParticle = this.p.getPoint();
		
		// run through database
		for (int i=0; i<db_list.size(); i++){
			
			// get coordinates from list
			Point p_tmp = db_list.get(i).getPoint();
			
			// Point from database is in first quadrant
			if (currentParticle.quad1st(p_tmp)){	
				// add to Map for fist quadrant
				q1_map.put(i, p_tmp);
			}
			
			// Point from database is in second quadrant
			if (currentParticle.quad2nd(p_tmp)){
				// add to Map for second quadrant
				q2_map.put(i, p_tmp);
			}

			// Point from database is in third quadrant
			if (currentParticle.quad3rd(p_tmp)){
				// add to Map for third quadrant
				q3_map.put(i, p_tmp);
			}

			// Point from database is in fourth quadrant
			if (currentParticle.quad4th(p_tmp)){
				// add to Map for fourth quadrant
				q4_map.put(i, p_tmp);
			}			
		}

		
		// Index of the nearest neighbor in first quadrant
		int idx_1q = getNearestPoint(q1_map);
		// Index of the nearest neighbor in second quadrant
		int idx_2q = getNearestPoint(q2_map);
		// Index of the nearest neighbor in third quadrant
		int idx_3q = getNearestPoint(q3_map);
		// Index of the nearest neighbor in fourth quadrant
		int idx_4q = getNearestPoint(q4_map);
		
		// temp Particle
		Particle p_tmp2 = null;		
		// List for all relevant measures for the particle
		List<Measure> m_list = new ArrayList<Measure>();
		// temp List
		List<Measure> tmp_meas = null;
		
		// there are neighbors in first quadrant
		if (idx_1q != Integer.MAX_VALUE){
			
			// Particle in database is a neighbor
			p_tmp2 = db_list.get(idx_1q);
			// List of all measures of the particle
			tmp_meas = p_tmp2.getMeasure();
			// add all measurements of nearest Particle to list
			m_list.addAll(tmp_meas);
		}
		// there are neighbors in second quadrant
		if (idx_2q != Integer.MAX_VALUE){

			// Particle in database is a neighbor
			p_tmp2 = db_list.get(idx_2q);
			// List of all measures of the particle
			tmp_meas = p_tmp2.getMeasure();
			// add all measurements of nearest Particle to list
			m_list.addAll(tmp_meas);
		}
		// there are neighbors in third quadrant
		if (idx_3q != Integer.MAX_VALUE){

			// Particle in database is a neighbor
			p_tmp2 = db_list.get(idx_3q);
			// List of all measures of the particle
			tmp_meas = p_tmp2.getMeasure();
			// add all measurements of nearest Particle to list
			m_list.addAll(tmp_meas);
		}
		// there are neighbors in fourth quadrant
		if (idx_4q != Integer.MAX_VALUE){

			// Particle in database is a neighbor
			p_tmp2 = db_list.get(idx_4q);
			// List of all measures of the particle
			tmp_meas = p_tmp2.getMeasure();
			// add all measurements of nearest Particle to list
			m_list.addAll(tmp_meas);
		}
		
		
		// List m_list contains all measures in our room; mean measurements will
		// be calculated and stored in a clean list
		List<Measure> clean_list = getMeanMeasure(m_list);
		
		// we take the old position for the interpolated particle
		Point point = p.getPoint();
		
		// new Particle with mean measurements
		return new Particle(point, clean_list);	
	}	
	
	
	/**
	 * Calculates the distance between two Point-objetcts
	 * 
	 * @return Distance between two points
	 */
	private double getDistance(Point p0, Point p1){
		
		double dist = 0;
		double dist_x = 0;
		double dist_y = 0;	
	
		// difference to x- & y-coordinates
		dist_x = Math.abs(p0.getX() - p1.getX());
		dist_y = Math.abs(p0.getY() - p1.getY());
		// calculate the euklidian distance 
		dist = Math.sqrt(dist_x * dist_x + dist_y * dist_y);
		
		return dist;
	}
	
	
	/**
	 * Gives back the index of the nearest point from map. If map is empty, 
	 * biggest Integrnumber will be returned 
	 * 
	 * @param map Map with index and Point
	 * @return Index of the nearest Point
	 */
	private int getNearestPoint(Map<Integer,Point> map){
		
		int index = 0;
		
		// if there is a neighbor
		if (map.size() != 0){
		
			// map sorted by key; distance of the points is key, index is value
			// nearest Point is first entry
			Map<Double,Integer> sorted_map = new TreeMap<Double,Integer>();
			
			// run through map 
			for (Map.Entry<Integer, Point> e : map.entrySet()){
			
				// calculate distance between actual Particle and Point-object
				double tmp_dist = getDistance(p.getPoint(), e.getValue());
				// get index of the Point
				index = e.getKey();
				// add distance and index of this Point
				sorted_map.put(tmp_dist, index);		
			}
			
			// get list with values from map; first entry is index of nearest 
			// Point 
			index = sorted_map.values().iterator().next();
		
		} else{
			
			// there is no neighbor; escape-number infinity
			index = Integer.MAX_VALUE;				
		}
		
		return index;
	}
	
	
	/**
	 * Searches in list for all relevant measures. If SSID is equal to measures 
	 * from actual position, a mean measuere will be calculated  
	 * mit allen Messwerten.
	 * 
	 * @return List with measures
	 */
	private List<Measure> getMeanMeasure(List<Measure> mlist){		
		
		// Map stores name and measure of a hotspot
		Map<String,List<Integer>> hotspot = new TreeMap<String,List<Integer>>(); 
		
		// run through measures
		for (int i=0; i<mlist.size(); i++){
			
			// get actual measure from list
			Measure tmp_meas = mlist.get(i);
						
			// get name from list
			String s = tmp_meas.getBSSID();
			// get signal-strength
			int m = tmp_meas.getRssi();
				
			// if measure list contains ssid, add to map, else put new key 
			if (hotspot.containsKey(s)){

				// temp list with entries from map
				List<Integer> tmp = hotspot.get(s);

				// add measure
				tmp.add(m);

				// update entry
				hotspot.put(s, tmp);						
			} 
			else {

				// name is not in map --> add him

				// new list for measures 
				List<Integer> int_list = new ArrayList<Integer>();
				// add measure to list
				int_list.add(m);
				// add measure and name to map
				hotspot.put(s, int_list);					
			}								
		}
		
		
		// mean measure list 
		List<Measure> mean_list = new ArrayList<Measure>();
		
		// map contains list with measures and names, so run through it
		for (Map.Entry<String, List<Integer>> e : hotspot.entrySet()){
			
			// name of the hotspot
			String ssid = e.getKey();
			// list with measures
			List<Integer> measure = e.getValue(); 
			// get mean measure from signal-strength
			int rssi = meanMeasure(measure);
			
			// new Measure-object with mean values
			Measure mean = new Measure(ssid,rssi);
			// add to return-list
			mean_list.add(mean);			
		}
		
		return mean_list;
	}
	
	
	/**
	 * Calculates the average value of measures and return it as Integer.
	 * 
	 * @param list List with measures
	 * @return Mean value of measurements
	 */
	private int meanMeasure(List<Integer> list){
		
		// length of list
		int size = list.size();
	
		double sum = 0;
		
		// run through list
		for (int i=0; i<size; i++){
			
			// add all entries
			sum = sum + list.get(i);		
		}
		
		// get mean value
		sum = sum / size;
		
		// typecast into Integer 
		int back = (int) sum; 
		
		return back;
	}	

}
