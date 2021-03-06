import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class HW2_Program {
	private static Scanner s = new Scanner(System.in);
	private static final String F_WORKERS = "workers.dat";
	private static final int NAME_SIZE = 20;
	private static final int DEP_SIZE = 50;
	private static final int BOSS_SIZE = 10;
	private static final int RECORD_SIZE_DEP = 2 * (NAME_SIZE + DEP_SIZE + BOSS_SIZE) + Integer.BYTES;
	private static final int RECORD_SIZE_STR = 2 * (NAME_SIZE + DEP_SIZE) + Integer.BYTES;

	public static void main(String[] args) {
		try {
			System.out.println("Press 1 for dep as class Department, any other key for dep as String : ");
			char c = s.next().charAt(0);

			ArrayList<Worker<?>> lst = createList(c);
			System.out.println("ArrayList content: ");
			showList(lst);

			TreeMap<?, ? extends Worker<?>> myMap = createMap(lst); // Q3

			System.out.println("\nMap content backward, order by worker's name:");
			printMapBackWard(myMap); // Q4
			saveMapToFile(F_WORKERS, c, myMap); // Q5

			System.out.println("\nFile content:");
			readFile(F_WORKERS, c); // Q6

			Comparator<Worker<?>> comp = new Comparator<Worker<?>>() {
				@Override
				public int compare(Worker<?> w1, Worker<?> w2) {
					return w1.getSalary() - w2.getSalary();
				}
			};
			compareSalaries(F_WORKERS, c, comp); // Q7
			System.out.println("\nFile content after sorting:");
			readFile(F_WORKERS, c);

			MyListIterator listIterator = new MyListIterator(0, new RandomAccessFile(F_WORKERS, "rw"), c);// Q9
			System.out.println("\ncheckIterator:\n");
			checkIterator(listIterator); // Q10

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}
	}

	/** Create the list of workers */
	public static ArrayList<Worker<?>> createList(char c) {
		final String[] aNames = { "Elvis", "Samba", "Bamba", "Bisli", "Kinder Bueno", "Elvis" };
		final String[] aDepNames = { "Software Engineering", "Mechanical Engineering",
				"Industrial And Medical Engineering", "Electrical Engineering", "Electrical Engineering",
				"Software Engineering" };
		final String[] aDepHeads = { "Boss1", "Boss2", "Boss3", "Boss4", "Boss4", "Boss1" };
		final int[] aSalaries = { 1000, 2000, 3000, 4000, 1000, 9999 };
		ArrayList<Worker<?>> lst = new ArrayList<>();
		for (int i = 0; i < aNames.length; i++)
			if (c == '1')
				lst.add(new Worker<Department>(aNames[i], new Department(aDepNames[i], aDepHeads[i]), aSalaries[i]));
			else
				lst.add(new Worker<String>(aNames[i], aDepNames[i], aSalaries[i]));
		return lst;
	}

	/** Shows the list of workers */
	public static void showList(ArrayList<Worker<?>> lst) {
		for (Worker<?> w : lst)
			System.out.println(w);
	}

	/** Creates the map of workers */
	private static <E extends Worker<?>> TreeMap<?, E> createMap(ArrayList<E> lst) {
		int i = 0;
		Set<E> treeSet = new TreeSet<>(lst);
		TreeMap<Integer, E> map = new TreeMap<>();
		Iterator<E> it = treeSet.iterator();
		while (it.hasNext())
			map.put(++i, it.next());
		return map;
	}

	private static void printMapBackWard(TreeMap<?, ?> myMap) {
		LinkedList<Entry<?, ?>> ll = new LinkedList<>(myMap.descendingMap().entrySet());
		ListIterator<Entry<?, ?>> li = ll.listIterator();
		while (li.hasNext()) {
			Entry<?, ?> e = li.next();
			System.out.println(e.getKey() + ": " + e.getValue());
		}
	}

	/** Writes the workers to the main file */
	public static <E extends Worker<?>> void saveMapToFile(String fName, char c, TreeMap<?, E> myMap)
			throws FileNotFoundException, IOException {
		boolean b = c == '1';
		try (DataOutputStream o = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fName)))) {
			for (E w : myMap.values())
				writeWorkerToFile(o, w, b);
			o.close();
		}
	}

	public static void writeWorkerToFile(DataOutput o, Worker<?> w, boolean b) throws IOException {
		FixedLengthStringIO.writeFixedLengthString(w.getName(), NAME_SIZE, o);
		if (b) {
			FixedLengthStringIO.writeFixedLengthString(((Department) w.getDep()).getDepName(), DEP_SIZE, o);
			FixedLengthStringIO.writeFixedLengthString(((Department) w.getDep()).getDepHead(), BOSS_SIZE, o);
		} else
			FixedLengthStringIO.writeFixedLengthString((String) w.getDep(), DEP_SIZE, o);
		o.writeInt(w.getSalary());
	}

	/** Reads the workers from the main file to the console */
	public static void readFile(String fName, char c) throws FileNotFoundException, IOException {
		boolean b = c == '1';
		try (DataInputStream i = new DataInputStream(new BufferedInputStream(new FileInputStream(fName)))) {
			while (i.available() > 0)
				System.out.println(readWorkerFromFile(i, b));
			i.close();
		}
	}

	public static Worker<?> readWorkerFromFile(DataInput i, boolean b) throws IOException {
		String name, depName, depHead = null;
		int salary;
		name = FixedLengthStringIO.readFixedLengthString(NAME_SIZE, i);
		depName = FixedLengthStringIO.readFixedLengthString(DEP_SIZE, i);
		if (b)
			depHead = FixedLengthStringIO.readFixedLengthString(BOSS_SIZE, i);
		salary = i.readInt();
		if (b)
			return new Worker<>(name, new Department(depName, depHead), salary);
		else
			return new Worker<>(name, depName, salary);
	}

	public static void compareSalaries(String fName, char c, Comparator<Worker<?>> comp)
			throws FileNotFoundException, IOException {
		boolean compare = false;
		boolean b = c == '1';
		long pos1 = 0, pos2 = 0;
		Worker<?> w1, w2 = null;
		try (RandomAccessFile f = new RandomAccessFile(fName, "rw")) {
			long count = f.length();
			while (f.getFilePointer() < count) {
				pos1 = f.getFilePointer();
				w1 = readWorkerFromFile(f, b);
				if (compare && comp.compare(w2, w1) > 0) {
					f.seek(pos2);
					writeWorkerToFile(f, w1, b);
					writeWorkerToFile(f, w2, b);
				} else {
					w2 = w1;
					compare = true;
				}
				pos2 = pos1;
				if (f.getFilePointer() >= count) {
					compare = false;
					count = pos2;
					f.seek(0);
				}
			}
			f.close();
		}
	}

	private static void checkIterator(MyListIterator listIterator) {
		System.out.println("File content FORWARD with ListIterator:");
		while (listIterator.hasNext())
			System.out.println(listIterator.next());
		System.out.println("\nFile content BACKWARD with ListIterator:");
		while (listIterator.hasPrevious())
			System.out.println(listIterator.previous());
	}

	public static ListIterator<Worker<?>> listIterator(int index, RandomAccessFile f, char c)
			throws FileNotFoundException, IOException {
		return new MyListIterator(index, f, c);
	}

	private static class MyListIterator implements ListIterator<Worker<?>> {
		private boolean b;
		private RandomAccessFile f;
		private int cursor = 0, lastRet = -1, recSize;
		private long numRec;

		public MyListIterator(int index, RandomAccessFile rf, char c) throws IOException {
			this.f = rf;
			this.b = c == '1';
			this.recSize = b ? RECORD_SIZE_DEP : RECORD_SIZE_STR; //worker length
			this.cursor = index;
			this.numRec = f.length() / recSize;
		}
		
		public boolean hasNext() {
			return cursor < numRec;
		}

		public Worker<?> next() {
			if (!hasNext())
				throw new NoSuchElementException();
			Worker<?> w = null;
			try {
				f.seek(cursor * recSize);
				w = readWorkerFromFile(f, b);
			} catch (IOException e) {
				e.printStackTrace();
			}
			lastRet = cursor;
			cursor++;
			return w;
		}
		
		public boolean hasPrevious() {
			return cursor > 0;
		}

		public Worker<?> previous() {
			if (!hasPrevious())
				throw new NoSuchElementException();
			cursor--;
			Worker<?> w = null;
			try {
				f.seek(cursor * recSize);
				w = readWorkerFromFile(f, b);
			} catch (IOException e) {
				e.printStackTrace();
			}
			lastRet = cursor;
			return w;
		}

		public void set(Worker<?> w) {
			if (lastRet == -1)
				throw new IllegalStateException();
			try {
				f.seek(lastRet * recSize);
				writeWorkerToFile(f, w, b);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void remove() {
			if (lastRet == -1)
				throw new IllegalStateException();
			try {
				ArrayList<Worker<?>> lst = fillToArrayList();
				lst.remove(lastRet);
				arrayListToFile(lst);
				numRec--;
				cursor = lastRet;
				lastRet = -1;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void add(Worker<?> w) {
			if (cursor < 0 || cursor > numRec)
				throw new IndexOutOfBoundsException();
			ArrayList<Worker<?>> lst;
			try {
				lst = fillToArrayList();
				lst.add(cursor, w);
				arrayListToFile(lst);
				numRec++;
				cursor++;
				lastRet = -1;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void arrayListToFile(ArrayList<Worker<?>> lst) throws IOException {
			f.seek(0);
			for (Worker<?> w : lst)
				writeWorkerToFile(f, w, b);
		}

		private ArrayList<Worker<?>> fillToArrayList() throws IOException {
			f.seek(0);
			cursor = 0;
			ArrayList<Worker<?>> lst = new ArrayList<>();
			while (hasNext()) {
				lst.add(readWorkerFromFile(f, b));
				cursor++;
			}
			return lst;
		}
	
		public int nextIndex() {
			return cursor;
		}

		public int previousIndex() {
			return lastRet;
		}
	}
}
