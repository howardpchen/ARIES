package com.howardpchen.aries;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;

import com.howardpchen.aries.dao.UserDAO;
import com.howardpchen.aries.model.CaseList;
import com.howardpchen.aries.model.Network;
import com.howardpchen.aries.model.User;
import com.howardpchen.aries.model.UserCaseInput;
import com.howardpchen.aries.network.DNETWrapper;
import com.howardpchen.aries.network.NetworkLoadingException;
import com.howardpchen.aries.network.NetworkWrapper;

@ManagedBean
@SessionScoped
public class ServerModel {
	/**
	 * 
	 */
	// private static final long serialVersionUID = -9026586621419425189L;

	private final String PATH = "/mnt/networks";
	/*
	 * private final String PATH = "/home/shalini/Desktop/ARIESNEW/networks/";
	 */
	private Map<String, String> userInputs;
	private Map<String, String> userInputs1;
	//private Map<String, String> highestProbsMap;
	private NetworkWrapper dw;
	private String pageLoad = "";
	private int educationCaseNo ;
	private String networkName = "";
	private int topDdx = 10;
	private List<String> networkFileList = new ArrayList<String>();
	private String[] nodes = new String[0];
	private String currentFeature = "";
	private String currentDisease = "";
	private String event="";
	private String featureFlag = "false"; 
	private String qcFlag = "false"; 
	private String neticaLoad = "false";
	private String[] titlesNew = new String[0]; 
	Map<String, Map<String, Double>> valuesNew = new HashMap<String, Map<String, Double>>();
    private List<String> errorMessages;
    private List<String> infoMessages;
    private List<String> researchErrorMessages;
    private List<String> educationErrorMessages = new ArrayList<String>();
    private String researchErrMsg;
    private String educationErrMsg;
    private String errorMsg="";
    private int caseid;
    private CaseList caseList;
    private int caseNo;
    private int caseNoforQC;
    private String caseNoforQCSetter;   
	private String reviewCase="false";
    private String qcperson;
    private String fromQcPage = "false";
    Map<String, Map<String, Double>> newMap = new HashMap<String, Map<String, Double>>();
    Map<String, Double> newValuesMap = new HashMap<String,Double>();
    private String fromGraph ="false";
    
    public String getCaseNoforQCSetter() {
		return caseNoforQCSetter;
	}

	public void setCaseNoforQCSetter(String caseNoforQCSetter) {
		String[] cast;
		int val = 0;
		if(!("").equalsIgnoreCase(caseNoforQCSetter) && caseNoforQCSetter!=null ){
			cast = caseNoforQCSetter.split("-");
			val= Integer.parseInt(cast[0]); 
		}
		this.caseNoforQC = val; 
		this.caseNoforQCSetter = caseNoforQCSetter;
	}

    
    //For Research page
    private int randomCaseNo;


	public int getRandomCaseNo() {
		return randomCaseNo;
	}

	public void setRandomCaseNo(int randomCaseNo) {
		this.randomCaseNo = randomCaseNo;
	}

	public String getFromQcPage() {
		return fromQcPage;
	}

	public void setFromQcPage(String fromQcPage) {
		this.fromQcPage = fromQcPage;
	}

	public CaseList getCaseList() {
		return caseList;
	}

	public void setCaseList(CaseList caseList) {
		this.caseList = caseList;
	}

	public int getCaseid() {
		int caseid = 0 ;
		if(this.getCaseList() != null)
		caseid=UserDAO.getCaseId(this.getCaseList());
		return caseid;
	}

	/*public void setCaseid(int caseid) {
		this.caseid = caseid;
	}
*/


	public String getNeticaLoad() {
		return neticaLoad;
	}



	public void setNeticaLoad(String neticaLoad) {
		this.neticaLoad = neticaLoad;
	}



	public String getFeatureFlag() {
		return featureFlag;
	}



	public void setFeatureFlag(String featureFlag) {
		this.featureFlag = featureFlag;
	}



	public String getEvent() {
		return event;
	}

	

	public void setEvent(String event) {
		this.event = event;
	}

	// changes starts for CR102,CR103
	private String highestSISensitiveNodeName = "";
	private String highestSPSensitiveNodeName = "";
	private String highestCLSensitiveNodeName = "";
	private String highestMSSensitiveNodeName = "";
	// changes ends for CR102,CR103
	private String SensitiveForDisease = "";

	// changes starts for CR101
	private Map<String, String> nodeNameDirectMapping = new HashMap<String, String>();
	private Map<String, String> nodeNameReverseMapping = new HashMap<String, String>();
	private TreeMap<String, List<String>> prefixNodeListMapping = new TreeMap<String, List<String>>();
	private ArrayList<String> networkPrefixList = new ArrayList<String>();
	private Map<String, String> prefixNameMapping = new HashMap<String, String>();
	// changes ends for CR101

	// changes starts for CR103
	private boolean sensityvityOn = false;

	public boolean isSensityvityOn() {
		return this.sensityvityOn;
	}

	public void setSensityvityOn(boolean sensityvityOn) {
		this.sensityvityOn = sensityvityOn;
	}

	// CR104
	private String option;
	private String accession;
	private String organization;
	private String description;
	private String modality;
	private String patientId;
	private String nwName;
	private String correctDx;
	private String correctDxText;
	private String age;
	private String gender;
	private List<String> supportingDataList;
    private List<String> correctDxList;
    private String firstDx;
   	private String secondDx;
    private String thirdDx;
    
    //For Research Page
    private String nwNameforResearch;
    //For Education Page
    private String nwNameforEducation;
    //For QC
    private String nwNameforQC;
    public String getNwNameforQC() {
		return nwNameforQC;
	}

	public void setNwNameforQC(String nwNameforQC) {
		this.nwNameforQC = nwNameforQC;
	}

	//
	public List<String> getCorrectDxList() {
		return correctDxList;
	}

	public void setCorrectDxList(List<String> correctDxList) {
		this.correctDxList = correctDxList;
	}
	//For Clinical
	private String disease;

	public ServerModel() {
		userInputs = new HashMap<String, String>();
		userInputs1 = new HashMap<String, String>();
		// changes starts for CR101
		populateNetworkList();
		// changes ends for CR101
		registerSession();

		File folder = new File(PATH);
		File[] listOfFiles = folder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.toLowerCase().endsWith(".dne")) {
					System.out.println("Found " + name);
					return true;
				} else
					return false;
			}
		});
		for (int i = 0; i < listOfFiles.length; i++) {
			networkFileList.add(listOfFiles[i].getName());
		}
		networkName = networkFileList.get(0);
		System.out.println("networkName" + networkName);
		System.out.println("Called constructor.");

	}

	// changes starts for CR101

	private void populateNetworkList() {
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

	public List<String> getNetworkPrefixList() {
		return networkPrefixList;
	}

	public String getNetworkPrefixName(String prefix) {
		return prefixNameMapping.get(prefix);
	}

	/**
	 * CR103 starts here
	 * 
	 * @param prefix
	 * @return
	 */
	public boolean getFeaturesRendered(String prefix) {
		
		for (Map.Entry<String, List<String>> entry : prefixNodeListMapping.entrySet()) {
			if (entry.getKey().equals(prefix)) {
				if (prefix.equals("MS") && entry.getValue().size() == 1) {
					return false;
				} else
					return true;
			}
		}
		
		return false;

	}
	public boolean getNetworkNodeSize(String prefix) {
		for (Map.Entry<String, List<String>> entry : prefixNodeListMapping.entrySet()) {
			if (entry.getKey().equals(prefix)) {
				if (prefix.equals("MS") && entry.getValue().size() == 1) {
					return false;
				} else
					return true;
			}
		}
		return false;

	}
	// CR103 Ends here

	public List<String> getSelectMenuFeatures(String prefix) {
		List<String> features = new ArrayList<String>();
		List<String> prefixNodeList = prefixNodeListMapping.get(prefix);

		if (prefixNodeList != null) {
			for (String nodeName : prefixNodeList) {
				if (nodeName.equals("Diseases"))
					continue;
				features.add(nodeName);
			}
		}

		return features;
	}

	private void processNodePrefixes() {
		nodeNameDirectMapping = new HashMap<String, String>();
		nodeNameReverseMapping = new HashMap<String, String>();
		prefixNodeListMapping = new TreeMap<String, List<String>>();

		for (int i = 0; i < nodes.length; i++) {
			/* if (oldNodeArray[i].equals("Diseases")) continue; */

			String nodeNameWithPrefix = nodes[i];
			String nodeNameWithoutPrefix = nodes[i];
			String[] stringArray = nodeNameWithPrefix.split("_");
			String prefix = stringArray[0];

			if (networkPrefixList.contains(prefix)) {
				nodeNameWithoutPrefix = nodeNameWithPrefix.replace(prefix + "_", "");
			} else {
				prefix = "MS";
			}

			nodeNameDirectMapping.put(nodeNameWithPrefix, nodeNameWithoutPrefix);
			nodeNameReverseMapping.put(nodeNameWithoutPrefix, nodeNameWithPrefix);

			List<String> prefixNodeList = prefixNodeListMapping.get(prefix);
			if (prefixNodeList == null) {
				prefixNodeList = new ArrayList<String>();
			}
			prefixNodeList.add(nodeNameWithoutPrefix);
			prefixNodeListMapping.put(prefix, prefixNodeList);
		}

		for (Map.Entry<String, List<String>> entry : prefixNodeListMapping.entrySet()) {
			List<String> prefixNodeList = entry.getValue();
			java.util.Collections.sort(prefixNodeList);
		}
	}

	// changes ends for CR101

	public void setNetworkInput(String s) {
		if (!s.equals(networkName)) {
			System.out.println("Cleared user inputs.");
			userInputs.clear();
			infoMessages = new ArrayList<String>();
		}
		networkName = s;
	}

	public String getNetworkInput() {
		return networkName;
	}
    public void setFeatureValue(String s){
    	boolean clearflag = false;
    	String[] inputs = s.split(":");
		//if(inputs.length == 1)return;
		if(userInputs.containsKey(nodeNameReverseMapping.get(inputs[0])))
			{
			if(inputs.length == 2){
			if((userInputs.get(nodeNameReverseMapping.get(inputs[0]))).equals(inputs[1]))
			return;
			}
			if(inputs.length == 1){
				if((userInputs.get(nodeNameReverseMapping.get(inputs[0]))).equals("clear"))
					return;
				clearflag = true;
			}
			}
	
		if(this.getEvent().equalsIgnoreCase("FEATURE ENTERED") && ((inputs.length == 2) || clearflag == true )){
			User user;
			UserCaseInput caseInput = new UserCaseInput();
			caseList = new CaseList();
			HttpSession session = Util.getSession();
			String username = null;
			String password = null;
			if(session.getAttribute("username") != null)
			username = session.getAttribute("username").toString();
			if(session.getAttribute("password") != null)
			password = session.getAttribute("password").toString();
			int userid = UserDAO.getUserID(username, password);
			
			caseInput.setUserid(userid);
			
			caseInput.setSessionid(session.getId());
			
			caseList.setAccession(this.getAccession());
			caseList.setOrganization(this.getOrganization());
			caseList.setPatientid(this.getPatientId());
			String networkcode = UserDAO.getCode(this.getNwName());
			caseList.setNetwork(networkcode);
			int caseid = UserDAO.getCaseId(caseList);
			caseInput.setCaseid(caseid);
			caseInput.setEventid(1001);
			//String code = UserDAO.getCode(this.getNwName());
			
			
			//s="";
			if (inputs.length == 2){
				caseInput.setValue("["+networkcode+"]"+" "+inputs[0]+"="+inputs[1]);
				userInputs.put(nodeNameReverseMapping.get(inputs[0]), inputs[1]);
		}
			else if(clearflag == true){
				caseInput.setValue("["+networkcode+"]"+" "+inputs[0]+"="+"[Clear]");
				userInputs.put(nodeNameReverseMapping.get(inputs[0]), "[Clear]");
			}
			UserDAO.SaveFeature(caseInput);
    }
    }
    public String getFeatureValue() {
		// old before CR101
		/*
		 * if (!currentFeature.equals("")) return currentFeature + ":" +
		 * userInputs.get(currentFeature);
		 */
		// one line change for CR101
		if (!currentFeature.equals(""))
			return currentFeature + ":" + userInputs.get(nodeNameReverseMapping.get(currentFeature));
		else
			return "";
	}
	public void setResearchNodeInput(String s) {
		String[] inputs = s.split(":");
		// old before CR101
		/*
		 * if (inputs.length == 2) userInputs.put(inputs[0], inputs[1]); else if
		 * (inputs.length == 1) userInputs.put(inputs[0], "[Clear]");
		 */

		// changes starts for CR101
		/*if (inputs.length == 2)
			userInputs.put(nodeNameReverseMapping.get(inputs[0]), inputs[1]);
		else if (inputs.length == 1)
			userInputs.put(nodeNameReverseMapping.get(inputs[0]), "[Clear]");*/
		// changes ends for CR101
		
	}

	public String getResearchNodeInput() {
		// old before CR101
		/*
		 * if (!currentFeature.equals("")) return currentFeature + ":" +
		 * userInputs.get(currentFeature);
		 */
		// one line change for CR101
		if (!currentFeature.equals(""))
			return currentFeature + ":" + userInputs.get(nodeNameReverseMapping.get(currentFeature));
		else
			return "";
	}
	public void setEducationNodeInput(String s) {
		String[] inputs = s.split(":");
		// old before CR101
		/*
		 * if (inputs.length == 2) userInputs.put(inputs[0], inputs[1]); else if
		 * (inputs.length == 1) userInputs.put(inputs[0], "[Clear]");
		 */

		// changes starts for CR101
		/*if (inputs.length == 2)
			userInputs.put(nodeNameReverseMapping.get(inputs[0]), inputs[1]);
		else if (inputs.length == 1)
			userInputs.put(nodeNameReverseMapping.get(inputs[0]), "[Clear]");*/
		// changes ends for CR101
		
	}

	public String getEducationNodeInput() {
		// old before CR101
		/*
		 * if (!currentFeature.equals("")) return currentFeature + ":" +
		 * userInputs.get(currentFeature);
		 */
		// one line change for CR101
		if (!currentFeature.equals(""))
			return currentFeature + ":" + userInputs.get(nodeNameReverseMapping.get(currentFeature));
		else
			return "";
	}
	public void setNodeInput(String s) {
		String[] inputs = s.split(":");
		// old before CR101
		/*
		 * if (inputs.length == 2) userInputs.put(inputs[0], inputs[1]); else if
		 * (inputs.length == 1) userInputs.put(inputs[0], "[Clear]");
		 */

		// changes starts for CR101
		
		if (inputs.length == 2)
			userInputs.put(nodeNameReverseMapping.get(inputs[0]), inputs[1]);
		else if (inputs.length == 1)
			userInputs.put(nodeNameReverseMapping.get(inputs[0]), "[Clear]");
	
	
		// changes ends for CR101
		
	}

	public String getNodeInput() {
		// old before CR101
		/*
		 * if (!currentFeature.equals("")) return currentFeature + ":" +
		 * userInputs.get(currentFeature);
		 */
		// one line change for CR101
		if (!currentFeature.equals(""))
			return currentFeature + ":" + userInputs.get(nodeNameReverseMapping.get(currentFeature));
		else
			return "";
	}

	public String getNodeInputString() {
		if (userInputs.size() == 0)
			return "None so far.";
		StringBuffer sb = new StringBuffer("");
		Set<String> s = userInputs.keySet();
		Iterator<String> it = s.iterator();
		sb.append("<table>");
		while (it.hasNext()) {
			String key = it.next();
			if (!userInputs.get(key).equals("[Clear]"))
				sb.append("<tr><td>").append(key).append("<td>").append(userInputs.get(key)).append("</tr>");
		}		sb.append("</table>");
		return sb.toString();
	}

	public String currentFeature(String cf) {
		currentFeature = cf;
		return "";
	}

	public String nodeInputHandler() {
		return ("index"); // return to index or refresh index
	}

	public String resetHandler() {
		userInputs.clear();
		infoMessages = new ArrayList<String>();
		return ("index"); // return to index or refresh index
	}

	@SuppressWarnings("rawtypes")
	public String[] getDiseaseTitles() {
		//String[] titles = null;
		/*System.out.println("this.getNwName() : " + this.getNwName());
		if (this.getNwName() != null && (!"-select-".equalsIgnoreCase(this.getNwName()))
				&& (!"".equalsIgnoreCase(this.getNwName())) && dw != null) {
			//String fileName = UserDAO.getFileName(this.getNwName());
			try {
				//dw = new DNETWrapper(PATH + "/" + fileName);
			//	nodes = dw.getNodeNames();
				for (int i = 0; i < nodes.length; i++) {
					if (nodes[i].equals("Diseases")) {
						titles = dw.getStates(nodes[i]);
					}
				}

				//dw.endSession();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("Coming hereeee");
				e.printStackTrace();
			}}
*/
		
		return this.titlesNew;

	}

	public String getTestNodes() {
		System.out.println("Called getTestNodes()");
		StringBuffer sb = new StringBuffer("");
			    
	    //List<String> newList = new ArrayList<String>();
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i].equals("Diseases"))
				continue;
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

	// commented for CR101
	/*
	 * public List<String> getSelectMenuFeatures() { List<String> features = new
	 * ArrayList<String>();
	 * 
	 * for (int i = 0; i < nodes.length; i++) { if (nodes[i].equals("Diseases"))
	 * continue; features.add(nodes[i]); } return features; }
	 */

	public String currentFeatureValue(String nodeName) {
		return userInputs.get(nodeName);
	}

	public String featureClass(String nodeName) {
		// old before CR101
		/*
		 * if (userInputs.containsKey(nodeName) &&
		 * !userInputs.get(nodeName).equals("[Clear]")) return "hasChoice";
		 */
		// one line change for CR101
		// commented for CR102
		/*
		 * if (userInputs.containsKey(nodeNameReverseMapping.get(nodeName)) &&
		 * !userInputs.get(nodeNameReverseMapping.get(nodeName)).equals(
		 * "[Clear]")) return "hasChoice";
		 */
		if (userInputs.containsKey(nodeNameReverseMapping.get(nodeName))
				&& !userInputs.get(nodeNameReverseMapping.get(nodeName)).equals("[Clear]")) {
			return "hasChoice";
		}
		
		// changes starts for CR102,CR103
		else if ((nodeNameReverseMapping.get(nodeName).equals(highestSISensitiveNodeName)
				|| nodeNameReverseMapping.get(nodeName).equals(highestSPSensitiveNodeName)
				|| nodeNameReverseMapping.get(nodeName).equals(highestCLSensitiveNodeName)
				|| nodeNameReverseMapping.get(nodeName).equals(highestMSSensitiveNodeName))
				&& (this.isSensityvityOn())) {
			return "sensitive";
		}
		/*if((nodeNameReverseMapping.get(nodeName).equals(SensitiveForDisease)))
				return "sensitive";*/
		// changes ends for CR102,CR103
		else
			return "";

	}

	public List<String> selectMenuInputs(String nodeName) {
		List<String> returnString = new ArrayList<String>();
		// old before CR101
		/* Map<String, Double> values = dw.getNodeProbs(nodeName); */
		// one line cha for CR101
		double highest = 0.0;
		Map<String, Double> values = dw.getNodeProbs(nodeNameReverseMapping.get(nodeName));
		Set<String> s = values.keySet();
		Iterator<String> it = s.iterator();
		returnString.add(nodeName);
		while (it.hasNext()) {
			String key = it.next();
			returnString.add(nodeName + ":" + key);
		}
		// returnString.add(nodeName + ":[Clear]");
		//System.out.println("newMap"+newMap);
		return returnString;
	}
	
	public List<String> selectMenuInputsFromCase(String nodeName) {
		List<String> returnString = new ArrayList<String>();
		// old before CR101
		/* Map<String, Double> values = dw.getNodeProbs(nodeName); */
		// one line change for CR101
		if(this.getFeatureFlag().equalsIgnoreCase("true")){
		 
			Map<String, Double> values = this.valuesNew.get(nodeName);//dw.getNodeProbs(nodeNameReverseMapping.get(nodeName));
			Set<String> s = values.keySet();
			Iterator<String> it = s.iterator(); 
			returnString.add(nodeName);
			while (it.hasNext()) {
				String key = it.next();
				returnString.add(nodeName + ":" + key);
			}
			// returnString.add(nodeName + ":[Clear]");
		}
		return returnString;
	}
	public List<String> selectMenuInputsForResearch(String nodeName) {
		List<String> returnString = new ArrayList<String>();
		// old before CR101
		/* Map<String, Double> values = dw.getNodeProbs(nodeName); */
		// one line change for CR101
	
		 
			Map<String, Double> values = this.valuesNew.get(nodeName);//dw.getNodeProbs(nodeNameReverseMapping.get(nodeName));
			if(values != null){
			Set<String> s = values.keySet();
			Iterator<String> it = s.iterator(); 
			returnString.add(nodeName);
			while (it.hasNext()) {
				String key = it.next();
				returnString.add(nodeName + ":" + key);
			}
			}
			// returnString.add(nodeName + ":[Clear]");
			return returnString;
}
	public List<String> selectMenuInputsForEducation(String nodeName) {
		List<String> returnString = new ArrayList<String>();
		// old before CR101
		/* Map<String, Double> values = dw.getNodeProbs(nodeName); */
		// one line change for CR101
	
		 
			Map<String, Double> values = this.valuesNew.get(nodeName);//dw.getNodeProbs(nodeNameReverseMapping.get(nodeName));
			if(values != null){
			Set<String> s = values.keySet();
			Iterator<String> it = s.iterator(); 
			returnString.add(nodeName);
			while (it.hasNext()) {
				String key = it.next();
				returnString.add(nodeName + ":" + key);
			}
			}
			// returnString.add(nodeName + ":[Clear]");
			return returnString;
}
	
	public void updateDiagnosisNode1() {
		Set<String> toRemove = new TreeSet<String>();
		Set<String> s = userInputs1.keySet();
		Iterator<String> it = s.iterator();
		while (it.hasNext()) {
			String key = it.next();
			String response = userInputs1.get(key);
			
			if (response.equals("[Clear]")) {
				toRemove.add(key);
				dw.clearNodeState(key);
			} else
				dw.setNodeState(key, response);
			
		}

		it = toRemove.iterator();
		while (it.hasNext()) {
			userInputs1.remove(it.next());
		}
	}
	public void updateDiagnosisNode() {
		Set<String> toRemove = new TreeSet<String>();
		Set<String> s = userInputs.keySet();
		if( fromGraph.equalsIgnoreCase("true") && userInputs.containsKey("Diseases")){
			userInputs.remove("Diseases");
			dw.clearNodeState("Diseases");
		}
		Iterator<String> it = s.iterator();
		while (it.hasNext()) {
			String key = it.next();
			String response = userInputs.get(key);
			
			if (response.equals("[Clear]")) {
				toRemove.add(key);
				dw.clearNodeState(key);
			} else
				dw.setNodeState(key, response);
			
		}

		it = toRemove.iterator();
		while (it.hasNext()) {
			userInputs.remove(it.next());
		}
	}
	
	
	public void updateRadioDiagnosisNode() {
		Set<String> toRemove = new TreeSet<String>();
		Set<String> s = userInputs.keySet();
		Iterator<String> it = s.iterator();
		while (it.hasNext()) {
			String key = it.next();
			String response = userInputs.get(key);
			if(key!=null){
			if(key.startsWith("CL_")){ 
				response ="[Clear]";
			}
			}
			if (response.equals("[Clear]")) {
				toRemove.add(key);
				dw.clearNodeState(key);
			} else
				dw.setNodeState(key, response);
			
		}

		it = toRemove.iterator();
		while (it.hasNext()) {
			userInputs.remove(it.next());
		}
	}
	
    public String getRadiographicDiag(){
    	// Update the diagnosis node first
    	 updateRadioDiagnosisNode();
    			Set<String> s = userInputs.keySet();
    			Iterator<String> it = s.iterator();

    			// Then produce the node output

    			StringBuffer sb = new StringBuffer("");
    			sb.append("<table id='radiodiagnosistable'><tr><td>Diagnosis</td><td>Probability (%)</td></tr>");
    			Map<String, Double> values = sortByValue(dw.getRadioDiagnosisProbs(), -1);
    			s = values.keySet();
    			int noOfKeys = s.size();
    			//double diagpercent = 100/noOfKeys;
    			it = s.iterator();
    			int count = 0;
    			while (it.hasNext() && ++count <= topDdx) {
    				String key = it.next();
    				String diag = key.replaceAll("_", " ");
    				if(userInputs.keySet().size() == 0){
        				sb.append("<tr><td>" + diag).append("<td>").append((100/noOfKeys)).append("</tr>");
    				}else{
    				sb.append("<tr><td>" + diag).append("<td>").append(convertToPercentage(values.get(key))).append("</tr>");
    				}
    				
    			}
    			sb.append("</table>");
    			return sb.toString();
    }
	public String getDiagnosisNode() {
		// Update the diagnosis node first
		fromGraph = "true";
		updateDiagnosisNode();
		Set<String> s = userInputs.keySet();
		Iterator<String> it = s.iterator();

		// Then produce the node output

		StringBuffer sb = new StringBuffer("");
		sb.append("<table id='diagnosistable1'><tr><td>Diagnosis</td><td>Probability (%)</td></tr>");
		Map<String, Double> values = sortByValue(dw.getDiagnosisProbs(), -1);
	
		if(values != null){
		s = values.keySet();
		it = s.iterator();
		int count = 0;
		while (it.hasNext() && ++count <= topDdx) {
			String key = it.next();
			String diag = key.replaceAll("_", " ");
			sb.append("<tr><td>" + diag).append("<td>").append(convertToPercentage(values.get(key))).append("</tr>");
		}
		sb.append("</table>");
		}
		
		return sb.toString();
	}

	public String getParetoTable() {
		// Update the diagnosis node first
		updateDiagnosisNode();
		Set<String> s = userInputs.keySet();
		Iterator<String> it = s.iterator();

		// Then produce the node output

		StringBuffer sb = new StringBuffer("");
		sb.append(
				"<table id='paretotable'><tr><td>Top Diagnoses</td><td>Probability (%)<td>Cumulative Probability (%)</td></tr>");
		Map<String, Double> values = sortByValue(dw.getDiagnosisProbs(), -1);
		
		s = values.keySet();
		it = s.iterator();
		int count = 0;
		double cumulative = 0;
		while (it.hasNext() && ++count <= topDdx) {
			String key = it.next();
			cumulative += values.get(key);
			sb.append("<tr><td>" + count).append("<td>").append(convertToPercentage(values.get(key))).append("<td>")
					.append(convertToPercentage(cumulative)).append("</tr>");
		}
		sb.append("</table>");
		
		return sb.toString();
	}

	public void setQCPrePageLoad(String pl) {
		this.pageLoad = pl;
	}
	public void getQCPrePageLoad(ValueChangeEvent event){
		System.out.println("DNET Wrapper session started");
		this.setEvent("FEATURE ENTERED");
		this.setFeatureFlag("false");
		this.setQcFlag("true");
		String newValue= event.getNewValue().toString();
		String oldValue = "";
		if(event.getOldValue()!= null){
			oldValue = event.getOldValue().toString();
		}
		if(newValue!= null && !newValue.equalsIgnoreCase("-select-") && !newValue.equalsIgnoreCase(oldValue)){
		try {
			
			Map<String, Double> values = new HashMap<String, Double>();
			Map<String, Map<String, Double>> valuesNode = new HashMap<String, Map<String, Double>>();
			
			networkName = UserDAO.getFileName(newValue);
			dw = new DNETWrapper(PATH + "/" + networkName);
			nodes = dw.getNodeNames();
			for (int i = 0; i < nodes.length; i++) {
				if (nodes[i].equals("Diseases")) {
					this.titlesNew = dw.getStates(nodes[i]);
				}
			}
			
			processNodePrefixes(); 
			
			for(String prefix :this.getNetworkPrefixList()) {
				
				List<String> features  = getSelectMenuFeatures(prefix);
				for(String menu : features){	
					values = dw.getNodeProbs(nodeNameReverseMapping.get(menu));
					valuesNode.put(menu, values);
				}
			}
			this.valuesNew = valuesNode; 
			
			Arrays.sort(nodes);
			
			
				
			}  
		 catch (NetworkLoadingException e) {
			System.out.println("Error loading the network.");

		} catch (Exception e) {
			System.out.println("Error converting filename.");
		}
		}
		//return this.pageLoad;
	}public void setPrePageLoad1(String pl) {
		this.pageLoad = pl;
	}
	
	public void getPrePageLoad1(ValueChangeEvent event) {
		System.out.println("DNET Wrapper session started");
		this.setEvent("FEATURE ENTERED");
		this.setFeatureFlag("false");
		String newValue= event.getNewValue().toString();
		String oldValue = "";
		if(event.getOldValue()!= null){
			oldValue = event.getOldValue().toString();
		}
		if(newValue!= null && !newValue.equalsIgnoreCase("-select-") && !newValue.equalsIgnoreCase(oldValue)){
		try {
			
			Map<String, Double> values = new HashMap<String, Double>();
			Map<String, Map<String, Double>> valuesNode = new HashMap<String, Map<String, Double>>();
			
			networkName = UserDAO.getFileName(newValue);
			dw = new DNETWrapper(PATH + "/" + networkName);
			nodes = dw.getNodeNames();
			for (int i = 0; i < nodes.length; i++) {
				if (nodes[i].equals("Diseases")) {
					this.titlesNew = dw.getStates(nodes[i]);
				}
			}
			
			processNodePrefixes(); 
			
			for(String prefix :this.getNetworkPrefixList()) {
				
				List<String> features  = getSelectMenuFeatures(prefix);
				for(String menu : features){	
					values = dw.getNodeProbs(nodeNameReverseMapping.get(menu));
					valuesNode.put(menu, values);
				}
			}
			this.valuesNew = valuesNode; 
			
			Arrays.sort(nodes);
			
			
				
			}  
		 catch (NetworkLoadingException e) {
			System.out.println("Error loading the network.");

		} catch (Exception e) {
			System.out.println("Error converting filename.");
		}
		}
		//return this.pageLoad;
	}
	
	public String getFeatureProb() {
				
	if(this.getDisease()!= null && !this.getDisease().equalsIgnoreCase("--select--")){
		
		try {
			dw = new DNETWrapper(PATH + "/" + networkName);
		} catch (NetworkLoadingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		nodes = dw.getNodeNames();
		fromGraph = "false";
		updateDiagnosisNode();
		double sihighest = 0.0;
		String sihighestkey = "" ; 
		String sinodeName = "" ;
		double sphighest = 0.0;
		String sphighestkey = "" ; 
		String spnodeName = "" ;
		double clhighest = 0.0;
		String clhighestkey = "" ; 
		String clnodeName = "" ;
		double mshighest = 0.0;
		String mshighestkey = "" ; 
		String msnodeName = "" ;
		Map<String, Map<String, Double>> newMap = new HashMap<String, Map<String, Double>>();
	    Map<String, Double> newValuesMap = new HashMap<String,Double>();
	    Map<String, Double> spNewValuesMap = new HashMap<String,Double>();
	    Map<String, Double> clNewValuesMap = new HashMap<String,Double>();
	    Map<String, Double> msNewValuesMap = new HashMap<String,Double>();
	    List <Double> siValuesList = new ArrayList<Double>();
	    List <Double> spValuesList = new ArrayList<Double>();
	    Map<String, String> highestProbsMap = new HashMap<String,String>();
	    List <Double> clValuesList = new ArrayList<Double>();
	    List <Double> msValuesList = new ArrayList<Double>();
	    infoMessages = new ArrayList<String>();
	    
	    //List<String> newList = new ArrayList<String>();
		for (int i = 0; i < nodes.length; i++) { 
			if (nodes[i].equals("Diseases"))
				continue;
			
			Map<String, Double> values = dw.getNodeProbs(nodes[i]);
			
			if(nodes[i].startsWith("SI_")){
				for(Entry<String, Double> entry : values.entrySet()) {
				    String key = entry.getKey();
				    double value = entry.getValue();
				    siValuesList.add(value);
				    if(value >= sihighest){
					sihighest = value;
					sihighestkey = key;
					sinodeName = nodes[i];				
				    }
			    }
			}
			if(nodes[i].startsWith("SP_")){
				for(Entry<String, Double> entry : values.entrySet()) {
				    String key= entry.getKey();
				    double value = entry.getValue();
				    spValuesList.add(value);
				    if(value >= sphighest){
					sphighest = value;
					sphighestkey = key;
					spnodeName = nodes[i];				
				    }
			    }
			}
			if(nodes[i].startsWith("CL_")){
				for(Entry<String, Double> entry : values.entrySet()) {
				    String key = entry.getKey();
				    double value = entry.getValue();
				    clValuesList.add(value);
				    if(value >= clhighest){
					clhighest = value;
					clhighestkey = key;
					clnodeName = nodes[i];				
				    }
			    }
			}
			if(!(nodes[i].startsWith("SI_")) && !(nodes[i].startsWith("SP_")) && !nodes[i].startsWith("CL_")){
				for(Entry<String, Double> entry : values.entrySet()) {
				    String key = entry.getKey();
				    double value = entry.getValue();
				    msValuesList.add(value);
				    if(value >= mshighest){
					mshighest = value;
					mshighestkey = key;
					msnodeName = nodes[i];				
				    }
			    }
			}
			
		}
		int counter = 0;
		for(Double siValue : siValuesList){
			if(siValue.equals(sihighest)){
				counter++;
			}
		}
		if(counter == 1){
			newValuesMap.put(sihighestkey, sihighest);
			newMap.put(sinodeName, newValuesMap);
			highestProbsMap.put(nodeNameDirectMapping.get(sinodeName), sihighestkey); 
			//setNodeInput(nodeName+":"+highestkey);
			
			/*if(userInputs.containsKey("Diseases")){
				userInputs.remove("Diseases");
			}*/
			//infoMessages.add("Higest Probability Features are "+highestProbsMap);
		}
		//For Sp
		int spcounter = 0;
		for(Double spValue : spValuesList){
			if(spValue.equals(sphighest)){
				spcounter++;
			}
		}
		if(spcounter == 1){
			spNewValuesMap.put(sphighestkey, sphighest);
			newMap.put(spnodeName, spNewValuesMap);
			highestProbsMap.put(nodeNameDirectMapping.get(spnodeName), sphighestkey); 
			//setNodeInput(nodeName+":"+highestkey);
			/*if(userInputs.containsKey("Diseases")){
				userInputs.remove("Diseases");
			}*/
			//infoMessages.add("Higest Probability Features are "+highestProbsMap);
		}
		//For CL
		int clcounter = 0;
		for(Double clValue : clValuesList){
			if(clValue.equals(clhighest)){
				clcounter++;
			}
		}
		if(clcounter == 1){
			clNewValuesMap.put(clhighestkey, clhighest);
			newMap.put(clnodeName, clNewValuesMap);
			highestProbsMap.put(nodeNameDirectMapping.get(clnodeName), clhighestkey); 
			//setNodeInput(nodeName+":"+highestkey);
			/*if(userInputs.containsKey("Diseases")){
				userInputs.remove("Diseases");
			}*/
			//infoMessages.add("Higest Probability Features are "+highestProbsMap);
		}
		//For MS
		int mscounter = 0;
		for(Double msValue : msValuesList){
			if(msValue.equals(mshighest)){
				mscounter++;
			}
		}
		if(mscounter == 1){
			msNewValuesMap.put(mshighestkey, mshighest);
			newMap.put(msnodeName, msNewValuesMap);
			highestProbsMap.put(nodeNameDirectMapping.get(msnodeName), mshighestkey); 
			//setNodeInput(nodeName+":"+highestkey);
			/*if(userInputs.containsKey("Diseases")){
				userInputs.remove("Diseases");
			}*/
			//infoMessages.add("Higest Probability Features are "+highestProbsMap);
		}
		if(highestProbsMap !=null && !highestProbsMap.isEmpty()){
		infoMessages.add("Higest Probability Features are "+highestProbsMap);
		}
		
	}
	
	return "";
		
	}
	
	
	
	public boolean FeaturesValidate(String inputs){
		
		this.setEducationErrMsg("");
		/*if(educationErrorMessages!=null && educationErrorMessages.size()>0)
			educationErrorMessages.clear();*/
		
		//educationErrorMessages = new ArrayList<String>();
		
		String[] values;
		List<String> featuresList = new ArrayList<String>();
		List<String> DiagList = new ArrayList<String>();
        
		List<UserCaseInput> userCaseInputList = new ArrayList<UserCaseInput>();
		userCaseInputList = UserDAO.getUserCaseInput(getEducationCaseNo());
		for(UserCaseInput userCaseInput : userCaseInputList){
			if(userCaseInput.getValue().contains("] ")){
			values = userCaseInput.getValue().split("] ");
			featuresList.add(values[1]);
			
			}
			else{
				values = userCaseInput.getValue().split(", ");
				for (String s : values) {
					DiagList.add(s);
				}
	
			}
			
		}
		if(inputs.contains(":")){
		inputs = inputs.replace(":","=");
		
		if(!featuresList.contains(inputs)){
			educationErrorMessages.add("Please Select "+featuresList+"features");
		}
		}
		if(educationErrorMessages != null && educationErrorMessages.size()>0){
			return true;
		}else 
			return false;
	}

	public void saveFeaturesforEducation(ValueChangeEvent event){
		//educationErrorMessages = new ArrayList<String>();
		String newValue=null;
		String[] inputs = null ;
		
		if(event.getNewValue() !=null){
			
			newValue = event.getNewValue().toString();
		    inputs = newValue.split(":");
		   
		}
		/* if(FeaturesValidate(newValue) == true){
				return;
			}*/
		//String oldValue = "";
	    
		String saveflag ="";
		if(newValue.contains(":")){
		String newValue1 =newValue.replace(":", "=");
		String code = UserDAO.getCode(this.getNwNameforEducation());
		
		String newValue2 = "["+code+"] "+newValue1;
	    boolean flag = UserDAO.checkFeatures(this.getEducationCaseNo(),newValue2);
		if(!flag){
			saveflag ="false";
			educationErrorMessages.add("Selected "+newValue+" is not there in DB for this case");
		}else{
			saveflag = "true";
		}
		}
		boolean clearflag = false;
		
		/*if(event.getOldValue()!= null){
			oldValue = event.getOldValue().toString();
		}*/
		
		if(userInputs.containsKey(nodeNameReverseMapping.get(inputs[0])))
		{
		if(inputs.length == 2){
		if((userInputs.get(nodeNameReverseMapping.get(inputs[0]))).equals(inputs[1]))
		return;
		}
		if(inputs.length == 1){
			if((userInputs.get(nodeNameReverseMapping.get(inputs[0]))).equals("clear"))
				return;
			clearflag = true;
		}
		}

	if((inputs.length == 2) || clearflag == true ){
		User user;
		UserCaseInput caseInput = new UserCaseInput();
		HttpSession session = Util.getSession();
		String username = null;
		String password = null;
		if(session.getAttribute("username") != null)
		username = session.getAttribute("username").toString();
		if(session.getAttribute("password") != null)
		password = session.getAttribute("password").toString();
		int userid = UserDAO.getUserID(username, password);
		
		caseInput.setUserid(userid);
		
		caseInput.setSessionid(session.getId());
		String networkcode = UserDAO.getCode(this.getNwNameforResearch());
		caseInput.setCaseid(this.getCaseNo());
		caseInput.setEventid(1001);
		String code = UserDAO.getCode(this.getNwNameforResearch());
		
		
		//s="";
		if (inputs.length == 2 && saveflag.equalsIgnoreCase("true")){
			caseInput.setValue("["+code+"]"+" "+inputs[0]+"="+inputs[1]);
			userInputs.put(nodeNameReverseMapping.get(inputs[0]), inputs[1]);
	}
		else if(clearflag == true){
			caseInput.setValue("["+code+"]"+" "+inputs[0]+"="+"[Clear]");
			userInputs.put(nodeNameReverseMapping.get(inputs[0]), "[Clear]");
		}
		if(saveflag.equalsIgnoreCase("true")){
		UserDAO.SaveFeature(caseInput);
		}
}
			}
	
	public void saveFeatures(ValueChangeEvent event){
		String newValue=null;
		String[] inputs = null ;
		if(event.getNewValue() !=null){
			newValue = event.getNewValue().toString();
		    inputs = newValue.split(":");
		}
		//String oldValue = "";
		boolean clearflag = false;
		
		/*if(event.getOldValue()!= null){
			oldValue = event.getOldValue().toString();
		}*/
		if(userInputs.containsKey(nodeNameReverseMapping.get(inputs[0])))
		{
		if(inputs.length == 2){
		if((userInputs.get(nodeNameReverseMapping.get(inputs[0]))).equals(inputs[1]))
		return;
		}
		if(inputs.length == 1){
			if((userInputs.get(nodeNameReverseMapping.get(inputs[0]))).equals("clear"))
				return;
			clearflag = true;
		}
		}

	if((inputs.length == 2) || clearflag == true ){
		User user;
		UserCaseInput caseInput = new UserCaseInput();
		HttpSession session = Util.getSession();
		String username = null;
		String password = null;
		if(session.getAttribute("username") != null)
		username = session.getAttribute("username").toString();
		if(session.getAttribute("password") != null)
		password = session.getAttribute("password").toString();
		int userid = UserDAO.getUserID(username, password);
		
		caseInput.setUserid(userid);
		
		caseInput.setSessionid(session.getId());
		String networkcode = UserDAO.getCode(this.getNwNameforResearch());
		caseInput.setCaseid(this.getCaseNo());
		caseInput.setEventid(1001);
		String code = UserDAO.getCode(this.getNwNameforResearch());
		
		
		//s="";
		if (inputs.length == 2){
			caseInput.setValue("["+code+"]"+" "+inputs[0]+"="+inputs[1]);
			userInputs.put(nodeNameReverseMapping.get(inputs[0]), inputs[1]);
	}
		else if(clearflag == true){
			caseInput.setValue("["+code+"]"+" "+inputs[0]+"="+"[Clear]");
			userInputs.put(nodeNameReverseMapping.get(inputs[0]), "[Clear]");
		}
		UserDAO.SaveFeature(caseInput);
}
	}
	public void researchPageLoad(ValueChangeEvent event) {
		System.out.println("DNET Wrapper session started");
		//this.setEvent("FEATURE ENTERED");
		//this.setFeatureFlag("false");
		String newValue= event.getNewValue().toString();
		String oldValue = "";
		if(event.getOldValue()!= null){
			oldValue = event.getOldValue().toString();
		}
		if(newValue!= null && !newValue.equalsIgnoreCase("-select-") && !newValue.equalsIgnoreCase(oldValue)){
		try {
			this.setRandomCaseNo(CaseNo(newValue));
			
			Map<String, Double> values = new HashMap<String, Double>();
			Map<String, Map<String, Double>> valuesNode = new HashMap<String, Map<String, Double>>();
			
			networkName = UserDAO.getFileName(newValue);
			dw = new DNETWrapper(PATH + "/" + networkName);
			nodes = dw.getNodeNames();
			for (int i = 0; i < nodes.length; i++) {
				if (nodes[i].equals("Diseases")) {
					this.titlesNew = dw.getStates(nodes[i]);
				}
			}
			
			processNodePrefixes(); 
			
			for(String prefix :this.getNetworkPrefixList()) {
				
				List<String> features  = getSelectMenuFeatures(prefix);
				for(String menu : features){	
					values = dw.getNodeProbs(nodeNameReverseMapping.get(menu));
					valuesNode.put(menu, values);
				}
			}
			this.valuesNew = valuesNode; 
			
			Arrays.sort(nodes);
			
			
				
			}  
		 catch (NetworkLoadingException e) {
			System.out.println("Error loading the network.");

		} catch (Exception e) {
			System.out.println("Error converting filename.");
		}
		}
		//return this.pageLoad;
	}
	public void educationPageLoad(ValueChangeEvent event) {
		System.out.println("DNET Wrapper session started");
		//this.setEvent("FEATURE ENTERED");
		//this.setFeatureFlag("false");
		String newValue= event.getNewValue().toString();
		String oldValue = "";
		if(event.getOldValue()!= null){
			oldValue = event.getOldValue().toString();
		}
		if(newValue!= null && !newValue.equalsIgnoreCase("-select-") && !newValue.equalsIgnoreCase(oldValue)){
		try {
			this.setEducationCaseNo(CaseNo(newValue));
			Map<String, Double> values = new HashMap<String, Double>();
			Map<String, Map<String, Double>> valuesNode = new HashMap<String, Map<String, Double>>();
			
			networkName = UserDAO.getFileName(newValue);
			dw = new DNETWrapper(PATH + "/" + networkName);
			nodes = dw.getNodeNames();
			for (int i = 0; i < nodes.length; i++) {
				if (nodes[i].equals("Diseases")) {
					this.titlesNew = dw.getStates(nodes[i]);
				}
			}
			
			processNodePrefixes(); 
			
			for(String prefix :this.getNetworkPrefixList()) {
				
				List<String> features  = getSelectMenuFeatures(prefix);
				for(String menu : features){	
					values = dw.getNodeProbs(nodeNameReverseMapping.get(menu));
					valuesNode.put(menu, values);
				}
			}
			this.valuesNew = valuesNode; 
			
			Arrays.sort(nodes);
			
			
				
			}  
		 catch (NetworkLoadingException e) {
			System.out.println("Error loading the network.");

		} catch (Exception e) {
			System.out.println("Error converting filename.");
		}
		}
		//return this.pageLoad;
	}
	public void setPrePageLoad(String pl) {
		this.pageLoad = pl;
	}

	public String getPrePageLoad() {
		System.out.println("DNET Wrapper session started");
		this.setEvent("");
		try {
			dw = new DNETWrapper(PATH + "/" + networkName);
			nodes = dw.getNodeNames();
			for (int i = 0; i < nodes.length; i++) {
				if (nodes[i].equals("Diseases")) {
					this.titlesNew = dw.getStates(nodes[i]);
				}
			}
			// changes starts for CR101
			processNodePrefixes();
			// changes ends for CR101

			// changes starts for CR102,CR103
			highestSISensitiveNodeName = dw.getHighestSISensitiveNodeName(userInputs);
			highestSPSensitiveNodeName = dw.getHighestSPSensitiveNodeName(userInputs);
			highestCLSensitiveNodeName = dw.getHighestCLSensitiveNodeName(userInputs);
			highestMSSensitiveNodeName = dw.getHighestMSSensitiveNodeName(userInputs);
			// changes ends for CR102,CR103
			 
			Arrays.sort(nodes);
		} catch (NetworkLoadingException e) {
			System.out.println("Error loading the network.");

		} catch (Exception e) {
			System.out.println("Error converting filename.");
		}
		return this.pageLoad;
	}

	public void setPostPageLoad(String pl) {
		this.pageLoad = pl;
	}
	
	public void setPostPageLoad1(String pl) {
		this.pageLoad = pl;
	}

	public String getPostPageLoad() {
		dw.endSession();
		System.out.println("DNET Wrapper session ended");
		return this.pageLoad;
	}
	public void setPostResearchPageLoad(String pl) {
		this.pageLoad = pl;
	}
	public String getPostResearchPageLoad() {
		if(this.getNwNameforEducation()!=null && !this.getNwNameforEducation().equalsIgnoreCase("-select-") && dw != null){
		dw.endSession();
		System.out.println("DNET Wrapper session ended");
		}
		return this.pageLoad;
	}
	public void setPostEducationPageLoad(String pl) {
		this.pageLoad = pl;
	}
	public String getPostEducationPageLoad() {
		if(this.getNwNameforEducation()!=null && !this.getNwNameforEducation().equalsIgnoreCase("-select-") && dw != null){
		dw.endSession();
		System.out.println("DNET Wrapper session ended");
		}
		return this.pageLoad;
	}

	public String getPostPageLoad1() {
		if(this.getNwName()!=null && !this.getNwName().equalsIgnoreCase("-select-") && dw != null){
		dw.endSession();
		System.out.println("DNET Wrapper session ended");
		}
		return this.pageLoad;
	}
	public static double convertToPercentage(Double d) {
		double db = d.doubleValue();
		return Math.round(db * 1000) / 10d;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, int direction) {
		final int dir = direction;
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue()) * dir;
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public void valueBound(HttpSessionBindingEvent event) {
		System.out.println("valueBound:" + event.getName() + " session:" + event.getSession().getId());
	}

	public void registerSession() {
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("sessionBindingListener", this);
		System.out.println("registered sessionBindingListener");
	}

	public void valueUnbound(HttpSessionBindingEvent event) {
		System.out.println("valueUnBound:" + event.getName() + " session:" + event.getSession().getId());
	}

	public String getOption() {
		return option;
	}

	public void setOption(String option) {
		this.option = option;
	}

	public String homePage() {
		return "login?faces-redirect=true";
	}

	public String getAccession() {
		return accession;
	}

	public void setAccession(String accession) {
		this.accession = accession;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getModality() {
		return modality;
	}

	public void setModality(String modality) {
		this.modality = modality;
	}

	public String getPatientId() {
		return patientId;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}

	public List<Network> networkList() {
		List<Network> networkList = null;
		networkList = UserDAO.getNetworkList();
		return networkList;
	}

	public List<String> getNetworkNameList() {
		List<Network> networkList;
		List<String> nwNameList = new ArrayList<String>();
		try {
			networkList = UserDAO.getNetworkList();
			for (Network network1 : networkList()) {
				nwNameList.add(network1.getDescription());
			}
		} catch (Exception e) {

		}
		return nwNameList;
	}

	public String getNwName() {
		return nwName;
	}

	public void setNwName(String nwName) {
		if(!nwName.equals(this.getNwName())){
		userInputs.clear();
		System.out.println("Clearing User Inputs");
		}
		this.nwName = nwName;
	}

	public String getCorrectDx() {
		return correctDx;
	}

	public void setCorrectDx(String correctDx) {
		this.correctDx = correctDx;
	}
	public boolean basicCaseValidate(){
		this.setErrorMsg("");
		if(errorMessages != null && errorMessages.size()>0)
			errorMessages.clear();
		errorMessages = new ArrayList<String>();
		if(this.getNwName().equals("") || "-select-".equalsIgnoreCase(this.getNwName())){
			errorMessages.add("Please select Network");
		}
		else if(this.getAccession().equals("")){
			errorMessages.add("Please enter Accession");
		}
		else if(this.getPatientId().equals("")){
			errorMessages.add("Please enter MR Number");
		}
		else if(this.getOrganization().equals("")){
			errorMessages.add("Please select Organization");
		}
		else if(this.getModality().equals("")){
			errorMessages.add("Please select Modality");
		}
		else if(this.getDescription().equals("")){
			errorMessages.add("Please select Supporting Data Details");
		}
		else if(this.getCorrectDx().equals("")){
			errorMessages.add("Please select Correct Diagnosis");
		}
		else if(this.getSupportingDataList().size()==0 || this.supportingDataList== null){
			errorMessages.add("Please enter Supporting Data");
		}
		else if(this.getAge().equals("")){
			errorMessages.add("Please enter age");
		}
		else if(this.getGender().equals("")){
			errorMessages.add("Please select Gender");
		}
		if(errorMessages.size()>0){
			return true;
		}else 
			return false;
	}

	public boolean ResearchCaseValidate(){
		this.setResearchErrMsg("");
		if(researchErrorMessages!=null && researchErrorMessages.size()>0)
		researchErrorMessages.clear();
		researchErrorMessages = new ArrayList<String>();
		if(this.getNwNameforResearch().equals("") || "-select-".equalsIgnoreCase(this.getNwNameforResearch())){
			researchErrorMessages.add("Please select Network");
		}
		/*else if(this.getCorrectDxList()!= null && this.getCorrectDxList().size()== 0){
			researchErrorMessages.add("Please enter Correct Diagnosis");
		}*/
		else if(this.getFirstDx() == null || "".equals(this.getFirstDx())){
			researchErrorMessages.add("Please select First Diagnosis");
		}
		else if(this.getSecondDx() == null || "".equals(this.getSecondDx())){
			researchErrorMessages.add("Please select Second Diagnosis");
		}
		else if(this.getThirdDx() == null || "".equals(this.getThirdDx())){
			researchErrorMessages.add("Please select Third Diagnosis");
		}
		else if(this.getFirstDx().equals(this.secondDx) || this.getFirstDx().equals(this.thirdDx)){
			researchErrorMessages.add("Diagnosis Entered twice.Please Select different diagnosis");
		}
		else if(this.getSecondDx().equals(this.firstDx) || this.getSecondDx().equals(this.thirdDx)){
			researchErrorMessages.add("Diagnosis Entered twice.Please Select different diagnosis");
		}
		else if(this.getThirdDx().equals(this.firstDx) || this.getThirdDx().equals(this.secondDx)){
			researchErrorMessages.add("Diagnosis Entered twice.Please Select different diagnosis");
		}
		
		
		if(researchErrorMessages.size()>0){
			return true;
		}else 
			return false;
	}

	public boolean EducationCaseValidate(){
		this.setResearchErrMsg("");
		if(educationErrorMessages!=null && educationErrorMessages.size()>0)
			educationErrorMessages.clear();
		educationErrorMessages = new ArrayList<String>();
		String[] values = new String[0];
		List<UserCaseInput> userCaseInputList = new ArrayList<UserCaseInput>();
		userCaseInputList = UserDAO.getUserCaseInput(getEducationCaseNo());
		List<String> DiagList = new ArrayList<String>();
		for(UserCaseInput userCaseInput : userCaseInputList){
			if(userCaseInput.getValue().contains("] ")){
			//values = userCaseInput.getValue().split("] ");
			//featuresList.add(values[1]);
			
			}
			else{
				values = userCaseInput.getValue().split(", ");
				for (String s : values) {
					DiagList.add(s);
				}
	
			}
			
		}
	/*	if(this.getNwNameforEducation().equals("") || "-select-".equalsIgnoreCase(this.getNwNameforEducation())){
			educationErrorMessages.add("Please select Network");
		}*/
/*		else if(this.getCorrectDxList()!= null && this.getCorrectDxList().size()== 0){
			researchErrorMessages.add("Please enter Correct Diagnosis");
		}
		else if(this.getFirstDx() == null || "".equals(this.getFirstDx())){
			educationErrorMessages.add("Please select First Diagnosis");
		}
		else if(this.getSecondDx() == null || "".equals(this.getSecondDx())){
			educationErrorMessages.add("Please select Second Diagnosis");
		}
		else if(this.getThirdDx() == null || "".equals(this.getThirdDx())){
			educationErrorMessages.add("Please select Third Diagnosis");
		}
		else if(this.getFirstDx().equals(this.secondDx) || this.getFirstDx().equals(this.thirdDx)){
			educationErrorMessages.add("Diagnosis Entered twice.Please Select different diagnosis");
		}
		else if(this.getSecondDx().equals(this.firstDx) || this.getSecondDx().equals(this.thirdDx)){
			educationErrorMessages.add("Diagnosis Entered twice.Please Select different diagnosis");
		}
		else if(this.getThirdDx().equals(this.firstDx) || this.getThirdDx().equals(this.secondDx)){
			educationErrorMessages.add("Diagnosis Entered twice.Please Select different diagnosis");
		}*/
		if(DiagList.size() == 3){
		if(!DiagList.get(0).equals(this.firstDx)){
			educationErrorMessages.add("Please enter "+DiagList.get(0)+" as a first diagnosis");
		}
		else if(!DiagList.get(1).equals(this.secondDx)){
			educationErrorMessages.add("Please enter "+DiagList.get(0)+" as a second diagnosis");
		}
		else if(!DiagList.get(2).equals(this.thirdDx)){
			educationErrorMessages.add("Please enter "+DiagList.get(0)+" as a Third diagnosis");
		}
		}
		if(educationErrorMessages.size()>0){
			return true;
		}else 
			return false;
	}

	public String save() {
		String networkcode = null;
		User user ;
		if(basicCaseValidate() == true){
			return null;
		}
		if(this.getReviewCase().equalsIgnoreCase("false") && this.getFromQcPage().equalsIgnoreCase("false")){
		try {
			networkcode = UserDAO.getCode(this.getNwName());
			CaseList caseList = new CaseList();
			caseList.setAccession(this.getAccession());
			caseList.setOrganization(this.getOrganization());
			caseList.setNetwork(networkcode);
			caseList.setModality(this.getModality());
			caseList.setDescription(this.getDescription());
			if ("Other".equalsIgnoreCase(this.getCorrectDx())) {
				caseList.setCorrectDx(this.getCorrectDxText());
			} else {
				caseList.setCorrectDx(this.getCorrectDx());
			}
			caseList.setPatientid(this.getPatientId());
			caseList.setAge(this.getAge());
			caseList.setGender(this.getGender());
			caseList.setSubmittedDate(new Date());
			
			//caseList.setSubmittedBy(user.getUserid());
			String withoutLastComma = null;
			if(this.getSupportingDataList().size() > 0){
			StringBuffer sb = new StringBuffer();
			for (String supportData : this.getSupportingDataList()) {
				sb.append(supportData).append(", ");
			}
			withoutLastComma = sb.substring(0, sb.length() - ", ".length());
			}
			caseList.setSupportingData(withoutLastComma);
			HttpSession session = Util.getSession();
			String username = null;
			String password = null;
			if(session.getAttribute("username") != null){
			username = session.getAttribute("username").toString();
			}
			if(session.getAttribute("password") != null){
			password = session.getAttribute("password").toString();
			}
			int userid = UserDAO.getUserID(username, password);
			caseList.setSubmittedBy(userid);
			caseList.setQcperson(this.getQcperson());
			this.setCaseList(caseList);
			if (caseListValidation()) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Combination of Organization,MR Number,Network Already exists", ""));
			} else {
				boolean success = UserDAO.SaveCaseList(caseList);
				if(success){
				this.featureFlag = "true";
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Save Successful!", ""));
				}
				else{
					
					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage(FacesMessage.SEVERITY_INFO, "Save is not Successful!", ""));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		}
		else{
			if(this.getReviewCase().equalsIgnoreCase("true")){
			try {
				networkcode = UserDAO.getCode(this.getNwName());
				CaseList caseList = this.getCaseList();
				caseList.setCaseid(this.getCaseid());
				caseList.setAccession(this.getAccession());
				caseList.setOrganization(this.getOrganization());
				caseList.setNetwork(networkcode);
				caseList.setModality(this.getModality());
				caseList.setDescription(this.getDescription());
				if ("Other".equalsIgnoreCase(this.getCorrectDx())) {
					caseList.setCorrectDx(this.getCorrectDxText());
				} else {
					caseList.setCorrectDx(this.getCorrectDx());
				}
				caseList.setPatientid(this.getPatientId());
				caseList.setAge(this.getAge());
				caseList.setGender(this.getGender());
				caseList.setSubmittedDate(new Date());
				
				//caseList.setSubmittedBy(user.getUserid());
				String withoutLastComma = null;
				if(this.getSupportingDataList().size() > 0){
				StringBuffer sb = new StringBuffer();
				for (String supportData : this.getSupportingDataList()) {
					sb.append(supportData).append(", ");
				}
				withoutLastComma = sb.substring(0, sb.length() - ", ".length());
				}
				caseList.setSupportingData(withoutLastComma);
				HttpSession session = Util.getSession();
				String username = null;
				String password = null;
				if(session.getAttribute("username") != null){
				username = session.getAttribute("username").toString();
				}
				if(session.getAttribute("password") != null){
				password = session.getAttribute("password").toString();
				}
				int userid = UserDAO.getUserID(username, password);
				caseList.setSubmittedBy(userid);
				caseList.setQcperson(this.getQcperson());
				this.setCaseList(caseList);
				/*if (this.getCaseid() != 0 && caseListValidation()) {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
							"Combination of Organization,MR Number,Network Already exists", ""));
				} else {*/
				boolean success = UserDAO.UpdateCaseList(caseList);
				if(success){
				   this.featureFlag = "true";
				   this.setReviewCase("false");
				   FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage(FacesMessage.SEVERITY_INFO, "Case Updated Successfully!", ""));
				   }
				   else{
						
						FacesContext.getCurrentInstance().addMessage(null,
								new FacesMessage(FacesMessage.SEVERITY_INFO, "Case Update is not Successful!", ""));
					}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(this.getFromQcPage().equalsIgnoreCase("true")){
			try {
				networkcode = UserDAO.getCode(this.getNwName());
				//CaseList caseList = this.getCaseList();
				CaseList caselist = new CaseList();
				caselist.setCaseid(this.getCaseNoforQC());
				caselist.setAccession(this.getAccession());
				caselist.setOrganization(this.getOrganization());
				caselist.setNetwork(networkcode);
				caselist.setModality(this.getModality());
				caselist.setDescription(this.getDescription());
				if ("Other".equalsIgnoreCase(this.getCorrectDx())) {
					caselist.setCorrectDx(this.getCorrectDxText());
				} else {
					caselist.setCorrectDx(this.getCorrectDx());
				}
				caselist.setPatientid(this.getPatientId());
				caselist.setAge(this.getAge());
				caselist.setGender(this.getGender());
				caselist.setSubmittedDate(new Date());
				
				//caseList.setSubmittedBy(user.getUserid());
				String withoutLastComma = null;
				if(this.getSupportingDataList().size() > 0){
				StringBuffer sb = new StringBuffer();
				for (String supportData : this.getSupportingDataList()) {
					sb.append(supportData).append(", ");
				}
				withoutLastComma = sb.substring(0, sb.length() - ", ".length());
				}
				caselist.setSupportingData(withoutLastComma);
				HttpSession session = Util.getSession();
				String username = null;
				String password = null;
				if(session.getAttribute("username") != null){
				username = session.getAttribute("username").toString();
				}
				if(session.getAttribute("password") != null){
				password = session.getAttribute("password").toString();
				}
				int userid = UserDAO.getUserID(username, password);
				caselist.setSubmittedBy(userid);
				caselist.setQcperson(this.getQcperson());
				this.setCaseList(caselist);
				/*if (this.getCaseid() != 0 && caseListValidation()) {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
							"Combination of Organization,MR Number,Network Already exists", ""));
				} else {*/
				String[] input = new String[0];
				String[] val = new String[0];
				List<String> newList = new ArrayList<String>();
				boolean success = UserDAO.QCCaseList(caselist);
				List<UserCaseInput> list = new ArrayList<UserCaseInput>();
				 list = UserDAO.getUserCaseInput(caseNoforQC);
				 for(UserCaseInput userCaseInput:list){
					 input = userCaseInput.getValue().split("] ");
					 if(!input[1].contains("[Clear]")){
					 newList.add(input[1]);
					 }
					// val = input[1];
					// for(String s : input){
						// newList.add(s);
					// }
					
					// values.add(input[1]);
					//val = input[1].split("=");
					 
					 //userInputs.put(nodeNameReverseMapping.get(val[0]),val[1]);
				 }
				infoMessages = new ArrayList<String>();
				if(newList.size()>0){
				infoMessages.add("selected features are "+newList.toString());
				}
				 
				if(success){
				   this.featureFlag = "true";
				   //this.setReviewCase("false");
				   this.setFromQcPage("false");
				   FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage(FacesMessage.SEVERITY_INFO, "Case Updated Successfully!", ""));
				   }
				   else{
						
						FacesContext.getCurrentInstance().addMessage(null,
								new FacesMessage(FacesMessage.SEVERITY_INFO, "Case Update is not Successful!", ""));
					}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		}
		return "";
	}
	public String selectionDone(){
		this.setFeatureFlag("false");
		userInputs.clear();
		clear();
		//return "login?faces-redirect=true";
		return "";
	}
	public String qcSelectionDone(){
		this.setFeatureFlag("false");
		UserDAO.UpdateCaseList("Yes",this.getCaseNoforQC());
		userInputs.clear();
		this.setQcFlag("false");
		clear();
		//return "login?faces-redirect=true";
		return "";
	}
	public String deleteCase(){
		UserDAO.deleteCaseList(this.getCaseNoforQC());
		this.setFeatureFlag("false");
		userInputs.clear();
		clear();
		this.setQcperson("");
		return "";
	}
    public String backtoLogin(){
		 return "login?faces-redirect=true";
	}
	public String educationCompleted(){
		this.setResearchErrMsg("");
		if(EducationCaseValidate() == true){
			return null;
		}
		HttpSession session = Util.getSession();
		try{
		UserCaseInput userCaseInput = new UserCaseInput();
		userCaseInput.setEventid(1002);
		userCaseInput.setCaseid(this.getCaseNo());
		String withoutLastComma ="";
		StringBuffer sb = new StringBuffer();
		/*if(this.getCorrectDxList() != null & this.getCorrectDxList().size() >0){
		for(String CorrectDx:correctDxList){
			sb.append(CorrectDx).append(", ");
			}
			withoutLastComma = sb.substring(0, sb.length() - ", ".length());
		}
		userCaseInput.setValue(withoutLastComma);*/
		sb.append(this.getFirstDx()+", "+this.getSecondDx()+", "+this.getThirdDx());
		userCaseInput.setValue(sb.toString());
		String username =null; 
		String password = null;
		if(session.getAttribute("username")!= null)
		username = session.getAttribute("username").toString();
		if(session.getAttribute("password")!= null)
		password = session.getAttribute("password").toString();
		int userid = UserDAO.getUserID(username, password);
		
		userCaseInput.setUserid(userid);
		
		userCaseInput.setSessionid(session.getId());
		UserDAO.SaveFeature(userCaseInput);
		String nwcode = UserDAO.getCode(this.getNwNameforEducation());
        UserDAO.UpdateCaseList(nwcode,this.getCaseNo(),"Yes");
		}catch(Exception e){
			e.printStackTrace();
		}
		userInputs.clear();
		this.setNwNameforEducation("-select-");
		this.setCorrectDxList(new ArrayList<String>());
	    //session.invalidate();
		return "";
		
	}

	public String actionCompleted(){
		this.setResearchErrMsg("");
		if(ResearchCaseValidate() == true){
			return null;
		}
		HttpSession session = Util.getSession();
		try{
		UserCaseInput userCaseInput = new UserCaseInput();
		userCaseInput.setEventid(1002);
		userCaseInput.setCaseid(this.getCaseNo());
		String withoutLastComma ="";
		StringBuffer sb = new StringBuffer();
		/*if(this.getCorrectDxList() != null & this.getCorrectDxList().size() >0){
		for(String CorrectDx:correctDxList){
			sb.append(CorrectDx).append(", ");
			}
			withoutLastComma = sb.substring(0, sb.length() - ", ".length());
		}
		userCaseInput.setValue(withoutLastComma);*/
		sb.append(this.getFirstDx()+", "+this.getSecondDx()+", "+this.getThirdDx());
		userCaseInput.setValue(sb.toString());
		String username =null; 
		String password = null;
		if(session.getAttribute("username")!= null)
		username = session.getAttribute("username").toString();
		if(session.getAttribute("password")!= null)
		password = session.getAttribute("password").toString();
		int userid = UserDAO.getUserID(username, password);
		
		userCaseInput.setUserid(userid);
		
		userCaseInput.setSessionid(session.getId());
		UserDAO.SaveFeature(userCaseInput);
		String nwcode = UserDAO.getCode(this.getNwNameforResearch());
        UserDAO.UpdateCaseList(nwcode,this.getCaseNo(),"Yes");
		}catch(Exception e){
			e.printStackTrace();
		}
		userInputs.clear();
		this.setNwNameforResearch("-select-");
		this.setCorrectDxList(new ArrayList<String>());
	    session.invalidate();
		return "";
		
	}

	public boolean caseListValidation() {
		boolean flag = false;
		List<CaseList> caselists = null;
		caselists = UserDAO.getCaseList();
		String nwcode = UserDAO.getCode(this.getNwName());
		for (CaseList caselist : caselists) {
			if ((this.getOrganization().equalsIgnoreCase(caselist.getOrganization()))
					&& (this.getPatientId().equalsIgnoreCase(caselist.getPatientid()))
					&& (nwcode.equalsIgnoreCase(caselist.getNetwork()))) {
				flag = true;
				continue;

			}
		}
		return flag;
	}
	public String saveUserCaseInput(){
		System.out.println("cominggg");
		return "";
		
	}
	public String clear() {
		if(errorMessages != null && errorMessages.size() >0)
			errorMessages.clear();
		this.setAccession("");
		this.setOrganization("");
		this.setDescription("");
		this.setModality("");
		this.setCorrectDx("");
		this.setPatientId("");
		this.setNwName("");
		this.setCorrectDx("");
		this.setCorrectDxText("");
		this.setAge("");
		this.setGender("");
		//this.setQcperson("");
		this.setSupportingDataList(new ArrayList<String>());
		this.setQcFlag("false");
		return "";
	}
	
	public String getReviewCaseList(){
    	this.setFeatureFlag("false");
    	this.setReviewCase("true");
      	return "caseInput_form?faces-redirect=true";
    }
	public String getReviewQCCaseList(){
    	this.setFeatureFlag("false");
    	this.setReviewCase("true");
      	return "qualityControl?faces-redirect=true";
    }
	
    public String clearInput(){
    	this.setNwNameforResearch("");
    	//this.getRandomCaseNo();
    	return "";
    }
	public String getRenderInputText() {
		String rendered = "N";
		if (("Other").equalsIgnoreCase(this.getCorrectDx())) {
			rendered = "Y";
		}
		return rendered;
	}

	public String getCorrectDxText() {
		return correctDxText;
	}

	public void setCorrectDxText(String correctDxText) {
		this.correctDxText = correctDxText;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public List<String> getSupportingDataList() {
		return supportingDataList;
	}

	public void setSupportingDataList(List<String> supportingDataList) {
		this.supportingDataList = supportingDataList;
	}

	public String getErrorMsg() {
		String errMsg;
		if(errorMessages == null || errorMessages.size() == 0){
			errMsg ="";
		}else{
			errMsg = "<FONT COLOR=RED><B><UL>\n";
			for(String message: errorMessages) {
				errMsg = errMsg + "<LI>" + message + "\n";
			}
			errMsg = errMsg + "</UL></B></FONT>\n";
			}
		return errMsg;
	}
	public String getInfoMsg() {
		String infoMsg;
		if(infoMessages == null || infoMessages.size() == 0){
			infoMsg ="";
		}else{
			infoMsg = "<FONT COLOR=BLUE><B><UL>\n";
			for(String message: infoMessages) {
				infoMsg = infoMsg + "<LI>" + message + "\n";
			}
			infoMsg = infoMsg + "</UL></B></FONT>\n";
			}
		return infoMsg;
	}
	public String getResearchErrMsg() {
		String errMsg;
		if(researchErrorMessages == null || researchErrorMessages.size() == 0){
			errMsg ="";
		}else{
			errMsg = "<FONT COLOR=RED><B><UL>\n";
			for(String message: researchErrorMessages) {
				errMsg = errMsg + "<LI>" + message + "\n";
			}
			errMsg = errMsg + "</UL></B></FONT>\n";
			}
		return errMsg;
	}
	public void setResearchErrMsg(String errorMsg) {
		this.researchErrMsg = errorMsg;
	}

    public void getSaveCorrectDxList(ValueChangeEvent event){
    	System.out.println("Event Value"+event.getNewValue());
    }
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public int getCaseNo() {
		return caseNo;
	}

	public void setCaseNo(int caseNo) {
		this.caseNo = caseNo;
	}
	
	public String getReviewCase() {
		return reviewCase;
	}

	public void setReviewCase(String reviewCase) {
		this.reviewCase = reviewCase;
	}


	public String getNwNameforResearch() {
		return nwNameforResearch;
	}

	public void setNwNameforResearch(String nwNameforResearch) {
		
		if(nwNameforResearch != null && !nwNameforResearch.equalsIgnoreCase(this.getNwNameforResearch())){
			this.setErrorMsg("");
			userInputs.clear();
			System.out.println("Clearing User Inputs");
			}
		this.nwNameforResearch = nwNameforResearch;
	}

	public String getDisease() {
		return currentDisease;
	}
	public void getDiseaseAction(ValueChangeEvent event){
		String newValue= event.getNewValue().toString();
		String oldValue = "";
		if(event.getOldValue()!= null){
			oldValue = event.getOldValue().toString();
		}
		if(!userInputs.isEmpty())
			userInputs.clear();
		
		if(newValue!= null && !newValue.equalsIgnoreCase("-select-") && !newValue.equalsIgnoreCase(oldValue)){
			userInputs.put("Diseases", newValue);
		}
		//updateDiagnosisNode1();
		//Map<String, Double> values = sortByValue(dw.getDiagnosisProbs(), -1);
	}
	public void setDisease(String disease) {
		boolean flag = false;
		if (!disease.equals(currentDisease)) {
			flag = true;
		}
		
		currentDisease = disease;
		if(flag){
			getFeatureProb();
		}
		
		
	}
	 public String getFirstDx() {
			return firstDx;
		}

		public void setFirstDx(String firstDx) {
			this.firstDx = firstDx;
		}

		public String getSecondDx() {
			return secondDx;
		}

		public void setSecondDx(String secondDx) {
			this.secondDx = secondDx;
		}

		public String getThirdDx() {
			return thirdDx;
		}

		public void setThirdDx(String thirdDx) {
			this.thirdDx = thirdDx;
		}

	public int CaseNo(String nwName) {
		int randomCaseNo = 0;
		String code = null;
		if(nwName != null && !"-select-".equalsIgnoreCase(nwName)){
			code= UserDAO.getCode(nwName);
			randomCaseNo = UserDAO.getCaseId(code);
		}
		return randomCaseNo;
	}
	public String getAccessionNo(){
		String accessionNo ="";
	    int caseid = this.getEducationCaseNo();
		accessionNo = UserDAO.getAccessionNo(caseid);
		return accessionNo;
	}
	public String getRsrchAccessionNo(){
		String accessionNo ="";
	    int caseid = this.getRandomCaseNo();
		accessionNo = UserDAO.getAccessionNo(caseid);
		return accessionNo;
	}
	 public List<String> getUsersList(){
		 List<String> users = new ArrayList<String>();
		 
		 //users = UserDAO.getUserName();
		 if(this.getNwName() != null){
		 if(this.getNwName().equalsIgnoreCase("Neuro - Basal Ganglia")){
			 users.add("manuel");
		 }
		 else if(this.getNwName().equalsIgnoreCase("Chest - Lung Disease")){
			 users.add("manuel1");
		 }
		 else if(this.getNwName().equalsIgnoreCase("Body - Kidney")){
			 users.add("manuel2");
		 }
		 else if(this.getNwName().equalsIgnoreCase("MSK - Bone Lesions")){
			 users.add("manuel3");
		 }
		 else if(this.getNwName().equalsIgnoreCase("Neuro - Validation Trial")){
			 users.add("manuel4");
		 }
		 }
		return users;
		 
	 }
	 public List<String> getSelectCaseList(){
	    	
	    	List<String> caseListforNw = new ArrayList<String>();
	    	String code = null;
	    	String username = null;
	    	HttpSession session = Util.getSession();
	    	if(session.getAttribute("username") != null)
				username = session.getAttribute("username").toString();
				//if(session.getAttribute("password") != null) 
				//password = session.getAttribute("password").toString();
				//int userid = UserDAO.getUserID(username, password);
				
			if(this.getNwNameforQC() != null && !"-select-".equalsIgnoreCase(this.getNwNameforQC())){
	    	code= UserDAO.getCode(this.getNwNameforQC());
	    	caseListforNw.addAll(UserDAO.getCaseIdforQC(code,username));
	    	}
			return caseListforNw;
	    	
	    }
	 
	 public String doQC(){
		 //String nwcode = UserDAO.getCode(this.getNwNameforQC());
	       // UserDAO.UpdateCaseList(this.getCaseNoforQC(),nwcode,"Yes");
		 this.setFromQcPage("true");
		 
		 CaseList caselist = new CaseList();
		// this.setCase(caselist.getCaseid());
		 
		 caselist = UserDAO.getCaseList(caseNoforQC);
		 if(caselist != null){
		 String nwname = UserDAO.getNwName(caselist.getNetwork());
		 this.setNwName(nwname);
		 this.setOrganization(caselist.getOrganization());
		 this.setModality(caselist.getModality());
		 this.setAccession(caselist.getAccession());
		 this.setPatientId(caselist.getPatientid());
		 this.setAge(caselist.getAge());
		 this.setGender(caselist.getGender());
		 this.setDescription(caselist.getDescription());
		 this.setCorrectDx(caselist.getCorrectDx());
		 //System.out.println("caselist.getCorrectDx() :"+this.getCorrectDx());
		 this.setSupportingDataList(caselist.getSupportingDataList());
		 this.setQcperson(caselist.getQcperson());
		// UserCaseInput usercaseinput = new UserCaseInput();
		 this.setQcFlag("true");
		 String[] input = new String[0];
		// List<String> values = new ArrayList<String>();
		/* List<UserCaseInput> list = new ArrayList<UserCaseInput>();
		 list = UserDAO.getUserCaseInput(caseNoforQC);
		 for(UserCaseInput userCaseInput:list){
			 input = userCaseInput.getValue().split("] ");
			// values.add(input[1]);
			 String[] val = input[1].split("=");
			 userInputs.put(nodeNameReverseMapping.get(val[0]),val[1]);
		 }*/
		 } 
		 return "";
	 }

	public int getCaseNoforQC() {
		return caseNoforQC;
	}

	public void setCaseNoforQC(int caseNoforQC) {
		this.caseNoforQC = caseNoforQC;
	}

	public String getQcperson() {
		return qcperson;
	}

	public void setQcperson(String qcperson) {
		this.qcperson = qcperson;
	}

	public String getNwNameforEducation() {
		return nwNameforEducation;
	}

	public void setNwNameforEducation(String nwNameforEducation) {
		this.nwNameforEducation = nwNameforEducation;
	}

	public String getEducationErrMsg() {
		String errMsg;
		if(educationErrorMessages == null || educationErrorMessages.size() == 0){
			errMsg ="";
		}else{
			errMsg = "<FONT COLOR=RED><B><UL>\n";
			for(String message: educationErrorMessages) {
				errMsg = errMsg + "<LI>" + message + "\n";
			}
			errMsg = errMsg + "</UL></B></FONT>\n";
			}
		System.out.println("Err"+errMsg);
		return errMsg;
	}

	public void setEducationErrMsg(String educationErrMsg) {
		this.educationErrMsg = educationErrMsg;
	}

	public int getEducationCaseNo() {
		return educationCaseNo;
	}

	public void setEducationCaseNo(int educationCaseNo) {
		this.educationCaseNo = educationCaseNo;
	}
	public String getQcFlag() {
		return qcFlag;
	}

	public void setQcFlag(String qcFlag) {
		this.qcFlag = qcFlag;
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
