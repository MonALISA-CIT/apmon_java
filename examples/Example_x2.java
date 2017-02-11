/**
 * \file Example_x2.java
 * This example shows how several jobs can be monitored with ApMon. We must
 * provide ApMon the PIDs of the jobs we want to monitor. In order to find 
 * the PIDs of the jobs we shall parse the output of the ps command. We shall 
 * monitor the following applications: the current job, MonALISA and Apache 
 * (assuming the last two are currently running). You may have to run this example
 * as root to obtain complete information on the jobs.
 * The file "destinations_x2.conf" contains the addresses of the hosts to which
 * we send the parameters, and also the corresponding ports. It also contains
 * lines in which different parameters from the job/system monitoring datagrams
 * can be enabled or disabled.
 */ 

import java.util.Random;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.StringTokenizer;
import apmon.*;
import apmon.lisa_host.cmdExec;
import apmon.util.*;

public class Example_x2 {
    private static Logger logger = Logger.getLogger("apmon");
    /* we need this so that we can use the getpid() function */
    static {
	    String osName = System.getProperty("os.name");
	    if (osName.indexOf("Linux") >=0)
		System.loadLibrary("nativeapm");	
    }
    /** Finds the PID of the parent process for an application knowing
     * the command line (or a part of it). 
     */
    public static int getAppPid(String cmd) {
	String psOut = (new cmdExec()).executeCommandReality("ps afx","");
	StringTokenizer st = new StringTokenizer(psOut, "\n");

	while(st.hasMoreTokens()) {
	    String line = st.nextToken();
	    if (line.indexOf(cmd) > 0) {
		StringTokenizer lst = new StringTokenizer(line, " \t");
		return Integer.parseInt(lst.nextToken());
	    }
	}
	return -1;
    }

    public static void main(String args[]) {
	String filename = "destinations_x2.conf";
	int nDatagrams = 20;
	Vector paramNames = new Vector();
	Vector paramValues = new Vector();
	Vector valueTypes = new Vector();
	double val = 0;
	int i, val2 = 0, mypid = 0, timestamp;
	ApMon apm = null;

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
	   
	try {
	    apm = new ApMon(filename);
	} catch (Exception e) {
	    logger.severe("Error initializing ApMon: " + e);
	    System.exit(-1);    
	}
	// set the time interval at which the config file is checked for changes	
	apm.setRecheckInterval(300);   
	apm.setJobMonitoring(true, 5);
	apm.setSysMonitoring(true, 10);
	apm.setGenMonitoring(true, 100); 

	String osName = System.getProperty("os.name");
	if (osName.indexOf("Linux") >= 0)
	    mypid = (int) (new NativeLinux().mygetpid());
	else {
	    logger.severe("This example is only for Linux systems.");
	    System.exit(-1);
	}		
	logger.info("process PID = " + mypid);

	/* monitor this job */
	apm.addJobToMonitor(mypid, "", "JobClusterx2_java_apmon", null);

	/* monitor MonALISA */
	int pid_ml = getAppPid("java -DMonaLisa_HOME");
	if (pid_ml != -1)
	    apm.addJobToMonitor(pid_ml, "", "JobClusterx2_java_ml", null);
	else
	    logger.warning("Error obtaining PID for: MonaLisa");

	/* monitor apache */
	int pid_a = getAppPid("apache");
	if (pid_a != -1)
	    apm.addJobToMonitor(pid_a, "", "JobClusterx2_java_apache", null);
	else
	    logger.warning("Error obtaining PID for: apache");
  
	for (i = 0; i < nDatagrams - 1; i++) {
	    /* add a value for the my_cpu_load (random between 0 and 2) */
	    val = 2 * rgen.nextDouble();  
	    logger.info("Sending " + val + " for my_cpu_load");
	    paramValues.setElementAt(new Double(val), 0);
	   			
	    /* add a value for my_cpu_idle (random between 0 and 20) */
	    val2 = (int)(50 * rgen.nextDouble());  
	    logger.info("Sending " + val2 + " for my_cpu_idle");
	    paramValues.setElementAt(new Integer(val2), 2);
				
	    try {
		apm.sendParameters("TestClusterx2_java", "MyNodex2", 
		       paramNames.size(), paramNames, valueTypes, paramValues);
		/* now send a datagram with timestamp (as if this was 3h ago) */
		timestamp = (int)System.currentTimeMillis() / 1000 - (3 * 3600);
		apm.sendTimedParameters("TestClusterOldx2_java", "MyNodex2", 
		       paramNames.size(), paramNames, valueTypes, paramValues, 
					timestamp);
		Thread.sleep(1000);
	    } catch(Exception e) {
		logger.warning("Send operation failed: " + e);
	    }
	   
	    if (i == 25) {
		//apm.removeJobToMonitor(pid_a);
		apm.removeJobToMonitor(mypid);
	    }
	}
	apm.stopIt();
    }
}
