package example;

public class InstrumentMeLaunch 
{
	public static void main(String args[]) throws Exception {
		System.out.println("Currently running sample code in background thread.");
		System.out.println("Make sure this program has -javaagent:./path/to/intrace-agent.jar on its command line.");
		System.out.println("Start InTrace GUI, download-able at https://mchr3k.github.io/org.intrace/");
		System.out.println("'Connect' & then configure GUI to trace any of these patterns:\n\n");
		System.out.println("example.InstrumentMe					-- to trace all methods in class example.InstrumentMe");
		System.out.println("example.InstrumentMe					-- to trace all methods in class example.InstrumentMeLaunch");
		System.out.println("example.InstrumentMe#<init>()V				-- to trace this single method.");
		System.out.println("example.InstrumentMe#run()V					-- to trace this single method.");
		System.out.println("example.InstrumentMe#byteArg(B)V				-- to trace this single method.");
		System.out.println("example.InstrumentMeLaunch#<init>()V			-- to trace this single method.");
		System.out.println("example.InstrumentMeLaunch#main([Ljava/lang/String;)V	-- to trace this single method.\n\n");
     
		System.out.println("Press Ctrl+C to quit this program.");
		Runnable r = new InstrumentMe();
		while(true) {
			System.out.println("About to start thread.");
			new Thread(r).start();
			Thread.sleep(2000);
			
		}
	}
}
