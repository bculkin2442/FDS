package bjc.fds;

/**
 * A command attached to an FDS interface.
 * 
 * @author bjculkin
 *
 * @param <S>
 *                The state type of the interface.
 */
@FunctionalInterface
public interface FDSCommand<S> {
	/**
	 * Run this command.
	 * 
	 * @param state
	 *                The current FDS state.
	 */
	void run(FDSState<S> state);
}
