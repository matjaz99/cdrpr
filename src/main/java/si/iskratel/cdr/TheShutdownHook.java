package si.iskratel.cdr;

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

		for (CdrSimulatorThread t :
				Start.simulatorThreads) {
			t.setRunning(false);
		}
		
	}
	
}
