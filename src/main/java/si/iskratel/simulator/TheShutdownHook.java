package si.iskratel.simulator;

import si.iskratel.simulator.generator.CdrGeneratorThread;

public class TheShutdownHook extends Thread {
	
	@Override
	public void run() {
		System.out.println("Shutdown hook activated");
		
		// disconnect
		// store data to DB
		// close connection to DB
		// stop processes and threads
		// release resources...
		
		Start.running = false;

		for (CdrGeneratorThread t :
				Start.simulatorThreads) {
			t.setRunning(false);
		}
		
	}
	
}
