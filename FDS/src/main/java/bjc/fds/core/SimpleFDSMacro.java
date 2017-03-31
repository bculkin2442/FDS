package bjc.fds.core;

import java.util.List;
import java.util.function.Consumer;

import bjc.utils.cli.CommandHelp;
import bjc.utils.ioutils.Block;

/**
 * Simple implementation of {@link FDSMacro}
 * 
 * @author bjculkin
 *
 */
public class SimpleFDSMacro implements FDSMacro {
	private final List<Block> body;

	private final CommandHelp help;

	/**
	 * Create a new FDS macro.
	 * 
	 * @param macroBody
	 *                The blocks that make up the body of the macro.
	 * 
	 * @param macroHelp
	 *                The help for the macro
	 */
	public SimpleFDSMacro(List<Block> macroBody, CommandHelp macroHelp) {
		body = macroBody;
		help = macroHelp;
	}

	@Override
	public void invoke(Consumer<Block> action) {
		body.forEach(action);
	}

	@Override
	public CommandHelp getHelp() {
		return help;
	}
}