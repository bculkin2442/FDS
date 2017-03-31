package bjc.fds;

/**
 * Exception thrown when something goes wrong with FDS.
 * 
 * @author bjculkin
 *
 */
public class FDSException extends Exception {
	/**
	 * Create a new FDS exception with a message and a cause.
	 * 
	 * @param message
	 *                The message for the exception.
	 * 
	 * @param cause
	 *                The cause of the exception.
	 */
	public FDSException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a new FDS exception with a message.
	 * 
	 * @param message
	 *                The message for the exception.
	 */
	public FDSException(String message) {
		super(message);
	}
}
