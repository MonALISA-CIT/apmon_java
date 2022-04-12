/*
 * ApMon - Application Monitoring Tool
 * Version: 2.2.9
 *
 * Copyright (C) 2006 - 2010 California Institute of Technology
 *
 * Permission is hereby granted, free of charge, to use, copy and modify
 * this software and its documentation (the "Software") for any
 * purpose, provided that existing copyright notices are retained in
 * all copies and that this notice is included verbatim in any distributions
 * or substantial portions of the Software.
 * This software is a part of the MonALISA framework (http://monalisa.caltech.edu).
 * Users of the Software are asked to feed back problems, benefits,
 * and/or suggestions about the software to the MonALISA Development Team
 * (MonALISA-CIT@cern.ch). Support for this software - fixing of bugs,
 * incorporation of new features - is done on a best effort basis. All bug
 * fixes and enhancements will be made available under the same terms and
 * conditions as the original software,
 * 
 * IN NO EVENT SHALL THE AUTHORS OR DISTRIBUTORS BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE, ITS DOCUMENTATION, OR ANY DERIVATIVES THEREOF,
 * EVEN IF THE AUTHORS HAVE BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE AUTHORS AND DISTRIBUTORS SPECIFICALLY DISCLAIM ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT. THIS SOFTWARE IS
 * PROVIDED ON AN "AS IS" BASIS, AND THE AUTHORS AND DISTRIBUTORS HAVE NO
 * OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS.
 */

package apmon;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import apmon.lisa_host.HostPropertiesMonitor;
import apmon.lisa_host.cmdExec;

/**
 * Data structure used for sending monitoring data to a MonaLisa module.
 * The data is packed in UDP datagrams, in XDR format.
 * A datagram has the following structure:
 * - header which contains the ApMon version and the password for the MonALISA
 * host and has the following syntax: v:<ApMon_version>p:<password>
 * - cluster name (string)
 * - node name (string)
 * - number of parameters (int)
 * - for each parameter: name (string), value type (int), value
 * <BR>
 * There are two ways to send parameters:
 * 1) a single parameter in a packet (with the function sendParameter() which
 * has several overloaded variants
 * 2) multiple parameters in a packet (with the function sendParameters())
 * <BR>
 * ApMon can be configured to send periodically datagrams with monitoring information
 * concerning the current application or the whole system. Some of the monitoring
 * information is only available on Linux systems.
 */

public class ApMon {

	/**
	 * ApMon version
	 */
	public static final String APMON_VERSION = "2.2.9";

	/**
	 * Max UDP datagram size
	 */
	public static final int MAX_DGRAM_SIZE = 8192; // TODO: raise it to 65500 (less than 64K otherwise the full UDP packet is too large) after GeneridUDPListener is deployed with a similarly large
													// value

	/** < Maximum UDP datagram size. */
	public static final int XDR_STRING = 0;

	/** < Used to code the string data type */
	public static final int XDR_INT32 = 2;

	/** < Used to code the 4 bytes integer data type */
	public static final int XDR_REAL32 = 4;

	/** < Used to code the 4 bytes real data type */
	public static final int XDR_REAL64 = 5;

	/** < Used to code the 8 bytes real data type */
	public static final int DEFAULT_PORT = 8884;

	/** < The default port on which MonALISA listens */

	/** Time interval (in sec) at which job monitoring datagrams are sent. */
	public static final int JOB_MONITOR_INTERVAL = 20;

	/** Time interval (in sec) at which system monitoring datagams are sent. */
	public static final int SYS_MONITOR_INTERVAL = 20;

	/** Time interval (in sec) at which the configuration files are checked for changes. */
	public static final int RECHECK_INTERVAL = 600;

	/** The maxim number of mesages that will be sent to MonALISA */
	public static final int MAX_MSG_RATE = 20;

	/**
	 * The number of time intervals at which ApMon sends general system monitoring information (considering the time
	 * intervals at which ApMon sends system monitoring information).
	 */
	public static final int GEN_MONITOR_INTERVALS = 100;

	/** Constant that indicates this object was initialized from a file. */
	static final int FILE_INIT = 1;

	/** Constant that indicates this object was initialized from a list. */
	static final int LIST_INIT = 2;

	/** Constant that indicates this object was initialized directly. */
	static final int DIRECT_INIT = 3;

	/** The initialization source (can be a file or a list). */
	Object initSource = null;

	/** The initialization type (from file / list / directly). */
	int initType;

	/**
	 * The configuration file and the URLs are checked for changes at this numer of seconds (if the network connections
	 * are good).
	 */
	long recheckInterval = RECHECK_INTERVAL;

	/**
	 * If the configuraion URLs cannot be reloaded, the interval until the next attempt will be increased. This is the
	 * actual value of the interval that is used by ApMon
	 */
	long crtRecheckInterval = RECHECK_INTERVAL;

	/**
	 * ML Cluster name
	 */
	String clusterName;

	/** < The name of the monitored cluster. */
	String nodeName;

	/** < The name of the monitored node. */

	Vector<String> destAddresses;

	/** < The IP addresses where the results will be sent. */
	Vector<Integer> destPorts;

	/** < The ports where the destination hosts listen. */
	Vector<String> destPasswds;

	/** < The Passwdords used for the destination hosts. */

	byte[] buf;

	/** < The buffer which holds the message data (encoded in XDR). */
	int dgramSize;

	/** < The size of the data inside the datagram */

	/**
	 * Hashtable which holds the initialization resources (Files, URLs) that must be periodically checked for changes,
	 * and their latest modification times
	 */
	Hashtable<Object, Long> confResources;

	/**
	 * Stream
	 */
	ByteArrayOutputStream baos;

	/**
	 * Socket
	 */
	DatagramSocket dgramSocket;

	/**
	 * The background thread which performs operations like checking the configuration file/URLs for changes and sending
	 * datagrams with monitoring information.
	 */
	BkThread bkThread = null;

	/** Is true if the background thread was started. */
	boolean bkThreadStarted = false;

	/** Protects the variables that hold the settings for the background thread. */
	Object mutexBack = new Object();

	/** Used for the wait/notify mechanism in the background thread. */
	Object mutexCond = new Object();

	/** Indicates if any of the settings for the background thread was changed. */
	boolean condChanged = false;

	/** These flags indicate changes in the monitoring configuration. */
	boolean recheckChanged;

	/**
	 * Job monitoring changed
	 */
	boolean jobMonChanged;

	/**
	 * System monitoring changed
	 */
	boolean sysMonChanged;

	/**
	 * If this flag is set to true, when the value of a parameter cannot be read from proc/, ApMon will not attempt to
	 * include that value in the next datagrams.
	 */
	boolean autoDisableMonitoring = true;

	/** If this flag is true, the configuration file / URLs are periodically rechecked for changes. */
	boolean confCheck = false;

	/**
	 * If this flag is true, packets with system information taken from /proc are periodically sent to MonALISA
	 */
	boolean sysMonitoring = false;

	/**
	 * If this flag is true, packets with job information taken from /proc are periodically sent to MonALISA
	 */
	boolean jobMonitoring = false;

	/**
	 * If this flag is true, packets with general system information taken from /proc are periodically sent to MonALISA
	 */
	boolean genMonitoring = false;

	/** Job/System monitoring information obtained from /proc is sent at these time intervals */
	long jobMonitorInterval = JOB_MONITOR_INTERVAL;

	/**
	 * System monitoring interval
	 */
	long sysMonitorInterval = SYS_MONITOR_INTERVAL;

	/**
	 * Max message rate
	 */
	int maxMsgRate = MAX_MSG_RATE;

	/**
	 * General system monitoring information is sent at a time interval equal to genMonitorIntervals *
	 * sysMonitorInterval.
	 */
	int genMonitorIntervals = GEN_MONITOR_INTERVALS;

	/**
	 * Hashtables that associate the names of the parameters included in the monitoring datagrams and flags that
	 * indicate whether they are active or not.
	 */
	long sysMonitorParams;

	/**
	 * Job monitoring paramters
	 */
	long jobMonitorParams;

	/**
	 * System monitoring parameters
	 */
	long genMonitorParams;

	/**
	 * The time when the last datagram with job monitoring information was sent (in milliseconds since the Epoch).
	 */
	long lastJobInfoSend;

	/**
	 * The time when the last datagram with job monitoring information was sent (in milliseconds since the Epoch).
	 */
	long lastSysInfoSend;

	/** The last value for "utime" for the current process that was read from proc/ (only on Linux). */
	double lastUtime;

	/** The last value for "stime" for the current process that was read from proc/ (only on Linux). */
	double lastStime;

	// long appPID;

	/** The name of the host on which ApMon currently runs. */
	String myHostname = null;

	/** The IP address of the host on which ApMon currently runs. */
	String myIP = null;

	/** The number of CPUs allocated to the job slot (wall time multiplier). */
	int numCPUs = 1;

	/** The names of the network interfaces on this machine. */
	Vector<String> netInterfaces = new Vector<>();

	/** The IPs of this machine. */
	Vector<String> allMyIPs = new Vector<>();

	/** the cluster name that will be included in the monitoring datagrams */
	String sysClusterName = "ApMon_userSend";

	/** the node name that will be included in the monitoring datagrams */
	String sysNodeName = null;

	/**
	 * Monitored jobs
	 */
	Vector<MonitoredJob> monJobs = new Vector<>();

	/**
	 * Sequence
	 */
	private final AtomicInteger SEQ_NR = new AtomicInteger(0);

	private final int INSTANCE_ID = ThreadLocalRandom.current().nextInt(0x7FFFFFFF);

	private static final Logger logger = Logger.getLogger("apmon");

	private static final String osName = System.getProperty("os.name");

	/** Java type -> XDR Type mapping **/
	private static Map<String, Integer> mValueTypes = new HashMap<>();

	static {

		try {
			LogManager logManager = LogManager.getLogManager();

			// check if LogManager is already defined
			if (logManager.getProperty("handlers") == null) {
				try {
					FileHandler fh = null;
					try {
						fh = new FileHandler("apmon.log");
						fh.setFormatter(new SimpleFormatter());
						logger.addHandler(fh);
					}
					catch (Throwable t) {
						t.printStackTrace();
					}

					logger.setUseParentHandlers(false);
					logger.setLevel(Level.INFO);
				}
				catch (Throwable t) {
					System.err.println("[ ApMon ] [ static init ] [ logging ] Unable to load default logger props. Cause:");
					t.printStackTrace();
				}
			}
			else {
				if (logger.isLoggable(Level.FINE)) {
					logger.log(Level.FINE, "[ ApMon ] [ static init ] [ logging ] uses predefined logging properties");
				}
			}

		}
		catch (Throwable t) {
			System.err.println("[ ApMon ] [ static init ] [ logging ] Unable to check/load default logger props. Cause:");
			t.printStackTrace();
		}

		mValueTypes.put(String.class.getName(), Integer.valueOf(XDR_STRING));
		mValueTypes.put(Short.class.getName(), Integer.valueOf(XDR_INT32));
		mValueTypes.put(Integer.class.getName(), Integer.valueOf(XDR_INT32));
		mValueTypes.put(Long.class.getName(), Integer.valueOf(XDR_REAL64));
		mValueTypes.put(Float.class.getName(), Integer.valueOf(XDR_REAL64));
		mValueTypes.put(Double.class.getName(), Integer.valueOf(XDR_REAL64));
	}

	/**
	 * Initializes an ApMon object from a configuration file.
	 * 
	 * @param filename
	 *            The name of the file which contains the addresses and the ports of the destination hosts (see README
	 *            for details about the structure of this file).
	 * @throws ApMonException
	 *             ,
	 *             SocketException, IOException
	 * @throws SocketException
	 * @throws IOException
	 */
	public ApMon(String filename) throws ApMonException, SocketException, IOException {

		initType = FILE_INIT;
		initMonitoring();
		initSource = filename;
		initialize(filename, true);
	}

	/**
	 * Add a job pid to monitored jobs vector
	 * 
	 * @param pid
	 * @param workDir
	 * @param clustername
	 * @param nodename
	 */
	public void addJobToMonitor(final int pid, final String workDir, final String clustername, final String nodename) {
		@SuppressWarnings("resource")
		final MonitoredJob job = new MonitoredJob(pid, workDir, clustername, nodename, numCPUs);

		if (!monJobs.contains(job)) {
			monJobs.add(job);
		}
		else {
			if (logger.isLoggable(Level.WARNING))
				logger.warning("Job <" + job + "> already exists.");

	/**
	 * Add a MonitoredJob instance to monitorized jobs vector
	 *
	 * @param monJob
	 */
	public MonitoredJob addJobInstanceToMonitor(MonitoredJob job) {
		if (!monJobs.contains(job)) {
			monJobs.add(job);
		}
		else
			if (logger.isLoggable(Level.WARNING))
				logger.warning("Job <" + job + "> already exsist.");
		return job;
	}

	/**
	 * Remove a pid from monitored jobs vector
	 * 
	 * @param pid
	 */
	public void removeJobToMonitor(final int pid) {
		final Iterator<MonitoredJob> it = monJobs.iterator();

		while (it.hasNext()) {
			@SuppressWarnings("resource")
			final MonitoredJob job = it.next();
			if (job.getPid() == pid) {
				it.remove();
				job.close();
				break;
			}
		}
	}

	/**
	 * @param job
	 * @return <code>true</code> if the job was found and removed
	 */
	public boolean removeJobToMonitor(final MonitoredJob job) {
		if (job != null) {
			final boolean result = monJobs.remove(job);
			job.close();

			return result;
		}

		return false;
	}

	/**
	 * This is used to set the cluster and node name for the system-related monitored data.
	 * 
	 * @param cName
	 * @param nName
	 */
	public void setMonitorClusterNode(String cName, String nName) {
		if (cName != null)
			sysClusterName = cName;
		if (nName != null)
			sysNodeName = nName;
	}

	/**
	 * Initializes an ApMon object from a configuration file.
	 * 
	 * @param filename
	 *            The name of the file which contains the addresses and the ports of the destination hosts (see README
	 *            for details about the structure of this file).
	 * @param firstTime
	 *            If it is true, all the initializations will be done (the object is being constructed now). Else, only
	 *            some structures will be reinitialized.
	 * @throws ApMonException
	 *             ,
	 *             SocketException, IOException
	 * @throws SocketException
	 * @throws IOException
	 */
	void initialize(String filename, boolean firstTime) throws ApMonException, SocketException, IOException {
		Vector<String> vdestAddresses = new Vector<>();
		Vector<Integer> vdestPorts = new Vector<>();
		Vector<String> vdestPasswds = new Vector<>();

		Hashtable<Object, Long> confRes = new Hashtable<>();
		try {
			loadFile(filename, vdestAddresses, vdestPorts, vdestPasswds, confRes);
		}
		catch (Exception e) {
			if (firstTime) {
				if (e instanceof IOException)
					throw (IOException) e;
				if (e instanceof ApMonException)
					throw (ApMonException) e;
			}
			else {
				logger.warning("Configuration not reloaded successfully, keeping the previous one");
				return;
			}
		}

		synchronized (this) {
			arrayInit(vdestAddresses, vdestPorts, vdestPasswds, firstTime);
			this.confResources = confRes;
		}
	}

	/**
	 * Initializes an ApMon object from a list with URLs.
	 * 
	 * @param destList
	 *            The list with URLs. the ports of the destination hosts (see README for details about the structure of
	 *            this file).
	 * @throws ApMonException
	 *             ,
	 *             SocketException, IOException
	 * @throws SocketException
	 * @throws IOException
	 */
	public ApMon(Vector<String> destList) throws ApMonException, SocketException, IOException {
		initType = LIST_INIT;
		initMonitoring();
		initSource = destList;
		initialize(destList, true);
	}

	/**
	 * Initializes an ApMon object from a list with URLs.
	 * 
	 * @param destList
	 *            The list with URLs.
	 * @param firstTime
	 *            If it is true, all the initializations will be done (the object is being constructed now). Else, only
	 *            some structures will be reinitialized.
	 * @throws ApMonException
	 *             ,
	 *             SocketException, IOException
	 * @throws SocketException
	 * @throws IOException
	 */
	void initialize(Vector<String> destList, boolean firstTime) throws ApMonException, SocketException, IOException {
		int i;
		Vector<String> vdestAddresses = new Vector<>();
		Vector<Integer> vdestPorts = new Vector<>();
		Vector<String> vdestPasswds = new Vector<>();
		String dest;
		Hashtable<Object, Long> confRes = new Hashtable<>();

		logger.info("Initializing destination addresses & ports:");
		try {
			for (i = 0; i < destList.size(); i++) {
				dest = destList.get(i);
				if (dest.startsWith("http")) { // get the list from a remote location
					loadURL(dest, vdestAddresses, vdestPorts, vdestPasswds, confRes);
				}
				else { // the destination address & port are given directly
					addToDestinations(dest, vdestAddresses, vdestPorts, vdestPasswds);
				}
			}
		}
		catch (Exception e) {
			if (firstTime) {
				if (e instanceof IOException)
					throw (IOException) e;
				if (e instanceof ApMonException)
					throw (ApMonException) e;
				if (e instanceof SocketException)
					throw (SocketException) e;
			}
			else {
				logger.warning("Configuration not reloaded successfully, keeping the previous one");
				return;
			}
		}

		synchronized (this) {
			arrayInit(vdestAddresses, vdestPorts, vdestPasswds, firstTime);
			this.confResources = confRes;
		}
	}

	/**
	 * Initializes an ApMon data structure, using arrays instead of a file.
	 * 
	 * @param destAddresses
	 *            Array that contains the hostnames or IP addresses of the destination hosts.
	 * @param destPorts
	 *            The ports where the MonaLisa modules listen on the destination hosts.
	 * @throws ApMonException
	 *             ,
	 *             SocketException, IOException
	 * @throws SocketException
	 * @throws IOException
	 */
	public ApMon(Vector<String> destAddresses, Vector<Integer> destPorts) throws ApMonException, SocketException, IOException {
		this.initType = DIRECT_INIT;
		arrayInit(destAddresses, destPorts, null);
	}

	/**
	 * Initializes an ApMon data structure, using arrays instead of a file.
	 * 
	 * @param destAddresses
	 *            Array that contains the hostnames or IP addresses of the destination hosts.
	 * @param destPorts
	 *            The ports where the MonaLisa modules listen on the destination hosts.
	 * @param destPasswds
	 *            The passwords for the destination hosts.
	 * @throws ApMonException
	 *             ,
	 *             SocketException, IOException
	 * @throws SocketException
	 * @throws IOException
	 */
	public ApMon(Vector<String> destAddresses, Vector<Integer> destPorts, Vector<String> destPasswds) throws ApMonException, SocketException, IOException {
		this.initType = DIRECT_INIT;
		initMonitoring();
		arrayInit(destAddresses, destPorts, destPasswds);
	}

	/**
	 * Sets the number of CPU Cores that the job will make use of.
	 * 
	 * @param numCPUs
	 *            The number of CPU Cores to be used.
	 */
	public void setNumCPUs(int numCPUs) {
		this.numCPUs = numCPUs;
	}

	/**
	 * Parses a configuration file which contains addresses, ports and passwords for the destination hosts and puts the
	 * results in the vectors given as parameters.
	 * 
	 * @param filename
	 *            The name of the configuration file.
	 * @param destaddresses
	 *            Will contain the destination addresses.
	 * @param destports
	 *            Will contain the ports from the destination hosts.
	 * @param destpasswds
	 *            Will contain the passwords for the destination hosts.
	 * @param confRes
	 *            Will contain the configuration resources (file, URLs).
	 * @throws IOException
	 *             ,
	 *             ApMonException
	 * @throws ApMonException
	 */
	private void loadFile(String filename, Vector<String> destaddresses, Vector<Integer> destports, Vector<String> destpasswds, Hashtable<Object, Long> confRes) throws IOException, ApMonException {
		String line, tmp;
		try (BufferedReader in = new BufferedReader(new FileReader(filename))) {
			confRes.put(new File(filename), Long.valueOf(System.currentTimeMillis()));

			/** initializations for the destination addresses */
			logger.info("Loading file " + filename + "...");

			/** parse the input file */
			while ((line = in.readLine()) != null) {
				tmp = line.trim();
				// skip empty lines & comment lines
				if (tmp.length() == 0 || tmp.startsWith("#"))
					continue;
				if (tmp.startsWith("xApMon_loglevel")) {
					StringTokenizer lst = new StringTokenizer(tmp, " =");
					lst.nextToken();
					setLogLevel(lst.nextToken());
					continue;
				}
				if (tmp.startsWith("xApMon_")) {
					parseXApMonLine(tmp);
					continue;
				}

				addToDestinations(tmp, destaddresses, destports, destpasswds);
			}
		}
	}

	/**
	 * Parses a web page which contains addresses, ports and passwords for the destination hosts and puts the results in
	 * the vectors given as parameters.
	 * 
	 * @param url
	 * 
	 * @param destaddresses
	 *            Will contain the destination addresses.
	 * @param destports
	 *            Will contain the ports from the destination hosts.
	 * @param destpasswds
	 *            Will contain the passwords for the destination hosts.
	 * @param confRes
	 *            Will contain the configuration resources (file, URLs).
	 * @throws IOException
	 * @throws ApMonException
	 */
	private void loadURL(String url, Vector<String> destaddresses, Vector<Integer> destports, Vector<String> destpasswds, Hashtable<Object, Long> confRes) throws IOException, ApMonException {

		System.setProperty("sun.net.client.defaultConnectTimeout", "5000");
		System.setProperty("sun.net.client.defaultReadTimeout", "5000");
		URL destURL = null;
		try {
			destURL = new URL(url);
		}
		catch (MalformedURLException e) {
			throw new ApMonException(e.getMessage());
		}

		URLConnection urlConn = destURL.openConnection();
		long lmt = urlConn.getLastModified();
		confRes.put(new URL(url), Long.valueOf(lmt));

		logger.info("Loading from URL " + url + "...");

		try (BufferedReader br = new BufferedReader(new InputStreamReader(destURL.openStream()))) {
			String destLine;
			while ((destLine = br.readLine()) != null) {
				String tmp2 = destLine.trim();
				// skip empty lines & comment lines
				if (tmp2.length() == 0 || tmp2.startsWith("#"))
					continue;
				if (tmp2.startsWith("xApMon_loglevel")) {
					StringTokenizer lst = new StringTokenizer(tmp2, " =");
					lst.nextToken();
					setLogLevel(lst.nextToken());
					continue;
				}
				if (tmp2.startsWith("xApMon_")) {
					parseXApMonLine(tmp2);
					continue;

				}
				addToDestinations(tmp2, destaddresses, destports, destpasswds);
			}
		}
	}

	/**
	 * Parses a line from a (local or remote) configuration file and adds the address and the port to the vectors that
	 * are given as parameters.
	 * 
	 * @param lineParam
	 *            The line to be parsed.
	 * @param destaddresses
	 *            Contains destination addresses.
	 * @param destports
	 *            Contains the ports from the destination hosts.
	 * @param destpasswds
	 *            Contains the passwords for the destination hosts.
	 */
	private static void addToDestinations(final String lineParam, Vector<String> destaddresses, Vector<Integer> destports, Vector<String> destpasswds) {
		String line = lineParam;

		String addr;
		int port = DEFAULT_PORT;
		String tokens[] = line.split("(\\s)+");
		String passwd = "";

		if (tokens.length == 0)
			return; // skip blank lines

		line = tokens[0].trim();
		if (tokens.length > 1)// a password was provided
			passwd = tokens[1].trim();

		/** the address and the port are separated with ":" */
		StringTokenizer st = new StringTokenizer(line, ":");
		addr = st.nextToken();
		try {
			if (st.hasMoreTokens())
				port = Integer.parseInt(st.nextToken());
			else
				port = DEFAULT_PORT;
		}
		catch (@SuppressWarnings("unused") NumberFormatException e) {
			logger.warning("Wrong address: " + line);
		}

		destaddresses.add(addr);
		destports.add(Integer.valueOf(port));
		if (passwd != null)
			destpasswds.add(passwd);
	}

	/**
	 * Internal method used to initialize an ApMon data structure.
	 * 
	 * @param addresses
	 *            Array that contains the hostnames or IP addresses of the destination hosts.
	 * @param ports
	 *            The ports where the MonaLisa modules listen on the destination hosts.
	 * @param passwds
	 *            The passwords for the destination hosts.
	 * @throws ApMonException
	 *             ,
	 *             SocketException, IOException
	 * @throws SocketException
	 * @throws IOException
	 */
	private void arrayInit(Vector<String> addresses, Vector<Integer> ports, Vector<String> passwds) throws ApMonException, SocketException, IOException {
		arrayInit(addresses, ports, passwds, true);
	}

	/**
	 * Internal method used to initialize an ApMon data structure.
	 * 
	 * @param adresses
	 *            Array that contains the hostnames or IP addresses of the destination hosts.
	 * @param ports
	 *            The ports where the MonaLisa modules listen on the destination hosts.
	 * @param passwds
	 *            The passwords for the destination hosts.
	 * @param firstTime
	 *            If it is true, all the initializations will be done (the object is being constructed now). Else, only
	 *            some of the data structures will be reinitialized.
	 * @throws ApMonException
	 *             ,
	 *             SocketException, IOException
	 * @throws SocketException
	 * @throws IOException
	 */
	private void arrayInit(Vector<String> adresses, Vector<Integer> ports, Vector<String> passwds, boolean firstTime) throws ApMonException, SocketException, IOException {

		Vector<String> tmpAddresses;
		Vector<Integer> tmpPorts;
		Vector<String> tmpPasswds;

		if (adresses.size() == 0 || ports.size() == 0)
			throw new ApMonException("No destination hosts specified");

		tmpAddresses = new Vector<>();
		tmpPorts = new Vector<>();
		tmpPasswds = new Vector<>();

		/**
		 * put the destination addresses, ports & passwords in some temporary buffers (because we don't want to keep the
		 * monitor while making DNS requests)
		 */

		for (int i = 0; i < adresses.size(); i++) {
			InetAddress inetAddr = InetAddress.getByName(adresses.get(i));
			String ipAddr = inetAddr.getHostAddress();

			/**
			 * add the new destination only if it doesn't already exist in this.destAddresses
			 */
			if (!tmpAddresses.contains(ipAddr)) {
				tmpAddresses.add(ipAddr);
				tmpPorts.add(ports.get(i));
				if (passwds != null) {
					tmpPasswds.add(passwds.get(i));
				}
				logger.info("adding destination: " + ipAddr + ":" + ports.get(i));
			}
		}

		synchronized (this) {
			this.destPorts = new Vector<>(tmpPorts);
			this.destAddresses = new Vector<>(tmpAddresses);
			this.destPasswds = new Vector<>(tmpPasswds);
		}

		/** these should be done only the first time the function is called */
		if (firstTime) {
			try (cmdExec exec = new cmdExec()) {
				myHostname = exec.executeCommand("hostname -f", "");
			}

			sysNodeName = myHostname;

			dgramSocket = new DatagramSocket();
			lastJobInfoSend = System.currentTimeMillis();

			try {
				lastSysInfoSend = BkThread.getBootTime();
			}
			catch (Exception e) {
				logger.warning("Error reading boot time from /proc/stat/: " + e.getMessage());
				lastSysInfoSend = 0;
			}

			lastUtime = lastStime = 0;

			BkThread.getNetConfig(netInterfaces, allMyIPs);
			if (allMyIPs.size() > 0)
				this.myIP = allMyIPs.get(0);
			else
				this.myIP = "unknown";

			try {
				baos = new ByteArrayOutputStream();
			}
			catch (Throwable t) {
				logger.log(Level.WARNING, "", t);
				throw new ApMonException("Got General Exception while encoding:" + t);
			}
		}
		/**
		 * start job/system monitoring according to the settings previously read from the configuration file
		 */
		setJobMonitoring(jobMonitoring, jobMonitorInterval);
		setSysMonitoring(sysMonitoring, sysMonitorInterval);
		setGenMonitoring(genMonitoring, genMonitorIntervals);
		setConfRecheck(confCheck, recheckInterval);
	}

	/**
	 * For backward compatibility.
	 * 
	 * @param clustername
	 * @param nodename
	 * @param nParams
	 * @param paramNames
	 * @param valueTypes
	 * @param paramValues
	 * @param timestamp
	 * @throws ApMonException
	 * @throws UnknownHostException
	 * @throws SocketException
	 * @throws IOException
	 */
	public void sendTimedParameters(String clustername, String nodename, int nParams, Vector<String> paramNames, @SuppressWarnings("unused") Vector<?> valueTypes, Vector<Object> paramValues,
			int timestamp) throws ApMonException, UnknownHostException, SocketException, IOException {
		sendTimedParameters(clustername, nodename, nParams, paramNames, paramValues, timestamp);
	}

	/**
	 * Sends a set of parameters and their values to the MonALISA module.
	 * 
	 * @param clustername
	 *            The name of the cluster that is monitored.
	 * @param nodename
	 *            The name of the node from the cluster from which the value was taken.
	 * @param nParams
	 * @param paramNames
	 *            Vector with the names of the parameters.
	 * @param paramValues
	 *            Vector with the values of the parameters.
	 * @param timestamp
	 * @throws ApMonException
	 *             ,
	 *             UnknownHostException, SocketException
	 * @throws UnknownHostException
	 * @throws SocketException
	 * @throws IOException
	 *             partial_start
	 */
	public synchronized void sendTimedParameters(String clustername, String nodename, int nParams, Vector<String> paramNames, Vector<Object> paramValues, int timestamp)
			throws ApMonException, UnknownHostException, SocketException, IOException {

		int i;

		if (!shouldSend())
			return;

		if (clustername != null) { // don't keep the cached values for cluster name
			// and node name
			this.clusterName = clustername;

			if (nodename != null)
				/** the user provided a name */
				this.nodeName = nodename;
			else {
				/** set the node name to the node's IP */
				this.nodeName = this.myHostname;
			} // else
		} // if

		final byte[] referenceOS = baos.toByteArray();

		int start = 0;

		do {
			if (start > 0) {
				baos.reset();
				baos.write(referenceOS);
			}

			/** try to encode the parameters */
			final int up_to = encodeParams(start, nParams, paramNames, paramValues, timestamp);

			if (up_to < nParams) {
				if (up_to <= start)
					throw new ApMonException("Cannot encode the given parameter set");

				baos.reset();
				baos.write(referenceOS);

				encodeParams(start, up_to, paramNames, paramValues, timestamp);
			}

			start = up_to;

			/** for each destination */
			for (i = 0; i < destAddresses.size(); i++) {
				InetAddress destAddr = InetAddress.getByName(destAddresses.get(i));
				int port = destPorts.get(i).intValue();

				String header = "v:" + APMON_VERSION + "_jp:";
				String passwd = "";
				if (destPasswds != null && destPasswds.size() == destAddresses.size()) {
					passwd = destPasswds.get(i);
				}
				header += passwd;

				byte[] newBuff = null;
				try {
					XDROutputStream xdrOS = new XDROutputStream(baos);

					xdrOS.writeString(header);
					xdrOS.pad();
					xdrOS.writeInt(INSTANCE_ID);
					xdrOS.pad();
					xdrOS.writeInt(SEQ_NR.incrementAndGet());
					xdrOS.pad();

					xdrOS.flush();
					byte[] tmpbuf = baos.toByteArray();
					baos.reset();

					newBuff = new byte[tmpbuf.length + buf.length];
					System.arraycopy(tmpbuf, 0, newBuff, 0, tmpbuf.length);
					System.arraycopy(buf, 0, newBuff, tmpbuf.length, buf.length);

				}
				catch (Throwable t) {
					logger.warning("Cannot add ApMon header...." + t);
					newBuff = buf;
				}

				if (newBuff == null || newBuff.length == 0) {
					logger.warning("Cannot send null or 0 length buffer!!");
					continue;
				}

				dgramSize = newBuff.length;
				final DatagramPacket dp = new DatagramPacket(newBuff, dgramSize, destAddr, port);

				try {
					dgramSocket.send(dp);
				}
				catch (final IOException e) {
					if (logger.isLoggable(Level.WARNING))
						logger.log(Level.WARNING, "Error sending parameters to " + destAddresses.get(i), e);

					dgramSocket.close();
					dgramSocket = new DatagramSocket();
				}

				if (logger.isLoggable(Level.FINE)) {
					final StringBuilder sbLogMsg = new StringBuilder();
					sbLogMsg.append(" Datagram with size ").append(dgramSize);
					sbLogMsg.append(" sent to ").append(destAddresses.get(i)).append(", cluster = ").append(this.clusterName).append(", node = ").append(this.nodeName)
							.append(", containing parameters:\n");
					sbLogMsg.append(printParameters(paramNames, paramValues));
					logger.log(Level.FINE, sbLogMsg.toString());
				}
			}
		} while (start < nParams);
	}

	/**
	 * For backward compatibility.
	 * 
	 * @param clustername
	 * @param nodename
	 * @param nParams
	 * @param paramNames
	 * @param valueTypes
	 * @param paramValues
	 * @throws ApMonException
	 * @throws UnknownHostException
	 * @throws SocketException
	 * @throws IOException
	 */
	public void sendParameters(String clustername, String nodename, int nParams, Vector<String> paramNames, @SuppressWarnings("unused") Vector<?> valueTypes, Vector<Object> paramValues)
			throws ApMonException, UnknownHostException, SocketException, IOException {
		sendParameters(clustername, nodename, nParams, paramNames, paramValues);
	}

	/**
	 * Sends a set of parameters and thier values to the MonALISA module.
	 * 
	 * @param clustername
	 *            The name of the cluster that is monitored.
	 * @param nodename
	 *            The name of the node from the cluster from which the value was taken.
	 * @param params
	 * @param paramNames
	 *            Vector with the names of the parameters.
	 * @param paramValues
	 *            Vector with the values of the parameters.
	 * @throws ApMonException
	 *             ,
	 *             UnknownHostException, SocketException
	 * @throws UnknownHostException
	 * @throws SocketException
	 * @throws IOException
	 */
	public void sendParameters(String clustername, String nodename, int params, Vector<String> paramNames, Vector<Object> paramValues)
			throws ApMonException, UnknownHostException, SocketException, IOException {
		sendTimedParameters(clustername, nodename, params, paramNames, paramValues, -1);
	}

	/**
	 * For backward compatibility.
	 * 
	 * @param clustername
	 * @param nodename
	 * @param paramName
	 * @param valueType
	 * @param paramValue
	 * @throws ApMonException
	 * @throws UnknownHostException
	 * @throws SocketException
	 * @throws IOException
	 */
	public void sendParameter(String clustername, String nodename, String paramName, @SuppressWarnings("unused") int valueType, Object paramValue)
			throws ApMonException, UnknownHostException, SocketException, IOException {
		sendParameter(clustername, nodename, paramName, paramValue);
	}

	/**
	 * Sends a parameter and its value to the MonALISA module.
	 * 
	 * @param clustername
	 *            The name of the cluster that is monitored. If it is NULL, we keep the same cluster and node name as in
	 *            the previous datagram.
	 * @param nodename
	 *            The name of the node from the cluster from which the value was taken.
	 * @param paramName
	 *            The name of the parameter.
	 * @param paramValue
	 *            The value of the parameter.
	 * @throws ApMonException
	 *             ,
	 *             UnknownHostException, SocketException
	 * @throws UnknownHostException
	 * @throws SocketException
	 * @throws IOException
	 */
	public void sendParameter(String clustername, String nodename, String paramName, Object paramValue) throws ApMonException, UnknownHostException, SocketException, IOException {
		Vector<String> paramNames = new Vector<>(1);
		paramNames.add(paramName);
		Vector<Object> paramValues = new Vector<>(1);
		paramValues.add(paramValue);

		sendParameters(clustername, nodename, 1, paramNames, paramValues);
	}

	/**
	 * For backward compatibility.
	 * 
	 * @param clustername
	 * @param nodename
	 * @param paramName
	 * @param valueType
	 * @param paramValue
	 * @param timestamp
	 * @throws ApMonException
	 * @throws UnknownHostException
	 * @throws SocketException
	 * @throws IOException
	 */
	public void sendTimedParameter(String clustername, String nodename, String paramName, @SuppressWarnings("unused") int valueType, Object paramValue, int timestamp)
			throws ApMonException, UnknownHostException, SocketException, IOException {
		sendTimedParameter(clustername, nodename, paramName, paramValue, timestamp);
	}

	/**
	 * Sends a parameter and its value to the MonALISA module.
	 * 
	 * @param clustername
	 *            The name of the cluster that is monitored. If it is NULL, we keep the same cluster and node name as in
	 *            the previous datagram.
	 * @param nodename
	 *            The name of the node from the cluster from which the value was taken.
	 * @param paramName
	 *            The name of the parameter.
	 * @param paramValue
	 *            The value of the parameter.
	 * @param timestamp
	 * @throws ApMonException
	 *             ,
	 *             UnknownHostException, SocketException
	 * @throws UnknownHostException
	 * @throws SocketException
	 * @throws IOException
	 */
	public void sendTimedParameter(String clustername, String nodename, String paramName, Object paramValue, int timestamp) throws ApMonException, UnknownHostException, SocketException, IOException {
		Vector<String> paramNames = new Vector<>(1);
		paramNames.add(paramName);
		Vector<Object> paramValues = new Vector<>(1);
		paramValues.add(paramValue);

		sendTimedParameters(clustername, nodename, 1, paramNames, paramValues, timestamp);
	}

	/**
	 * Sends an integer parameter and its value to the MonALISA module.
	 * 
	 * @param clustername
	 *            The name of the cluster that is monitored. If it is NULL, we keep the same cluster and node name as in
	 *            the previous datagram.
	 * @param nodename
	 *            The name of the node from the cluster from which the value was taken.
	 * @param paramName
	 *            The name of the parameter.
	 * @param paramValue
	 *            The value of the parameter.
	 * @throws ApMonException
	 *             ,
	 *             UnknownHostException, SocketException
	 * @throws UnknownHostException
	 * @throws SocketException
	 * @throws IOException
	 */
	public void sendParameter(String clustername, String nodename, String paramName, int paramValue) throws ApMonException, UnknownHostException, SocketException, IOException {
		sendParameter(clustername, nodename, paramName, Integer.valueOf(paramValue));
	}

	/**
	 * Sends an integer parameter and its value to the MonALISA module.
	 * 
	 * @param clustername
	 *            The name of the cluster that is monitored. If it is NULL, we keep the same cluster and node name as in
	 *            the previous datagram.
	 * @param nodename
	 *            The name of the node from the cluster from which the value was taken.
	 * @param paramName
	 *            The name of the parameter.
	 * @param paramValue
	 *            The value of the parameter.
	 * @param timestamp
	 *            The user's timestamp
	 * @throws ApMonException
	 *             ,
	 *             UnknownHostException, SocketException
	 * @throws UnknownHostException
	 * @throws SocketException
	 * @throws IOException
	 */
	public void sendTimedParameter(String clustername, String nodename, String paramName, int paramValue, int timestamp) throws ApMonException, UnknownHostException, SocketException, IOException {
		sendTimedParameter(clustername, nodename, paramName, Integer.valueOf(paramValue), timestamp);
	}

	/**
	 * Sends a parameter of type double and its value to the MonALISA module.
	 * 
	 * @param clustername
	 *            The name of the cluster that is monitored. If it is NULL,we keep the same cluster and node name as in
	 *            the previous datagram.
	 * @param nodename
	 *            The name of the node from the cluster from which the value was taken.
	 * @param paramName
	 *            The name of the parameter.
	 * @param paramValue
	 *            The value of the parameter.
	 * @throws ApMonException
	 *             ,
	 *             UnknownHostException, SocketException
	 * @throws UnknownHostException
	 * @throws SocketException
	 * @throws IOException
	 */
	public void sendParameter(String clustername, String nodename, String paramName, double paramValue) throws ApMonException, UnknownHostException, SocketException, IOException {
		sendParameter(clustername, nodename, paramName, Double.valueOf(paramValue));
	}

	/**
	 * Sends an integer parameter and its value to the MonALISA module.
	 * 
	 * @param clustername
	 *            The name of the cluster that is monitored. If it is NULL, we keep the same cluster and node name as in
	 *            the previous datagram.
	 * @param nodename
	 *            The name of the node from the cluster from which the value was taken.
	 * @param paramName
	 *            The name of the parameter.
	 * @param paramValue
	 *            The value of the parameter.
	 * @param timestamp
	 *            The user's timestamp
	 * @throws ApMonException
	 *             ,
	 *             UnknownHostException, SocketException
	 * @throws UnknownHostException
	 * @throws SocketException
	 * @throws IOException
	 */
	public void sendTimedParameter(String clustername, String nodename, String paramName, double paramValue, int timestamp) throws ApMonException, UnknownHostException, SocketException, IOException {

		sendTimedParameter(clustername, nodename, paramName, Double.valueOf(paramValue), timestamp);
	}

	/**
	 * Checks that the size of the stream does not exceed the maximum size of an UDP diagram.
	 * 
	 * @param os
	 * @throws ApMonException
	 */
	private static void ensureSize(final ByteArrayOutputStream os) throws ApMonException {
		if (os == null)
			throw new ApMonException("Null ByteArrayOutputStream");
		if (os.size() > MAX_DGRAM_SIZE)
			throw new ApMonException("Maximum datagram size exceeded");
	}

	/**
	 * Encodes in the XDR format the data from a ApMon structure. Must be called before sending the data over the
	 * network.
	 * 
	 * @param nParams
	 * @param paramNames
	 * @param paramValues
	 * @param timestamp
	 * 
	 * @throws ApMonException
	 */
	private int encodeParams(final int start, final int nParams, final Vector<String> paramNames, final Vector<Object> paramValues, final int timestamp) throws ApMonException {
		int i, valType;
		try {
			/** encode the cluster name, the node name and the number of parameters */
			ensureSize(baos);

			XDROutputStream xdrOS = new XDROutputStream(baos);
			xdrOS.writeString(clusterName);
			xdrOS.pad();
			xdrOS.writeString(nodeName);
			xdrOS.pad();
			xdrOS.writeInt(nParams - start);
			xdrOS.pad();

			Object oValue;
			/** encode the parameters */
			for (i = start; i < nParams; i++) {
				oValue = paramValues.get(i);

				if (oValue == null) {
					logger.log(Level.SEVERE, "Null value at index " + i + " for parameter name " + paramNames.get(i));
					continue;
				}

				final Integer type = mValueTypes.get(oValue.getClass().getName());

				if (type == null) {
					logger.log(Level.SEVERE, "Don't know how to encode parameter of type " + oValue.getClass().getCanonicalName() + " at index " + i + " (parameter name " + paramNames.get(i) + ")");
					continue;
				}

				/** parameter name */
				xdrOS.writeString(paramNames.get(i));
				xdrOS.pad();
				/** parameter value type */

				valType = type.intValue();
				xdrOS.writeInt(valType);
				xdrOS.pad();
				/** parameter value */
				switch (valType) {
					case XDR_STRING:
						xdrOS.writeString((String) paramValues.get(i));
						break;
					case XDR_INT32:// INT16 is not supported
						int ival = ((Number) paramValues.get(i)).intValue();
						xdrOS.writeInt(ival);
						break;
					case XDR_REAL64: // REAL32 is not supported
						double dval = ((Number) paramValues.get(i)).doubleValue();
						xdrOS.writeDouble(dval);
						break;
					default:
						throw new ApMonException("Unknown type for XDR encoding");
				}
				xdrOS.pad();

				if (baos.size() > MAX_DGRAM_SIZE)
					return i - 1;

			} // end for()

			/** put the timestamp at the and of XDROutputStream */
			if (timestamp > 0) {
				xdrOS.writeInt(timestamp);
				xdrOS.pad();
			}

			xdrOS.flush();

			if (baos.size() > MAX_DGRAM_SIZE)
				return nParams - 1;

			buf = baos.toByteArray();
			logger.fine("Send buffer length: " + buf.length + "B");

			return nParams;
		}
		catch (final Throwable t) {
			logger.log(Level.WARNING, "", t);

			final ApMonException ex = new ApMonException("Got General Exception while encoding");
			ex.initCause(t);

			throw ex;
		}
		finally {
			baos.reset();
		}
	}

	/**
	 * Returns the value of the confCheck flag. If it is true, the configuration file and/or the URLs are periodically
	 * checked for modifications.
	 * 
	 * @return true if enabled
	 */
	public boolean getConfCheck() {
		boolean val;
		synchronized (mutexBack) {
			val = this.confCheck;
		}
		return val;
	}

	/**
	 * Settings for the periodical configuration rechecking feature.
	 * 
	 * @param confCheck
	 *            If it is true, the configuration rechecking is enabled.
	 * @param interval
	 *            The time interval at which the verifications are done. The interval will be automatically increased if
	 *            ApMon cannot connect to the configuration URLs.
	 */
	public void setConfRecheck(boolean confCheck, long interval) {
		int val = -1;
		if (confCheck)
			logger.info("Enabling configuration reloading (interval " + interval + " s)");

		synchronized (mutexBack) {
			if (initType == DIRECT_INIT) { // no need to reload onfiguration
				logger.warning("setConfRecheck(): no configuration file/URL to reload\n");
			}
			else {
				this.confCheck = confCheck;
				if (confCheck) {
					if (interval > 0) {
						this.recheckInterval = interval;
						this.crtRecheckInterval = interval;
					}
					else {
						this.recheckInterval = RECHECK_INTERVAL;
						this.crtRecheckInterval = RECHECK_INTERVAL;
					}
					val = 1;
				}
				else {
					if (jobMonitoring == false && sysMonitoring == false)
						val = 0;
				}
			}
		} // synchronized

		if (val == 1) {
			setBackgroundThread(true);
			return;
		}
		if (val == 0) {
			setBackgroundThread(false);
			return;
		}
	}

	/**
	 * Returns the requested value of the time interval (in seconds) between two recheck operations for the
	 * configuration files.
	 * 
	 * @return interval
	 */
	public long getRecheckInterval() {
		long val;
		synchronized (mutexBack) {
			val = this.recheckInterval;
		}
		return val;
	}

	/**
	 * Returns the actual value of the time interval (in seconds) between two recheck operations for the configuration
	 * file/URLs.
	 * 
	 * @return recheck interval
	 */
	long getCrtRecheckInterval() {
		long val;
		synchronized (mutexBack) {
			val = this.crtRecheckInterval;
		}
		return val;
	}

	/**
	 * Sets the value of the time interval (in seconds) between two recheck operations for the configuration file/URLs.
	 * If the value is negative, the configuration rechecking is
	 * turned off.
	 * 
	 * @param val
	 */
	public void setRecheckInterval(long val) {
		if (val > 0)
			setConfRecheck(true, val);
		else
			setConfRecheck(false, val);
	}

	/**
	 * @param val
	 */
	void setCrtRecheckInterval(long val) {
		synchronized (mutexBack) {
			crtRecheckInterval = val;
		}
	}

	/**
	 * Settings for the job monitoring feature.
	 * 
	 * @param jobMonitoring
	 *            If it is true, the job monitoring is enabled.
	 * @param interval
	 *            The time interval at which the job monitoring datagrams are sent.
	 */
	public void setJobMonitoring(boolean jobMonitoring, long interval) {
		int val = -1;
		if (jobMonitoring)
			logger.info("Enabling job monitoring, time interval " + interval + " s");
		else
			logger.info("Disabling job monitoring...");

		synchronized (mutexBack) {
			this.jobMonitoring = jobMonitoring;
			this.jobMonChanged = true;
			if (jobMonitoring == true) {
				if (interval > 0)
					this.jobMonitorInterval = interval;
				else
					this.jobMonitorInterval = JOB_MONITOR_INTERVAL;
				val = 1;
			}
			else {
				// disable the background thread if it is not needed anymore
				if (this.sysMonitoring == false && this.confCheck == false)
					val = 0;
			}
		}
		if (val == 1) {
			setBackgroundThread(true);
			return;
		}
		if (val == 0) {
			setBackgroundThread(false);
			return;
		}
	}

	/**
	 * Returns the value of the interval at which the job monitoring datagrams are sent.
	 * 
	 * @return interval
	 */
	public long getJobMonitorInterval() {
		long val;
		synchronized (mutexBack) {
			val = this.jobMonitorInterval;
		}
		return val;
	}

	/**
	 * Returns true if the job monitoring is enabled and false otherwise.
	 * 
	 * @return true if enabled
	 */
	public boolean getJobMonitoring() {
		boolean val;
		synchronized (mutexBack) {
			val = this.jobMonitoring;
		}
		return val;
	}

	/**
	 * Settings for the system monitoring feature.
	 * 
	 * @param sysMonitoring
	 *            If it is true, the system monitoring is enabled.
	 * @param interval
	 *            The time interval at which the system monitoring datagrams are sent.
	 */
	public void setSysMonitoring(boolean sysMonitoring, long interval) {
		int val = -1;
		if (sysMonitoring)
			logger.info("Enabling system monitoring, time interval " + interval + " s");
		else
			logger.info("Disabling system monitoring...");

		synchronized (mutexBack) {
			this.sysMonitoring = sysMonitoring;
			this.sysMonChanged = true;
			if (sysMonitoring == true) {
				if (interval > 0)
					this.sysMonitorInterval = interval;
				else
					this.sysMonitorInterval = SYS_MONITOR_INTERVAL;
				val = 1;
			}
			else {
				// disable the background thread if it is not needed anymore
				if (this.jobMonitoring == false && this.confCheck == false)
					val = 0;
			}
		}

		if (val == 1) {
			setBackgroundThread(true);
			return;
		}

		if (val == 0) {
			setBackgroundThread(false);
			return;
		}
	}

	/**
	 * Returns the value of the interval at which the system monitoring datagrams are sent.
	 * 
	 * @return system monitoring interval
	 */
	public long getSysMonitorInterval() {
		long val;
		synchronized (mutexBack) {
			val = this.sysMonitorInterval;
		}

		return val;
	}

	/**
	 * Returns true if the job monitoring is enabled and false otherwise.
	 * 
	 * @return true if enabled
	 */
	public boolean getSysMonitoring() {
		boolean val;
		synchronized (mutexBack) {
			val = this.sysMonitoring;
		}
		return val;
	}

	/**
	 * Settings for the general system monitoring feature.
	 * 
	 * @param genMonitoring
	 *            If it is true, the general system monitoring is enabled.
	 * @param interval
	 *            The number of time intervals at which the general system monitoring datagrams are sent (a
	 *            "time interval" is the time interval between two subsequent system
	 *            monitoring datagrams).
	 */
	public void setGenMonitoring(boolean genMonitoring, int interval) {

		logger.info("Setting general information monitoring to " + genMonitoring);

		synchronized (mutexBack) {
			this.genMonitoring = genMonitoring;
			this.recheckChanged = true;
			if (genMonitoring == true) {
				if (interval > 0)
					this.genMonitorIntervals = interval;
				else
					this.genMonitorIntervals = GEN_MONITOR_INTERVALS;
			}
		}

		/** automatically set system monitoring to true if necessary */
		if (genMonitoring && this.sysMonitoring == false) {
			setSysMonitoring(true, SYS_MONITOR_INTERVAL);
		}
	}

	/**
	 * Returns true if the general system monitoring is enabled and false otherwise.
	 * 
	 * @return true if enabled
	 */
	public boolean getGenMonitoring() {
		boolean val;
		synchronized (mutexBack) {
			val = this.genMonitoring;
		}
		return val;
	}

	/**
	 * @param paramName
	 * @return the value
	 */
	public Double getSystemParameter(final String paramName) {
		if (bkThread == null) {
			logger.info("The background thread is not started - returning null");
			return null;
		}

		if (bkThread.monitor == null) {
			logger.info("No HostPropertiesMonitor defined - returning null");
			return null;
		}

		HashMap<Long, String> hms = HostPropertiesMonitor.getHashParams();
		if (hms == null) {
			logger.info("No parameters map defined - returning null");
			return null;
		}

		Long paramId = ApMonMonitoringConstants.HT_SYS_NAMES_TO_CONSTANTS.get("sys_" + paramName);

		if (paramId == null) {
			logger.info("The parameter " + paramName + " does not exist.");
			return null;
		}

		String paramValue = hms.get(paramId);
		double dVal = -1;

		try {
			dVal = Double.parseDouble(paramValue);
		}
		catch (Exception e) {
			logger.log(Level.INFO, "Could not obtain parameter value from the map: " + paramName, e);
			return null;
		}

		return Double.valueOf(dVal);
	}

	/**
	 * Enables or disables the background thread.
	 * 
	 * @param val
	 */
	void setBackgroundThread(final boolean val) {
		boolean stoppedThread = false;

		synchronized (mutexCond) {
			condChanged = false;
			if (val == true)
				if (!bkThreadStarted) {
					bkThreadStarted = true;
					bkThread = new BkThread(this);
					// bkThread.setDaemon(true);
					bkThread.start();
				}
				else {
					condChanged = true;
					mutexCond.notify();
				}

			if (val == false && bkThreadStarted) {
				bkThread.stopIt();
				condChanged = true;
				mutexCond.notify();
				stoppedThread = true;
				logger.info("[Stopping the thread for configuration reloading...]\n");
			}
		}
		if (stoppedThread) {
			try {
				// debugging stuff...
				/*
				 * System.out.println("### active count: " + bkThread.activeCount()); Thread[] tarray = new Thread[500];
				 * bkThread.enumerate(tarray); for (int tz = 0; tz <
				 * tarray.length; tz++) { if (tarray[tz] != null) System.out.println("### " + tarray[tz]); }
				 */
				bkThread.join();
			}
			catch (@SuppressWarnings("unused") Exception e) {
				// ignore
			}
			bkThreadStarted = false;
		}
	}

	/**
	 * Gets background threads.
	 *
	 * @return bkThread
	 */
	public BkThread getBackgroundThread() {
		if (bkThread == null)
			bkThread = new BkThread(this);
		return bkThread;
	}

	private static final String levels_s[] = { "FATAL", "WARNING", "INFO", "FINE", "FINER", "FINEST", "DEBUG", "ALL", "OFF", "CONFIG" };

	private static final Level levels[] = { Level.SEVERE, Level.WARNING, Level.INFO, Level.FINE, Level.FINER, Level.FINEST, Level.FINEST, Level.ALL, Level.OFF, Level.CONFIG };

	/**
	 * Sets the ApMon loglevel. The possible values are: "FATAL", "WARNING", "INFO", "FINE", "DEBUG".
	 * 
	 * @param newLevel_s
	 */
	public static void setLogLevel(final String newLevel_s) {
		for (int i = 0; i < levels_s.length; i++)
			if (newLevel_s.equals(levels_s[i])) {
				logger.info("Setting logging level to " + newLevel_s);
				logger.setLevel(levels[i]);
				return;
			}

		logger.warning("[ setLogLevel() ] Invalid level value: " + newLevel_s);
	}

	/**
	 * This sets the maxim number of messages that are send to MonALISA in one second. Default, this number is 50.
	 * 
	 * @param maxRate
	 */
	public void setMaxMsgRate(final int maxRate) {
		this.maxMsgRate = maxRate;
	}

	/**
	 * Must be called when the ApMon object is no longer in use. Closes the UDP socket used for sending the parameters
	 * and sends a last job monitoring datagram to register the time
	 * when the application was finished.
	 */
	public void stopIt() {

		if (bkThreadStarted) {
			if (jobMonitoring) {
				logger.info("Sending last job monitoring packet...");
				/**
				 * send a datagram with job monitoring information which covers the last time interval
				 */
				bkThread.sendJobInfo();
			}
		}
		dgramSocket.close();

		HostPropertiesMonitor.stopIt();
		setBackgroundThread(false);

		synchronized (monJobs) {
			final Iterator<MonitoredJob> it = monJobs.iterator();

			while (it.hasNext()) {
				@SuppressWarnings("resource")
				final MonitoredJob job = it.next();
				job.close();
				it.remove();
			}
		}

	}

	/** Initializes the data structures used to configure the monitoring part of ApMon. */
	void initMonitoring() {
		autoDisableMonitoring = true;
		sysMonitoring = false;
		jobMonitoring = false;
		genMonitoring = false;
		confCheck = false;

		recheckInterval = RECHECK_INTERVAL;
		crtRecheckInterval = RECHECK_INTERVAL;
		jobMonitorInterval = JOB_MONITOR_INTERVAL;
		sysMonitorInterval = SYS_MONITOR_INTERVAL;

		sysMonitorParams = 0L;

		/** CPU usage percent */
		sysMonitorParams |= ApMonMonitoringConstants.SYS_CPU_USAGE;
		/** average system load over the last minute */
		sysMonitorParams |= ApMonMonitoringConstants.SYS_LOAD1;
		/** average system load over the 5 min */
		sysMonitorParams |= ApMonMonitoringConstants.SYS_LOAD5;
		/** average system load over the 15 min */
		sysMonitorParams |= ApMonMonitoringConstants.SYS_LOAD15;
		/** percent of the time spent by the CPU in user mode */
		sysMonitorParams |= ApMonMonitoringConstants.SYS_CPU_USR;
		/** percent of the time spent by the CPU in system mode */
		sysMonitorParams |= ApMonMonitoringConstants.SYS_CPU_SYS;
		/** percent of the CPU idle time */
		sysMonitorParams |= ApMonMonitoringConstants.SYS_CPU_IDLE;
		/** percent of the time spent by the CPU in nice mode */
		sysMonitorParams |= ApMonMonitoringConstants.SYS_CPU_NICE;
		/** amount of free memory, in MB */
		sysMonitorParams |= ApMonMonitoringConstants.SYS_MEM_FREE;
		/** used system memory in percent */
		sysMonitorParams |= ApMonMonitoringConstants.SYS_MEM_USAGE;
		/** amount of currently used memory, in MB */
		sysMonitorParams |= ApMonMonitoringConstants.SYS_MEM_USED;
		/** amount of currently used swap, in MB */
		sysMonitorParams |= ApMonMonitoringConstants.SYS_PAGES_IN;
		sysMonitorParams |= ApMonMonitoringConstants.SYS_PAGES_OUT;
		/** network (input) transfer in KBps */
		sysMonitorParams |= ApMonMonitoringConstants.SYS_NET_IN;
		/** network (output) transfer in KBps */
		sysMonitorParams |= ApMonMonitoringConstants.SYS_NET_OUT;
		/** number of processes in thesystem */
		sysMonitorParams |= ApMonMonitoringConstants.SYS_PROCESSES;
		/** number of opened sockets for each proto => sockets_tcp/udp/unix */
		sysMonitorParams |= ApMonMonitoringConstants.SYS_NET_SOCKETS;
		/** number of tcp sockets in each state => sockets_tcp_LISTEN, ... */
		sysMonitorParams |= ApMonMonitoringConstants.SYS_NET_TCP_DETAILS;
		/** swap used, in MB */
		sysMonitorParams |= ApMonMonitoringConstants.SYS_SWAP_USED;
		/** swap free, in MB */
		sysMonitorParams |= ApMonMonitoringConstants.SYS_SWAP_FREE;
		/** swap usage in percent */
		sysMonitorParams |= ApMonMonitoringConstants.SYS_SWAP_USAGE;
		/** number of network errors */
		sysMonitorParams |= ApMonMonitoringConstants.SYS_NET_ERRS;
		/** in days */
		sysMonitorParams |= ApMonMonitoringConstants.SYS_UPTIME;

		genMonitorParams = 0L;

		genMonitorParams |= ApMonMonitoringConstants.GEN_HOSTNAME;
		genMonitorParams |= ApMonMonitoringConstants.GEN_IP;

		if (osName.indexOf("Linux") >= 0) {
			genMonitorParams |= ApMonMonitoringConstants.GEN_CPU_MHZ;
			/** number of the CPUs in the system */
			genMonitorParams |= ApMonMonitoringConstants.GEN_NO_CPUS;
			/** total amount of system memory in MB */
			genMonitorParams |= ApMonMonitoringConstants.GEN_TOTAL_MEM;
			/** total amount of swap in MB */
			genMonitorParams |= ApMonMonitoringConstants.GEN_TOTAL_SWAP;

			genMonitorParams |= ApMonMonitoringConstants.GEN_CPU_VENDOR_ID;

			genMonitorParams |= ApMonMonitoringConstants.GEN_CPU_FAMILY;

			genMonitorParams |= ApMonMonitoringConstants.GEN_CPU_MODEL;

			genMonitorParams |= ApMonMonitoringConstants.GEN_CPU_MODEL_NAME;

			genMonitorParams |= ApMonMonitoringConstants.GEN_BOGOMIPS;
		}

		jobMonitorParams = 0L;

		/** elapsed time from the start of this job in seconds */
		jobMonitorParams |= ApMonMonitoringConstants.JOB_RUN_TIME;
		/** processor time spent running this job in seconds */
		jobMonitorParams |= ApMonMonitoringConstants.JOB_CPU_TIME;
		/** current percent of the processor used for this job, as reported by ps */
		jobMonitorParams |= ApMonMonitoringConstants.JOB_CPU_USAGE;
		/** percent of the memory occupied by the job, as reported by ps */
		jobMonitorParams |= ApMonMonitoringConstants.JOB_MEM_USAGE;
		/** size in MB of the working directory of the job */
		jobMonitorParams |= ApMonMonitoringConstants.JOB_WORKDIR_SIZE;
		/** size in MB of the total size of the disk partition containing the working directory */
		jobMonitorParams |= ApMonMonitoringConstants.JOB_DISK_TOTAL;
		/** size in MB of the used disk partition containing the working directory */
		jobMonitorParams |= ApMonMonitoringConstants.JOB_DISK_USED;
		/** size in MB of the free disk partition containing the working directory */
		jobMonitorParams |= ApMonMonitoringConstants.JOB_DISK_FREE;
		/** percent of the used disk partition containing the working directory */
		jobMonitorParams |= ApMonMonitoringConstants.JOB_DISK_USAGE;
		/** size in KB of the virtual memory occupied by the job, as reported by ps */
		jobMonitorParams |= ApMonMonitoringConstants.JOB_VIRTUALMEM;
		/** size in KB of the resident image size of the job, as reported by ps */
		jobMonitorParams |= ApMonMonitoringConstants.JOB_RSS;
		/** opended files by job */
		jobMonitorParams |= ApMonMonitoringConstants.JOB_OPEN_FILES;
		/** current percent of the processor used for this job, as reported by contents of /proc/stat */
		jobMonitorParams |= ApMonMonitoringConstants.JOB_INSTANT_CPU_USAGE;
		/** current total child processes */
		jobMonitorParams |= ApMonMonitoringConstants.JOB_TOTAL_PROCS;
		/** current total child threads */
		jobMonitorParams |= ApMonMonitoringConstants.JOB_TOTAL_THREADS;
		/** current total context switches */
		jobMonitorParams |= ApMonMonitoringConstants.JOB_TOTAL_CONTEXTSW;
		/** current rate context switches */
		jobMonitorParams |= ApMonMonitoringConstants.JOB_RATE_CONTEXTSW;
	}

	/*******************************************************************************************************************************************************************************
	 * Parses a "xApMon" line from the configuration file and changes the ApMon settings according to the line.
	 *
	 * @param line
	 */
	protected void parseXApMonLine(final String line) {
		boolean flag = false, found;

		/** the line has the syntax "xApMon_parameter = value" */
		String tmp = line.replaceFirst("xApMon_", "");
		StringTokenizer st = new StringTokenizer(tmp, " \t=");
		String param = st.nextToken();
		String value = st.nextToken();

		/** for boolean values */
		if (value.equals("on"))
			flag = true;
		if (value.equals("off"))
			flag = false;

		synchronized (mutexBack) {
			found = false;
			if (param.equals("job_monitoring")) {
				this.jobMonitoring = flag;
				found = true;
			}
			if (param.equals("sys_monitoring")) {
				this.sysMonitoring = flag;
				found = true;
			}
			if (param.equals("job_interval")) {
				this.jobMonitorInterval = Long.parseLong(value);
				found = true;
			}
			if (param.equals("sys_interval")) {
				this.sysMonitorInterval = Long.parseLong(value);
				found = true;
			}
			if (param.equals("general_info")) {
				this.genMonitoring = flag;
				found = true;
			}
			if (param.equals("conf_recheck")) {
				this.confCheck = flag;
				found = true;
			}
			if (param.equals("recheck_interval")) {
				this.recheckInterval = this.crtRecheckInterval = Long.parseLong(value);
				found = true;
			}
			if (param.equals("maxMsgRate")) {
				this.maxMsgRate = Integer.parseInt(value);
				found = true;
			}
			if (param.equals("auto_disable")) {
				this.autoDisableMonitoring = flag;
				found = true;
			}
		}

		if (found)
			return;

		/***************************************************************************************************************************************************************************
		 * mutexBack protects the variables which hold settings for the backgrund thread (for sending monitoring
		 * information and for rechecking the configuration file/URLS)
		 */
		synchronized (mutexBack) {
			found = false;
			Long val = null;

			if (param.startsWith("sys")) {
				val = ApMonMonitoringConstants.getSysIdx(param);
				long lval = val.longValue();
				if (flag) {
					sysMonitorParams |= lval;
				}
				else {
					sysMonitorParams &= ~lval;
				}
			}
			else
				if (param.startsWith("job")) {
					val = ApMonMonitoringConstants.getJobIdx(param);
					long lval = val.longValue();
					if (flag) {
						jobMonitorParams |= lval;
					}
					else {
						jobMonitorParams &= ~lval;
					}
				}

			if (val == null) {
				logger.warning("Invalid parameter name in the configuration file: " + param);
			}
			else {
				found = true;
			}

		}

		if (!found)
			logger.warning("Invalid parameter name in the configuration file: " + param);
	}

	/**
	 * Displays the names, values and types of a set of parameters.
	 * 
	 * @param paramNames
	 *            Vector with the parameters' names.
	 * @param paramValues
	 *            Vector with the values of the parameters.
	 * @return parameters
	 */
	public static String printParameters(final Vector<String> paramNames, final Vector<Object> paramValues) {
		final StringBuilder res = new StringBuilder();

		for (int i = 0; i < paramNames.size(); i++) {
			res.append(paramNames.get(i)).append('=').append(paramValues.get(i)).append(' ');
		}

		return res.toString();
	}

	/** don't allow a user to send more than MAX_MSG messages per second, in average */
	protected long prvTime = 0;

	/**
	 * previously sent
	 */
	protected double prvSent = 0;

	/**
	 * previously dropped
	 */
	protected double prvDrop = 0;

	/**
	 * current time
	 */
	protected long crtTime = 0;

	/**
	 * sent
	 */
	protected long crtSent = 0;

	/**
	 * dropped
	 */
	protected long crtDrop = 0;

	/**
	 * threshold
	 */
	protected double hWeight = Math.exp(-5.0 / 60.0);

	/**
	 * Decide if the current datagram should be sent. This decision is based on the number of messages previously sent.
	 * 
	 * @return true if sending is still allowed
	 */
	public boolean shouldSend() {

		final long now = System.currentTimeMillis() / 1000;
		boolean doSend = true;

		if (now != crtTime) {
			/** new time, update previous counters; */
			prvSent = hWeight * prvSent + (1.0 - hWeight) * crtSent / (now - crtTime);
			prvTime = crtTime;
			logger.log(Level.FINE, "previously sent: " + crtSent + " dropped: " + crtDrop);
			/** reset current counter */
			crtTime = now;
			crtSent = 0;
			crtDrop = 0;
		}

		/** compute the history */
		final int valSent = (int) (prvSent * hWeight + crtSent * (1.0 - hWeight));

		/** when we should start dropping messages */
		final int level = this.maxMsgRate - this.maxMsgRate / 10;

		if (valSent > (this.maxMsgRate - level)) {
			doSend = ThreadLocalRandom.current().nextInt(this.maxMsgRate / 10) < (this.maxMsgRate - valSent);
		}

		/** counting sent and dropped messages */
		if (doSend) {
			crtSent++;
		}
		else {
			crtDrop++;
		}

		return doSend;
	}

	/**
	 * @return my hostname
	 */
	public String getMyHostname() {
		return myHostname;
	}

	/**
	 * Supported in Sun JRE >1.5 (returns -1 in prior versions)
	 * 
	 * @return self pid
	 */
	public static int getPID() {
		try {
			final java.lang.management.RuntimeMXBean rt = java.lang.management.ManagementFactory.getRuntimeMXBean();
			return Integer.parseInt(rt.getName().split("@")[0]);
		}
		catch (@SuppressWarnings("unused") Throwable t) {
			return -1;
		}
	}

}
