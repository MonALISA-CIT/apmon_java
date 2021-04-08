/*
 * ApMon - Application Monitoring Tool
 * Version: 2.2.8
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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import apmon.lisa_host.cmdExec;

/**
 * @author ML team
 */
public class MonitoredJob {
	/**
	 * process id
	 */
	final int pid;

	/** the job's working directory */
	final String workDir;

	/** the cluster name that will be included in the monitoring datagrams */
	final String clusterName;

	/** the node name that will be included in the monitoring datagrams */
	final String nodeName;

	private static final Logger logger = Logger.getLogger("apmon");

	private cmdExec exec = null;

	final int numCPUs;

	/**
	 * @param _pid
	 * @param _workDir
	 * @param _clusterName
	 * @param _nodeName
	 */
	public MonitoredJob(int _pid, String _workDir, String _clusterName, String _nodeName, int _numCPUs) {
		this.pid = _pid;
		this.workDir = _workDir;
		this.clusterName = _clusterName;
		this.nodeName = _nodeName;
		this.exec = new cmdExec();
		this.numCPUs = _numCPUs;
	}

	/**
	 * @return monitored process id
	 */
	public int getPid() {
		return pid;
	}

	/**
	 * @return disk usage
	 */
	public HashMap<Long, Double> readJobDiskUsage() {
		HashMap<Long, Double> hm = new HashMap<>();
		String cmd = null, aux = null, result = null;
		double workdir_size = 0.0, disk_total = 0.0, disk_used = 0.0, disk_free = 0.0, disk_usage = 0.0;

		if (workDir == null)
			return null;

		cmd = "du -Lscm " + workDir + " | tail -1 | cut -f 1";
		result = exec.executeCommandReality(cmd, "");
		workdir_size = Double.parseDouble(result);
		hm.put(ApMonMonitoringConstants.LJOB_WORKDIR_SIZE, Double.valueOf(workdir_size));

		cmd = "df -P -m " + workDir + " | tail -1";
		result = exec.executeCommand(cmd, "");
		final StringTokenizer st = new StringTokenizer(result, " \t%");

		st.nextToken(); // skip over the filesystem name

		aux = st.nextToken();
		disk_total = Double.parseDouble(aux);
		hm.put(ApMonMonitoringConstants.LJOB_DISK_TOTAL, Double.valueOf(disk_total));

		aux = st.nextToken();
		disk_used = Double.parseDouble(aux);
		hm.put(ApMonMonitoringConstants.LJOB_DISK_USED, Double.valueOf(disk_used));

		aux = st.nextToken();
		disk_free = Double.parseDouble(aux);
		hm.put(ApMonMonitoringConstants.LJOB_DISK_FREE, Double.valueOf(disk_free));

		aux = st.nextToken();
		disk_usage = Double.parseDouble(aux);
		hm.put(ApMonMonitoringConstants.LJOB_DISK_USAGE, Double.valueOf(disk_usage));

		return hm;
	}

	/**
	 * @return children processes
	 */
	public Vector<Integer> getChildren() {
		Vector<Integer> pids, ppids, children;
		String cmd = null, result = null;
		int nProcesses = 0, nChildren = 1;
		int i, j;

		cmd = "ps -eo ppid,pid";
		result = exec.executeCommandReality(cmd, "");
		boolean pidFound = false;
		if (result == null) {
			logger.warning("The child processes could not be determined");
			return null;
		}

		StringTokenizer st = new StringTokenizer(result, " \n");
		nProcesses = st.countTokens() / 2 - 1;

		// Skip over the header line with its two tokens
		st.nextToken();
		st.nextToken();

		pids = new Vector<>();
		ppids = new Vector<>();
		children = new Vector<>();
		children.add(Integer.valueOf(pid));
		while (st.hasMoreTokens()) {
			i = Integer.parseInt(st.nextToken());
			j = Integer.parseInt(st.nextToken());
			if (j == pid)
				pidFound = true;
			ppids.add(Integer.valueOf(i));
			pids.add(Integer.valueOf(j));
			if (i == (children.elementAt(0)).intValue()) {
				children.add(Integer.valueOf(j));
				nChildren++;
			}
		}

		if (!pidFound)
			return null;

		i = 1;

		while (i < nChildren) {
			/* find the children of the i-th child */
			for (j = 0; j < nProcesses; j++) {
				if (ppids.elementAt(j).equals(children.elementAt(i))) {
					children.add(pids.elementAt(j));
					nChildren++;
				}
			}
			i++;
		}

		return children;
	}

	/**
	 * @param s
	 * @return parsed time
	 */
	public static long parsePSTime(String s) {
		long days, hours, mins, secs;
		if (s.indexOf('-') > 0) {
			StringTokenizer st = new StringTokenizer(s, "-:.");
			days = Long.parseLong(st.nextToken());
			hours = Long.parseLong(st.nextToken());
			mins = Long.parseLong(st.nextToken());
			secs = Long.parseLong(st.nextToken());
			return 24 * 3600 * days + 3600 * hours + 60 * mins + secs;
		}
		if (s.indexOf(':') > 0 && s.indexOf(':') != s.lastIndexOf(':')) {
			StringTokenizer st = new StringTokenizer(s, ":.");
			hours = Long.parseLong(st.nextToken());
			mins = Long.parseLong(st.nextToken());
			secs = Long.parseLong(st.nextToken());
			return 3600 * hours + 60 * mins + secs;
		}

		if (s.indexOf(':') > 0) {
			StringTokenizer st = new StringTokenizer(s, ":.");
			mins = Long.parseLong(st.nextToken());
			secs = Long.parseLong(st.nextToken());
			return 60 * mins + secs;
		}

		return -1;
	}

	/**
	 * @return job monitoring
	 * @throws IOException
	 */
	public HashMap<Long, Double> readJobInfo() throws IOException {
		Vector<Integer> children;
		HashMap<Long, Double> ret = new HashMap<>();
		String cmd = null, result = null;
		String line = null;

		int i;

		double rsz = 0.0, vsz = 0.0;
		double etime = 0.0, cputime = 0.0;
		double pcpu = 0.0, pmem = 0.0;

		double _rsz, _vsz;
		double _etime, _cputime;
		double _pcpu, _pmem;

		long apid, fd = 0;

		/*
		 * this list contains strings of the form "rsz_vsz_command" for every pid; it is used to avoid adding several times processes that have multiple threads and appear in ps as
		 * separate processes, occupying exactly the same amount of memory and having the same command name. For every line from the output of the ps command we verify if the
		 * rsz_vsz_command combination is already in the list.
		 */
		Vector<String> mem_cmd_list = new Vector<>();

		/* get the list of the process' descendants */
		children = getChildren();

		if (children == null)
			return null;

		logger.fine("Number of children for process " + pid + ": " + children.size());

		/* issue the "ps" command to obtain information on all the descendants */
		cmd = "ps -p ";
		for (i = 0; i < children.size() - 1; i++)
			cmd = cmd + children.elementAt(i) + ",";
		cmd = cmd + children.elementAt(children.size() - 1);

		cmd = cmd + " -o pid,etime,time,%cpu,%mem,rss,vsz,comm";
		result = exec.executeCommandReality(cmd, "");

		// skip over the first line of the `ps` output
		int idx = result.indexOf('\n');

		if (idx > 0)
			result = result.substring(idx + 1);

		StringTokenizer rst = new StringTokenizer(result, "\n");
		while (rst.hasMoreTokens()) {
			line = rst.nextToken();
			try {
				StringTokenizer st = new StringTokenizer(line, " \t");

				apid = Long.parseLong(st.nextToken());
				_etime = parsePSTime(st.nextToken());
				_cputime = parsePSTime(st.nextToken());
				_pcpu = Double.parseDouble(st.nextToken());
				_pmem = Double.parseDouble(st.nextToken());
				_rsz = Double.parseDouble(st.nextToken());
				_vsz = Double.parseDouble(st.nextToken());
				String cmdName = st.nextToken();

				etime = etime > _etime ? etime : _etime;
				cputime += _cputime;
				pcpu += _pcpu;

				String mem_cmd_s = "" + _rsz + "_" + _vsz + "_" + cmdName;
				// mem_cmd_list.add(mem_cmd_s);
				if (mem_cmd_list.indexOf(mem_cmd_s) == -1) {
					pmem += _pmem;
					vsz += _vsz;
					rsz += _rsz;
					mem_cmd_list.add(mem_cmd_s);
					long _fd = countOpenFD(apid);
					if (_fd != -1)
						fd += _fd;
				}
			}
			catch (final Exception e) {
				System.err.println("Exception parsing line `" + line + "` of the output of `" + cmd + "`: " + e.getMessage());
				e.printStackTrace();
			}
		}

		double pssKB = 0;
		double swapPssKB = 0;
		
		for (Integer child: children) {
			File f = new File("/proc/"+child+"/smaps");
			
			if (f.exists() && f.canRead()) {
				try (BufferedReader br = new BufferedReader(new FileReader(f))){					
					String s;
					
					while ( (s=br.readLine())!=null ) {
						// File content is something like this (the keys that we are missing from the `ps` output only):
						// Pss:                  16 kB
						// SwapPss:               0 kB
						if (s.startsWith("Pss:") || s.startsWith("SwapPss:")) {
							final StringTokenizer st = new StringTokenizer(s);
							
							if (st.countTokens()==3) {
								st.nextToken();
								try {
									long value = Long.parseLong(st.nextToken());
									
									if (s.startsWith("S"))
										swapPssKB += value;
									else
										pssKB += value;
								}
								catch (@SuppressWarnings("unused") final NumberFormatException nfe) {
									// ignore
								}
							}
						}
					}
				}
				catch (@SuppressWarnings("unused") final IOException ioe) {
					// ignore
				}
			}
		}

		ret.put(ApMonMonitoringConstants.LJOB_RUN_TIME, Double.valueOf(etime * numCPUs));
		ret.put(ApMonMonitoringConstants.LJOB_CPU_TIME, Double.valueOf(cputime));
		ret.put(ApMonMonitoringConstants.LJOB_CPU_USAGE, Double.valueOf(pcpu));
		ret.put(ApMonMonitoringConstants.LJOB_MEM_USAGE, Double.valueOf(pmem));
		ret.put(ApMonMonitoringConstants.LJOB_RSS, Double.valueOf(rsz));
		ret.put(ApMonMonitoringConstants.LJOB_VIRTUALMEM, Double.valueOf(vsz));
		ret.put(ApMonMonitoringConstants.LJOB_OPEN_FILES, Double.valueOf(fd));

		if (pssKB == 0 && rsz > 0) {
			// fake the PSS values if they are not supported, assuming that
			// PSS == RSS and SwapPSS = Virtual - RSS

			pssKB = rsz;
			swapPssKB = vsz - rsz;
		}

		ret.put(ApMonMonitoringConstants.LJOB_PSS, Double.valueOf(pssKB));
		ret.put(ApMonMonitoringConstants.LJOB_SWAPPSS, Double.valueOf(swapPssKB));

		return ret;
	}

	/**
	 * count the number of open files for the given pid
	 * 
	 * @param processid
	 * @return opened file descriptors
	 */
	public static long countOpenFD(final long processid) {

		long open_files;
		int mypid = ApMon.getPID();
		String dir = "/proc/" + processid + "/fd";
		File f = new File(dir);
		if (f.exists()) {
			if (f.canRead()) {
				open_files = (f.list()).length - 2;
				if (processid == mypid)
					open_files -= 2;
				logger.log(Level.FINE, "Counting open_files for process " + processid);
			}
			else {
				open_files = -1;
				logger.log(Level.SEVERE, "ProcInfo: cannot count the number of opened files for job" + processid);
			}
		}
		else {
			open_files = -1;
			logger.log(Level.SEVERE, "ProcInfo: job " + processid + "not exist.");
		}
		return open_files;
	}

	@Override
	public String toString() {
		return "[" + pid + "]" + " " + workDir + " " + " " + clusterName + " " + nodeName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == null)
			return false;

		if (!(obj instanceof MonitoredJob))
			return false;

		final MonitoredJob other = (MonitoredJob) obj;

		return pid == other.pid && clusterName.equals(other.clusterName) && nodeName.equals(other.nodeName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 13 * pid + 19 * clusterName.hashCode() + 31 * nodeName.hashCode();
	}

}
