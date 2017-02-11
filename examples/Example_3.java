/**
 * \file Example_3.java
 * This example illustrates the way to send several parameters in the
 * same UDP datagram, with the functions sendParameters() and 
 * sendTimedParameters().
 * The number of parameter sets can be specified in the command line (if 
 * it is not, the default is 20). A parameter set contains: the OS name and two 
 * random values for the parameters "my_cpu_load" and "my_cpu_idle".
 * The number of parameter sets that will be sent can be specified in the command 
 * line (if it is not, the default is 20). For each parameter set two 
 * datagrams are sent: one with a timestamp and one without a timestamp. For the
 * latter, the local time at the MonALISA host will be considered.
 * The file "destinations_3.conf" contains the addresses of the hosts to which
 * we send the parameters, and also the corresponding ports.
 */ 
import java.util.Random;
import java.util.Vector;
import apmon.*;

public class Example_3 {
	public static void main(String args[]) {
	   String filename = "destinations_3.conf";  
	   int nDatagrams = 20;
	   Vector paramNames = new Vector();
	   Vector paramValues = new Vector();
	   Vector valueTypes = new Vector();
	   double val = 0;
	   int val2 = 0, timestamp;
	   int i;

	   Random rgen = new Random();

	   if (args.length == 1)
		   nDatagrams = Integer.parseInt(args[0]);
    
	   paramNames.add(new String("my_cpu_load"));
	   paramValues.add(new Double(val));
	   valueTypes.add(new Integer(ApMon.XDR_REAL64));
	   paramNames.add(new String("my_os_name"));
	   paramValues.add(new String("linux"));
	   valueTypes.add(new Integer(ApMon.XDR_STRING));
	   paramNames.add(new String("my_cpu_idle"));
	   paramValues.add(new Integer(val2));
	   valueTypes.add(new Integer(ApMon.XDR_INT32));
	   
	   //System.out.println((String)paramValues.get(1));
	   try {
	   		ApMon apm = new ApMon(filename);
	   		for (i = 0; i < nDatagrams; i++) {
	   			/* add a value for the CPU load (random between 0 and 2) */
	   			val = 2 * rgen.nextDouble();  
	   			//System.out.println("Sending " + val + " for my_cpu_load");
	   			paramValues.setElementAt(new Double(val), 0);
	   			
				/* add a value for cpu_idle (random between 0 and 20) */
				val2 = (int)(50 * rgen.nextDouble());  
			
				//System.out.println("Sending " + val2 + " for my_cpu_idle");
				paramValues.setElementAt(new Integer(val2), 2);
				
	   			try {
	   				apm.sendParameters("TestCluster3_java", "MyNode3", 
	   						paramNames.size(), paramNames, valueTypes, paramValues);
	   				/* now send a datagram with timestamp (as if this was 9h ago) */
					long crtTime = System.currentTimeMillis();
					timestamp = (int)(crtTime / 1000 - (9 * 3600));
					apm.sendTimedParameters("TestClusterOld3_java", "MyNode3", 
	   					paramNames.size(), paramNames, valueTypes, paramValues, timestamp);
	   			} catch(Exception e) {
	   				System.err.println("Send operation failed: ");
					e.printStackTrace();
	   			}
				Thread.sleep(500);
	   		} // for
			apm.stopIt();			
	   } catch (Exception e) {
	   		e.printStackTrace();
		}
	}
}
