/** Generic class Worker */
public class Worker<T> implements Comparable<Worker<?>>{
	private String name;
	private T dep;
	private int salary;
	
	/** Constructor */
	public Worker(String name, T dep, int salary) {
		this.name = name;
		this.dep = dep;
		this.salary = salary;
	}

	/** Gets name */
	public String getName() {
		return name;
	}

	/** Gets dep */
	public T getDep() {
		return dep;
	}

	/** Gets salary */
	public int getSalary() {
		return salary;
	}

	public String toString() {
		return String.format("%-20s %-50s %d", name, dep, salary);
	}

	@Override
	public int compareTo(Worker<?> o) {
		if (this.getName().equals(o.getName()))
			return 1;
		else
		return this.getName().compareTo(o.getName());	
	}
}
