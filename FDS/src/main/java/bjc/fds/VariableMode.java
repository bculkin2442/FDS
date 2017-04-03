package bjc.fds;

import bjc.fds.FDSCommand;
import bjc.fds.FDSException;
import bjc.fds.FDSMode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A mode that supports variable input from strings.
 * 
 * @author EVE
 * 
 * @param <I>
 *                The type of the variables.
 * 
 * @param <S>
 *                The FDS state type.
 */
public class VariableMode<I, S> implements FDSMode<S> {
	/*
	 * The set of valid commands.
	 */
	private static final Set<String>	COMMANDS;
	private static String[]			COMMAND_ARR;

	/*
	 * Initialize static state.
	 */
	static {
		COMMANDS = new HashSet<>();

		/**
		 * List bindings.
		 */
		COMMANDS.add("l");

		/**
		 * Create a binding.
		 */
		COMMANDS.add("c");

		/**
		 * Edit a binding.
		 */
		COMMANDS.add("e");

		/**
		 * Delete a binding.
		 */
		COMMANDS.add("d");

		COMMAND_ARR = COMMANDS.toArray(new String[0]);
	}

	private Map<String, I> varMap;

	private Predicate<String>	validator;
	private Function<String, I>	transform;

	/**
	 * Create a new mode for binding variables.
	 * 
	 * @param varMap
	 *                The place to put the variables.
	 * 
	 * @param validator
	 *                The predicate to determine valid input.
	 * 
	 * @param transform
	 *                The function to convert input into values.
	 */
	public VariableMode(Map<String, I> varMap, Predicate<String> validator, Function<String, I> transform) {
		this.varMap = varMap;
		this.validator = validator;
		this.transform = transform;
	}

	@Override
	public String[] registeredChars() {
		return COMMAND_ARR;
	}

	@Override
	public boolean hasCommand(String c) {
		return COMMANDS.contains(c);
	}

	@Override
	public boolean hasSubmode(String c) {
		/*
		 * None of our commands are submodes.
		 */
		return false;
	}

	@Override
	public FDSCommand<S> getCommand(String c) throws FDSException {
		switch(c) {
		case "l":
			return state -> {
				System.out.printf("Bound variables:\n");
				for(Entry<String, I> entry : varMap.entrySet()) {
					System.out.printf("\t%s\t%s\n", entry.getKey(), entry.getValue());
				}
				System.out.println();
			};
		default:
			throw new FDSException(String.format("Unknown command '%s'"));
		}
	}

	@Override
	public FDSMode<S> getSubmode(String c) throws FDSException {
		throw new FDSException(String.format("No submodes available"));
	}
}