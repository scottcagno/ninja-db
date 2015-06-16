package com.cagnosolutions.ninja.test;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class E {

	/**
	 * Composite key for mapE in A
	 */

	String bid;
	String cid;

	public E(String bid, String cid) {
		this.bid = bid;
		this.cid = cid;
	}

	public String getBid() {
		return this.bid;
	}

	public String getCid() {
		return this.cid;
	}

	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		E e = (E) o;
		if (bid != null ? !bid.equals(e.bid) : e.bid != null) return false;
		return !(cid != null ? !cid.equals(e.cid) : e.cid != null);

	}

	public int hashCode() {
		int result = bid != null ? bid.hashCode() : 0;
		result = 31 * result + (cid != null ? cid.hashCode() : 0);
		return result;
	}

	public String toString() {
		return String.format("%s:%s", bid, cid);
	}
}
