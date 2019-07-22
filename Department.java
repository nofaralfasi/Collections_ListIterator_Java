/** Class Department */
public class Department {
	private String depName;
	private String depHead;
	
	/** Constructor */
	public Department(String depName, String depHead) {
		this.depName = depName;
		this.depHead = depHead;
	}
	
	/** Gets depName */
	public String getDepName() {
		return depName;
	}
	
	/** Gets depHead */
	public String getDepHead() {
		return depHead;
	}
	
	public String toString() {
		return String.format("%-50s %-10s", depName, depHead);
	}
}
