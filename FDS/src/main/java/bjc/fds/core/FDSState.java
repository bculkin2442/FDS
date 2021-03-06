package bjc.fds.core;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import bjc.fds.core.macro.MacroFDSMode;
import bjc.fds.core.macro.SimpleFDSMacro;
import bjc.utils.esodata.SimpleStack;
import bjc.utils.esodata.Stack;
import bjc.utils.ioutils.blocks.PushbackBlockReader;

/**
 * Internal state for an FDS interface.
 *
 * @author bjculkin
 *
 * @param <S>
 *                The state type of the interface.
 */
public class FDSState<S> {
	/**
	 * The input mode for the interface.
	 *
	 * @author bjculkin
	 *
	 */
	public static enum InputMode {
		/**
		 * Normal mode.
		 *
		 * Reads only the first character in the block as a command.
		 */
		NORMAL,
		/**
		 * Reads every character in the block as a command.
		 */
		CHORD,
		/**
		 * Reads every character in the block, but after a terminal
		 * command, data will be read in-line separated by spaces until
		 * a semicolon is read.
		 *
		 * The semicolon can be escaped with a backslash.
		 */
		INLINE;
	}

	/**
	 * The state of the interface
	 */
	public S		state;
	/**
	 * The input mode for the interface.
	 */
	public InputMode	mode;

	/**
	 * The modes being used.
	 */
	public Stack<FDSMode<S>> modes;

	/**
	 * The source to read command blocks from.
	 */
	public PushbackBlockReader comin;

	/**
	 * The source to read data blocks from.
	 */
	public PushbackBlockReader datain;

	/**
	 * The destination for output.
	 */
	public PrintStream printer;

	/**
	 * The repository for data macros.
	 */
	public Map<String, SimpleFDSMacro> dataMacros;

	/**
	 * The repository for command macros.
	 */
	public Map<String, SimpleFDSMacro> commandMacros;

	FDSMode<S>	dataMacroMode;
	FDSMode<S>	comMacroMode;

	/**
	 * Function to change the current data prompt.
	 */
	public Consumer<String> dataPrompter;

	/**
	 * The default data prompt.
	 */
	public String defaultPrompt;

	/**
	 * Create a new interface state.
	 *
	 * @param stat
	 *                The initial state for the interface.
	 *
	 * @param inputMode
	 *                The input mode for the interface.
	 * @param cmin
	 *                The source of command blocks.
	 *
	 * @param datin
	 *                The source of data blocks.
	 *
	 * @param print
	 *                The destination for output.
	 * @param dataPrompt
	 *                The function to use for changing the data prompt.
	 *
	 * @param normalPrompt
	 *                The default data prompt.
	 */
	public FDSState(final S stat, final InputMode inputMode, final PushbackBlockReader cmin,
			final PushbackBlockReader datin, final PrintStream print, final Consumer<String> dataPrompt,
			final String normalPrompt) {
		state = stat;
		mode = inputMode;

		comin = cmin;
		datain = datin;
		printer = print;

		dataPrompter = dataPrompt;
		defaultPrompt = normalPrompt;

		modes = new SimpleStack<>();

		dataMacros = new HashMap<>();
		commandMacros = new HashMap<>();

		dataMacroMode = new MacroFDSMode<>(dataMacros, datain::addBlock);
		comMacroMode = new MacroFDSMode<>(commandMacros, comin::addBlock);
	}
}