import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class represents a single DataPoint with any number of fields, stored in a HashMap
 * @Author Andrew Elenbogen and Quang Tran
 * @Version March 11, 2015
 *
 */
public class DataPoint 
{
	
	private HashMap<String, Float> fields;
	//Static field which tracks whether we're using the writingData or other data.
	private static boolean isWritingData;
	
	public static void setIsWritingData(boolean newVal)
	{
		isWritingData=newVal;
	}
	
	public DataPoint()
	{
		fields=new HashMap<String, Float>();
	}
	/**
	 * 
	 * @param fields
	 */
	public DataPoint(HashMap<String, Float> fields)
	{
		this.fields=fields;
	}
	
	/**
	 * Returns all of the fields in DataPoints in this project. Changing this method will change ALL of the code without furthermodifications. 
	 * Adds great modifiability.
	 */
	public static ArrayList<String> getDefaultKeysForWritingData()
	{
		ArrayList<String> toReturn=new ArrayList<String>();
		if(isWritingData)
			toReturn.addAll(Arrays.asList("Minnesota Credits", "International", "Birth Year", "HS Rank", "Verbal SAT", "Math SAT", 
				"ACT Composite", "Cumulative GPA", "Number of Essays", "Abroad Credits", "AP Credits", "CS Credits", "English Credits", "Science Credits", "Writing Credits"));
		else
			toReturn.addAll(Arrays.asList("X", "Y"));
		return toReturn;
	}
	
	/**
	 * Returns a deep copy of this DataPoint 
	 */
	public DataPoint copy()
	{
		HashMap<String, Float> otherFields=new HashMap<String, Float>();
		for(String current: fields.keySet())
		{
			otherFields.put(current, fields.get(current));
		}
		return new DataPoint(otherFields);
	}

	/**
	 * Returns the field of the DataPoint associated with the given String
	 */
	public Float get(String name)
	{
		return fields.get(name);
	}
	
	/**
	 * Sets each field of the point, x, to be log(x+1) with a base of 2.
	 */
	public void convertToLog() 
	{
		for(String key: fields.keySet())
		{
			fields.put(key, (float) (Math.log((double) (fields.get(key) + 1.0f) )/Math.log(2)));
		}		
	}
	
	/**
	 * Gets the squared Euclidean distance of this point to the other point.
	 */
	public float getEuclideanDistance(DataPoint otherPoint) 
	{
		float total=0;
		for(String key: fields.keySet())
		{
			if(this.get(key)!=null && otherPoint.get(key)!=null)
				total+=(Math.pow((this.get(key) - otherPoint.get(key)), 2));
		}
		return (float) Math.sqrt(total);
	}
	
	/**
	 * Normalizes the point
	 */
	public void normalize(){
		float total = 0;
		for(float value: fields.values()){
			total += Math.pow(value, 2);
		}
		total = (float) Math.sqrt(total);
		for(String key: fields.keySet()){
			fields.put(key, (float) fields.get(key)/total);
		}
	}
	/**
	 * Returns whether or not this point is the same as the other Object.
	 */
	@Override
	public boolean equals(Object other)
	{
		if (!(other instanceof DataPoint)){
			return false;
		}
		DataPoint otherPoint = (DataPoint) other;
		for(String key: fields.keySet())
		{	
			if(this.get(key)==null && otherPoint.get(key)==null )
				continue;
			
			if(this.get(key)==null && otherPoint.get(key)!=null || 
				this.get(key)!=null && otherPoint.get(key)==null ||	
					!this.get(key).equals(otherPoint.get(key)))
				return false;
		}
		return true;
	}
	/**
	 * @return This points map of field names to values.
	 */
	public HashMap<String, Float> getMap(){
		return fields;
	}
	
	/**
	 * Adds the given field to this point with the given name
	 */
	public void put(String name, Float value)
	{
		fields.put(name, value);
	}
	
	/**
	 * Divides all fields by the given number.
	 */
	public void divideAll(float num){
		for (String key: fields.keySet()){
			fields.put(key, fields.get(key)/num);
		}
	}
	
	/**
	 * Used for hashing DataPoints, required to store them well in a HashMap
	 */
	@Override
	public int hashCode()
	{
		int total=0;
		int seen=0;
		for(String key: fields.keySet())
		{
			total+=Math.pow(2, seen)*fields.get(key);
			seen++;
		}
		return total;
	}
	
	/**
	 * Undoes the converToLog procedure.
	 */
	public void deLogify(){
		for(String key: fields.keySet())
		{
			fields.put(key, (float) (Math.pow(2, fields.get(key))-1));
		}
	}
	/**
	 * Returns a nicely formatted summary of the DataPoint
	 */
	public String toString()
	{
		String summary="";
		
		for(String key: fields.keySet())
		{
			summary+=key+":"+fields.get(key)+"\t";
		}
		return summary;
	}
	/**
	 * Converts it to a String for easy pasting into excel
	 */
	public String toStringForTable()
	{
		String summary="";
		for(String key: DataPoint.getDefaultKeysForWritingData())
		{
			summary+=fields.get(key)+"\t";
		}
		
		return summary;
	}
	
}