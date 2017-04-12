package bjc.fds;

import static bjc.inflexion.InflectionML.iprintf;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import bjc.fds.core.FDSCommand;
import bjc.fds.core.FDSException;
import bjc.fds.core.FDSMode;
import bjc.fds.core.FDSState;
import bjc.utils.cli.CommandHelp;
import bjc.utils.cli.GenericHelp;
import bjc.utils.cli.NullHelp;

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
	private static final String	LIST_HELP	= "List Bindings";
	private static final String	EDIT_HELP	= "Edit Binding";
	private static final String	DELETE_HELP	= "Delete Binding";
	private static final String	CREATE_HELP	= "Create Binding";

	private final class DeleteBindingCommand implements FDSCommand<S> {
		public DeleteBindingCommand() {
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public void run(final FDSState<S> state) {
			state.dataPrompter.accept("Enter name of binding to delete: ");
			String varName = state.datain.next().contents.trim();

			while (!varMap.containsKey(varName)) {
				state.printer.printf("! UNV\n");

				varName = state.datain.next().contents.trim();
			}

			varMap.remove(varName);
		}
	}

	private final class EditBindingCommand implements FDSCommand<S> {
		public EditBindingCommand() {
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public void run(final FDSState<S> state) {
			state.dataPrompter.accept("Enter name of binding to edit: ");
			String varName = state.datain.next().contents.trim();

			while (!varMap.containsKey(varName)) {
				state.printer.printf("! UNV\n");

				varName = state.datain.next().contents.trim();
			}

			state.dataPrompter.accept("Enter new variable value: ");
			String varBody = state.datain.next().contents.trim();

			while (!validator.test(varBody)) {
				state.printer.printf("! INV\n");

				varBody = state.datain.next().contents.trim();
			}

			final I varValue = transform.apply(varBody);
			varMap.put(varName, varValue);
		}
	}

	private final class CreateBindingCommand implements FDSCommand<S> {
		public CreateBindingCommand() {
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public void run(final FDSState<S> state) {
			state.dataPrompter.accept("Enter variable name: ");
			final String varName = state.datain.next().contents.trim();

			state.dataPrompter.accept("Enter variable value: ");
			String varBody = state.datain.next().contents.trim();

			while (!validator.test(varBody)) {
				state.printer.printf("! INV\n");

				varBody = state.datain.next().contents.trim();
			}

			final I varValue = transform.apply(varBody);
			varMap.put(varName, varValue);
		}
	}

	private final class ListVariablesCommand implements FDSCommand<S> {

		public ListVariablesCommand() {
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public void run(final FDSState<S> state) {
			if (varMap.size() == 0) {
				state.printer.printf("No variables bound\n");
				return;
			}

			final String msg = iprintf("Bound <#ds:%d> <N:variables>:\n", varMap.size());

			state.printer.printf(msg);
			for (final Entry<String, I> entry : varMap.entrySet()) {
				state.printer.printf("\t%s\t%s\n", entry.getKey(), entry.getValue());
			}

			state.printer.println();
		}
	}

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

	private final Map<String, I> varMap;

	private final Predicate<String>		validator;
	private final Function<String, I>	transform;

	private final ListVariablesCommand	listVariablesCommand;
	private final CreateBindingCommand	createBindingCommand;
	private final EditBindingCommand	editBindingCommand;
	private final DeleteBindingCommand	deleteBindingCommand;

	private final String modeName;

	/**
	 * Create a new mode for binding variables.
	 *
	 * @param name
	 *                The name of the mode.
	 *
	 * @param vMap
	 *                The place to put the variables.
	 *
	 * @param validtor
	 *                The predicate to determine valid input.
	 *
	 * @param transfrm
	 *                The function to convert input into values.
	 */
	public VariableMode(final String name, final Map<String, I> vMap, final Predicate<String> validtor,
			final Function<String, I> transfrm) {
		modeName = name;

		varMap = vMap;
		validator = validtor;
		transform = transfrm;

		listVariablesCommand = new ListVariablesCommand();
		createBindingCommand = new CreateBindingCommand();
		editBindingCommand = new EditBindingCommand();
		deleteBindingCommand = new DeleteBindingCommand();
	}

	@Override
	public String getName() {
		return modeName;
	}

	@Override
	public String[] registeredChars() {
		return COMMAND_ARR;
	}

	@Override
	public boolean hasCommand(final String c) {
		return COMMANDS.contains(c);
	}

	@Override
	public boolean hasSubmode(final String c) {
		/*
		 * None of our commands are submodes.
		 */
		return false;
	}

	@Override
	public FDSCommand<S> getCommand(final String c) throws FDSException {
		switch (c) {
		case "l":
			return listVariablesCommand;
		case "c":
			return createBindingCommand;
		case "e":
			return editBindingCommand;
		case "d":
			return deleteBindingCommand;
		default:
			throw new FDSException(String.format("Unknown command '%s'"));
		}
	}

	@Override
	public FDSMode<S> getSubmode(final String c) throws FDSException {
		throw new FDSException(String.format("No submodes available"));
	}

	@Override
	public Collection<CommandHelp> getHelp(final String c) {
		return Arrays.asList(internalGetHelp(c));
	}

	private static CommandHelp internalGetHelp(final String c) {
		switch (c) {
		case "c":
			return new GenericHelp(CREATE_HELP, CREATE_HELP);
		case "d":
			return new GenericHelp(DELETE_HELP, DELETE_HELP);
		case "e":
			return new GenericHelp(EDIT_HELP, EDIT_HELP);
		case "l":
			return new GenericHelp(LIST_HELP, LIST_HELP);
		default:
			return new NullHelp();
		}
	}
}