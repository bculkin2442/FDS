package bjc.fds.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static bjc.utils.ioutils.blocks.BlockReaders.*;

import bjc.fds.core.FDSState.InputMode;
import bjc.fds.core.macro.SimpleFDSMacro;
import bjc.utils.cli.CommandHelp;
import bjc.utils.cli.GenericHelp;
import bjc.utils.funcutils.StringUtils;
import bjc.utils.ioutils.blocks.Block;
import bjc.utils.ioutils.blocks.BlockReader;
import bjc.utils.ioutils.blocks.PushbackBlockReader;

import com.ibm.icu.text.BreakIterator;

/**
 * Runs a FDS (FDiskScript) interface.
 * 
 * This is a rudimentary console interface inspired heavily by FDisk's interface
 * style.
 * 
 * Commands are denoted by a single character, but can invoke submodes.
 * 
 * @author bjculkin
 *
 */
public class FDS {
	private static SimpleFDSMode<?> miscMode;

	static {
		miscMode = new SimpleFDSMode<>("MSC");
		configureMiscMode();
	}

	private static void configureMiscMode() {
		GenericHelp loadScriptHelp = new GenericHelp("load-script\tLoad a script from a file", "");
		miscMode.addCommand("X", FDS::loadScript, loadScriptHelp);

		GenericHelp quitProgramHelp = new GenericHelp("quit-program\tQuit the program", "");
		miscMode.addCommand("Q", (state) -> state.modes.drop(state.modes.size()), quitProgramHelp);

		GenericHelp inlineInputHelp = new GenericHelp("inline-input\tProvide data input inline with commands",
				"");
		miscMode.addCommand("I", (state) -> {
			if(state.mode == InputMode.CHORD) {
				state.mode = InputMode.INLINE;
			} else if(state.mode == InputMode.INLINE) {
				state.mode = InputMode.CHORD;
			} else {
				state.printer.printf("? MNV\n");
			}
		}, inlineInputHelp);

		GenericHelp dataMacroHelp = new GenericHelp("input-macro\tDefine a macro for providing data", "");
		miscMode.addCommand("i", (state) -> createMacro(state, state.dataMacros), dataMacroHelp);

		GenericHelp comMacroHelp = new GenericHelp("command-macro\tDefine a macro for providing commands", "");
		miscMode.addCommand("c", (state) -> createMacro(state, state.commandMacros), comMacroHelp);
	}

	private static void createMacro(FDSState<?> state, Map<String, SimpleFDSMacro> macroStore) {
		PushbackBlockReader source = state.datain;

		String macroName;

		state.dataPrompter.accept("Enter macro name: ");
		Block nameBlock = source.next();
		String nameContents = nameBlock.contents.trim();

		BreakIterator charBreaker = BreakIterator.getCharacterInstance();
		charBreaker.setText(nameContents);

		macroName = nameContents.substring(0, charBreaker.next());

		state.dataPrompter.accept("Enter macro summary: ");
		Block summBlock = source.next();
		String summContents = summBlock.contents.trim();

		GenericHelp macroHelp = new GenericHelp(summContents, "");

		state.dataPrompter.accept("Enter macro body (. to end body)");
		List<Block> macroBody = new LinkedList<>();

		while(source.hasNext()) {
			Block bodyBlock = source.next();

			String bodyContents = bodyBlock.contents.trim();

			if(bodyContents.equals(".")) break;

			macroBody.add(new Block(bodyBlock.blockNo, bodyContents, bodyBlock.startLine,
					bodyBlock.endLine));
		}

		macroStore.put(macroName, new SimpleFDSMacro(macroBody, macroHelp));

		state.dataPrompter.accept(state.defaultPrompt);
	}

	/**
	 * Run a provided FDS mode until it is exited or there is no more input.
	 * 
	 * @param state
	 *                The initial state for the mode.
	 * 
	 * @return The final state of the mode.
	 * 
	 * @throws FDSException
	 *                 If something went wrong during mode execution.
	 */
	public static <S> S runFDS(FDSState<S> state) throws FDSException {
		BlockReader blockSource = state.comin;

		while(blockSource.hasNext() && !state.modes.empty()) {
			Block comBlock = blockSource.next();

			handleCommandString(comBlock, state);
		}

		return state.state;
	}

	private static <S> void handleCommandString(Block comBlock, FDSState<S> state) throws FDSException {
		String comString = comBlock.contents.trim();
		BreakIterator charBreaker = BreakIterator.getCharacterInstance();
		charBreaker.setText(comString);

		switch(state.mode) {
		case INLINE:
			if(comString.contains(" ")) {
				handleInlineCommand(comString.split(" "), state, comBlock);
				break;
			}
		case CHORD:
			if(StringUtils.graphemeCount(comString) > 1) {
				chordCommand(comBlock, state, comString);
				break;
			}
		case NORMAL:
			handleCommand(comString.substring(0, charBreaker.next()), state);
			break;
		default:
			throw new FDSException(String.format("Unknown input mode '%s'", state.mode));
		}
	}

	private static <S> void handleInlineCommand(String[] commands, FDSState<S> state, Block comBlock) {
		boolean dataInput = false;

		for(int i = 0; i < commands.length; i++) {
			String strang = commands[i].trim();

			if(dataInput) {
				if(strang.equals(";")) {
					dataInput = false;
				} else {
					Block dataBlock = new Block(comBlock.blockNo + i, strang, comBlock.startLine,
							comBlock.endLine);

					state.datain.addBlock(dataBlock);
				}
			} else {
				chordCommand(comBlock, state, strang);

				dataInput = true;
			}
		}
	}

	private static <S> void chordCommand(Block comBlock, FDSState<S> state, String comString) {
		PushbackBlockReader source = state.comin;

		BreakIterator charBreaker = BreakIterator.getCharacterInstance();
		charBreaker.setText(comString);

		int lastPos = charBreaker.first();

		while(charBreaker.next() != BreakIterator.DONE && lastPos < comString.length()) {
			String c = comString.substring(lastPos, charBreaker.current());

			Block newCom = new Block(comBlock.blockNo + 1, c, comBlock.startLine, comBlock.startLine);

			source.addBlock(newCom);

			lastPos = charBreaker.current();
		}
	}

	@SuppressWarnings("unchecked")
	private static <S> void handleCommand(String com, FDSState<S> state) throws FDSException {
		if(state.modes.empty()) return;

		PrintStream printer = state.printer;

		/*
		 * Handle built-in commands over user commands.
		 */
		switch(com) {
		case "x":
			if(state.mode == InputMode.CHORD) {
				state.mode = InputMode.NORMAL;
			} else if(state.mode == InputMode.NORMAL) {
				state.mode = InputMode.CHORD;
			} else {
				printer.println("? MNV\n");
			}
			break;
		case "q":
			FDSMode<S> md = state.modes.pop();
			printer.printf("!< %s\n", md.getName());
			break;
		case "m":
			helpSummary(printer, state);
			break;
		case "M":
			/*
			 * We'll see if this cast actually causes any issues.
			 * 
			 * None of the commands in misc. mode should depend on
			 * the state type, so it shouldn't matter.
			 */
			state.modes.push((FDSMode<S>) miscMode);
			printer.printf("!> MSC\n");
			break;
		case "@":
			state.modes.push(state.dataMacroMode);
			printer.printf("!> IDM\n");
			break;
		case "#":
			state.modes.push(state.comMacroMode);
			printer.printf("!> ICM\n");
			break;
		default:
			FDSMode<S> curMode = state.modes.top();

			if(curMode.hasSubmode(com)) {
				FDSMode<S> mode = curMode.getSubmode(com);

				state.modes.push(mode);
				printer.printf("!> %s\n", mode.getName());
			} else if(curMode.hasCommand(com)) {
				curMode.getCommand(com).run(state);
			} else {
				printer.printf("? UBC '%s'\n", com);
			}
		}
	}

	private static <S> void loadScript(FDSState<S> state) {
		state.dataPrompter.accept("Enter a filename: ");

		String fileName = state.datain.next().contents.split(" ")[0];

		PrintStream printer = state.printer;

		try {
			FileInputStream fis = new FileInputStream(fileName);

			BlockReader reader = simple("\\R", new InputStreamReader(fis));

			state.comin = pushback(serial(reader, state.comin));
			state.datain = pushback(serial(reader, state.datain));
		} catch(@SuppressWarnings("unused") FileNotFoundException fnfex) {
			printer.printf("? FNF '%s'\n", fileName);
		}

		state.dataPrompter.accept(state.defaultPrompt);
	}

	private static <S> void helpSummary(PrintStream printer, FDSState<S> state) {
		FDSMode<S> mode = state.modes.top();

		printer.printf("\nHelp for mode %s:\n", mode.getName());

		for(String bound : mode.registeredChars()) {
			Collection<CommandHelp> help = mode.getHelp(bound);

			if(help.size() > 1) {
				for(CommandHelp hlp : help) {
					printer.printf("\t%s\t- %s\n", bound, hlp.getSummary());
				}

				printer.println();
			} else {
				CommandHelp hlp = help.iterator().next();

				printer.printf("\t%s\t- %s\n", bound, hlp.getSummary());
			}
		}

		printer.printf("\nGlobal Help:\n");
		printer.printf("\tx\t- chord mode\ttreat each character as a seperate command\n");
		printer.printf("\tq\t- exit mode\texit the current submode\n");
		printer.printf("\tm\t- show help\tshow this help message\n");
		printer.printf("\tM\t- misc. mode\tsubmode with misc. commands\n");
		printer.printf("\t@\t- invoke data macro\tinvoke bound data macros\n");
		printer.printf("\t#\t- invoke command macro\tinvoke bound command macros\n\n");
	}
}