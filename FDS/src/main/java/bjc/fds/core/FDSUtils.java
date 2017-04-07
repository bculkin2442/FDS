package bjc.fds.core;

import java.io.PrintStream;
import java.io.Reader;

import static bjc.utils.ioutils.blocks.BlockReaders.pushback;
import static bjc.utils.ioutils.blocks.BlockReaders.simple;
import static bjc.utils.ioutils.blocks.BlockReaders.trigger;

import bjc.fds.core.FDSState.InputMode;
import bjc.utils.ioutils.Prompter;
import bjc.utils.ioutils.blocks.BlockReader;
import bjc.utils.ioutils.blocks.PushbackBlockReader;

/**
 * Utilities for dealing with FDS
 * 
 * @author bjculkin
 *
 */
public class FDSUtils {
	/**
	 * Run a FDS instance from a reader.
	 * 
	 * @param reader
	 *                The reader to use.
	 * 
	 * @param out
	 *                The output stream to use.
	 * 
	 * @param mode
	 *                The mode to use.
	 * 
	 * @param ctx
	 *                The initial state.
	 * 
	 * @return The final state.
	 * 
	 * @throws FDSException
	 *                 If something goes wrong.
	 */
	public static <S> S runFromReader(Reader reader, PrintStream out, FDSMode<S> mode, S ctx) throws FDSException {
		BlockReader input = simple("\\R", reader);

		Prompter comPrompter = new Prompter("Enter a command (m for help): ", out);
		Prompter dataPrompter = new Prompter("> ", out);

		BlockReader rawComInput = trigger(input, comPrompter);
		BlockReader rawDataInput = trigger(input, dataPrompter);

		PushbackBlockReader comInput = pushback(rawComInput);
		PushbackBlockReader dataInput = pushback(rawDataInput);

		FDSState<S> fdsState = new FDSState<>(ctx, InputMode.NORMAL, comInput, dataInput, out,
				dataPrompter::setPrompt, "> ");

		fdsState.modes.push(mode);

		FDS.runFDS(fdsState);

		return ctx;
	}
}