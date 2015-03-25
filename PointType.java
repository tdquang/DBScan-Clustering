/**
 * This class represents the possible types of points in the DBScan algorithm. It exists for readability, but 
 * is functionally the same as just allowing 1,2 and 3 to represent point types.
 * @Author Andrew Elenbogen and Quang Tran
 * @Version March 11, 2015
 *
 */
public enum PointType 
{
	CORE, BORDER, NOISE;
}
