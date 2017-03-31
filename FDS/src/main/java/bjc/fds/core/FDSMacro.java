package bjc.fds.core;

import java.util.function.Consumer;

import bjc.utils.cli.CommandHelp;
import bjc.utils.ioutils.Block;

/**
 * A FDS macro.
 * 
 * A macro for FDS is a list of blocks bound to a identifier.
 * 
 * @author bjculkin
 *
 */
public interface FDSMacro {

	/**
	 * Invoke this macro using the specified action.
	 * 
	 * @param action
	 *                The place to output blocks from the macro.
	 */
	void invoke(Consumer<Block> action);

	/**
	 * Get the help for this macro.
	 * 
	 * @return The help for this macro.
	 */
	CommandHelp getHelp();
}