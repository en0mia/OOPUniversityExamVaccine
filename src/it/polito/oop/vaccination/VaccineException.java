/*
    @author: Simone Nicol <en0mia.dev@gmail.com>
    @created: 06/08/21
    @copyright: Check the repository license.
*/

package it.polito.oop.vaccination;

public class VaccineException extends Exception {
	private static final long serialVersionUID = 1L;
	public VaccineException() {super("Vaccine system error");}
	public VaccineException(String msg ) {super(msg);}
}
