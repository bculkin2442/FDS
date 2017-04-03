package bjc.fds.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import bjc.utils.cli.CommandHelp;
import bjc.utils.cli.NullHelp;

/**
 * A collection of related FDS commands.
 * 
 * @author bjculkin
 *
 * @param <S>
 *                The FDS state type.
 */
public interface FDSMode<S> {
	/**
	 * The default help for anything in a mode command.
	 */
	public static final List<CommandHelp> DEFAULT_HELP = Arrays.asList(new NullHelp());

	/**
	 * Get the name of this mode.
	 * 
	 * @return The mode of this name.
	 */
	default String getName() {
		return "Unnamed Mode";
	}

	/**
	 * Get all the characters that are registered to something in this mode.
	 * 
	 * In this context, something means a command or submode.
	 * 
	 * @return All of the characters registered to something in this mode.
	 */
	String[] registeredChars();

	/*
	 * Check for the existence of commands/submodes.
	 */

	/**
	 * Check if there is a command registered to the given character.
	 * 
	 * @param c
	 *                The character to check
	 * 
	 * @return Whether or not there is a command bound to that character.
	 */
	boolean hasCommand(String c);

	/**
	 * Check if there is a submode registered to the given character.
	 * 
	 * @param c
	 *                The character to check
	 * 
	 * @return Whether or not there is a submode bound to that character.
	 */
	boolean hasSubmode(String c);

	/*
	 * Get commands and submodes.
	 */

	/**
	 * Get the command attached to a given character.
	 * 
	 * @param c
	 *                The character to get the command for.
	 * 
	 * @return The command bound to that character.
	 * 
	 * @throws FDSException
	 *                 If there is no command bound to that character.
	 */
	FDSCommand<S> getCommand(String c) throws FDSException;

	/**
	 * Get the command attached to a given character.
	 * 
	 * @param c
	 *                The character to get the command for.
	 * 
	 * @return The command bound to that character.
	 * 
	 * @throws FDSException
	 *                 If there is no command bound to that character.
	 */
	FDSMode<S> getSubmode(String c) throws FDSException;

	/*
	 * Help utilities
	 */
	/**
	 * Get the help for what's bound to a character.
	 * 
	 * This should be one line.
	 * 
	 * @param c
	 *                The character to look at the help for.
	 * 
	 * @return The help for what's bound to the character.
	 */
	default Collection<CommandHelp> getHelp(String c) {
		return DEFAULT_HELP;
	}
}
