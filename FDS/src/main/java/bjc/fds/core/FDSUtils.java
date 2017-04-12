package bjc.fds.core;

import static bjc.utils.ioutils.blocks.BlockReaders.pushback;
import static bjc.utils.ioutils.blocks.BlockReaders.simple;
import static bjc.utils.ioutils.blocks.BlockReaders.trigger;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;

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
	@SuppressWarnings("resource")
	public static <S> S runFromReader(final Reader reader, final PrintStream out, final FDSMode<S> mode,
			final S ctx) throws FDSException {
		/*
		 * Input is closed through other readers.
		 */
		final BlockReader input = simple("\\R", reader);

		final Prompter comPrompter = new Prompter("Enter a command (m for help): ", out);
		final Prompter dataPrompter = new Prompter("> ", out);

		/*
		 * Input is closed through other readers.
		 */
		final BlockReader rawComInput = trigger(input, comPrompter);
		final BlockReader rawDataInput = trigger(input, dataPrompter);

		try (PushbackBlockReader comInput = pushback(rawComInput);
				PushbackBlockReader dataInput = pushback(rawDataInput)) {

			final FDSState<S> fdsState = new FDSState<>(ctx, InputMode.NORMAL, comInput, dataInput, out,
					dataPrompter::setPrompt, "> ");

			fdsState.modes.push(mode);

			FDS.runFDS(fdsState);
		} catch (final IOException ioex) {
			out.println("? IOX");
		}

		return ctx;
	}
}