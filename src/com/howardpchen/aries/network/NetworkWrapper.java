package com.howardpchen.aries.network;

import java.util.Map;

public abstract class NetworkWrapper {
	public Map<String,Double> getDiagnosisProbs() {
		return getNodeProbs("Diseases");
	}
	public abstract Map<String, Double> getNodeProbs(String nodeName);
	public abstract String[] getNodeNames();
	public abstract void setNodeState(String nodeName, String state);
	public abstract void clearNodeState(String nodeName);
	public abstract void endSession();
	
	//changes starts for CR102
	public abstract String getHighestSensitiveNodeName(Map<String, String> userInputs);
	//changes ends for CR102
}
