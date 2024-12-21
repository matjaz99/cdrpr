package si.matjazcerkvenik.datasims.cdrpr.simulator;

import si.matjazcerkvenik.datasims.Start;
import si.matjazcerkvenik.datasims.cdrpr.simulator.generator.CdrNodeGeneratorThread;

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

		for (CdrNodeGeneratorThread t : Start.nodeSimulatorThreads) {
			t.setRunning(false);
		}
		
	}
	
}
