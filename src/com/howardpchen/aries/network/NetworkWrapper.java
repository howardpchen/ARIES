package com.howardpchen.aries.network;

import java.util.Map;

public abstract class NetworkWrapper {
	public Map<String,Double> getDiagnosisProbs() {
		return getNodeProbs("Disease");
	}
	public abstract Map<String, Double> getNodeProbs(String nodeName);
	public abstract String[] getNodeNames();
	public abstract void setNodeState(String nodeName, String state);
	public abstract void clearNodeState(String nodeName);
}
