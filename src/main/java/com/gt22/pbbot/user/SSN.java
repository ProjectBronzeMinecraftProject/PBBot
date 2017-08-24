package com.gt22.pbbot.user;

import com.gt22.pbbot.Core;
import com.gt22.randomutils.Instances;

public class SSN {


	@SuppressWarnings("SSN-Костыль")
	static SSN randomSSN() {
		char[] ssnc = new char[9];
		for(int i = 0; i < ssnc.length; i++) {
			ssnc[i] = (char) ('0' + Instances.getRand().nextInt(10));
		}
		int ssn = 0;
		for(int i = 0; i < ssnc.length; i++) {
			ssn += Character.digit(ssnc[i], 10) * ((int)Math.pow(10, 8 - i));
		}
		return new SSN(ssn);
	}

	private final int ssn;

	SSN(int ssn) {
		this.ssn = ssn;
	}

	public int getSSN() {
		return ssn;
	}

	@SuppressWarnings("SSN-Костыль")
	public String getSSNString(boolean redacted) {
		char[] ssn = new char[9];
		String ssns = Integer.toString(getSSN());
		int zeros = 9 - ssns.length();
		for (int i = 0; i < zeros; i++) {
			ssn[i] = '0';
		}
		ssns.getChars(0, ssns.length(), ssn, zeros);
		if (redacted) {
			return String.format("XXX-XX-%c%c%c%c", ssn[5], ssn[6], ssn[7], ssn[8]);
		} else {
			return String.format("%c%c%c-%c%c-%c%c%c%c", ssn[0], ssn[1], ssn[2], ssn[3], ssn[4], ssn[5], ssn[6], ssn[7], ssn[8]);
		}
	}

	@Override

	public String toString() {
		return getSSNString(false);
	}
}
