/*
    @author: Simone Nicol <en0mia.dev@gmail.com>
    @created: 06/08/21
    @copyright: Check the repository license.
*/

package it.polito.oop.vaccination;

public class Interval implements Comparable<Interval>{
	private int start;
	private int end;
	
	public Interval(int start, int end) {
		this.start = start;
		this.end = end;
	}
	
	public Interval(int start, String end) {
		this.start = start;
		this.end = -1;
	}



	public int getStart() {
		return start;
	}



	public void setStart(int start) {
		this.start = start;
	}



	public int getEnd() {
		return end;
	}



	public void setEnd(int end) {
		this.end = end;
	}
	
	public boolean between(int age) {
		if (this.end == -1) {
			return age >= this.start;
		}
		return (age >= this.start && age < this.end);
	}
	
	@Override
	public String toString() {
		if (this.end == -1) {
			return "[" + this.start + "," + "+" + ")";
		}
		return "[" + this.start + "," + this.end + ")";
	}

	@Override
	public int compareTo(Interval o) {
		return this.getStart() - o.getStart();
	}
}
