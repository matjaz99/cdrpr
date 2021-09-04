package si.iskratel.simulator;

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
