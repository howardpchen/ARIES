package com.howardpchen.aries.network;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class NetworkWrapper {
	public Map<String,Double> getDiagnosisProbs() {
		return getNodeProbs("Diseases");
	}
	public Map<String,Double> getRadioDiagnosisProbs() {
		return getNodeProbs1("Diseases");
	}
	public abstract Map<String, Double> getNodeProbs(String nodeName);
	public abstract Map<String, Double> getNodeProbs1(String nodeName);
	public abstract String[] getNodeNames();
	public abstract void setNodeState(String nodeName, String state);
	public abstract void clearNodeState(String nodeName);
	public abstract void endSession();
	
	//changes starts for CR102
	//public abstract String getHighestSensitiveNodeName(Map<String, String> userInputs);
	//changes ends for CR102
	//Changes for CR103
	public abstract String getHighestSISensitiveNodeName(Map<String, String> userInputs);
	public abstract String getHighestSPSensitiveNodeName(Map<String, String> userInputs);
	public abstract String getHighestCLSensitiveNodeName(Map<String, String> userInputs);
	public abstract String getHighestMSSensitiveNodeName(Map<String, String> userInputs);
	public abstract List<String> getSINodeNames();
	public abstract List<String> getSPNodeNames();
    public abstract List<String> getCLNodeNames();
	public abstract List<String> getMSNodeNames();

	//Ends here
	 public abstract String[] getStates(String nodeName);
	 public abstract String getSensitiveForDisease(String Disease,Map<String, String> userInputs);
	
}
