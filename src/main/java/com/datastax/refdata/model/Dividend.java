package com.datastax.refdata.model;

import java.util.Date;

public class Dividend {

	private String key;
	private Date date;
	private double dividend;
	
	public Dividend(String key, Date date, double dividend) {
		super();
		this.key = key;
		this.date = date;
		this.dividend = dividend;
	}
	public String getKey() {
		return key;
	}
	public Date getDate() {
		return date;
	}
	public double getDividend() {
		return dividend;
	}
	@Override
	public String toString() {
		return "Dividend [key=" + key + ", date=" + date + ", dividend=" + dividend
				+ "]";
	}	
}
