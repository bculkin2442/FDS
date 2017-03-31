package bjc.fds.core;

import bjc.utils.ioutils.Block;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
		private String macroName;

		public MacroFDSCommand(String c) {
			macroName = c;
		}

		@Override
		public void run(FDSState<S> state) {
			macros.get(macroName).forEach(dest);
		}
	}

	/*
	 * The available macros.
	 */
	private Map<String, List<Block>> macros;

	/*
	 * Where to send blocks from macros.
	 */
	private Consumer<Block> dest;

	/**
	 * Create a new FDS mode for macros.
	 * 
	 * @param macros
	 *                The macros to use.
	 * 
	 * @param dest
	 *                The destination for blocks from the macros.
	 */
	public MacroFDSMode(Map<String, List<Block>> macros, Consumer<Block> dest) {
		this.macros = macros;
		this.dest = dest;
	}

	@Override
	public String[] registeredChars() {
		return macros.keySet().toArray(new String[0]);
	}

	@Override
	public boolean hasCommand(String comName) {
		return macros.containsKey(comName);
	}

	@Override
	public boolean hasSubmode(String submodeName) {
		return false;
	}

	@Override
	public FDSCommand<S> getCommand(String comName) throws FDSException {
		return new MacroFDSCommand(comName);
	}

	@Override
	public FDSMode<S> getSubmode(String submodeName) throws FDSException {
		throw new FDSException("Submodes aren't available in macroName modes");
	}
}