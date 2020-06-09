package si.iskratel.cdr;

public class MyShutdownHook extends Thread {
	
	@Override
	public void run() {
		System.out.println("Shutdown hook activated");
		
		// disconnect
		// store data to DB
		// close connection to DB
		// stop processes and threads
		// release resources...
		
		Test.running = false;

		for (CdrSimulatorThread t :
				Test.simulatorThreadThreads) {
			t.setRunning(false);
		}
		
	}
	
}
