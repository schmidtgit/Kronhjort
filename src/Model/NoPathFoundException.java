package Model;

public class NoPathFoundException extends Exception {
	private static final long serialVersionUID = 16052016L;

	/**
	 * Parameterless constructor
	 */
	public NoPathFoundException() {}

    /**
     * Constructor that accepts an error message
     * @param message
     */
    public NoPathFoundException(String message) {
    	super(message);
    }
}
