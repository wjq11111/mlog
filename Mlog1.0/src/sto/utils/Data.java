package sto.utils;

public class Data {

	private String result;
	private String info;

	public String getResult() {
		return result;
	}

	public synchronized void setResult(String result) {
		this.result = result;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}
	
}
