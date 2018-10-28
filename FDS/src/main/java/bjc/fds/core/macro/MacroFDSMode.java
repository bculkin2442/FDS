package bjc.fds.core.macro;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

import bjc.fds.core.FDSCommand;
import bjc.fds.core.FDSException;
import bjc.fds.core.FDSMode;
import bjc.fds.core.FDSState;
import bjc.utils.cli.CommandHelp;
import bjc.utils.ioutils.blocks.Block;

/**
 * A implementation of FDS mode that invokes macros bound into a map.
 *
 * @author EVE
 *
 * @param <S>
 *                The FDS state type.
 */
public class MacroFDSMode<S> implements FDSMode<S> {
	private final class MacroFDSCommand implements FDSCommand<S> {
		private final String macroName;

		public MacroFDSCommand(final String c) {
			macroName = c;
		}

		@Override
		public void run(final FDSState<S> state) {
			mcros.get(macroName).invoke(dst);
		}
	}

	/*
	 * The available macros.
	 */
	private final Map<String, SimpleFDSMacro> mcros;

	/*
	 * Where to send blocks from macros.
	 */
	private final Consumer<Block> dst;

	/**
	 * Create a new FDS mode for macros.
	 *
	 * @param macros
	 *                The macros to use.
	 *
	 * @param dest
	 *                The destination for blocks from the macros.
	 */
	public MacroFDSMode(final Map<String, SimpleFDSMacro> macros, final Consumer<Block> dest) {
		mcros = macros;
		dst = dest;
	}

	@Override
	public String[] registeredChars() {
		return mcros.keySet().toArray(new String[0]);
	}

	@Override
	public boolean hasCommand(final String comName) {
		return mcros.containsKey(comName);
	}

	@Override
	public boolean hasSubmode(final String submodeName) {
		return false;
	}

	@Override
	public FDSCommand<S> getCommand(final String comName) throws FDSException {
		return new MacroFDSCommand(comName);
	}

	@Override
	public FDSMode<S> getSubmode(final String submodeName) throws FDSException {
		throw new FDSException("Submodes aren't available in macroName modes");
	}

	@Override
	public Collection<CommandHelp> getHelp(final String c) {
		return Arrays.asList(mcros.get(c).getHelp());
	}
}