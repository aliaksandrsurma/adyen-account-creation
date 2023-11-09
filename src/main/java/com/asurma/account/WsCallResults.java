package com.asurma.account;

public class WsCallResults {

	public Constants.WsCallStatus wsCallstatus;
	public String detailedMessage;

	public WsCallResults(Constants.WsCallStatus wsCallstatus, String detailedMessage) {
		super();
		this.wsCallstatus = wsCallstatus;
		this.detailedMessage = detailedMessage;
	}

	public Constants.WsCallStatus getWsCallstatus() {
		return wsCallstatus;
	}

	public void setWsCallstatus(Constants.WsCallStatus wsCallstatus) {
		this.wsCallstatus = wsCallstatus;
	}

	public String getDetailedMessage() {
		return detailedMessage;
	}

	public void setDetailedMessage(String detailedMessage) {
		this.detailedMessage = detailedMessage;
	}


}
