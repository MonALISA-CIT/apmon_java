 /**
 * \file Example_4.java
 * This example shows how multiple ApMon objects can be used in the same 
 * program. However, this is not the suggested way to use ApMon; if possible, 
 * it is better to work with a single ApMon object because in this way the
 * network socket and other resources are re-used.
 * The number of times we instantiate an ApMon object can be specified in the 
 * command line (if it is not, the default is 20).
 */ 
import java.util.Random;
import java.util.Vector;
import java.util.logging.Logger;

import apmon.*;

public class Example_4 {
    private static Logger logger = Logger.getLogger("apmon");
	
    public static void main(String args[]) {	    
	String filename = "destinations_1.conf";
	String initURL = "http://lcfg.rogrid.pub.ro/~corina/destinations_x1.conf";
	long nObjects = 20, i;
	ApMon apm = null;
	double val = 0;		
	Random rgen = new Random();
	
	if (args.length == 1)
	    nObjects = Integer.parseInt(args[0]);
	
	Vector initVect = new Vector();
	initVect.add(initURL);

	for (i = 0; i < nObjects; i++) {
	    try {
		apm = new ApMon(filename);
		apm.sendParameter("multi_init1", null, "my_cpu_load", val);
		//Thread.sleep(2000);
		apm.stopIt();
	    } catch (Exception e) {
		logger.severe("ApMon error: " + e);
		e.printStackTrace();
		System.exit(-1);    
	    }
	}
    }
}
