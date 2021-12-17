package apmon.lisa_host;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringTokenizer;

/**
 * @author ML Team
 */
public class Parser {

	private StringTokenizer st = null;

	private StringTokenizer auxSt = null;

	/**
	 * 
	 */
	public Parser() {
		// nothing
	}

	/**
	 * @param text
	 */
	public void parse(String text) {

		st = new StringTokenizer(text);
	}

	/**
	 * @param text
	 */
	public void parseAux(String text) {

		auxSt = new StringTokenizer(text);
	}

	/**
	 * @param fileName
	 */
	public void parseFromFile(String fileName) {
		try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
			final StringBuilder str = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				if (str.length() > 0)
					str.append('\n');
				str.append(line);
			}
			st = new StringTokenizer(str.toString());
		}
		catch (@SuppressWarnings("unused") Throwable t) {
			st = null;
		}
	}

	/**
	 * @return next line
	 */
	public String nextLine() {

		if (st == null)
			return null;
		try {
			return st.nextToken("\n");
		}
		catch (@SuppressWarnings("unused") Exception e) {
			return null;
		}
	}

	/**
	 * @return next aux line
	 */
	public String nextAuxLine() {

		if (auxSt == null)
			return null;
		try {
			return auxSt.nextToken("\n");
		}
		catch (@SuppressWarnings("unused") Exception e) {
			return null;
		}
	}

	/**
	 * @return next token
	 */
	public String nextToken() {

		if (st == null)
			return "";
		try {
			return st.nextToken();
		}
		catch (@SuppressWarnings("unused") Exception e) {
			return null;
		}
	}

	/**
	 * @return true if more
	 */
	public boolean hasMoreTokens() {
		return (st != null && st.hasMoreTokens());
	}

	/**
	 * @param token
	 * @return next token
	 */
	public String nextToken(String token) {

		if (st == null)
			return "";
		try {
			return st.nextToken(token);
		}
		catch (@SuppressWarnings("unused") Exception e) {
			return null;
		}
	}

	/**
	 * @return true if more
	 */
	public boolean hasMoreAuxTokens() {
		return (auxSt != null && auxSt.hasMoreTokens());
	}

	/**
	 * @return next aux token
	 */
	public String nextAuxToken() {

		if (auxSt == null)
			return "";
		try {
			return auxSt.nextToken();
		}
		catch (@SuppressWarnings("unused") Exception e) {
			return null;
		}
	}

	/**
	 * @param token
	 * @return next aux token
	 */
	public String nextAuxToken(String token) {

		if (auxSt == null)
			return "";
		try {
			return auxSt.nextToken(token);
		}
		catch (@SuppressWarnings("unused") Exception e) {
			return null;
		}
	}

	/**
	 * @param text
	 * @param start
	 * @return token after token
	 */
	public static String getTextAfterToken(String text, String start) {
		final int idx = text.indexOf(start);

		if (idx < 0)
			return null;

		return text.substring(idx + start.length());
	}

	/**
	 * @param text
	 * @param end
	 * @return text before token
	 */
	public static String getTextBeforeToken(String text, String end) {
		final int idx = text.indexOf(end);

		if (idx < 0)
			return text;

		return text.substring(0, idx);
	}

	/**
	 * @param textParam
	 * @param start
	 * @param end
	 * @return text between
	 */
	public static String getTextBetween(String textParam, String start, String end) {
		String text = textParam;

		if (text.indexOf(start) == -1)
			return null;

		text = text.substring(text.indexOf(start) + start.length());
		if (text.lastIndexOf(end) == -1)
			return text;
		return text.substring(0, text.lastIndexOf(end));
	}

	/**
	 * @param directory
	 * @return list of files in the directory
	 */
	public static String[] listFiles(String directory) {

		String[] fileList = null;
		try {
			File dir = new File(directory);
			if (!dir.isDirectory())
				return null;
			File[] list = dir.listFiles();
			if (list == null)
				return null;
			fileList = new String[list.length];
			for (int i = 0; i < list.length; i++)
				fileList[i] = list[i].getName();
		}
		catch (@SuppressWarnings("unused") Exception e) {
			return null;
		}
		return fileList;
	}

} // end of class Parser
