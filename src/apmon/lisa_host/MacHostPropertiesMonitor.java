package apmon.lisa_host;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author Gregory Denis
 *
 *         TODO To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Style - Code Templates
 */
/**
 * @author costing
 * @since Nov 13, 2010
 */
public class MacHostPropertiesMonitor {
	private static boolean DEBUG = false;

	private String[] networkInterfaces;
	private String activeInterface;
	private String cpuUsage = "0";
	private String cpuUSR = "0";
	private String cpuSYS = "0";
	private String cpuIDLE = "0";
	private String nbProcesses = "0";
	private String load1 = "0";
	private String load5 = "0";
	private String load15 = "0";
	private String memUsed = "0";
	private String memFree = "0";
	private String memUsage = "0";
	private String netIn = "0";
	private String netOut = "0";
	private String pagesIn = "0";
	private String pagesOut = "0";
	private String macAddress = "unknown";
	private String diskIO = "0";
	private String diskIn = "0";
	private String diskOut = "0";
	private String diskFree = "0";
	private String diskUsed = "0";
	private String diskTotal = "0";
	private String command = "";
	private cmdExec execute = null;
	private String sep = null;
	private Parser parser = null;
	private final Hashtable<String, Integer> netSockets = null;
	private final Hashtable<String, Integer> netTcpDetails = null;

	/**
	 *
	 */
	public MacHostPropertiesMonitor() {
		parser = new Parser();
		execute = new cmdExec();
		execute.setTimeout(3 * 1000);
		sep = System.getProperty("file.separator");
		// get the network interfaces up
		command = sep + "sbin" + sep + "ifconfig -l -u";
		String result = execute.executeCommand(command, "lo0");

		if (DEBUG)
			System.out.println(command + " = " + result);

		if (result == null || result.equals("")) {
			System.out.println(command + ": No result???");
		}
		else {
			final int where = result.indexOf("lo0");
			networkInterfaces = result.substring(where + 3, result.length()).replaceAll("  ", " ").trim().split(" ");

			// get the currently used Mac Address
			for (final String current : networkInterfaces) {
				command = sep + "sbin" + sep + "ifconfig " + current;
				result = execute.executeCommand(command, current);

				if (DEBUG)
					System.out.println(command + " = " + result);

				if (result == null || result.equals("")) {
					System.out.println(command + ": No result???");
				}
				else {
					if (result.indexOf("inet ") != -1) {
						final int pointI = result.indexOf("ether");
						final int pointJ = result.indexOf("media", pointI);
						macAddress = result.substring(pointI + 5, pointJ).trim();

						if (DEBUG)
							System.out.println("Mac Address:" + macAddress);

						activeInterface = current;
					}
				}
			}
		}

		// get the disk information
		command = sep + "bin" + sep + "df -k -h " + sep;
		result = execute.executeCommand(command, "/dev");

		if (DEBUG)
			System.out.println(command + " = " + result);

		if (result == null || result.equals("")) {
			System.out.println(command + ": No result???");
		}
		else {
			parseDf(result);
		}

		update();
	}

	/**
	 * @return mac addresses
	 */
	public String getMacAddresses() {
		return macAddress;
	}

	/**
	 *
	 */
	public void update() {

		if (execute == null) {
			execute = new cmdExec();
			execute.setTimeout(100 * 1000);
		}

		// get CPU, load, Mem, Pages, Processes from '/usr/bin/top'
		command = sep + "usr" + sep + "bin" + sep + "top -d -l2 -n1 -F -R";
		final String result = execute.executeCommand(command, "PID", 2);

		if (DEBUG)
			System.out.println(command + " = " + result);

		if (result == null || result.equals("")) {
			System.out.println("No result???");
		}
		else {
			parseTop(result);
		}
	}

	private void parseDf(final String toParse) {
		if (DEBUG)
			System.out.println("result of df -k -h /:" + toParse);

		int pointI = toParse.indexOf("/dev/");
		int pointJ = 0;
		int pointK = 0;

		// Get the size of the root disk
		try {
			pointJ = toParse.indexOf(" ", pointI);
			pointK = indexOfUnitLetter(toParse, pointJ);
			diskTotal = toParse.substring(pointJ, pointK).trim();
		}
		catch (final java.lang.StringIndexOutOfBoundsException e) {
			if (DEBUG) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}

		// Get the capacity used
		try {
			pointI = toParse.indexOf(" ", pointK);
			pointJ = indexOfUnitLetter(toParse, pointI);
			diskUsed = toParse.substring(pointI, pointJ).trim();
		}
		catch (final java.lang.StringIndexOutOfBoundsException e) {
			if (DEBUG) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}

		// Get the free space
		try {
			pointK = toParse.indexOf(" ", pointJ);
			pointI = indexOfUnitLetter(toParse, pointK);
			diskFree = toParse.substring(pointK, pointI).trim();
		}
		catch (final java.lang.StringIndexOutOfBoundsException e) {
			if (DEBUG) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}

		if (DEBUG)
			System.out.println("Disk: Total:" + diskTotal + " Used:" + diskUsed + " Free:" + diskFree);
	}

	private static int indexOfUnitLetter(final String inside, final int from) {

		int temp = inside.indexOf('K', from);
		if (temp == -1 || (temp - from > 10)) {
			temp = inside.indexOf('M', from);
			if (temp == -1 || (temp - from > 10)) {
				temp = inside.indexOf('G', from);
				if (temp == -1 || (temp - from > 10)) {
					temp = inside.indexOf('B', from);
					if (temp == -1 || (temp - from > 10)) {
						temp = inside.indexOf('T', from);
						if (temp == -1 || (temp - from > 10)) {
							temp = inside.indexOf('b', from);
							if (temp - from > 10)
								temp = -1;
						}
					}
				}
			}
		}
		return temp;
	}

	private static int lastIndexOfUnitLetter(final String inside, final int from) {

		int temp = inside.lastIndexOf('K', from);
		if (temp == -1 || (from - temp > 10)) {
			temp = inside.lastIndexOf('M', from);
			if (temp == -1 || (from - temp > 10)) {
				temp = inside.lastIndexOf('G', from);
				if (temp == -1 || (from - temp > 10)) {
					temp = inside.lastIndexOf('B', from);
					if (temp == -1 || (from - temp > 10)) {
						temp = inside.lastIndexOf('T', from);
						if (temp == -1 || (from - temp > 10)) {
							temp = inside.lastIndexOf('b', from);
							if (from - temp > 10)
								temp = -1;
						}
					}
				}
			}
		}
		return temp;
	}

	private static double howMuchMegaBytes(final char a) {

		switch (a) {
		case 'T':
			return 1048576.0;
		case 'G':
			return 1024.0;
		case 'M':
			return 1.0;
		case 'K':
			return 0.0009765625;
		case 'B':
			return 0.0000009537;
		default:
			return 1.0;
		}
	}

	private static final String TOP_PROCESSES = "Processes:";
	private static final String TOP_LOAD_AVG = "Load Avg:";
	private static final String TOP_CPU_USAGE = "CPU usage:";
	private static final String TOP_PHYS_MEM = "PhysMem:";
	private static final String TOP_VM = "VM:";

	private void parseTop(final String toParse) {

		if (DEBUG)
			System.out.println("\n******\n" + toParse + "\n********\n");

		int pointA = 0;
		int pointB = 0;
		int unitPos = 0;
		double sum = 0.0;

		// Get number of total Processes
		// Processes: 209 total, 13 running, 196 sleeping, 661 threads
		try {
			pointA = toParse.indexOf(TOP_PROCESSES);

			if (DEBUG)
				System.out.println("First Procs at " + pointA);

			pointA = toParse.indexOf(TOP_PROCESSES, pointA + TOP_PROCESSES.length()) + TOP_PROCESSES.length();

			if (DEBUG)
				System.out.println("Second Procs at " + pointA);

			pointB = toParse.indexOf(",", pointA + 1);
			nbProcesses = toParse.substring(pointA, pointB).trim();

			if (DEBUG)
				System.out.println(nbProcesses + " processes");
		}
		catch (final java.lang.StringIndexOutOfBoundsException e) {
			if (DEBUG) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}

		// Get the loads...
		// Load Avg: 12.81, 11.31, 11.34
		try {
			pointA = toParse.indexOf(TOP_LOAD_AVG, pointA);
			pointA += TOP_LOAD_AVG.length() + 1;
			pointB = toParse.indexOf(",", pointA);
			load1 = toParse.substring(pointA, pointB).trim();
			pointA = toParse.indexOf(",", pointB + 1);
			load5 = toParse.substring(pointB + 1, pointA).trim();
			pointB = toParse.indexOf(TOP_CPU_USAGE, pointA);
			load15 = toParse.substring(pointA + 1, pointB).trim();

			if (DEBUG)
				System.out.println("load: [" + load1 + "][" + load5 + "][" + load15 + "]");
		}
		catch (final java.lang.StringIndexOutOfBoundsException e) {
			if (DEBUG) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}

		// Get CPUs...
		// CPU usage: 87.87% user, 12.12% sys, 0.0% idle
		try {
			pointB = toParse.indexOf(TOP_CPU_USAGE, pointB) + TOP_CPU_USAGE.length();
			pointA = toParse.indexOf("% user", pointB);
			cpuUSR = toParse.substring(pointB, pointA).trim();
			pointA = toParse.indexOf(",", pointA);
			pointB = toParse.indexOf("% sys", pointA + 1);
			cpuSYS = toParse.substring(pointA + 1, pointB).trim();
			pointA = toParse.indexOf(",", pointB);
			pointB = toParse.indexOf("% idle", pointA + 1);
			cpuIDLE = toParse.substring(pointA + 1, pointB).trim();
			sum = 100.0 - Double.parseDouble(cpuIDLE);
			cpuUsage = String.valueOf(sum);

			if (DEBUG)
				System.out.println("Cpu Usage:" + cpuUsage + " user:" + cpuUSR + " sys:" + cpuSYS + " idle:" + cpuIDLE);
		}
		catch (final java.lang.StringIndexOutOfBoundsException e) {
			if (DEBUG) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}

		// Get Mem...
		// PhysMem: 5890M used (1023M wired), 2300M unused.
		try {
			pointA = toParse.indexOf(TOP_PHYS_MEM, pointB);
			pointA += TOP_PHYS_MEM.length();
			pointB = toParse.indexOf("M used", pointA);
			memUsed = toParse.substring(pointA + 1, pointB).trim();
			pointB = toParse.indexOf("M unused", pointB);
			pointA = toParse.lastIndexOf(",", pointB);
			memFree = toParse.substring(pointA + 1, pointB).trim();

			if (DEBUG)
				System.out.println("Mem Used:" + memUsed + "M Free:" + memFree + "M");

			sum = Double.parseDouble(memUsed) + Double.parseDouble(memFree);

			final double percentage = Integer.parseInt(memUsed) * 100 / sum;
			memUsage = String.valueOf(percentage);
		}
		catch (final java.lang.StringIndexOutOfBoundsException e) {
			if (DEBUG) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}

		// Pages In/Out...
		// VM: 946G vsize, 0B framework vsize, 0(0) swapins, 0(0) swapouts.
		try {
			pointA = toParse.indexOf(TOP_VM, pointB + 6);
			pointB = toParse.indexOf("swapins", pointA);
			pointA = toParse.lastIndexOf(",", pointB);
			pagesIn = toParse.substring(pointA + 1, pointB).trim();

			if (pagesIn.indexOf('(') > 0)
				pagesIn = pagesIn.substring(0, pagesIn.indexOf('('));

			pointA = toParse.indexOf("swapouts", pointB);
			pointB = toParse.lastIndexOf(",", pointA);
			pagesOut = toParse.substring(pointB + 1, pointA).trim();

			if (pagesOut.indexOf('(') > 0)
				pagesOut = pagesOut.substring(0, pagesOut.indexOf('('));

			if (DEBUG)
				System.out.println("Pages In:" + pagesIn + " Out" + pagesOut);
		}
		catch (final java.lang.StringIndexOutOfBoundsException e) {
			if (DEBUG) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}

		// Get Network IO...
		// Networks: packets: 2/172B in, 1/732B out.
		try {
			pointA = toParse.indexOf("Networks:", pointB) + 9;
			pointB = toParse.indexOf("packets: ", pointA) + 9;
			pointA = toParse.indexOf("in", pointB);
			unitPos = lastIndexOfUnitLetter(toParse, pointA);
			netIn = toParse.substring(pointB, unitPos).trim();

			if (netIn.indexOf('/') >= 0)
				netIn = netIn.substring(netIn.indexOf('/') + 1);

			if (DEBUG)
				System.out.print("Net In:" + netIn);

			double factor = howMuchMegaBytes((toParse.substring(unitPos, unitPos + 1).toCharArray())[0]);
			netIn = String.valueOf(Double.parseDouble(netIn) * factor * 4);
			pointB = toParse.indexOf("out", pointA);
			unitPos = lastIndexOfUnitLetter(toParse, pointB);
			factor = howMuchMegaBytes((toParse.substring(unitPos, unitPos + 1).toCharArray())[0]);
			netOut = toParse.substring(pointA + 3, unitPos).trim();

			if (netOut.indexOf('/') >= 0)
				netOut = netIn.substring(netOut.indexOf('/') + 1);

			if (DEBUG)
				System.out.println("Net Out:" + netOut);

			netOut = String.valueOf(Double.parseDouble(netOut) * factor);
		}
		catch (final java.lang.StringIndexOutOfBoundsException e) {
			if (DEBUG) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}

		// Get Disks IO...
		// Disks: 0/0B read, 4/2172K written.
		try {
			pointA = toParse.indexOf("Disks:", pointA) + 6;
			pointB = toParse.indexOf("read,", pointA);
			unitPos = lastIndexOfUnitLetter(toParse, pointB);
			diskIn = toParse.substring(pointA, unitPos).trim();

			if (diskIn.indexOf('/') >= 0)
				diskIn = diskIn.substring(diskIn.indexOf('/') + 1);

			pointA = toParse.indexOf("written", pointB);
			unitPos = lastIndexOfUnitLetter(toParse, pointA);
			diskOut = toParse.substring(pointB + 3, unitPos).trim();

			if (diskOut.indexOf('/') >= 0)
				diskOut = diskOut.substring(diskOut.indexOf('/') + 1);

			if (DEBUG)
				System.out.println("diskIO In:" + diskIn + " Out:" + diskOut);

			diskIO = diskIn + diskOut;
		}
		catch (final java.lang.StringIndexOutOfBoundsException e) {
			if (DEBUG) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return processes states
	 */
	public Hashtable<String, Integer> getProcessesState() {
		Hashtable<String, Integer> states = null;
		final String output = execute.executeCommandReality("ps -e -A -o state", "");

		if (DEBUG)
			System.err.println("Command: `ps -e -A -o state`, output:\n" + output);

		if (output != null && !output.equals("")) {
			parser.parse(output);
			String line = parser.nextLine();

			states = new Hashtable<>();
			states.put("D", Integer.valueOf(0));
			states.put("R", Integer.valueOf(0));
			states.put("S", Integer.valueOf(0));
			states.put("T", Integer.valueOf(0));
			states.put("Z", Integer.valueOf(0));

			while (line != null) {
				if ((line.startsWith(" ") || line.startsWith("\t"))) {
					line = parser.nextLine();
					continue;
				}
				final Enumeration<String> e = states.keys();
				while (e.hasMoreElements()) {
					final String key = e.nextElement();
					if (line.startsWith(key)) {
						int x = (states.get(key)).intValue();
						x++;
						states.put(key, Integer.valueOf(x));
					}
				}
				line = parser.nextLine();
			}
		}
		return states;
	}

	/**
	 * @return cpu usage
	 */
	public String getCpuUsage() {
		return cpuUsage;
	}

	/**
	 * @return cpu user
	 */
	public String getCpuUSR() {
		return cpuUSR;
	}

	/**
	 * @return cpu system
	 */
	public String getCpuSYS() {
		return cpuSYS;
	}

	/**
	 * @return cpu nice
	 */
	@SuppressWarnings("static-method")
	public String getCpuNICE() {
		return "0";
	}

	/**
	 * @return cpu idle
	 */
	public String getCpuIDLE() {
		return cpuIDLE;
	}

	/**
	 * @return pages in
	 */
	public String getPagesIn() {
		return pagesIn;
	}

	/**
	 * @return pages out
	 */
	public String getPagesOut() {
		return pagesOut;
	}

	/**
	 * @return memory usage
	 */
	public String getMemUsage() {
		return memUsage;
	}

	/**
	 * @return memory used
	 */
	public String getMemUsed() {
		return memUsed;
	}

	/**
	 * @return memory free
	 */
	public String getMemFree() {
		return memFree;
	}

	/**
	 * @return disk io
	 */
	public String getDiskIO() {
		return diskIO;
	}

	/**
	 * @return disk total
	 */
	public String getDiskTotal() {
		return diskTotal;
	}

	/**
	 * @return disk used
	 */
	public String getDiskUsed() {
		return diskUsed;
	}

	/**
	 * @return disk free
	 */
	public String getDiskFree() {
		return diskFree;
	}

	/**
	 * @return number of processes
	 */
	public String getNoProcesses() {
		return nbProcesses;
	}

	/**
	 * @return sockets
	 */
	public Hashtable<String, Integer> getNetSockets() {
		return netSockets;
	}

	/**
	 * @return tcp states
	 */
	public Hashtable<String, Integer> getTcpDetails() {
		return netTcpDetails;
	}

	/**
	 * @return load 1
	 */
	public String getLoad1() {
		return load1;
	}

	/**
	 * @return load 5
	 */
	public String getLoad5() {
		return load5;
	}

	/**
	 * @return load 15
	 */
	public String getLoad15() {
		return load15;
	}

	/**
	 * @return net interfaces
	 */
	public String[] getNetInterfaces() {
		return networkInterfaces;
	}

	/**
	 * @param ifName
	 * @return net in
	 */
	public String getNetIn(final String ifName) {
		if (ifName.equalsIgnoreCase(activeInterface))
			return netIn;
		return "0";
	}

	/**
	 * @param ifName
	 * @return net out
	 */
	public String getNetOut(final String ifName) {
		if (ifName.equalsIgnoreCase(activeInterface))
			return netOut;
		return "0";
	}

	/**
	 * Debug method
	 *
	 * @param args
	 */
	public static void main(final String[] args) {
		DEBUG = true;

		final MacHostPropertiesMonitor monitor = new MacHostPropertiesMonitor();

		final Map<String, Integer> state = monitor.getProcessesState();

		if (state != null)
			for (final Map.Entry<String, Integer> entry : state.entrySet())
				System.err.println(entry.getKey() + " : " + entry.getValue());
		else
			System.err.println("No processes state information");

		System.err.println(MacHostPropertiesMonitor.class.getCanonicalName() + " testing is done, exiting now");
	}
}
