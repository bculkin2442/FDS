package bjc.fds.examples;

import java.io.InputStreamReader;
import java.util.HashMap;

import bjc.fds.VariableMode;
import bjc.fds.core.FDSException;
import bjc.fds.core.FDSMode;
import bjc.fds.core.FDSUtils;
import bjc.fds.core.SimpleFDSMode;
import bjc.utils.cli.GenericHelp;
import bjc.utils.functypes.ID;

/**
 * Simple example for FDS.
 * 
 * @author bjculkin
 *
 */
public class FDSExample {
	/**
	 * Main method.
	 * 
	 * @param args
	 *                Unused CLI arguments.
	 */
	public static void main(String[] args) {
		System.out.println("Entering rudimentary FDS");
		System.out.println();

		HashMap<String, String> stringVars = new HashMap<>();
		FDSMode<TestContext> stringVarMode = new VariableMode<>("String Variable Mode", stringVars,
				(strang) -> true, ID.id());
		GenericHelp stringVarHelp = new GenericHelp("String Variables", "");

		SimpleFDSMode<TestContext> testMode = new SimpleFDSMode<>("Test Mode");
		testMode.addSubmode("v", stringVarMode, stringVarHelp);

		TestContext ctx = new TestContext();

		InputStreamReader reader = new InputStreamReader(System.in);

		try {
			FDSUtils.runFromReader(reader, System.out, testMode, ctx);
		} catch(FDSException fex) {
			fex.printStackTrace();
		}

		System.out.println();
		System.out.println("Exiting FDS");
	}
}
