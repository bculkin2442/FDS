package bjc.fds.examples;

import java.io.InputStreamReader;

import bjc.fds.FDSException;
import bjc.fds.FDSMode;
import bjc.fds.FDSUtils;
import bjc.fds.SimpleFDSMode;
import bjc.fds.examples.TestContext;

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

		FDSMode<TestContext> testMode = new SimpleFDSMode<>();
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
