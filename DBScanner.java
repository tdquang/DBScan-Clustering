import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;

/**
 * DBSannner
 * @Author Andrew Elenbogen and Quang Tran
 * @Version March 11, 2015
 * This class generates clusters using the DBScanner algorithm.
 */

public class DBScanner 
{
	private ArrayList<DataPoint> data = new ArrayList<DataPoint>();
	private static final String WRITING_PORTFOLIO_DATAFILE_LOCATION = "/Accounts/trand/Desktop/portfoliodata.txt";
	private static final String TEST_DATA_LOCATION = "/Accounts/trand/Desktop/Dbscan.txt";
	private HashMap<String, Float> standardDevs;
	private HashMap<String, Float> means;
	// Boolean whether to run the algorithm on WRiting Portfolio Data
	private boolean isWritingPortData;
	
	public DBScanner(boolean isWritingPortData)
	{
		this.isWritingPortData=isWritingPortData;
		standardDevs= new HashMap<String, Float>();
		means=new HashMap<String, Float>(); 
		DataPoint.setIsWritingData(isWritingPortData);
		if(isWritingPortData)
		{
			readWritingPortfolioFile();
			standardize();
		}
		else
			readTestFile();
		
	}
	
	/**
	 * Standardizing the data
	 */
	public void standardize()
	{
		ArrayList<DataPoint> standardizedData=new ArrayList<DataPoint>(data.size());
		for(int i=0; i<data.size(); i++)
			standardizedData.add(new DataPoint());
		
		for(String key: DataPoint.getDefaultKeysForWritingData())
		{
			int nonNullSize=data.size();
			
			float total=0;
			for(DataPoint currentPoint: data)
			{
				if(currentPoint.get(key)==null)
					nonNullSize--;
				else
					total+=currentPoint.get(key);
			}
			float mean=total/nonNullSize;
			
			float stdTotal=0;
			for(DataPoint currentPoint: data)
			{
				if(currentPoint.get(key)!=null)
					stdTotal+=Math.pow(currentPoint.get(key)-mean, 2);
			}
			float stdDev=(float) Math.sqrt(stdTotal/nonNullSize);
			
			standardDevs.put(key, stdDev);
			means.put(key, mean);
			
			for(int i=0; i<data.size(); i++)
			{
				Float currentValFromCurrentPoint=data.get(i).get(key);
				if(currentValFromCurrentPoint!=null)
					standardizedData.get(i).put(key, (currentValFromCurrentPoint-mean)/stdDev);
				else
					standardizedData.get(i).put(key, 0f);
			}
		}
		data=standardizedData;
	}
	
	/**
	 * Method to convert the standardized data back to normal
	 */
	private void unstandardize(DataPoint point)
	{
		for(String key: point.getMap().keySet())
		{
			point.getMap().put(key, point.get(key)*standardDevs.get(key)+means.get(key));
		}
	}
	/**
	 * Unstandardizes all the clusters
	 */
	public void unstandardizeAllClusters(ArrayList<Cluster> clusters)
	{
		for(Cluster currentCluster: clusters)
		{
			for(DataPoint currentPoint: currentCluster.getEntireCluster())
			{
				this.unstandardize(currentPoint);
			}
		}
		
	}
	
	/**
	 * Reads the initial data file
	 */
	public void readWritingPortfolioFile(){
		try(Scanner scanner=new Scanner(new File(WRITING_PORTFOLIO_DATAFILE_LOCATION)))
		{
			scanner.nextLine();
			while (scanner.hasNextLine())
			{
				String[] split=scanner.nextLine().split("\t");
				HashMap<String, Float> currentMap=new HashMap<String, Float>();
				ArrayList<String> fieldNames=DataPoint.getDefaultKeysForWritingData();
				for(int i=0; i<split.length; i++)
				{
					try
					{
						float value=Float.parseFloat(split[i]);
						if(Math.abs(value-9999.99)<1)
							currentMap.put(fieldNames.get(i), null);
						else
							currentMap.put(fieldNames.get(i), value);
					}
					catch(NumberFormatException e)
					{
						currentMap.put(fieldNames.get(i), null);
					}
				}
				data.add(new DataPoint(currentMap));
			}
		}
		catch (IOException e){
			System.out.println(e);
			System.exit(0);
		}
	}
	
	/**
	 * Method to read the non-writing portfolio data (test data)
	 */
	public void readTestFile(){
		try(Scanner scanner=new Scanner(new File(TEST_DATA_LOCATION)))
		{
			scanner.nextLine();
			while (scanner.hasNextLine())
			{
				String[] split=scanner.nextLine().split(" ");
				HashMap<String, Float> currentMap=new HashMap<String, Float>();
				ArrayList<String> fieldNames=DataPoint.getDefaultKeysForWritingData();
				
				//System.out.println(Arrays.toString(split));
				split=Arrays.copyOfRange(split, 1, split.length);
				
				
				for(int i=0; i<split.length; i++)
				{
					try
					{
						float value=Float.parseFloat(split[i]);
						if(Math.abs(value-9999.99)<1)
							currentMap.put(fieldNames.get(i), null);
						else
							currentMap.put(fieldNames.get(i), value);
					}
					catch(NumberFormatException e)
					{
						currentMap.put(fieldNames.get(i), null);
					}
				}
				data.add(new DataPoint(currentMap));
			}
		}
		catch (IOException e){
			System.out.println(e);
			System.exit(0);
		}
	}
	
	/**
	 * Takes the minimum number of points and the radius
	 * 
	 */
	public ArrayList<Cluster> cluster(int minPoints, float radius)
	{
		//Stores which points are Core vs. Border vs. Noise
		HashMap<PointType, ArrayList<DataPoint>> typesToPoints=new HashMap<PointType, ArrayList<DataPoint>>();
		
		for(PointType type:PointType.values())
		{
			typesToPoints.put(type,  new ArrayList<DataPoint>());
		}
		 
		//Counts the number of data points with in radius of each point, and classifies then into core or noise accordingly
		for(DataPoint point: data)
		{
			int count=0;
			for(DataPoint otherPoint: data)
			{
				if(!point.equals(otherPoint) && point.getEuclideanDistance(otherPoint)<radius)
				{
					count++;
				}
			}
			if (count > minPoints){
				typesToPoints.get(PointType.CORE).add(point);
			}
			else{
				typesToPoints.get(PointType.NOISE).add(point);
			}
		}
		
		//Separates all border points from the Noise
		for (DataPoint current: typesToPoints.get(PointType.NOISE)){
			boolean border = false;
			for (DataPoint core: typesToPoints.get(PointType.CORE)){
				if (current.getEuclideanDistance(core) < radius){
					border = true;
					break;
				}
			}
			if (border){
				typesToPoints.get(PointType.BORDER).add(current);
			}
		}
		typesToPoints.get(PointType.NOISE).removeAll(typesToPoints.get(PointType.BORDER));
		System.out.println("Noise Points Removed: "+ typesToPoints.get(PointType.NOISE).size());
		
		
		boolean[][] adjacencyMatrix= new boolean[typesToPoints.get(PointType.CORE).size()][typesToPoints.get(PointType.CORE).size()];

		
		for(boolean[] current: adjacencyMatrix)
			Arrays.fill(current, false);
		
		//Puts true in all appropriate locations of the adjacency matrix
		for (int i=0; i< typesToPoints.get(PointType.CORE).size(); i++)
		{
			for (int j=0; j < typesToPoints.get(PointType.CORE).size(); j++)
			{
				if (typesToPoints.get(PointType.CORE).get(i).getEuclideanDistance(typesToPoints.get(PointType.CORE).get(j)) < radius){
					adjacencyMatrix[i][j]=true;
					adjacencyMatrix[j][i]=true;
				}
			}
		}
		
		//Calculates which points are reachable from which points
		adjacencyMatrix=warshalls(typesToPoints.get(PointType.CORE).size(), adjacencyMatrix);
	
		
		ArrayList<Cluster> clusters=new ArrayList<Cluster>();
		ArrayList<Integer> skipList=new ArrayList<Integer>();
		
		//Assigns points to clusters based on the reachabiility matrix
		for(int i=0; i<adjacencyMatrix.length; i++)
		{
			if(skipList.contains(i))
				continue;
			Cluster newCluster=new Cluster();
			for(int j=0; j<adjacencyMatrix[i].length; j++)
			{
				if(i==j || skipList.contains(j))
					continue;
				boolean reachable=adjacencyMatrix[i][j];
				if(reachable)
				{
					newCluster.add(typesToPoints.get(PointType.CORE).get(j));
					skipList.add(j);
				}
			}
			if(!newCluster.getEntireCluster().isEmpty())
			{
				clusters.add(newCluster);
			}
		}
		
		HashMap<DataPoint, Cluster> assignment=new HashMap<DataPoint, Cluster>();
		
		//Assigns border points to the cluster containing the closest point to them
		for(DataPoint border: typesToPoints.get(PointType.BORDER))
		{
			Cluster closest=null;
			float closestDistance=0;
			for(Cluster cluster: clusters)
			{
				float distance=cluster.getDistance(border);
				if(closest==null || closestDistance<distance )
				{
					closest=cluster;
					closestDistance=distance;
				}
			}
			assignment.put(border,  closest);
		}
		
		for(DataPoint point: assignment.keySet())
		{
			assignment.get(point).add(point);
		}
		
		return clusters;
	}
	
	/**
	 * Run's Warshall's Algorithm to determine whether the point i is reachable from point j.
	 */
	private boolean[][] warshalls(int numberOfVertices, boolean[][] adjacencyMatrix)
	{
		for(int k=0; k<numberOfVertices; k++)
		{
			for(int i=0; i<numberOfVertices; i++)
			{
				for(int j=0; j<numberOfVertices; j++ )
				{
					adjacencyMatrix[i][j]=(adjacencyMatrix[j][k] && adjacencyMatrix[i][j]) || adjacencyMatrix[i][j];
				}
			}
		}
		return adjacencyMatrix;
	}
	
	/**
	 * Prints the cluster centers in tab separated format
	 */
	public void printClusters(ArrayList<Cluster> clusters)
	{
		int i=1;
		for(Cluster current: clusters)
		{
			System.out.println("Cluster "+i+": "+current.getEntireCluster().size());
			i++;
		}
		if(this.isWritingPortData)
			this.unstandardizeAllClusters(clusters);
		for(Cluster currentCluster: clusters)
		{
			System.out.print(currentCluster.getCenter()+"\t");
		}
	}
	
	/**
	 * Getting the distance from each point to it's kth nearest neighbor
	 */
	private ArrayList<Float> getKDistance(int k)
	{
		ArrayList<Float> toReturn=new ArrayList<Float>();
		
		for(DataPoint current: data)
		{
			PriorityQueue<DataPoint> queue= new PriorityQueue<DataPoint>( new Comparator<DataPoint>() {
				@Override
				public int compare(DataPoint o1, DataPoint o2) 
				{		
					double distanceDiff=o1.getEuclideanDistance(current)-
										o2.getEuclideanDistance(current);
					if(distanceDiff<0)
						return (int) Math.floor(distanceDiff);
					else
						return (int) Math.ceil(distanceDiff);
				}
			});
			queue.addAll(data);
			queue.remove(current);
			
			DataPoint closest=null;
			for(int i=0; i<k; i++)
			{
				closest=queue.remove();
			}
			toReturn.add(closest.getEuclideanDistance(current));
		}
		Collections.sort(toReturn);
		return toReturn;
	}
	/**
	 * Prints  distances from each point to it's kth nearest neighbor for all values 1 to kBound.
	 */
	public void printAllKDistances(int kBound)
	{
		for(int i=1; i<=kBound; i++)
		{
			System.out.print("k="+i+"\nDistances=\n");
			for(float current: getKDistance(i))
			{
				System.out.print("\t"+current);
			}
			System.out.println();
		}
	}
	
	
	public static void main(String[] args)
	{
		Scanner scanner=new Scanner(System.in);
		System.out.print("Use Writing Portfolio Data (y or n)>");
		boolean useWrit=scanner.next().toLowerCase().startsWith("y");
		DBScanner clusterer= new DBScanner(useWrit);
		
		System.out.print("Print tab seperated, sorted k-Distance for radius tuning (y or n)>");
		boolean tune=scanner.next().toLowerCase().startsWith("y");
		if(tune)
		{
			System.out.print("Print k distances from 1 to what upper bound>");
			clusterer.printAllKDistances(scanner.nextInt());
		}
		
		System.out.print("Enter radius>");
		float radius=scanner.nextFloat();
		System.out.print("Enter minimum points>");
		int minPoints=scanner.nextInt();
		ArrayList<Cluster> result=clusterer.cluster(minPoints, radius);
		clusterer.printClusters(result);
	}
	
}