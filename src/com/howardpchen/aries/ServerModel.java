package com.howardpchen.aries;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSessionBindingEvent;

import com.howardpchen.aries.network.DNETWrapper;
import com.howardpchen.aries.network.NetworkLoadingException;
import com.howardpchen.aries.network.NetworkWrapper;

@ManagedBean
@SessionScoped
public class ServerModel {
	/**
	 * 
	 */
//	private static final long serialVersionUID = -9026586621419425189L;
	
	private final String PATH = "D:/WorkSpace/ARIES/networks";
	/*private final String PATH = "/home/shalini/Desktop/ARIESNEW/networks/";*/
	private Map<String, String> userInputs;
	private NetworkWrapper dw;
	private String pageLoad = ""; 
	private String networkName = "";
	private int topDdx = 10;
	private List<String> networkFileList = new ArrayList<String>();
	private String[] nodes = new String[0];
	private String currentFeature = "";
	
	//changes starts for CR102,CR103
	private String highestSISensitiveNodeName = "";
	private String highestSPSensitiveNodeName = "";
	private String highestCLSensitiveNodeName = "";
	private String highestMSSensitiveNodeName = "";
	//changes ends for CR102,CR103	
	
	//changes starts for CR101
	private Map<String, String> nodeNameDirectMapping = new HashMap<String, String>();
	private Map<String, String> nodeNameReverseMapping = new HashMap<String, String>();
	private TreeMap<String, List<String>> prefixNodeListMapping = new TreeMap<String, List<String>>();
	private ArrayList<String> networkPrefixList = new ArrayList<String>();
	private Map<String, String> prefixNameMapping = new HashMap<String, String>();	
	//changes ends for CR101
	
	//changes starts for CR103
	private boolean sensityvityOn = false;
	public boolean isSensityvityOn() {
		return this.sensityvityOn;
	}
	public void setSensityvityOn(boolean sensityvityOn) {
		this.sensityvityOn = sensityvityOn;
	}
 	public ServerModel() {
		userInputs = new HashMap<String, String>();
		//changes starts for CR101
		populateNetworkList();
		//changes ends for CR101
		registerSession();

		File folder = new File(PATH);
		File[] listOfFiles = folder.listFiles(
			new FilenameFilter() {
			    public boolean accept(File dir, String name) {
			    	if (name.toLowerCase().endsWith(".dne")) {
				    	System.out.println("Found " + name);
				        return true;
			    	} else return false;
			    }
			}
		);
		for (int i = 0; i < listOfFiles.length; i++) {
			networkFileList.add(listOfFiles[i].getName());
			System.out.println("N/W"+listOfFiles[i].getName());
		}
		networkName = networkFileList.get(0);
		System.out.println("networkName"+networkName);
		System.out.println("Called constructor.");

	}
	
	//changes starts for CR101
	
	private void populateNetworkList()
	{
		networkPrefixList = new ArrayList<String>();
		networkPrefixList.add("SI");
		networkPrefixList.add("SP");
		networkPrefixList.add("CL");
		networkPrefixList.add("MS");
		
		prefixNameMapping = new HashMap<String, String>();
		prefixNameMapping.put("SI", "Signal");
		prefixNameMapping.put("SP", "Spatial");
		prefixNameMapping.put("CL", "Clinical");
		prefixNameMapping.put("MS", "Miscellaneous");
	}
	
	public List<String> getNetworkPrefixList() 
	{		
		return networkPrefixList;
	}
	
	public String getNetworkPrefixName(String prefix) 
	{		
		return prefixNameMapping.get(prefix);
	}
	
	
	/**
	 * CR103 starts here
	 * @param prefix
	 * @return
	 */
	public boolean getNetworkNodeSize(String prefix) 
	{		
		for(Map.Entry<String, List<String>> entry : prefixNodeListMapping.entrySet()){
			if(entry.getKey().equals(prefix)){
				if(prefix.equals("MS") && entry.getValue().size() == 1){
					return false;
				}else return true;
			}
		}
		return false;
		
	}
	//CR103 Ends here
	
	public List<String> getSelectMenuFeatures(String prefix) {
		List<String> features = new ArrayList<String>();
		List<String> prefixNodeList = prefixNodeListMapping.get(prefix);
		
		if(prefixNodeList != null)
		{
			for (String nodeName : prefixNodeList) {
				if (nodeName.equals("Diseases")) continue;
				features.add(nodeName);
			}			
		}
		
		return features;
	}
	
	private void processNodePrefixes()
	{
		nodeNameDirectMapping = new HashMap<String, String>();
		nodeNameReverseMapping = new HashMap<String, String>();
		prefixNodeListMapping = new TreeMap<String, List<String>>();
		
		for (int i = 0; i < nodes.length; i++) 
		{
/*			if (oldNodeArray[i].equals("Diseases")) continue;*/
			
			String nodeNameWithPrefix = nodes[i];
			String nodeNameWithoutPrefix = nodes[i];
			String[] stringArray = nodeNameWithPrefix.split("_");
			String prefix = stringArray[0];
			
			if(networkPrefixList.contains(prefix))
			{
				nodeNameWithoutPrefix = nodeNameWithPrefix.replace(prefix + "_", "");
			}
			else
			{
				prefix = "MS";
			}
			
			nodeNameDirectMapping.put(nodeNameWithPrefix, nodeNameWithoutPrefix);
			nodeNameReverseMapping.put(nodeNameWithoutPrefix, nodeNameWithPrefix);
			
			List<String> prefixNodeList = prefixNodeListMapping.get(prefix);	
			if(prefixNodeList == null)
			{
				prefixNodeList = new ArrayList<String>();
			}
			prefixNodeList.add(nodeNameWithoutPrefix);
			prefixNodeListMapping.put(prefix, prefixNodeList);
		}
		
		for (Map.Entry<String, List<String>> entry : prefixNodeListMapping.entrySet())
		{
			List<String> prefixNodeList = entry.getValue();
			java.util.Collections.sort(prefixNodeList);
		}
	}
	
	//changes ends for CR101

	public void setNetworkInput(String s) {
		if (!s.equals(networkName)) {
			System.out.println("Cleared user inputs.");
			userInputs.clear();
		}
		networkName = s;
	}

	public String getNetworkInput() {		
		return networkName;
	}
	
	public void setNodeInput(String s) {
		String[] inputs = s.split(":");
		//old before CR101
/*		if (inputs.length == 2)	userInputs.put(inputs[0], inputs[1]);
		else if (inputs.length == 1) userInputs.put(inputs[0], "[Clear]");*/
		
		//changes starts for CR101
		if (inputs.length == 2)	userInputs.put(nodeNameReverseMapping.get(inputs[0]), inputs[1]);
		else if (inputs.length == 1) userInputs.put(nodeNameReverseMapping.get(inputs[0]), "[Clear]");
		//changes ends for CR101
	}
	
	public String getNodeInput() {
		//old before CR101
		/*if (!currentFeature.equals("")) return currentFeature + ":" + userInputs.get(currentFeature);*/
		//one line change for CR101
		if (!currentFeature.equals("")) return currentFeature + ":" + userInputs.get(nodeNameReverseMapping.get(currentFeature));
		else return "";
	}
	
	public String getNodeInputString() {
		if (userInputs.size() == 0) return "None so far.";
		StringBuffer sb = new StringBuffer("");
		Set<String> s = userInputs.keySet();
		Iterator<String> it = s.iterator();
		sb.append("<table>");
		while (it.hasNext()) {
			String key = it.next();
			if (!userInputs.get(key).equals("[Clear]")) sb.append("<tr><td>").append(key).append("<td>").append(userInputs.get(key)).append("</tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}
	
	public String currentFeature(String cf) {
		currentFeature = cf;
		return "";
	}
	
	public String nodeInputHandler() {
	    return("index");  // return to index or refresh index
	}
	public String resetHandler() {
		userInputs.clear();
	    return("index");  // return to index or refresh index
	}
	
	public String getTestNodes() {
		System.out.println("Called getTestNodes()");
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i].equals("Diseases")) continue;
			sb.append("<p class='node-title'>" + nodes[i] + "</p>");
			sb.append("<table>");
			Map<String, Double> values = dw.getNodeProbs(nodes[i]);
			Set<String> s = values.keySet();
			Iterator<String> it = s.iterator();
			while (it.hasNext()) {
				String key = it.next();
				sb.append("<tr><td>" + key).append("<td>" + convertToPercentage(values.get(key))).append("%</tr>");
			}
			sb.append("</table>");
		}
		return sb.toString();
	}

	public List<String> getSelectNetworkInputs() {
		return networkFileList;
	}
	
	//commented for CR101
/*	public List<String> getSelectMenuFeatures() {
		List<String> features = new ArrayList<String>();
		
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i].equals("Diseases")) continue;
			features.add(nodes[i]);
		}		
		return features;
	}*/

	public String currentFeatureValue(String nodeName) {
		return userInputs.get(nodeName);
	}
	public String featureClass(String nodeName) {
		//old before CR101
		/*if (userInputs.containsKey(nodeName) && !userInputs.get(nodeName).equals("[Clear]")) return "hasChoice";*/
		//one line change for CR101
		//commented for CR102
		/*if (userInputs.containsKey(nodeNameReverseMapping.get(nodeName)) && !userInputs.get(nodeNameReverseMapping.get(nodeName)).equals("[Clear]")) return "hasChoice";*/
		if (userInputs.containsKey(nodeNameReverseMapping.get(nodeName)) 
				&& !userInputs.get(nodeNameReverseMapping.get(nodeName)).equals("[Clear]"))
		{
			return "hasChoice";
		}		
		//changes starts for CR102,CR103
		else if((nodeNameReverseMapping.get(nodeName).equals(highestSISensitiveNodeName) ||
				nodeNameReverseMapping.get(nodeName).equals(highestSPSensitiveNodeName) ||
				nodeNameReverseMapping.get(nodeName).equals(highestCLSensitiveNodeName) ||
				nodeNameReverseMapping.get(nodeName).equals(highestMSSensitiveNodeName)) && (this.isSensityvityOn())){
			return "sensitive";
		}
		//changes ends for CR102,CR103		
		else return "";
		
		
	}

	public List<String> selectMenuInputs(String nodeName) {
		List<String> returnString = new ArrayList<String>();
		//old before CR101
		/*Map<String, Double> values = dw.getNodeProbs(nodeName);*/
		//one line change for CR101
		Map<String, Double> values = dw.getNodeProbs(nodeNameReverseMapping.get(nodeName));
		Set<String> s = values.keySet();
		Iterator<String> it = s.iterator();
		returnString.add(nodeName);
		while (it.hasNext()) {
			String key = it.next();
			returnString.add(nodeName + ":" + key);
		}			
		//returnString.add(nodeName + ":[Clear]");
	
		return returnString;
	}
	
	public void updateDiagnosisNode() {
		Set<String> toRemove = new TreeSet<String>();
		Set<String> s = userInputs.keySet();
		Iterator<String> it = s.iterator();
		while (it.hasNext()) {
			String key = it.next();
			String response = userInputs.get(key);
			if (response.equals("[Clear]")) {
				toRemove.add(key);				
				dw.clearNodeState(key);
			}
			else dw.setNodeState(key, response);
		}
		
		it = toRemove.iterator();
		while (it.hasNext()) {
			userInputs.remove(it.next());
		}
	}
	
	public String getDiagnosisNode() {
		// Update the diagnosis node first
		updateDiagnosisNode();
		Set<String> s = userInputs.keySet();
		Iterator<String> it = s.iterator();
		
		// Then produce the node output
		
		StringBuffer sb = new StringBuffer("");
		sb.append("<table id='diagnosistable'><tr><td>Diagnosis</td><td>Probability (%)</td></tr>");
		Map<String, Double> values = sortByValue(dw.getDiagnosisProbs(), -1);
		s = values.keySet();
		it = s.iterator();
		int count = 0;
		while (it.hasNext() && ++count <= topDdx) {
			String key = it.next();
			String diag = key.replaceAll("_",  " ");
			sb.append("<tr><td>" + diag).append("<td>").append(convertToPercentage(values.get(key))).append("</tr>");
		}			
		sb.append("</table>");
		return sb.toString();
	}

	public String getParetoTable() {
		// Update the diagnosis node first
		updateDiagnosisNode();
		Set<String> s = userInputs.keySet();
		Iterator<String> it = s.iterator();
		
		// Then produce the node output
		
		StringBuffer sb = new StringBuffer("");
		sb.append("<table id='paretotable'><tr><td>Top Diagnoses</td><td>Probability (%)<td>Cumulative Probability (%)</td></tr>");
		Map<String, Double> values = sortByValue(dw.getDiagnosisProbs(), -1);
		s = values.keySet();
		it = s.iterator();
		int count = 0;
		double cumulative = 0;
		while (it.hasNext() && ++count <= topDdx) {
			String key = it.next();
			cumulative += values.get(key);
			sb.append("<tr><td>" + count).append("<td>").append(convertToPercentage(values.get(key))).append("<td>").append(convertToPercentage(cumulative)).append("</tr>");
		}			
		sb.append("</table>");
		return sb.toString();
	}

	public void setPrePageLoad(String pl) {
		this.pageLoad = pl;
	}
	
	public String getPrePageLoad() {
	    System.out.println("DNET Wrapper session started");
		try {
			dw = new DNETWrapper(PATH + "/" + networkName);
			nodes = dw.getNodeNames();
			//changes starts for CR101			
			processNodePrefixes();
			//changes ends for CR101
			
			
			//changes starts for CR102,CR103
			highestSISensitiveNodeName = dw.getHighestSISensitiveNodeName(userInputs);
			highestSPSensitiveNodeName = dw.getHighestSPSensitiveNodeName(userInputs);
			highestCLSensitiveNodeName = dw.getHighestCLSensitiveNodeName(userInputs);
			highestMSSensitiveNodeName = dw.getHighestMSSensitiveNodeName(userInputs);
			//changes ends for CR102,CR103			
			
			Arrays.sort(nodes);
		} catch (NetworkLoadingException e) {
			System.out.println ("Error loading the network.");
			
		} catch (Exception e) {
			System.out.println("Error converting filename.");
		}
		return this.pageLoad;
	}

	public void setPostPageLoad(String pl) {
		this.pageLoad = pl;
	}
	
	public String getPostPageLoad() {
	    dw.endSession();
	    System.out.println("DNET Wrapper session ended");
		return this.pageLoad;
	}
	
	public static double convertToPercentage(Double d) {
		double db = d.doubleValue();
		return Math.round(db * 1000)/10d;
	}
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map, int direction) {
		final int dir = direction;
	    List<Map.Entry<K, V>> list =
	        new LinkedList<Map.Entry<K, V>>( map.entrySet() );
	    Collections.sort( list, new Comparator<Map.Entry<K, V>>() {
	            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 ) {
	            return (o1.getValue()).compareTo( o2.getValue() ) * dir;
	            }
	            } );

	    Map<K, V> result = new LinkedHashMap<K, V>();
	    for (Map.Entry<K, V> entry : list) {
	        result.put( entry.getKey(), entry.getValue() );
	    }
	    return result;
	}



	public void valueBound(HttpSessionBindingEvent event) {
	    System.out.println("valueBound:" + event.getName() + " session:" + event.getSession().getId() );
	}

	public void registerSession() {
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put( "sessionBindingListener", this  );
		System.out.println( "registered sessionBindingListener"  );
	}

	public void valueUnbound(HttpSessionBindingEvent event) {
		System.out.println("valueUnBound:" + event.getName() + " session:" + event.getSession().getId() );
	}

class Feature {
	private String featureName;
	private String featureValue;
	public Feature(String name, String value) {
		featureName = name;
		featureValue = value;
	}
	public String getName() {
		return featureName;
	}
	public String getValue() {
		return featureValue;
	}
	
}
	
	
}
