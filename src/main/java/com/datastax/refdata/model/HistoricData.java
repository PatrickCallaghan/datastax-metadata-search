package com.datastax.refdata.model;

import java.util.Date;

public class HistoricData {

	private String key;
	private Date date;
	private double open;
	private double high;
	private double low;
	private double close;
	private int volume;
	private double adjClose;
	public HistoricData(String key, Date date, double open, double high, double low, double close,
			int volume, double adjClose) {
		super();
		this.key = key;
		this.date = date;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.volume = volume;
		this.adjClose = adjClose;
	}
	public String getKey() {
		return key;
	}
	public Date getDate() {
		return date;
	}
	public double getOpen() {
		return open;
	}
	public double getHigh() {
		return high;
	}
	public double getLow() {
		return low;
	}
	public double getClose() {
		return close;
	}
	public int getVolume() {
		return volume;
	}
	public double getAdjClose() {
		return adjClose;
	}
	@Override
	public String toString() {
		return "HistoricData [key=" + key + ", date=" + date + ", open=" + open
				+ ", high=" + high + ", low=" + low + ", close=" + close + ", volume=" + volume + ", adjClose="
				+ adjClose + "]";
	}
}
