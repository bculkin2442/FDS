package bjc.fds.core;

import static java.lang.String.format;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import bjc.utils.cli.CommandHelp;

/**
 * Simple implementation of {@link FDSMode}.
 *
 * @author bjculkin
 *
 * @param <S>
 *                The FDS state type.
 */
public class SimpleFDSMode<S> implements FDSMode<S> {
	private Map<String, FDSCommand<S>>	commands;
	private Map<String, FDSMode<S>>		modes;
	private Multimap<String, CommandHelp>	help;

	private Set<String>	registered;
	private String[]	registeredArray;
	private boolean		changed;

	private String modeName;

	/**
	 * Create a new empty FDS mode.
	 */
	public SimpleFDSMode() {
		commands = new HashMap<>();
		modes = new HashMap<>();
		help = HashMultimap.create();

		registered = new HashSet<>();
		changed = true;
	}

	/**
	 * Create a new empty mode with a given name.
	 *
	 * @param name
	 *                The name of the mode.
	 */
	public SimpleFDSMode(final String name) {
		this();

		modeName = name;
	}

	@Override
	public String getName() {
		if (modeName == null) return FDSMode.super.getName();

		return modeName;
	}

	/**
	 * Add a command to the mode.
	 *
	 * @param c
	 *                The character to bind to the command.
	 *
	 * @param comm
	 *                The command to add.
	 *
	 * @param hlp
	 *                The help for the command.
	 *
	 * @throws IllegalArgumentException
	 *                 If the character is already bound to a command.
	 */
	public void addCommand(final String c, final FDSCommand<S> comm, final CommandHelp hlp) {
		if (comm == null)
			throw new NullPointerException("Command must not be null");
		else if (commands.containsKey(c))
			throw new IllegalArgumentException(format("Character '%s' is already bound"));

		commands.put(c, comm);
		help.put(c, hlp);

		registered.add(c);
		if (!changed) {
			changed = true;
		}
	}

	/**
	 * Add a submode to the mode.
	 *
	 * @param c
	 *                The character to bind to the submode.
	 *
	 * @param mode
	 *                The submode to add.
	 *
	 * @param hlp
	 *                The help for the submode.
	 *
	 * @throws IllegalArgumentException
	 *                 If the character is already bound to a submode.
	 */
	public void addSubmode(final String c, final FDSMode<S> mode, final CommandHelp hlp) {
		if (mode == null)
			throw new NullPointerException("Mode must not be null");
		else if (modes.containsKey(c))
			throw new IllegalArgumentException(format("Character '%s' is already bound"));

		modes.put(c, mode);
		help.put(c, hlp);

		registered.add(c);
		if (!changed) {
			changed = true;
		}
	}

	@Override
	public String[] registeredChars() {
		if (!changed) return registeredArray;

		registeredArray = new String[registered.size()];

		int i = 0;
		for (final String c : registered) {
			registeredArray[i] = c;

			i += 1;
		}

		changed = false;

		return registeredArray;
	}

	@Override
	public boolean hasCommand(final String c) {
		return commands.containsKey(c);
	}

	@Override
	public boolean hasSubmode(final String c) {
		return modes.containsKey(c);
	}

	@Override
	public FDSCommand<S> getCommand(final String c) throws FDSException {
		if (!commands.containsKey(c)) throw new FDSException(String.format("No command bound to '%s'", c));

		return commands.get(c);
	}

	@Override
	public FDSMode<S> getSubmode(final String c) throws FDSException {
		if (!modes.containsKey(c)) throw new FDSException(String.format("No mode bound to '%s'", c));

		return modes.get(c);
	}

	@Override
	public Collection<CommandHelp> getHelp(final String c) {
		return help.get(c);
	}
}