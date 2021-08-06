/*
    @author: Simone Nicol <en0mia.dev@gmail.com>
    @created: 06/08/21
    @copyright: Check the repository license.
*/

package it.polito.oop.vaccination;

public class Person {
	private String name;
	private String surname;
	private String ssn;
	private int year;
	
	public Person(String ssn, String name, String surname, int year) {
		this.name = name;
		this.surname = surname;
		this.ssn = ssn;
		this.year = year;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSurname() {
		return surname;
	}
	public void setSurname(String surname) {
		this.surname = surname;
	}
	public String getSsn() {
		return ssn;
	}
	public int getYear() {
		return this.year;
	}
	public void setSsn(String ssn) {
		this.ssn = ssn;
	}
	
	@Override
	public String toString() {
		return this.ssn + "," + this.surname + "," + this.name;
	}
}
