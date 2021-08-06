/*
    @author: Simone Nicol <en0mia.dev@gmail.com>
    @created: 06/08/21
    @copyright: Check the repository license.
*/

package it.polito.oop.vaccination;

public class Hub {
	private String name;
	private int doctors = 0;
	private int nurses = 0;
	private int other = 0;
	
	public Hub(String name) {
		this.name = name;
	}

	/*
	 * Auto generated code.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/*
	 * Auto generated code.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Hub))
			return false;
		Hub other = (Hub) obj;
		if (name == null) {
			return other.name == null;
		} else return name.equals(other.name);
	}
	
	@Override
	public String toString() {
		return this.name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDoctors() {
		return doctors;
	}

	public void setDoctors(int doctors) {
		this.doctors = doctors;
	}

	public int getNurses() {
		return nurses;
	}

	public void setNurses(int nurses) {
		this.nurses = nurses;
	}

	public int getOther() {
		return other;
	}

	public void setOther(int other) {
		this.other = other;
	}
	
	public int getHourlyCapacity() throws VaccineException {
		if ((this.doctors | this.nurses | this.other) == 0) {
			throw new VaccineException("Team not defined");
		}

		return Math.min(this.doctors * 10, Math.min(this.nurses * 12, this.other * 20));
	}
}
