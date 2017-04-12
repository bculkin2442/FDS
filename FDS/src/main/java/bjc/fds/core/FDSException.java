package bjc.fds.core;

/**
 * Exception thrown when something goes wrong with FDS.
 *
 * @author bjculkin
 *
 */
public class FDSException extends Exception {
	private static final long serialVersionUID = -7404226691172297306L;

	/**
	 * Create a new FDS exception with a message and a cause.
	 *
	 * @param message
	 *                The message for the exception.
	 *
	 * @param cause
	 *                The cause of the exception.
	 */
	public FDSException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a new FDS exception with a message.
	 *
	 * @param message
	 *                The message for the exception.
	 */
	public FDSException(final String message) {
		super(message);
	}
}
