package com.howardpchen.aries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.SQLException;
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
import com.howardpchen.aries.dao.Database;

import java.sql.DriverManager;

/**
 * Primary bean for handling all user inputs on all pages
 */
@ManagedBean
@SessionScoped
public class ServerModel {
	/**
	 * 
	 */
	// private static final long serialVersionUID = -9026586621419425189L;
 
	private final String PATH = "/mnt/networks";
	private final String NETWORKDESCRIPTIONS = "/mnt/networks/AriesNetworks.csv";
	
	//private final String PATH = System.getenv("ARIES_NETWORK_PATH");
	
	boolean debugMode = true;
	
	/**
	 * Features set by user
	 */
	private Map<String, String> userInputs;
	
	private Map<String, String> userInputs1;
	private Map<String, String> userInputs2;
	
	/**
	 * "Correct" features for case being viewed in Education page
	 */
	private Map<String, String> dbFeatures;
	
	/**
	 * Features set by user on QC page
	 */
	private Map<String, String> userInputsForQc;
	
	/**
	 * Map from network code to username for assigned QC 
	 */
	private Map<String, String> qcMap;
	
	/**
	 * User inputs for Research and Education page
	 */
	//private Map<String, String> userInputsForRs;
	
	/**
	 * Most likely Features for a given disease
	 */
	private Map<String, String> probInputs;
	
	private Map<String, String> userInputsCase;
	private Map<String, String> probInputs1;
	
	/* =============================================================
	 * Common to all pages 
	 * ============================================================= */
	
	/**
	 * Descriptive name of currently selected network
	 */
	private String activeNetwork = ""; 
	
	/**
	 * Is the network being changed
	 */
	private boolean changingActiveNetwork=false;
	
	/**
	 * Name of active subpage
	 */
	private enum PageType { HOME, CLINICAL, CASE, RESEARCH, EDUCATION, QC };
	private PageType activePage = PageType.CLINICAL;
	
	public String menuClass( String pageName ) {
		String className = "inactive";
		
		if ( (activePage==PageType.HOME) && pageName.equalsIgnoreCase("home") ) {
			className = "active";
		}
		else if ( (activePage==PageType.CLINICAL) && pageName.equalsIgnoreCase("clinical") ) {
			className = "active";
		}
		else if ( (activePage==PageType.CASE) && pageName.equalsIgnoreCase("case") ) {
			className = "active";
		}
		else if ( (activePage==PageType.RESEARCH) && pageName.equalsIgnoreCase("research") ) {
			className = "active";
		}
		else if ( (activePage==PageType.EDUCATION) && pageName.equalsIgnoreCase("education") ) {
			className = "active";
		}
		else if ( (activePage==PageType.QC) && pageName.equalsIgnoreCase("qc") ) {
			className = "active";
		}
		
		return className;
	}
	
	
	/**
	 * List of descriptive names for available networks
	 */
	private List<String> availableNetworks = new ArrayList<String>(); 
	
	/**
	 * Map from descriptive network name to filename
	 */
	private Map<String,String> networkNameMap = new HashMap<String,String>(); 
	
	/**
	 * Map from descriptive network name to network code
	 */
	private Map<String,String> networkCodeMap = new HashMap<String,String>();
	
	/**
	 * Map from network code to descriptive network name
	 */
	private Map<String,String> networkReverseCodeMap = new HashMap<String,String>();
	
	/**
	 * Map from network filename to descriptive name
	 */
	private Map<String,String> networkFileMap = new HashMap<String,String>();  

	/**
	 * List of all available network files
	 */
	private List<String> networkFileList = new ArrayList<String>();
	
	/**
	 * List of descriptive names for all available network files
	 */
	private List<String> networkNameList = new ArrayList<String>();
	
	/**
	 * List of network assigned to current user for QC
	 */
	private List<String> qcNetworkList = new ArrayList<String>();
	
	/** 
	 * Map from network to assigned QC case list for current user
	 */
	private Map<String, List<CaseList> > qcCaseMap = new HashMap<String, List<CaseList> >();
	
	/**
	 * List of caseid's for current user/network
	 */
	private List<String> qcCaseIdList = new ArrayList<String>();
	
	public List<String> getQcCaseIdList() {
		return this.qcCaseIdList;
	}
	
	
	
	/**
	 * Netica wrapper
	 */
	private NetworkWrapper dw;
	
	/** 
	 * Dummy variable set when loading network in
	 */
	private String pageLoad = "";
	
	private String event="";
	
	/* =============================================================
	 * For Clinical page
	 * ============================================================= */
	
	/**
	 * Names of all nodes for the current network
	 */
	private String[] nodes = new String[0];
	
	private static final String nullDisease = "Populate Features by Disease";
	
	private static final String highlightOff = "Highlight Most Discriminating Features";
	private static final String highlightOn =  "     Remove Feature Highlighting      ";
	
	/**
	 * flag indicating that new features are being loaded to the current form should be ignored
	 *   the features to be loaded are stored in: probInputs
	 */
	private boolean loadingFeatures = false;
	
	/**
	 * flag indicating a new case is being loaded so the current form should be ignored
	 *   case info to be loaded is stored in: loadCase
	 */
	private boolean loadingCase = false;
	
	/** 
	 * Info for new case to be loaded
	 */
	private CaseList loadCase = null;
	
	
	private String currentFeature = "";
	private String currentDisease = nullDisease;
	private String featureFlag = "false"; 
	private String[] titlesNew = new String[0];  // Disease names
	
	/**
	 * Descriptive names of diseases for current network
	 */
	private List<String> diseaseNames = new ArrayList<String>();
	
	/**
	 * Map from descriptive disease name to node state names
	 */
	private Map<String,String> diseaseNameMap = new HashMap<String,String>();
	
	private boolean sensityvityOn = false;       // Show most sensitive features
	
	/* =============================================================
	 * For Submit Page
	 * ============================================================= */
    //private int caseid;
    private CaseList caseList;
    private int caseNo;
    
    private boolean knownDx = true;
    
    private String activeCaseNetwork = "-select-";
    
	/**
	 * Descriptive names of diseases for current case submit network
	 */
	//private List<String> caseDiseaseNames = new ArrayList<String>();
	
	/**
	 * Map from descriptive disease name to node state names
	 */
	//private Map<String,String> caseDiseaseNameMap = new HashMap<String,String>();
	
	private boolean clearInputs = false;
	
	public List<String> saveCaseError = new ArrayList<String>();
	
	
	
	
	/* =============================================================
	 * For Education Page
	 * ============================================================= */
	private int educationCaseNo = 0;
    private List<String> educationErrorMessages = new ArrayList<String>();
    private String educationErrMsg;
    private String educationNetwork = "-select-";
    private boolean educationNetworkLoaded = false;
    
    private String educationDxFlag1 = "";
    private String educationDxFlag2 = "";
    private String educationDxFlag3 = "";
    
    private boolean changingEducationNetwork = false;
    
	public String getEducationNetwork( ) {
		return educationNetwork;
	}
	
	public void setEducationNetwork( String network ) {
		educationNetwork = network;
	}
	
	public String getEducationDxFlag1() {
		return educationDxFlag1;
	}
	
	public String getEducationDxFlag2() {
		return educationDxFlag2;
	}
	
	public String getEducationDxFlag3() {
		return educationDxFlag3;
	}
	
	public void resetEducationDxFlags() {
		this.educationDxFlag1 = "";
		this.educationDxFlag2 = "";
		this.educationDxFlag3 = "";
	}
	
	/* =============================================================
	 * For Research Page
	 * ============================================================= */
    private List<String> researchErrorMessages;
    private String researchErrMsg;
    private int randomCaseNo;
    private boolean researchNetworkLoaded = false;
    private String accessionNo = "";
    
    /**
     * Has the selected network just changed
     */
    private boolean changingResearchNetwork = false;
    
    
	/* =============================================================
	 * For QC Page
	 * ============================================================= */
    private int caseNoforQC = 0;
    private String caseNoforQCSetter;   
	private String reviewCase="false";
    private String qcperson;
    private String fromQcPage = "false";
    
    /**
     * Is there a currently loaded QC case
     */
    private int qcCaseLoaded = 0;
    
	/* =============================================================
	 * For Unknown Page
	 * ============================================================= */
	

	//private String networkName = "";
	//private String networkNamers = "";
	private int topDdx = 10;
	private String neticaLoad = "false"; 
	
	Map<String, Map<String, Double>> valuesNew = new HashMap<String, Map<String, Double>>();
    private List<String> errorMessages;
    private List<String> infoMessages;

    private String errorMsg="";

    Map<String, Map<String, Double>> newMap = new HashMap<String, Map<String, Double>>();
    Map<String, Double> newValuesMap = new HashMap<String,Double>();
    private String fromGraph ="false";
    List<String> caseListforNw = new ArrayList<String>();
   
    
    public String getHighlightMessage( ) {
    	String msg=highlightOff;
    	if ( this.sensityvityOn ) {
    		msg=highlightOn;
    	}
    	return msg;
    }
    
   
    public String getCaseNoforQCSetter() {
		return caseNoforQCSetter;
	}

	public void setCaseNoforQCSetter(String caseNoforQCSetter) {
		String[] cast;
		int val = 0;
		if( !("").equalsIgnoreCase(caseNoforQCSetter) && (caseNoforQCSetter!=null) &&
			( !"-select-".equalsIgnoreCase(caseNoforQCSetter))) {
			cast = caseNoforQCSetter.split("-");
			val = Integer.parseInt(cast[0]); 
		}
		this.caseNoforQC = val; 
		this.caseNoforQCSetter = caseNoforQCSetter;
		//this.qcCaseLoaded = 0;
		
		if (debugMode) System.out.println("caseNoforQC set to: " + this.caseNoforQC);
	}

    
    //For Research page



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

	private String highestSISensitiveNodeName = "";
	private String highestSPSensitiveNodeName = "";
	private String highestCLSensitiveNodeName = "";
	private String highestMSSensitiveNodeName = "";
	//private String SensitiveForDisease = "";

	private Map<String, String> nodeNameDirectMapping = new HashMap<String, String>();
	private Map<String, String> nodeNameReverseMapping = new HashMap<String, String>();
	private TreeMap<String, List<String>> prefixNodeListMapping = new TreeMap<String, List<String>>();
	private ArrayList<String> networkPrefixList = new ArrayList<String>();
	private Map<String, String> prefixNameMapping = new HashMap<String, String>();


	public boolean isSensityvityOn() {
		return this.sensityvityOn;
	}

	public void setSensityvityOn(boolean state) {
		this.sensityvityOn = state;
	}

	public boolean isKnownDx() {
		if (debugMode) System.out.println("isKnownDx()");
		return this.knownDx;
	}
	
	public boolean getKnownDx() {
		if (debugMode) System.out.println("getKnownDx()");
		return this.knownDx;
	}
	
	public void setKnownDx(boolean isKnown) {
		if (debugMode) System.out.println("setKnownDx(" + isKnown + ")");
		this.knownDx = isKnown;
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
    private String comments;


	//For Education Page
    private String nwNameforEducation;
    //For QC
    private String nwNameforQC;
    public String getNwNameforQC() {
		return nwNameforQC;
	}



	public List<String> getCorrectDxList() {
		return correctDxList;
	}

	public void setCorrectDxList(List<String> correctDxList) {
		this.correctDxList = correctDxList;
	}
	
	/**
	private String getAriesNetworkFileDescription( String filename ) {
		String desc = "";
	
		String baseName = filename.split(".dne")[0];
		String[] nameParts  = baseName.split("_");
		String code = nameParts[0];
		String version = nameParts[1].split("P")[1];
		String line = "";
		
		try ( BufferedReader reader = new BufferedReader(new FileReader(NETWORKDESCRIPTIONS)) ) {
			while (( line = reader.readLine()) != null ) {
				String[] networkEntry = line.split(",");
				if (networkEntry[0].equals(code) ) {
					desc = networkEntry[1] + " - " + networkEntry[2] + " (ver. " + version + ")";
				}
			}
			
			reader.close();
			
		} catch (IOException e ) {
			e.printStackTrace();
		} 
		
		return desc;
	}
	*/
	
	/**
	 *  close the current Netica session
	 */
	private void closeNeticaSession( ) {
		if ( this.dw != null ) {
			this.dw.endSession();
			this.dw = null;
		}
	}
	
	/*
	 * Get descriptions for all network files
	 */
	private void getAriesNetworkFileDescriptions(  )  {
		
		// Get all valid network filenames
		File folder = new File(PATH);
		File[] listOfFiles = folder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.toLowerCase().endsWith(".dne")) {
					//System.out.println("Found " + name);
					return true;
				} else
					return false;
			}
		});
		
		//System.out.println("Reading " + listOfFiles.length + " files");
		
		// For each file, get description and code
		for (int i = 0; i < listOfFiles.length; i++) {
			String netFileName = listOfFiles[i].getName();
			//System.out.println("Checking info for file: " + netFileName);
			
			String desc = "";
			String qc = "";
			
			String baseName = netFileName.split(".dne")[0];
			String[] nameParts  = baseName.split("_");
			String code = nameParts[0];
			String version = nameParts[1].split("P")[1];
			String line = "";
			
			try ( BufferedReader reader = new BufferedReader(new FileReader(NETWORKDESCRIPTIONS)) ) {
				while (( line = reader.readLine()) != null ) {
					String[] networkEntry = line.split(",");
					if (networkEntry[0].equals(code) ) {
						desc = networkEntry[1] + " - " + networkEntry[2] + " (ver. " + version + ")";
						qc = networkEntry[3];
					}
					
				}
				
				reader.close();
				
			} catch (IOException e ) {
				e.printStackTrace();
			}

			networkFileList.add(netFileName);
			networkNameList.add(desc);
			
			availableNetworks.add(desc);

			networkNameMap.put( desc, netFileName );
			networkFileMap.put( netFileName, desc );
			networkCodeMap.put( desc, code );
			networkReverseCodeMap.put(code, desc);
			
			qcMap.put( code, qc );

		}
		
		java.util.Collections.sort(availableNetworks);
		
	}
	
	/*
	 * Get descriptions for all network files
	 */
	private void uploadFeatures(  )  {
		
		String featureFile = "filename.csv";
		String line = "";
		
		try ( BufferedReader reader = new BufferedReader(new FileReader(featureFile)) ) {
			line = reader.readLine();
			String[] featureNames = line.split(",");
			String networkCode = featureNames[0];
			String networkName = networkReverseCodeMap.get(networkCode);
			this.setActiveNetwork( networkName );
			System.out.println("Uploading features for network: " + networkName);
			
			CaseList caseList = new CaseList();
			//caseList.setAccession(this.getAccession());
			caseList.setOrganization(this.getOrganization());
			caseList.setNetwork(networkCode);
			//caseList.setModality(this.getModality());
			//caseList.setDescription(this.getDescription());
			
			// set/load active network
			
			while (( line = reader.readLine()) != null ) {
				String[] featureValues = line.split(",");
				if ( featureValues.length != featureNames.length ) {
					System.out.println("Invalid .csv file: " + featureFile);
				}
				else {
					for ( int i=1; i<featureNames.length; i++ ) {
						String nodeNeticaName = this.nodeNameReverseMapping.get(featureNames[i]);

						if ( nodeNeticaName != null ) {
							System.out.println(featureNames[i] + "  -- node Netica name:" + nodeNeticaName);
						}
						else {
							System.out.println(featureNames[i] + "  -- No mapping found for this node");
						}
					}
					
				}
			}
			
			reader.close();
			
		} catch (IOException e ) {
			e.printStackTrace();
		}
	}
	
	

	/**
	 * Constructor
	 */
	public ServerModel() {
		
		// Register Session
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("sessionBindingListener", this);
		
		// Apriori feature categories
		networkPrefixList = new ArrayList<String>();
		networkPrefixList.add("SI");
		networkPrefixList.add("SP");
		networkPrefixList.add("CL");
		networkPrefixList.add("MS");

		// Apriori feature category display names
		prefixNameMapping = new HashMap<String, String>();
		prefixNameMapping.put("SI", "Signal");
		prefixNameMapping.put("SP", "Spatial");
		prefixNameMapping.put("CL", "Clinical");
		prefixNameMapping.put("MS", "Miscellaneous");
		
		// Initialize various hash maps for naming conversions
		userInputs = new HashMap<String, String>();
		userInputs1 = new HashMap<String, String>();
		userInputs2 = new HashMap<String, String>();
		dbFeatures = new HashMap<String, String>();
		userInputsForQc = new HashMap<String, String>();
		//userInputsForRs = new HashMap<String, String>();
		probInputs = new HashMap<String, String>();
		probInputs1 = new HashMap<String, String>();
		userInputsCase = new HashMap<String, String>();
		qcMap = new HashMap<String,String>();
		qcNetworkList = new ArrayList<String>();
		qcCaseMap = new HashMap<String, List<CaseList> >();
		
    
		// Read all network files
		getAriesNetworkFileDescriptions();
		
		// Check if user is assigned any QC networks and load assigned QC cases
		HttpSession session = Util.getSession();
		String username = null;
		if(session.getAttribute("username") != null){
			username = session.getAttribute("username").toString();
		}

		for ( Map.Entry<String,String> networkQC : qcMap.entrySet() ) {
			if ( networkQC.getValue().equals(username) ) {
				qcNetworkList.add( networkReverseCodeMap.get( networkQC.getKey()) );
			}
		}
		this.loadQCCaseMap();
				
		this.setActiveNetwork( availableNetworks.get(0) );
		// Default for clinical page
		/*
		activeNetwork = availableNetworks.get(0);
		
		
		String networkFileName = networkNameMap.get(activeNetwork);
		try {
			dw = new DNETWrapper(PATH + "/" + networkFileName);
		}
		 catch (NetworkLoadingException e) {
			System.out.println("Error loading the network.");

		} catch (Exception e) {
			System.out.println("Error converting filename.");
		}
		
		nodes = dw.getNodeNames();

	    diseaseNames = Arrays.asList(dw.getStates("Diseases"));
	    Collections.sort(diseaseNames);
	    
	    diseaseNameMap.clear();
	    for (int i=0; i < diseaseNames.size(); i++) {
	    	String formattedName = diseaseNames.get(i).replace("_", " ");
	    	diseaseNameMap.put(formattedName,  diseaseNames.get(i));
	    	diseaseNames.set(i, formattedName);
	    }
	    
	    this.closeNeticaSession();
	    */

	}

	private void loadQCCaseMap( ) {
		
		//System.out.println("loadQCCaseMap()");
		this.qcCaseMap.clear();

		for (String nw : this.qcNetworkList ) {
			List<CaseList> cases = this.loadQCCases(nw);
			this.qcCaseMap.put(nw, cases);
		}
		
	}
	
	private List<CaseList> loadQCCases( String network ) {
		//System.out.println("loadQCCases("+network+")");
		
		String networkCode = this.networkCodeMap.get(network);
		
		List<CaseList> caseList = new ArrayList<CaseList>();
		
		List<CaseList> allCases = UserDAO.getCaseList();
		for ( CaseList i : allCases ) {
			//System.out.println("Scanning case " + i.getCaseid()  + " in: " + i.getNetwork() );
			//System.out.println("  --> " + i.getQualityControl() );
			if ( i.getNetwork().equals(networkCode) && i.getCompleted().equals("No") && 
				 !i.isDeleted()	) {
				caseList.add( i );
				System.out.println("Adding case " + i.getAccession() + "(" + i.getCaseid() + ")" + " for " + network);
			}
 		}
		
		return caseList;
	}
	
	public List<String> getQCIdListByNetwork(String network) {
		List<CaseList> cases = this.qcCaseMap.get(network); //loadQCCases(network);
		List<String> idList = new ArrayList<String>();
		
		for ( CaseList i : cases ) {
			idList.add(Integer.toString(i.getCaseid()));
		}
		return idList;
	}

	/**
	 * Get the feature categories for the current network
	 * @return list of prefixes for feature categories of selected network
	 */
	public List<String> getNetworkPrefixList() {
		if (debugMode) {
			System.out.println("ServerModel.getNetworkPrefixList()");
			System.out.println(" -- prefix list size: " + networkPrefixList.size() );
		}
		return networkPrefixList;
	}

	/**
	 * Get the full name associated with a feature category prefix
	 * @param prefix two letter abbreviation used to name Netica nodes
	 * @return
	 */
	public String getNetworkPrefixName(String prefix) {
		if (debugMode) System.out.println("ServerModel.getNetworkPrefixName(" + prefix + ")");
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
	
	/**
	 * Determine if the current network contains features in a given category
	 * @param prefix two letter abbreviation for feature category
	 * @return boolean indicating if he current model contains any features in the passed feature category
	 */
	public boolean getNetworkNodeSize(String prefix) {
		if (debugMode) System.out.println("getNetworkNodeSize( " + prefix + " )");
		
		for (Map.Entry<String, List<String>> entry : prefixNodeListMapping.entrySet()) {
			if (entry.getKey().equals(prefix)) {
				
				// FIXME - What is this exception? 
				if (prefix.equals("MS") && entry.getValue().size() == 1) {
					return false;
				} else
					return true;
			}
		}
		if (debugMode) System.out.println(" -- No nodes for " + prefix);
		return false;

	}
	// CR103 Ends here

	public List<String> getSelectMenuFeatures(String prefix) {
		if (debugMode) System.out.println("getSelectMenuFeatures( " + prefix + " )");
		
		List<String> features = new ArrayList<String>();
		List<String> prefixNodeList = prefixNodeListMapping.get(prefix);

		if (prefixNodeList != null) {
			for (String nodeName : prefixNodeList) {
				if (nodeName.equals("Diseases"))
					continue;
				features.add(nodeName);
				//System.out.println(prefix + " - " + nodeName);
			}
		}

		return features;
	}

	/*
	 * Read in all network node names for menus. Strip off the prefix that indicates
	 * a category (signal, clinical, etc) and remove underscores for readability
	 * Fill in:
	 *   nodeNameDirectMapping:  Netica name  -> Display name
	 *   nodeNameReverseMapping: Display name -> Netica name
	 *   prefixNodeListMapping:  Display name -> Prefix code
	 */
	private void processNodePrefixes() {
		
		if ( debugMode ) System.out.println("ServerModel.processNodePrefixes()");
		
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
				nodeNameWithoutPrefix = nodeNameWithPrefix.replace(prefix + "_", "").replace("_", " ");
			} else {
				prefix = "MS";
			}
			
			//if (debugMode) System.out.println(nodeNameWithPrefix + " -> " + nodeNameWithoutPrefix);

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

	/*
	 * Get network for case page
	 */
	/*
	public String getActiveCaseNetwork () {
		if (debugMode) System.out.println( "getActiveCaseNetwork()" );
		return activeCaseNetwork;
	}
	*/
	
	/*
	 * Get network for clinical page
	 */
	public String getActiveNetwork () {
		if (debugMode) System.out.println( "getActiveNetwork()" );
		return activeNetwork;
	}
	

	/**
	 * Set the network being examined
	 * @param networkName display name of chosen network
	 */
	public void setActiveNetwork (String networkName) {
		if ( debugMode ) System.out.println( "setActiveNetwork( " + networkName + " )" );
		if (!networkName.equals(activeNetwork) ) {
			activeNetwork = networkName;
			userInputs.clear();
			diseaseNameMap.clear();
			
			if ( !networkName.equals("-select-")) {		
				
				String networkFileName = networkNameMap.get(activeNetwork);
				try {
					dw = new DNETWrapper(PATH + "/" + networkFileName);
				}
				 catch (NetworkLoadingException e) {
					System.out.println("Error loading the network.");
	
				} catch (Exception e) {
					System.out.println("Error converting filename.");
				}
				
				nodes = dw.getNodeNames();
				Arrays.sort(nodes);
				this.processNodePrefixes();
				
			    diseaseNames = Arrays.asList(dw.getStates("Diseases"));
			    Collections.sort(diseaseNames);
			    
			    diseaseNameMap.clear();
			    for (int i=0; i < diseaseNames.size(); i++) {
			    	String formattedName = diseaseNames.get(i).replace("_", " ");
			    	diseaseNameMap.put(formattedName,  diseaseNames.get(i));
			    	diseaseNames.set(i, formattedName);
			    }
			    
			    this.closeNeticaSession();
			    this.changingActiveNetwork = true;
			    
			}	
		}
		
	}
	
	/*
	 * Set network for case page
	 */
	/*
	public void setActiveCaseNetwork (String s) {
		if ( debugMode ) System.out.println( "setActiveCaseNetwork(" + s + ")" );
		if (!s.equals(activeCaseNetwork)) {
			System.out.println("Cleared user case inputs.");
			userInputs.clear();
			infoMessages = new ArrayList<String>();
		}
		activeCaseNetwork = s;
	}
	*/
	
	/*
	 * Get a list of all currently available network descriptions
	 */
	public List<String> getAvailableNetworks() {
		if ( debugMode ) System.out.println( "getAvailableNetworks()");
		return availableNetworks;
	}
	
	public List<String> getQcNetworkList() {
		return this.qcNetworkList;
	}
	
	public boolean getQcNetworkListExists() {
		return ( this.qcNetworkList.size() > 0 );
	}
	
	/*
	 * Toggle feature sensitivity highlighting
	 */
	public String toggleSensitivity( ) {
		this.sensityvityOn = !this.sensityvityOn;
		return "";
	}
	
  
	 public void setFeatureValueQC(String s){
	    	boolean clearflag = false;
	    	String[] inputs = s.split(":");
			//if(inputs.length == 1)return;
			if(userInputsForQc.containsKey(nodeNameReverseMapping.get(inputs[0])))
				{
				if(inputs.length == 2){
				if((userInputsForQc.get(nodeNameReverseMapping.get(inputs[0]))).equals(inputs[1]))
				return;
				}
				if(inputs.length == 1){
					if((userInputsForQc.get(nodeNameReverseMapping.get(inputs[0]))).equals("clear"))
						return;
					clearflag = true;
				}
				}
		
			if(this.getEvent().equalsIgnoreCase("FEATURE ENTERED") && ((inputs.length == 2) || clearflag == true )){
				//User user;
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
					userInputsForQc.put(nodeNameReverseMapping.get(inputs[0]), inputs[1]);
			}
				else if(clearflag == true){
					caseInput.setValue("["+networkcode+"]"+" "+inputs[0]+"="+"[Clear]");
					userInputsForQc.put(nodeNameReverseMapping.get(inputs[0]), "[Clear]");
				}
				UserDAO.SaveFeature(caseInput);
	    }
	    }
	    public String getFeatureValueQC() {
			// old before CR101
			/*
			 * if (!currentFeature.equals("")) return currentFeature + ":" +
			 * userInputs.get(currentFeature);
			 */
			// one line change for CR101
			if (!currentFeature.equals(""))
				return currentFeature + ":" + userInputsForQc.get(nodeNameReverseMapping.get(currentFeature));
			else
				return "";
		}
	
	
	
	public void setFeatureValue(String s){
    	boolean clearflag = false;
    	String[] inputs = s.split(":");
		//if(inputs.length == 1)return;
		if(userInputsCase.containsKey(nodeNameReverseMapping.get(inputs[0])))
			{
			if(inputs.length == 2){
			if((userInputsCase.get(nodeNameReverseMapping.get(inputs[0]))).equals(inputs[1]))
			return;
			}
			if(inputs.length == 1){
				if((userInputsCase.get(nodeNameReverseMapping.get(inputs[0]))).equals("clear"))
					return;
				clearflag = true;
			}
			}
	
		if(this.getEvent().equalsIgnoreCase("FEATURE ENTERED") && ((inputs.length == 2) || clearflag == true )){
			//User user;
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
				userInputsCase.put(nodeNameReverseMapping.get(inputs[0]), inputs[1]);
		}
			else if(clearflag == true){
				caseInput.setValue("["+networkcode+"]"+" "+inputs[0]+"="+"[Clear]");
				userInputsCase.put(nodeNameReverseMapping.get(inputs[0]), "[Clear]");
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
			return currentFeature + ":" + userInputsCase.get(nodeNameReverseMapping.get(currentFeature));
		else
			return "";
	}
 
/*  
	public String getResearchNodeInput() {
		if (!currentFeature.equals(""))
			return currentFeature + ":" + userInputsForRs.get(nodeNameReverseMapping.get(currentFeature));
		else
			return "";
	}
*/
	
	/*
	public void setEducationNodeInput(String s) {
		String[] inputs = s.split(":");	
	}
	*/

    /*
	public String getEducationNodeInput() {
		if (!currentFeature.equals(""))
			return currentFeature + ":" + userInputsForRs.get(nodeNameReverseMapping.get(currentFeature));
		else
			return "";
	}
	*/
    
	/**
	 * Set the parameter for a node 
	 * 
	 * This is called any time that a "Select Feature" menu is used to set a parameter.
	 * The input parameter will be of the form "node name" or "node name:value"
	 * If no value is passed (first case), the value is set to "[Clear]"
	 * 
	 * @param s The parameter value to set
	 */
	public void setNodeInput(String s) {
		
		if ( loadingFeatures && debugMode) {
			System.out.println("In 'loadingFeatures' state");
			System.out.println("probInputs size: " + probInputs.size());
			System.out.println("probInputs: " + probInputs.toString() );
	    	}	
		if (debugMode) System.out.println("setNodeInput(" + s + ")");
		
		String[] inputs = s.split(":");
		
	    //System.out.println("userInputs size: " + userInputs.size());
		//System.out.println( "userInputs: " + userInputs.toString() );
		
		if ( loadingFeatures  ) {
			//System.out.println("probInputs: " + probInputs.toString() );
			if ( !probInputs.containsKey(nodeNameReverseMapping.get(inputs[0])) ) {
				if (debugMode) System.out.println("Clearing value for" + inputs[0]);
				userInputs.put(nodeNameReverseMapping.get(inputs[0]), "[Clear]");
			}
			else {
				//System.out.println(nodeNameReverseMapping.get(inputs[0]));
				//System.out.println(probInputs.get( nodeNameReverseMapping.get(inputs[0])));
				userInputs.put( nodeNameReverseMapping.get(inputs[0]), probInputs.get( nodeNameReverseMapping.get(inputs[0])));
			}
		}
		
		if (inputs.length == 2) {
			
			/* A set value has changed */
			if ( userInputs.containsKey(nodeNameReverseMapping.get(inputs[0])) ) {
				if ( !userInputs.get( nodeNameReverseMapping.get(inputs[0])).equals(inputs[1]) ) {
					if (debugMode) System.out.println("Resetting value for " + inputs[0] + " to " +  probInputs.get( nodeNameReverseMapping.get(inputs[0])) );
					
					//userInputs.put(nodeNameReverseMapping.get(inputs[0]), inputs[1]);
					userInputs.put(nodeNameReverseMapping.get(inputs[0]), probInputs.get( nodeNameReverseMapping.get(inputs[0])));
					
					if ( probInputs.isEmpty() ) {
						resetDisease();
					}
				}
			}
			
			/* If disease has been selected */
			if ( !probInputs.isEmpty() ) {
				System.out.println("probInputs: " + probInputs.toString() );
				if ( !probInputs.containsKey(nodeNameReverseMapping.get(inputs[0])) ) {
					if (debugMode) System.out.println("Clearing value for" + inputs[0]);
					userInputs.put(nodeNameReverseMapping.get(inputs[0]), "[Clear]");
				}
				else {
					//userInputs.put(nodeNameReverseMapping.get(inputs[0]), )
					if ( debugMode ) { 
						System.out.println(nodeNameReverseMapping.get(inputs[0]));
						System.out.println(probInputs.get( nodeNameReverseMapping.get(inputs[0])));
					}
				}
			}
			else {
				/* Value set for first time */
				if (debugMode) System.out.println("Setting node value for " + inputs[0]);
				resetDisease();
				userInputs.put(nodeNameReverseMapping.get(inputs[0]), inputs[1]);
			}
		}
		else if (inputs.length == 1) {
			if ( !loadingFeatures ) {
				userInputs.put(nodeNameReverseMapping.get(inputs[0]), "[Clear]");
			}
		}
		
		if (debugMode) {
			System.out.println( "userInputs: " + userInputs.toString() );
			System.out.println("userInputs size: " + userInputs.size());	
		}
		
	}
	
	public String getNodeInput() {
		//System.out.println("getNodeInput() for currentFeature: " + currentFeature);
		
		if (!currentFeature.equals("")) {
			//System.out.println("  --> [" + nodeNameReverseMapping.get(currentFeature) + "] = " + userInputs.get(nodeNameReverseMapping.get(currentFeature)));
			return currentFeature + ":" + userInputs.get(nodeNameReverseMapping.get(currentFeature));
		}
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
		System.out.println("* currentFeature( "+cf+" )");
		currentFeature = cf;
		return "";
	}

	public String nodeInputHandler() {
		return ("index"); // return to index or refresh index
	}

	public String resetHandler() {
		userInputs.clear();
		this.setDisease("");
				
		infoMessages = new ArrayList<String>();
		return ("index"); // return to index or refresh index
	}
	
	
	public String resetDisease() {
		currentDisease = nullDisease;
		return "";
	}
 
	
	public List<String> getDiseaseTitles() {
		System.out.println("getDiseaseTitles()");
		return this.diseaseNames;
	}

	public String getTestNodes() {
		System.out.println("getTestNodes()");
		StringBuffer sb = new StringBuffer("");
			    
	    //List<String> newList = new ArrayList<String>();
		if(nodes != null){
		for (int i = 0; i < nodes.length; i++) {
			System.out.println(nodes[i]);
			
			if (nodes[i].equals("Diseases"))
				continue;
			sb.append("<p class='node-title'>" + nodes[i] + "</p>");
			sb.append("<table class=\"summarytable\" id=\"hood\">");
			Map<String, Double> values = dw.getNodeProbs(nodes[i], false);
			
			
			Set<String> s = values.keySet();
			Iterator<String> it = s.iterator();
			while (it.hasNext()) {
				String key = it.next();
				sb.append("<tr ><td >" + key).append("<td >" + convertToPercentage(values.get(key))).append("%</tr>");
			}
			sb.append("</table>");
		}
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
		System.out.println( "featureClass( " + nodeName + " )");

		if (userInputs.containsKey(nodeNameReverseMapping.get(nodeName))
				&& !userInputs.get(nodeNameReverseMapping.get(nodeName)).equals("[Clear]")) {
			return "hasChoice unverified";
		}
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
	public String featureClassCase(String nodeName) {

		if (userInputsCase.containsKey(nodeNameReverseMapping.get(nodeName))
				&& !userInputsCase.get(nodeNameReverseMapping.get(nodeName)).equals("[Clear]")) {
			return "hasChoice";
		} else
			return "";

	}

	public String featureClassEducation(String nodeName) {
		
		String nodeClass = "";
		String neticaNodeName = nodeNameReverseMapping.get(nodeName);
		
		if ( userInputs.containsKey(neticaNodeName) ) {
			if ( !userInputs.get(neticaNodeName).equals("[Clear]")) {
				nodeClass = "hasChoice";
			}
			
			if (userInputs1.containsKey(nodeNameReverseMapping.get(nodeName))){
				nodeClass = nodeClass + " correct";
			}
			else if (userInputs2.containsKey(nodeNameReverseMapping.get(nodeName))){
		    	nodeClass = nodeClass + " incorrect";
		    }
		}
		else {
			if (!dbFeatures.isEmpty() ) {
				if ( dbFeatures.containsKey(nodeNameReverseMapping.get(nodeName))) {
					nodeClass = "incorrect";
				}
			}
		}
		
		return nodeClass;

	}

	public List<String> selectMenuInputs(String nodeName) {
		if (debugMode) System.out.println("ServerModel.selectMenuInputs(" + nodeName + ")" );
		List<String> returnString = new ArrayList<String>();

		String nodeNeticaName = nodeNameReverseMapping.get(nodeName);
		
		if ( nodeNeticaName != null ) {
			System.out.println("  -- node Netica name:" + nodeNeticaName);
		}
		else {
			System.out.println("  -- No mapping found for this node");
		}
		
		
				
		Map<String, Double> values = dw.getNodeProbs(nodeNeticaName,false);	
		
		Set<String> s = values.keySet();
		Iterator<String> it = s.iterator();
		returnString.add(nodeName);
		while (it.hasNext()) {
			String key = it.next();
			returnString.add(nodeName + ":" + key);
		}

		//System.out.println("  -> " + returnString);
		return returnString;
	}
	

	
	public List<String> selectMenuInputsFromCase(String nodeName) {
		List<String> returnString = new ArrayList<String>();
		// old before CR101
		/* Map<String, Double> values = dw.getNodeProbs(nodeName); */
		// one line change for CR101
		//if(this.getFeatureFlag().equalsIgnoreCase("true")){
		 
			Map<String, Double> values = this.valuesNew.get(nodeName);//dw.getNodeProbs(nodeNameReverseMapping.get(nodeName));
			Set<String> s = values.keySet();
			Iterator<String> it = s.iterator(); 
			returnString.add(nodeName);
			while (it.hasNext()) {
				String key = it.next();
				returnString.add(nodeName + ":" + key);
			}
			// returnString.add(nodeName + ":[Clear]");
		//}
		return returnString;
	}
	
	
	public List<String> selectMenuInputsFromQC(String nodeName) {
		List<String> returnString = new ArrayList<String>();
		// old before CR101
		/* Map<String, Double> values = dw.getNodeProbs(nodeName); */
		// one line change for CR101
		//if(this.getFeatureFlag().equalsIgnoreCase("true")){
		 
			Map<String, Double> values = this.valuesNew.get(nodeName);//dw.getNodeProbs(nodeNameReverseMapping.get(nodeName));
			Set<String> s = values.keySet();
			Iterator<String> it = s.iterator(); 
			returnString.add(nodeName);
			while (it.hasNext()) {
				String key = it.next();
				returnString.add(nodeName + ":" + key);
			}
			// returnString.add(nodeName + ":[Clear]");
		//}
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
	
	/**
	 * Update the clinical diagnosis plot based on the currently selected feature values
	 */
	public void updateDiagnosisNode() {
		if (debugMode) System.out.println("ServerModel.updateDiagnosisNode()");
		Set<String> toRemove = new TreeSet<String>();
		Set<String> s = userInputs.keySet();
		
		
		if( fromGraph.equalsIgnoreCase("true") && userInputs.containsKey("Diseases")){
			userInputs.remove("Diseases");
			dw.clearNodeState("Diseases");
		}
		
		
		Iterator<String> it = s.iterator();
		while (it.hasNext()) {
			String key = it.next();
			if (key != null) {
				String response = userInputs.get(key);
				
				if ( response.equals("[Clear]")) {
					if (debugMode) System.out.println("key="+key);
					toRemove.add(key);
					dw.clearNodeState(key);
				} else
					dw.setNodeState(key, response);
			}
		}

		it = toRemove.iterator();
		while (it.hasNext()) {
			userInputs.remove(it.next());
		}
		
		if (debugMode) System.out.println("end ServerModel.updateDiagnosisNode()");
	}
	
	
	/**
	 * Update the radiographic diagnosis plot based on the currently selected feature values
	 * All clinical features ( "CL_*" ) should be ignored.
	 */
	public void updateRadioDiagnosisNode() {
		Set<String> toRemove = new TreeSet<String>();
		Set<String> s = userInputs.keySet();
		Iterator<String> it = s.iterator();
		while (it.hasNext()) {
			String key = it.next();
			if ( key != null) {
				String response = userInputs.get(key);

				if (key.startsWith("CL_")) { 
					response = "[Clear]";
				}

				if (response.equals("[Clear]")) {
					toRemove.add(key);
					dw.clearNodeState(key);
				} else
					dw.setNodeState(key, response);
			}
		}

		it = toRemove.iterator();
		while (it.hasNext()) {
			userInputs.remove(it.next());
		}
	}
	
	/**
	 * Get the html table with values for the radiographic diagnosis table
	 * @return A String containing an html table of values for radiographic diagnoses
	 */
    public String getRadiographicDiag(){
    	
    	// Update the diagnosis node first
    	updateRadioDiagnosisNode();
    	 
		Set<String> s = userInputs.keySet();
		Iterator<String> it = s.iterator();
		Map<String, Double> values = null;
		// Then produce the node output

		StringBuffer sb = new StringBuffer("");
		sb.append("<table id='radiodiagnosistable'><tr><td>Diagnosis</td><td>Probability (%)</td></tr>");
		if (dw != null) {
			values = sortByValue(dw.getRadioDiagnosisProbs(), -1);
		}
		if( values != null) {
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
				} else {
					sb.append("<tr><td>" + diag).append("<td>").append(convertToPercentage(values.get(key))).append("</tr>");
				}
				
			}
		}
		sb.append("</table>");
		return sb.toString();
    }
    
	/**
	 * Get the html table with values for the clinical diagnosis table
	 * @return A String containing an html table of values for clinincal diagnoses
	 */
	public String getDiagnosisNode() {
		System.out.println("ServerModel.getDiagnosisNode()");
		
		// Update the diagnosis node first
		fromGraph = "true";
		updateDiagnosisNode();
		Set<String> s = userInputs.keySet();
		Iterator<String> it = s.iterator();
		
		Map<String, Double> values = null;
		// Then produce the node output

		StringBuffer sb = new StringBuffer("");
		sb.append("<table id='diagnosistable1'><tr><td>Diagnosis</td><td>Probability (%)</td></tr>");
		
		
		if(dw != null) {
			values = sortByValue(dw.getDiagnosisProbs(), -1);
		} else {
			System.out.println("No network open");
		}
		
		System.out.println( "values size: " + values.size() );
	
		if (values != null) {
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
		
		System.out.println("end ServerModel.getDiagnosisNode()");
		return sb.toString();
	}

	public String getParetoTable() {
		// Update the diagnosis node first
		updateDiagnosisNode();
		Set<String> s = userInputs.keySet();
		Iterator<String> it = s.iterator();
		Map<String, Double> values = null;
		// Then produce the node output

		StringBuffer sb = new StringBuffer("");
		sb.append(
				"<table id='paretotable'><tr><td>Top Diagnoses</td><td>Probability (%)<td>Cumulative Probability (%)</td></tr>");
		if(dw != null){
	    values = sortByValue(dw.getDiagnosisProbs(), -1);
		}
		if(values != null){
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
		}
		sb.append("</table>");
		
		return sb.toString();
	}

	public void setQCPrePageLoad(String pl) {
		this.pageLoad = pl;
	}
	
	
	public void qcChangeCase(ValueChangeEvent event) {
		System.out.println("qcChangeCase()");
		
		if ( !(event.getNewValue() == null ) ) {
			String newValue= event.getNewValue().toString();
			String oldValue = "";
			if(event.getOldValue()!= null){
				oldValue = event.getOldValue().toString();
			}
			
			if(  newValue != null && !newValue.equalsIgnoreCase("-select-") && !newValue.equalsIgnoreCase(oldValue)) {
				this.setCaseNoforQCSetter(newValue);
				CaseList caselist = UserDAO.getCaseList(caseNoforQC);
				 if ( caselist != null ) {
					 this.loadCase = caselist;
					 this.loadingCase = true;
					 this.qcCaseLoaded = 1;			 
					 
					 List<UserCaseInput> featurelist = new ArrayList<UserCaseInput>();
					 featurelist = UserDAO.getUserCaseInput(caseNoforQC);
					 
					 this.userInputs.clear();
					 this.probInputs.clear();
					 this.loadingFeatures = true;
					 
					 for (UserCaseInput userCaseInput:featurelist) {
						 System.out.println( "  -- case input value: " + userCaseInput.getValue());
						 String[] fullFeature = userCaseInput.getValue().split("] ");
						 //String featureBase = fullFeature[0];
						 
						 // If is a feature value, e.g. [BG] Cerebellum=Yes
						 if ( fullFeature.length > 1 ) {
							 String[] featureParts = fullFeature[1].split("=");
							 String featureKey = featureParts[0].replace("_", " ");
							 String featureValue = featureParts[1];
							 System.out.println("  -- " + nodeNameReverseMapping.get(featureKey) + " -> " + featureValue);
							 probInputs.put(nodeNameReverseMapping.get(featureKey), featureValue);
						 }
						 else {
							 // FIXME - if disease set to correctDX ? 
						 }
					 }
					 System.out.println("probInputs size after loading qc case = " + probInputs.size());
				 } 
			}
		}
	}
	
	public void qcChangeNetwork(ValueChangeEvent event){
		
		System.out.println("qcChangeNetwork()");
		
		String newValue= event.getNewValue().toString();
		String oldValue = "";
		if(event.getOldValue()!= null){
			oldValue = event.getOldValue().toString();
		}
		
		if(  newValue!= null && !newValue.equalsIgnoreCase("-select-") && !newValue.equalsIgnoreCase(oldValue)){
			clear(true);
			this.activeNetwork = newValue;
			this.qcCaseIdList = getQCIdListByNetwork(this.activeNetwork);
			this.caseNoforQC = 0;
			this.setCaseNoforQCSetter("-select-");
			
			System.out.println("Clearing inputs for new qc network: " + this.activeNetwork);

			this.loadNeticaSession();
			nodes = dw.getNodeNames();
			
			// Get disease nodes
			String [] diseases = dw.getStates("Diseases");
			diseaseNames = Arrays.asList(diseases);
			Collections.sort(diseaseNames);
			
			// Format disease names for display
			diseaseNameMap.clear();
			for ( int i=0; i<diseaseNames.size(); i++) {
				String formattedName = diseaseNames.get(i).replace("_", " ");
				diseaseNameMap.put(formattedName, diseaseNames.get(i));
				diseaseNames.set(i, formattedName);
			}

			processNodePrefixes();
			Arrays.sort(nodes);		 
	    }
		
		
		/*
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
	*/
		
	
	
	}
	
	
	public void getDWInfo() {
		String newValue = this.activeNetwork;
		
		if( newValue != null && !newValue.equalsIgnoreCase("-select-") ){
			try {
				Map<String, Double> values = new HashMap<String, Double>();
				Map<String, Map<String, Double>> valuesNode = new HashMap<String, Map<String, Double>>();
				if(dw != null){
					dw.endSession();
					dw = null;
				}
				if(!activeNetwork.equals("")){
					String networkFileName = networkNameMap.get(activeNetwork);
					dw = new DNETWrapper(PATH + "/" + networkFileName);
					nodes = dw.getNodeNames();
					for (int i = 0; i < nodes.length; i++) {
						if (nodes[i].equals("Diseases")) {
							this.titlesNew = dw.getStates(nodes[i]);
							diseaseNames = Arrays.asList(this.titlesNew);
						}
					}
					
					processNodePrefixes(); 
					
					for(String prefix :this.getNetworkPrefixList()) {
						
						List<String> features  = getSelectMenuFeatures(prefix);
						for(String menu : features){	
							values = dw.getNodeProbs(nodeNameReverseMapping.get(menu),false);
							valuesNode.put(menu, values);
						}
					}
					this.valuesNew = valuesNode; 
					
					Arrays.sort(nodes);
			
				}	
				}  
			 catch (NetworkLoadingException e) {
				System.out.println("Error loading the network.");

			} catch (Exception e) {
				System.out.println("Error converting filename.");
			}
			}
	}
	
	
	public void setPrePageLoad1(String pl) {
		this.pageLoad = pl;
	}
	
	public void loadNeticaSession() {
		
		System.out.println("*");
		System.out.println("*");
		System.out.println("*");
		System.out.println("DNET Wrapper session started - " + this.activePage);
		
		if (!"".equals(this.activeNetwork) && !"-select-".equals(this.activeNetwork)) {
			String networkFileName = networkNameMap.get(this.activeNetwork);
			try {
		        if ( dw != null ) {
		        	System.out.println("call: dw.endSession()");
		        	dw.endSession();
		        	dw = null;
		        }
		        
				dw = new DNETWrapper(PATH + "/" + networkFileName);
				
			} catch (NetworkLoadingException e) {
				System.out.println("Error loading the network: " + this.activeNetwork);
	
			} catch (Exception e) {
				System.out.println("Error converting filename.");
			}
		}
		else {
			System.out.println("No active network to load");
		}
		
	}
	
	public void unloadNeticaSession() {
		
		if (dw != null) {
			dw.endSession();
			dw = null;
		}
		else {
			System.out.println("No active Netica session");
		}
		
		System.out.println("DNET Wrapper session ended - " + this.activePage);
		System.out.println("+");
		System.out.println("+");
		System.out.println("+");
	}
	
	public void caseChangeNetwork(ValueChangeEvent event) {
		System.out.println("caseChangeNetwork");
		clearInputs = true;
	}
	
	public String getCasePrePageLoad() {

		this.loadNeticaSession();
		
		if ( clearInputs == true ) {
			System.out.println("Clearing inputs for new network: " + this.activeNetwork);
			userInputs.clear();
			probInputs.clear();
			clearInputs = false;
		   
			nodes = dw.getNodeNames();
			
			// Get disease nodes
			String [] diseases = dw.getStates("Diseases");
			diseaseNames = Arrays.asList(diseases);
			Collections.sort(diseaseNames);
			
			// Format disease names for display
			diseaseNameMap.clear();
			for ( int i=0; i<diseaseNames.size(); i++) {
				String formattedName = diseaseNames.get(i).replace("_", " ");
				diseaseNameMap.put(formattedName, diseaseNames.get(i));
				diseaseNames.set(i, formattedName);
			}

			processNodePrefixes();
			Arrays.sort(nodes);
		    }
		else {
			if (! this.userInputs.isEmpty() ) {
				System.out.println( "Number of user inputs: " + this.userInputs.size() );
				for ( Map.Entry<String, String> entry: userInputs.entrySet() ) {
					System.out.println( "userInput: " + entry.getKey() + " = " + entry.getValue() );
				}
			}
			else {
				System.out.println("No user inputs for current case");
			}
		}
		
		if ( debugMode ) System.out.println("End casePrePageLoad()");	
		return "";
	}
	/**
	 * Load the the feature values for a given disease
	 * 
	 * @return
	 */
	public String getFeatureProb() {
		
		if ( debugMode ) System.out.println("getFeatureProb()");
		
		String iDisease = null;
		
		if ( activePage == PageType.CLINICAL) {
			iDisease = this.getDisease();
		}
		else if (activePage == PageType.CASE ) {
			iDisease = this.getCorrectDx();
		}
		
		if (iDisease != null && !iDisease.equalsIgnoreCase("--select--") && !iDisease.equalsIgnoreCase("-select-") && !iDisease.equals("") ) {
			System.out.println("  for disease: " + iDisease);
			
			if ( dw == null ) {

				try {
					String networkFileName = networkNameMap.get(activeNetwork);
					System.out.println("Open network file: " + networkFileName);
					dw = new DNETWrapper(PATH + "/" + networkFileName);
				} catch (NetworkLoadingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			nodes = dw.getNodeNames();
			
			fromGraph = "false";
			updateDiagnosisNode();

			// List<String> newList = new ArrayList<String>();
			if(nodes != null){
			for (int i = 0; i < nodes.length; i++) {
				if (nodes[i].equals("Diseases"))
					continue;

				Map<String, Double> values = dw.getNodeProbs(nodes[i],false);
				double highestVal = 0.000;
				String highestkey = "";
				List<Double> keylist = new ArrayList<Double>();
				for (Entry<String, Double> entry : values.entrySet()) {
					String key = entry.getKey();
					double value = entry.getValue();
					if (value >= highestVal) {
						highestVal = value;
						highestkey = key;
						// newValuesMap.put(highestkey, highestVal);
						keylist.add(highestVal);

					}
				}
				int counter = 0;
				for (Double siValue : keylist) {
					if (siValue.equals(highestVal)) {
						counter++;
					}
				}
				if (counter == 1) {
					probInputs.put(nodes[i], highestkey);
				}

			}
			}
			dw.endSession();
			dw = null;
		}

		if ( debugMode ) System.out.println("Finish getFeatureProb() with probInputs() size = " + this.probInputs.size() );
		
		return "";

	}
	//For CaseSubmitForm highlighting part
	public String getFeatureProbCase() {

		if (this.getCorrectDx() != null
				&& !this.getCorrectDx().equalsIgnoreCase("--select--")) {
			String filename = UserDAO.getFileName(this.getNwName());
			if(filename != null){
			try {
				if(dw!= null){
					dw.endSession();
					dw = null;
				}
				
				dw = new DNETWrapper(PATH + "/" + filename);
			} catch (NetworkLoadingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			nodes = dw.getNodeNames();
			fromGraph = "false";
			updateDiagnosisNode();

			// List<String> newList = new ArrayList<String>();
			if(nodes != null){
			for (int i = 0; i < nodes.length; i++) {
				if (nodes[i].equals("Diseases"))
					continue;

				Map<String, Double> values = dw.getNodeProbs(nodes[i],false);
				double highestVal = 0.000;
				String highestkey = "";
				List<Double> keylist = new ArrayList<Double>();
				for (Entry<String, Double> entry : values.entrySet()) {
					String key = entry.getKey();
					double value = entry.getValue();
					if (value >= highestVal) {
						highestVal = value;
						highestkey = key;
						// newValuesMap.put(highestkey, highestVal);
						keylist.add(highestVal);

					}
				}
				int counter = 0;
				for (Double siValue : keylist) {
					if (siValue.equals(highestVal)) {
						counter++;
					}
				}
				if (counter == 1) {
					probInputs1.put(nodes[i], highestkey);
				}

			}
			}
		}
			}
		if(dw!=null){
			dw.endSession();
			dw = null;
		}
		return "";

	}

		

	/*public String getFeatureProb() {
				
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
			probInputs.put(sinodeName, sihighestkey);

			//setNodeInput(nodeName+":"+highestkey);
			
			if(userInputs.containsKey("Diseases")){
				userInputs.remove("Diseases");
			}
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
			probInputs.put(spnodeName, sphighestkey);

			//setNodeInput(nodeName+":"+highestkey);
			if(userInputs.containsKey("Diseases")){
				userInputs.remove("Diseases");
			}
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
			probInputs.put(clnodeName, clhighestkey);
			//setNodeInput(nodeName+":"+highestkey);
			if(userInputs.containsKey("Diseases")){
				userInputs.remove("Diseases");
			}
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
			probInputs.put(msnodeName, mshighestkey);
			//setNodeInput(nodeName+":"+highestkey);
			if(userInputs.containsKey("Diseases")){
				userInputs.remove("Diseases");
			}
			//infoMessages.add("Higest Probability Features are "+highestProbsMap);
		}
		//userInputs.putAll(highestProbsMap);
		if(highestProbsMap !=null && !highestProbsMap.isEmpty()){
		infoMessages.add("Higest Probability Features are "+highestProbsMap);
		}
		
	}
	
	return "";
		
	}
	*/
	
	
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
	
	/**
	 * Check the user supplied inputs against the database
	 * @return
	 */
	public String checkFeatures() {
		
		 if ( debugMode ) System.out.println("ServerModel.checkFeatures()");
		
		 String[] input = new String[0];

		 List<UserCaseInput> list = new ArrayList<UserCaseInput>();
		 list = UserDAO.getUserCaseInput(this.getEducationCaseNo());
		 
		 System.out.println("Correct Features");
		 for ( UserCaseInput userCaseInput:list ) {
			 System.out.println( userCaseInput.getValue() );
		 }
		 System.out.println("Check vs. User Inputs");

		 for ( UserCaseInput userCaseInput:list ) {
			 System.out.println( userCaseInput.getValue() );
			 // Trim off category code
			 input = userCaseInput.getValue().split("] ");

			 if(input.length == 2) {
				 String[] val = input[1].split("=");
				 String nodeName = val[0].replace("_", " ");
				 System.out.println("  correct input: (" + nodeNameReverseMapping.get(nodeName) + ") " + val[0] + " -> " + val[1] );
				 dbFeatures.put(nodeNameReverseMapping.get(nodeName),val[1]);
				 }
			 
		 	}
		 
			 
			 //FIXME
			 if(!(userInputs.isEmpty())) {
				 for( Map.Entry<String, String> userInput : userInputs.entrySet() ) {
					 System.out.println("  User Input: " + userInput.getKey() + " -> " + userInput.getValue() );
					 if( !(dbFeatures.isEmpty()) ) {
						 
						 if ( dbFeatures.containsKey( userInput.getKey() ) ) {
							 
							 if ( userInput.getValue().equals( dbFeatures.get(userInput.getKey() ))) {
								 // Correct selection
								 userInputs1.put(userInput.getKey(), userInput.getValue());
								 
							 }
							 else {
								 //Incorrect selection
								 userInputs2.put(userInput.getKey(),  userInput.getValue());
							 }
							 
							 System.out.println("  Gold Input: " + userInput.getKey() + " -> " + dbFeatures.get( userInput.getKey() ) );
						 }
						 
						 /**
						 for(Map.Entry<String, String> dbInputs : dbFeatures.entrySet()) {
							 
							 //System.out.println("  dbInput: " + dbInputs.getKey() + " -> " + dbInputs.getValue() );
							 if (userInput.getKey().equals(dbInputs.getKey())) {
								 if (userInput.getValue().equals(dbInputs.getValue())) {
									 userInputs1.put(userInput.getKey(),userInput.getValue());
									 System.out.println("  -- Agree");
								 }
								 else {
									 userInputs2.put(userInput.getKey(), userInput.getValue());
									 System.out.println("  -- Disagree");
								 }
							 }
					     }
						 **/
					 }
					 else {
						 //userInputs2.put(userInput.getKey(), userInput.getValue());
					 }
				 }
			 }
			 
			 
			 educationErrorMessages = new ArrayList<String>();
			 //userInputs.putAll(dbFeatures);
			 
		return "";
	}
	

	public void saveFeaturesForQC(ValueChangeEvent event){
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
		//User user;
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
		//String networkcode = UserDAO.getCode(this.getNwNameforResearch());
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
	
	
	/**
	 * Save features selected for Research page
	 * @param event
	 */
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
		
		/*
		if(inputs != null) {
			if(userInputsForRs.containsKey(nodeNameReverseMapping.get(inputs[0]))) {
				if(inputs.length == 2) {
					//if((userInputsForRs.get(nodeNameReverseMapping.get(inputs[0]))).equals(inputs[1]))
					//	return;
				}
				if(inputs.length == 1) {
					//if((userInputsForRs.get(nodeNameReverseMapping.get(inputs[0]))).equals("clear"))
					//	return;
						clearflag = true;
				}
			}
		}
		*/

	if((inputs != null) && ((inputs.length == 2) || clearflag == true )){
		//User user;
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
		//String networkcode = UserDAO.getCode(this.getNwNameforResearch());
		caseInput.setCaseid(this.getCaseNo());
		caseInput.setEventid(1001);
		caseInput.setPageInfo("Research");
		String code = UserDAO.getCode(this.getNwNameforResearch());
		
		
		//s="";
		if (inputs.length == 2){
			caseInput.setValue("["+code+"]"+" "+inputs[0]+"="+inputs[1]);
			//userInputsForRs.put(nodeNameReverseMapping.get(inputs[0]), inputs[1]);
	}
		else if(clearflag == true){
			caseInput.setValue("["+code+"]"+" "+inputs[0]+"="+"[Clear]");
			//userInputsForRs.put(nodeNameReverseMapping.get(inputs[0]), "[Clear]");
		}
		UserDAO.SaveFeatureforOthers(caseInput);
}
	}
	
	public void setResearchPrePageLoad( String pl  ) {
		this.pageLoad = pl;
	}
	
	public String getResearchPrePageLoad() {
		System.out.println("DNET Wrapper session started - Research");
		
		if( ( changingResearchNetwork && !activeNetwork.equals("-select-") ) ||
			( !researchNetworkLoaded && !activeNetwork.equals("-select-") ) ) {
		try {
			changingResearchNetwork = false;
			this.setRandomCaseNo(CaseNo(this.activeNetwork));
			System.out.println("Random Case No: " + randomCaseNo);
			
			Map<String, Double> values = new HashMap<String, Double>();
			Map<String, Map<String, Double>> valuesNode = new HashMap<String, Map<String, Double>>();
			if(dw!= null){
				dw.endSession();
				dw = null;
			}
			
			String networkFileName = networkNameMap.get(activeNetwork);
			dw = new DNETWrapper(PATH + "/" + networkFileName);

			nodes = dw.getNodeNames();
			for (int i = 0; i < nodes.length; i++) {
				if (nodes[i].equals("Diseases")) {
					this.titlesNew = dw.getStates(nodes[i]);
					diseaseNames = Arrays.asList(this.titlesNew);
				}
			}
			
			processNodePrefixes(); 
			
			for(String prefix :this.getNetworkPrefixList()) {
				
				List<String> features  = getSelectMenuFeatures(prefix);
				for(String menu : features){	
					values = dw.getNodeProbs(nodeNameReverseMapping.get(menu),false);
					System.out.println(menu + " -> " + nodeNameReverseMapping.get(menu));
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
		
		return this.pageLoad;
	}
	
	
	public void researchChangeNetwork(ValueChangeEvent event) {
		System.out.println("researchChangeNetwork()");
		changingResearchNetwork = true;
		researchNetworkLoaded = false;
		accessionNo = "";
		randomCaseNo = 0;
	}
	
	public void setEducationPrePageLoad( String pl  ) {
		this.pageLoad = pl;
	}
	
	public String getEducationPrePageLoad() {
		System.out.println("DNET Wrapper session started - Education");
		
		if ( ( changingEducationNetwork && !this.activeNetwork.equals("-select-") ) ||
			 ( (!educationNetworkLoaded) && (!this.activeNetwork.equals("-select-")) ) ) {
		try {
			changingEducationNetwork = false;
			System.out.println("Edu network not loaded");
			this.setEducationCaseNo(CaseNo(this.activeNetwork));
			System.out.println("Education Case No: " + this.educationCaseNo);
			
			Map<String, Double> values = new HashMap<String, Double>();
			Map<String, Map<String, Double>> valuesNode = new HashMap<String, Map<String, Double>>();
			if(dw!= null){
				dw.endSession();
				dw = null;
			}
			
			String networkFileName = networkNameMap.get(this.activeNetwork);
			dw = new DNETWrapper(PATH + "/" + networkFileName);

			nodes = dw.getNodeNames();
			for (int i = 0; i < nodes.length; i++) {
				if (nodes[i].equals("Diseases")) {
					this.titlesNew = dw.getStates(nodes[i]);
					diseaseNames = Arrays.asList(this.titlesNew);
				}
			}
			
			processNodePrefixes(); 
			
			for(String prefix :this.getNetworkPrefixList()) {
				
				List<String> features  = getSelectMenuFeatures(prefix);
				for(String menu : features){	
					values = dw.getNodeProbs(nodeNameReverseMapping.get(menu),false);
					System.out.println(menu + " -> " + nodeNameReverseMapping.get(menu));
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
		
		return this.pageLoad;
	}
	
	public void educationChangeNetwork(ValueChangeEvent event) {
		System.out.println("educationChangeNetwork");
		this.resetEducationDxFlags();
		changingEducationNetwork = true;
		educationNetworkLoaded = false;
	}
	
	
	public void educationPageLoad(ValueChangeEvent event) {
		System.out.println("DNET Wrapper session started");
		//this.setEvent("FEATURE ENTERED");
		//this.setFeatureFlag("false");
		userInputs.clear();
		String newValue= event.getNewValue().toString();
		String oldValue = "";
		if(event.getOldValue()!= null){
			oldValue = event.getOldValue().toString();
		}
		Map<String, Double> values = new HashMap<String, Double>();
		Map<String, Map<String, Double>> valuesNode = new HashMap<String, Map<String, Double>>();
		if(newValue!= null && !newValue.equalsIgnoreCase("-select-") && !newValue.equalsIgnoreCase(oldValue)){
		try {
			if(dw != null){
				dw.endSession();
				dw = null;
			}

			
			if(!activeNetwork.equals("")){
				String networkFileName = networkNameMap.get(activeNetwork);
			dw = new DNETWrapper(PATH + "/" + networkFileName);
			nodes = dw.getNodeNames();
			for (int i = 0; i < nodes.length; i++) {
				if (nodes[i].equals("Diseases")) {
					this.titlesNew = dw.getStates(nodes[i]);
					diseaseNames = Arrays.asList(this.titlesNew);
				}
			}
			
			processNodePrefixes(); 
			
			for(String prefix :this.getNetworkPrefixList()) {
				
				List<String> features  = getSelectMenuFeatures(prefix);
				for(String menu : features){	
					values = dw.getNodeProbs(nodeNameReverseMapping.get(menu),false);
					valuesNode.put(menu, values);
				}
			}
			this.valuesNew = valuesNode; 
			
			Arrays.sort(nodes);
			
			this.setEducationCaseNo(CaseNo(newValue));
			}
			
		/*	 String[] input = new String[0];
				// List<String> values = new ArrayList<String>();
				 List<UserCaseInput> list = new ArrayList<UserCaseInput>();
				 list = UserDAO.getUserCaseInput(this.getEducationCaseNo());
				 for(UserCaseInput userCaseInput:list){
					 input = userCaseInput.getValue().split("] ");
					// values.add(input[1]);
					 String[] val = input[1].split("=");
					 userInputs.put(nodeNameReverseMapping.get(val[0]),val[1]);
				 }*/
				
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
		System.out.println("setPrePageLoad()");
		this.pageLoad = pl;
	}
	
	public void setCasePrePageLoad(String pl) {
		System.out.println("setCasePrePageLoad()");
		this.pageLoad = pl;
	}

	public void setQcPrePageLoad(String pl) {
		System.out.println("setQcPrePageLoad()");
		this.pageLoad = pl;
	}
	
	public boolean getInvalidQCCase( ) {
		
		if ( (this.activeNetwork==null) || 
			 ("".equalsIgnoreCase(this.activeNetwork)) || 
			 ("-select-".equalsIgnoreCase(this.activeNetwork)) || 
			 ("-select-".equalsIgnoreCase(this.caseNoforQCSetter) ) ||
			 (this.getCaseNoforQC()==0) ) {
			return true;
		}
		return false;
		
	}
	
	public int getQcCaseLoaded() {
		return this.qcCaseLoaded;
	}
	
	public String getQcPrePageLoad() {

		this.loadNeticaSession();
		
		if ( clearInputs == true ) {
			System.out.println("Clearing inputs for new network: " + this.activeNetwork);
			userInputs.clear();
			probInputs.clear();
			clearInputs = false;
		   
			nodes = dw.getNodeNames();
			
			// Get disease nodes
			String [] diseases = dw.getStates("Diseases");
			diseaseNames = Arrays.asList(diseases);
			Collections.sort(diseaseNames);
			
			// Format disease names for display
			diseaseNameMap.clear();
			for ( int i=0; i<diseaseNames.size(); i++) {
				String formattedName = diseaseNames.get(i).replace("_", " ");
				diseaseNameMap.put(formattedName, diseaseNames.get(i));
				diseaseNames.set(i, formattedName);
			}

			processNodePrefixes();
			Arrays.sort(nodes);
		    }
		else {
			if (! this.userInputs.isEmpty() ) {
				System.out.println( "Number of user inputs: " + this.userInputs.size() );
				for ( Map.Entry<String, String> entry: userInputs.entrySet() ) {
					System.out.println( "userInput: " + entry.getKey() + " = " + entry.getValue() );
				}
			}
			else {
				System.out.println("No user inputs for current case");
			}
		}
		
		if ( debugMode ) System.out.println("End qcPrePageLoad()");	
		return "";
	}
	
	
	/*
	 * Pre page loader for Clinical page
	 */
	public String getPrePageLoad() {
		System.out.println("+");
		System.out.println("+");
		System.out.println("+");
		System.out.println("DNET Wrapper session started - Clinical");
		this.setEvent("");
		
		this.closeNeticaSession();
		String networkFileName = networkNameMap.get(activeNetwork);
		
		try {
			dw = new DNETWrapper(PATH + "/" + networkFileName);
		} catch (NetworkLoadingException e) {
			System.out.println("Error loading the network.");
	
		} catch (Exception e) {
			System.out.println("Error converting filename.");
		}
		
		if (this.changingActiveNetwork) {
			this.changingActiveNetwork = false;
			userInputs.clear();

			nodes = dw.getNodeNames();
			for (int i = 0; i < nodes.length; i++) {
				if (nodes[i].equals("Diseases")) {
					this.titlesNew = dw.getStates(nodes[i]);
					diseaseNames = Arrays.asList(this.titlesNew);
				}
			}
			
			this.processNodePrefixes(); 
			
			Map<String, Double> values = new HashMap<String, Double>();
			Map<String, Map<String, Double>> valuesNode = new HashMap<String, Map<String, Double>>();
			for(String prefix :this.getNetworkPrefixList()) {
				
				List<String> features  = getSelectMenuFeatures(prefix);
				for(String menu : features){	
					values = dw.getNodeProbs(nodeNameReverseMapping.get(menu),false);
					System.out.println(menu + " -> " + nodeNameReverseMapping.get(menu));
					valuesNode.put(menu, values);
				}
			}
			this.valuesNew = valuesNode; 
			
			Arrays.sort(nodes);
			
		}

		System.out.println("activeNetwork: " + activeNetwork);
        if( !"".equals(activeNetwork)  ) {
			
			// FIXME - tesing this function
			//Map<String,String> mostSensitive = dw.getMostSensitiveUnsetNodes(networkPrefixList, userInputs);

			highestSISensitiveNodeName = dw.getHighestSISensitiveNodeName(userInputs);
			highestSPSensitiveNodeName = dw.getHighestSPSensitiveNodeName(userInputs);
			highestCLSensitiveNodeName = dw.getHighestCLSensitiveNodeName(userInputs);
			highestMSSensitiveNodeName = dw.getHighestMSSensitiveNodeName(userInputs);			 
			
        }

		if ( debugMode ) System.out.println("End getPrePageLoad()");
		return this.pageLoad;
	}

	public void setPostPageLoad(String pl) {
		if ( debugMode ) System.out.println("setPostPageLoad()");
		
		System.out.println("  probInputs size="+probInputs.size());
		
		if ( loadingFeatures && !(probInputs==null) && !(probInputs.isEmpty()) ) {
			System.out.println( "Setting " + probInputs.size() + " preloaded features and clearing all others");
			System.out.println( "Setting " + userInputs.size() + " total features");
			userInputs.clear();
			userInputs.putAll(probInputs);		
		}
		
		if ( loadingCase && (loadCase != null) ) {
			System.out.println("loading case info for caseid: " + loadCase.getCaseid() );
		
		 this.setOrganization(loadCase.getOrganization());
		 this.setModality(loadCase.getModality());
		 this.setAccession(loadCase.getAccession());
		 this.setPatientId(loadCase.getPatientid());
		 this.setAge(loadCase.getAge());
		 this.setGender(loadCase.getGender());
		 this.setDescription(loadCase.getDescription());
		 this.setCorrectDx(loadCase.getCorrectDx());
		 //System.out.println("caselist.getCorrectDx() :"+this.getCorrectDx());
		 this.setSupportingDataList(loadCase.getSupportingDataList());
		 this.setQcperson(loadCase.getQcperson());
 		 // UserCaseInput usercaseinput = new UserCaseInput();
 		 // this.setQcFlag("true");
		 // this.setFeatureFlag("false"); 
		 // QualityForm qForm = new QualityForm();
		 // qForm.setQcFlag("true"); 
		}
		
		
		loadingFeatures = false;
		loadingCase = false;
		
		if ( !probInputs.isEmpty() ) {
			probInputs.clear();
		}
		this.pageLoad = pl;
	}
	
	public void setPostPageLoad1(String pl) {
		this.pageLoad = pl;
	}

	public String getPostPageLoad() {
		
		System.out.println("getPostPageLoad()");
		
		loadingFeatures = false;
		if ( !probInputs.isEmpty() ) {
			System.out.println("Clear probInputs");
			probInputs.clear();
		}
		
		this.unloadNeticaSession();

		return this.pageLoad;
	}
	
	
	public void setPostResearchPageLoad(String pl) {
		this.pageLoad = pl;
	}
	public String getPostResearchPageLoad() {
		if(this.getNwNameforEducation()!=null && !this.getNwNameforEducation().equalsIgnoreCase("-select-") && dw != null){
		dw.endSession();
		dw = null;
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
		dw = null;
		System.out.println("DNET Wrapper session ended");
		}
		return this.pageLoad;
	}
	
	public void setPostQCPageLoad(String pl) {
		this.pageLoad = pl;
	}
	public String getPostQCPageLoad() {
		if ( dw != null) {
			dw.endSession();
			dw = null;
		}
		System.out.println("DNET Wrapper session ended - QC");
		return this.pageLoad;
	}

	public String getPostPageLoad1() {
		if(this.getNwName()!=null && !this.getNwName().equalsIgnoreCase("-select-") && dw != null){
		dw.endSession();
		dw = null;
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
		//System.out.println("valueBound:" + event.getName() + " session:" + event.getSession().getId());
	}

	public void valueUnbound(HttpSessionBindingEvent event) {
		//System.out.println("valueUnBound:" + event.getName() + " session:" + event.getSession().getId());
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
		//List<Network> networkList = null;
		List<String> nwNameList = new ArrayList<String>();
		
		try {
			List<Network> networkList = UserDAO.getNetworkList();
			for (Network network1 : networkList  ) {
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
			userInputsCase.clear();
			System.out.println("Clearing User Inputs");
		}
		this.nwName = nwName;
	}

	public String getCorrectDx() {
		return correctDx;
	}

	public void setCorrectDx(String correctDx) {
		this.correctDx = correctDx;
		
		if (correctDx.equalsIgnoreCase("Other")) {
			this.knownDx = false;
		}
		else {
			this.knownDx = true;
		}
		
		// Autopopulate features by disease
		if ( this.activePage == PageType.CASE ) {
			getFeatureProbCase();
		}
		
	}
	public boolean basicCaseValidate() {
		System.out.println("-- basicCaseValidate()");
		
		boolean hasErrors = false;
		
		FacesContext context = FacesContext.getCurrentInstance();
		Iterator<FacesMessage> msgIter = context.getMessages();
		while( msgIter.hasNext() ) {
			msgIter.next();
			msgIter.remove();
		}


		if(this.activeNetwork.equals("") || "-select-".equalsIgnoreCase(this.activeNetwork)){
			hasErrors = true;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Error: select network", ""));
		}
		
		if (this.getAccession().equals("")){
			hasErrors = true;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Error: add accession number", ""));
		}
		
		if(this.getPatientId().equals("")){
			hasErrors = true;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Error: add MR Number", ""));
		}
		
		if ( (this.getOrganization().equals("")) || this.getOrganization().equals("-select-") ) {
			hasErrors = true;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Error: select Organization", ""));
		}
		
		if ( (this.getModality().equals("")) || this.getModality().equals("-select-") ) {
			hasErrors = true;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Error: select Modality", ""));
		}

		
		if (this.getCorrectDx().equals("")){
			hasErrors = true;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Error: add correct diagnosis", ""));
		}
		
		if (this.getSupportingDataList().size()==0 || this.supportingDataList== null){
			hasErrors = true;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Error: add supporting data", ""));
		}
		/*else if(this.getDescription().equals("")){
		errorMessages.add("Please select Supporting Data Details");
	    }*/
		
		if (this.getAge().equals("")){
			hasErrors = true;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Error: add Patient Age", ""));
		}
		
		if (this.getGender().equals("")){
			hasErrors = true;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Error: add Patient Gender", ""));
		}
		
		return hasErrors;
	}

	public boolean ResearchCaseValidate(){
		this.setResearchErrMsg("");
		if(researchErrorMessages!=null && researchErrorMessages.size()>0)
			researchErrorMessages.clear();
		
		researchErrorMessages = new ArrayList<String>();
		if(this.activeNetwork.equals("") || "-select-".equalsIgnoreCase(this.activeNetwork)){
			//researchErrorMessages.add("Please select Network");
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Error: select network", ""));
			return true;
		}
		/*else if(this.getCorrectDxList()!= null && this.getCorrectDxList().size()== 0){
			researchErrorMessages.add("Please enter Correct Diagnosis");
		}*/
		else if(this.getFirstDx() == null || "".equals(this.getFirstDx())){
			//researchErrorMessages.add("Please select First Diagnosis");
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Error: select 1st diagnosis", ""));
			return true;
		}
		else if(this.getSecondDx() == null || "".equals(this.getSecondDx())){
			//researchErrorMessages.add("Please select Second Diagnosis");
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Error: select 2nd diagnosis", ""));
			return true;
		}
		else if(this.getThirdDx() == null || "".equals(this.getThirdDx())){
			//researchErrorMessages.add("Please select Third Diagnosis");
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Error: select 3rd diagnosis", ""));
			return true;
		}
		else if(this.getFirstDx().equals(this.secondDx) || this.getFirstDx().equals(this.thirdDx)){
			//researchErrorMessages.add("Diagnosis Entered twice.Please Select different diagnosis");
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Error: select 3 unique diagnoses", ""));
			return true;
		}
		else if(this.getSecondDx().equals(this.firstDx) || this.getSecondDx().equals(this.thirdDx)){
			//researchErrorMessages.add("Diagnosis Entered twice.Please Select different diagnosis");
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Error: select 3 unique diagnoses", ""));
			return true;
		}
		else if(this.getThirdDx().equals(this.firstDx) || this.getThirdDx().equals(this.secondDx)){
			//researchErrorMessages.add("Diagnosis Entered twice.Please Select different diagnosis");
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Error: select 3 unique diagnoses", ""));
			return true;
		}
		
		
		if (researchErrorMessages.size()>0) {
			return true;
		} else 
			return false;
	}

	public boolean EducationCaseValidate(){
		this.setResearchErrMsg("");
		if(educationErrorMessages!=null && educationErrorMessages.size()>0)
			educationErrorMessages.clear();
		educationErrorMessages = new ArrayList<String>();
/*		String[] values = new String[0];
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

		if(DiagList.size() == 3){
		if(!DiagList.get(0).equals(this.firstDx)){
			educationErrorMessages.add("Please enter "+DiagList.get(0)+" as a first diagnosis");
		}
		else if(!DiagList.get(1).equals(this.secondDx)){
			educationErrorMessages.add("Please enter "+DiagList.get(1)+" as a second diagnosis");
		}
		else if(!DiagList.get(2).equals(this.thirdDx)){
			educationErrorMessages.add("Please enter "+DiagList.get(2)+" as a Third diagnosis");
		}
		}*/
		String correctDx ="";
		if(this.getFirstDx() != null && this.getSecondDx() != null && this.getThirdDx() != null && !"".equalsIgnoreCase(this.getFirstDx()) &&  !"".equalsIgnoreCase(this.getSecondDx())&&  !"".equalsIgnoreCase(this.getThirdDx()))
				{
		if(this.getEducationCaseNo() != 0){
		correctDx = UserDAO.getCorrectDx(this.getEducationCaseNo());
		}
		if(!correctDx.equals("")){
			
			this.educationDxFlag1 = "Incorrect";
			this.educationDxFlag2 = "Incorrect";
			this.educationDxFlag3 = "Incorrect";
			
			boolean correctFound = false;
			
			if ( correctDx.equals(this.getFirstDx() ) ) {
				this.educationDxFlag1 = "Correct!";
				correctFound = true;
			}
			if ( correctDx.equals(this.getSecondDx() ) ) {
				this.educationDxFlag2 = "Correct!";
				correctFound = true;
			}
			if ( correctDx.equals(this.getThirdDx() ) ) {
				this.educationDxFlag3 = "Correct!";
				correctFound = true;
			}
			
			if ( !correctFound ) {
				this.educationDxFlag1 = "Incorrect - The correct diagnosis is: " + correctDx;
			}
			
			//if((correctDx.equals(this.getFirstDx())) ||( correctDx.equals(this.getSecondDx()) ) || (correctDx.equals(this.getThirdDx()) )){
			//	FacesContext.getCurrentInstance().addMessage(null,
			//			new FacesMessage(FacesMessage.SEVERITY_INFO, "You have selected the Correct Diagnosis : "+correctDx, ""));
			//}else{
			//	educationErrorMessages.add("Correct Diagnosis is "+ correctDx +". None of the selected diagnosis is correct! ");
			//}
		}
		}else{
			educationErrorMessages.add("Please Select all the Mandatory Fields before Check Diagnosis");
		}
		if(educationErrorMessages.size()>0){
			return true;
		}else 
			return false;
	}

	/**
	 * Submit case to the database
	 * @return
	 */
	public String submitCase() {
		System.out.println("Attempt to submit case to database");
		
		String networkcode = null;

		if( basicCaseValidate() == true ) {
			return "caseInput_form?redirect=true";
		}
		
		// Save a new case to the database
		try {
			networkcode = UserDAO.getCode(this.networkNameMap.get(this.activeNetwork));
			CaseList caseList = new CaseList();
			caseList.setAccession(this.getAccession());
			caseList.setOrganization(this.getOrganization());
			caseList.setNetwork(networkcode);
			caseList.setModality(this.getModality());
			caseList.setDescription(this.getDescription());
			
			if ("Other".equalsIgnoreCase(this.getCorrectDx())) {
				caseList.setCorrectDx(this.getCorrectDxText());
			} 
			else {
				caseList.setCorrectDx(this.getCorrectDx());
			}
			
			caseList.setPatientid(this.getPatientId());
			caseList.setAge(this.getAge());
			caseList.setGender(this.getGender());
			caseList.setSubmittedDate(new Date());
			
			String withoutLastComma = null;
			if (this.getSupportingDataList().size() > 0) {
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
			caseList.setQcperson(this.getQcperson()); // FIXME - what goes here?
						
			this.setCaseList(caseList);
			
			if (! this.userInputs.isEmpty() ) {
				System.out.println( "Number of user inputs: " + this.userInputs.size() );
				for ( Map.Entry<String, String> entry: userInputs.entrySet() ) {
					System.out.println( "userInput: " + entry.getKey() + " = " + entry.getValue() );
				}
			}
			if ( ! this.probInputs.isEmpty() ) {
				System.out.println( "Number of prob inputs: " + this.userInputs.size() );
			}
			
			if ( caseListValidation() ) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Case not submitted - This combination of organization, MR number, and network already exists!", ""));
				return "caseInput_form?redirect=true";
			} 
			else {
				boolean success = UserDAO.SaveCaseList(caseList, false);
				
				if( success ) {
					System.out.println("UserDAO.SaveCaseList() returned success");
					
					// FIXME - is this correct?
				    if(!userInputs.isEmpty()) {
				    	for (Map.Entry<String, String> entry : userInputs.entrySet()) {
					    	if(!entry.getKey().contains("Diseases")) {
						    	UserCaseInput caseinput = new UserCaseInput();
						    	caseinput.setUserid(userid);
						    	caseinput.setCaseid(getCaseid());
						    	caseinput.setSessionid(session.getId());
						    	caseinput.setEventid(1001);
						    	String featureName = nodeNameDirectMapping.get(entry.getKey()).replace(" ",  "_");
						        caseinput.setValue("[" + networkcode + "]" + " " + featureName + "=" + entry.getValue());
						        UserDAO.SaveFeature(caseinput);
					    	}
				    	}	
				    }
				  
				  // FIXME - reset all form values here or leave that to user?
				  this.clear(false);
				    
				  return "caseInput_form?faces-redirect=true";
				  //return "";
				}
				else{
					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage(FacesMessage.SEVERITY_INFO, "Save is not Successful!", ""));
					System.out.println("Failed to submit case");
					System.out.println("getReviewCase() = " + this.getReviewCase() );
					System.out.println("getFromQcPage() = " + this.getFromQcPage() );
					return "caseInput_form?faces-redirect=true";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "caseInput_form?faces-redirect=true";
		}

		/*
		else {
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
				//if (this.getCaseid() != 0 && caseListValidation()) {
				//	FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
				//			"Combination of Organization,MR Number,Network Already exists", ""));
				//} else {
				boolean success = UserDAO.UpdateCaseList(caseList);
				if(success){
				  // this.featureFlag = "true";
				   this.setReviewCase("false");
				   FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage(FacesMessage.SEVERITY_INFO, "Case Updated Successfully!", ""));
				   return "caseInput_form2?faces-redirect=true";
				   }
				   else{
						
						FacesContext.getCurrentInstance().addMessage(null,
								new FacesMessage(FacesMessage.SEVERITY_INFO, "Case Update is not Successful!", ""));
					}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		*/
		
		/*if(this.getFromQcPage().equalsIgnoreCase("true")){
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
				if (this.getCaseid() != 0 && caseListValidation()) {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
							"Combination of Organization,MR Number,Network Already exists", ""));
				} else {
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
		}*/
		//}
		//return "";
	//}
	
	public String selectionDone(){
		//this.setFeatureFlag("false");
		userInputsCase.clear();
		//clear();
		if(errorMessages != null && errorMessages.size() >0)
			errorMessages.clear();
		this.setAccession("");
		this.setOrganization("");
		this.setDescription("");
		this.setModality("");
		this.setCorrectDx("");
		this.setPatientId("");
		this.setCorrectDx("");
		this.setCorrectDxText("");
		this.setAge("");
		this.setGender("");
		//this.setQcperson("");
		this.setSupportingDataList(new ArrayList<String>());
		//this.setQcFlag("false");
		this.prefixNodeListMapping.clear(); 
		//return "login?faces-redirect=true";
		return "caseInput_form?faces-redirect=true";
	}
	
	
	public String qcSelectionDone() {
		
		System.out.println("qcSelectionDone()");
		
		try {

			String networkcode = this.networkCodeMap.get(this.activeNetwork);
			int newID = UserDAO.getNewCaseID();
			System.out.println("New caseid = " + newID);

			CaseList caselist = new CaseList();
			caselist.setCaseid(newID);
			caselist.setSubmittedid(this.getCaseNoforQC());
			
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
			caselist.setQualityControl("Yes");
			caselist.setCompleted("Yes");
			
			String withoutLastComma = null;
			if (this.getSupportingDataList().size() > 0) {
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
			if (session.getAttribute("username") != null) {
				username = session.getAttribute("username").toString();
			}
			if (session.getAttribute("password") != null) {
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
			//String[] input = new String[0];
			//String[] val = new String[0];
			//List<String> newList = new ArrayList<String>();
			
			// save case info
			System.out.println("Saving  info to caselist");
			boolean success = UserDAO.SaveCaseList(caselist, true); 
			
			// save features that are set
			System.out.println("Saving features to usercaseinput");
		    if(!userInputs.isEmpty()) {
		    	for (Map.Entry<String, String> entry : userInputs.entrySet()) {
			    	if(!entry.getKey().contains("Diseases")) {
			    		if ( !entry.getValue().equals("[Clear]") ) {
					    	UserCaseInput caseinput = new UserCaseInput();
					    	caseinput.setUserid(userid);
					    	caseinput.setCaseid(newID);
					    	caseinput.setSessionid(session.getId());
					    	caseinput.setEventid(1001);
					    	String featureName = nodeNameDirectMapping.get(entry.getKey()).replace(" ",  "_");
					        caseinput.setValue("[" + networkcode + "]" + " " + featureName + "=" + entry.getValue());
					        UserDAO.SaveFeature(caseinput);
			    		}
			    	}
		    	}	
		    }
		    
		    // FIXME - update original caseid to indicate QC has been submitted
		    UserDAO.UpdateCaseList(networkcode,this.getCaseNoforQC(),"Yes");
		    
		    // FIXME - update list of cases for this network
		    this.loadQCCaseMap();
		    this.qcCaseIdList = getQCIdListByNetwork(this.activeNetwork);
		    
			/*
			List<UserCaseInput> list = new ArrayList<UserCaseInput>();
			list = UserDAO.getUserCaseInput(caseNoforQC);
			
			for (UserCaseInput userCaseInput:list) {
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
			 }*/
		    
			//infoMessages = new ArrayList<String>();
			//if (newList.size()>0) {
			//	infoMessages.add("selected features are "+newList.toString());
			//}
			 
			if(success){
			  // this.featureFlag = "true";
			   //this.setReviewCase("false");
			  // this.setFromQcPage("false");
			   FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Case Updated Successfully!", ""));
			   this.setFeatureFlag("false");
				UserDAO.UpdateCaseList("Yes",this.getCaseNoforQC());
				caseListforNw.remove(this.getCaseNoforQCSetter());
				this.setCaseNoforQCSetter("");
				userInputsForQc.clear();
				//this.setQcFlag("false");
				if(errorMessages != null && errorMessages.size() >0)
					errorMessages.clear();
				this.setAccession("");
				this.setOrganization("");
				this.setDescription("");
				this.setModality("");
				this.setCorrectDx("");
				this.setPatientId("");
				this.setCorrectDxText("");
				this.setAge("");
				this.setGender("");
				//this.setQcperson("");
				this.setSupportingDataList(new ArrayList<String>());
				//this.setQcFlag("false");
				this.prefixNodeListMapping.clear(); 
				//return "login?faces-redirect=true";
				clear(false);
				return "qualityControl?faces-redirect=true";
			   }
			   else{
					
					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage(FacesMessage.SEVERITY_INFO, "Case Update is not Successful!", ""));
				}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "qualityControl?faces-redirect=true";
	}
	
	public String deleteCaseQC(){
		UserDAO.deleteCaseList(this.getCaseNoforQC());
		clear(false);
		return "qualityControl?faces-redirect=true";
	}
	
    public String backtoLogin(){
		 return "login?faces-redirect=true";
	}
    
    public String skipCaseQC() {
    	clear(false);
		return "qualityControl?faces-redirect=true";
	}
    
	public String educationCompleted(){
		this.setEducationErrMsg("");
		if(EducationCaseValidate() == true){
			return null;
		}
		HttpSession session = Util.getSession();
		try{
		UserCaseInput userCaseInput = new UserCaseInput();
		userCaseInput.setEventid(1002);
		userCaseInput.setCaseid(this.getCaseNo());
		//String withoutLastComma ="";
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
		//UserDAO.SaveFeature(userCaseInput);
		//String nwcode = UserDAO.getCode(this.getEducationNetwork());
        //UserDAO.UpdateCaseList(nwcode,this.getCaseNo(),"Yes");
		}catch(Exception e){
			e.printStackTrace();
		}
		/*userInputs.clear();
		this.setNwNameforEducation("-select-");
    	this.setFirstDx("");
    	this.setSecondDx("");
    	this.setThirdDx("");
		this.setCorrectDxList(new ArrayList<String>());
    	this.setEducationCaseNo(CaseNo(this.getNwNameforEducation()));*/
	    //session.invalidate();
		return "";
		
	}

	public String actionCompleted() {
		System.out.println("ServerModel.actionCompleted()");
		
		if( ResearchCaseValidate() == true) {
			System.out.println("Failed to validate the case");
			return "research?redirect=true";
		}
		
		HttpSession session = Util.getSession();
		try {
			UserCaseInput userCaseInput = new UserCaseInput();
			userCaseInput.setEventid(1002);
			userCaseInput.setCaseid(this.randomCaseNo);
			
			
			//String withoutLastComma ="";
			StringBuffer sb = new StringBuffer();
			
			/*
			if(this.getCorrectDxList() != null & this.getCorrectDxList().size() >0){
			for(String CorrectDx:correctDxList){
				sb.append(CorrectDx).append(", ");
				}
				withoutLastComma = sb.substring(0, sb.length() - ", ".length());
			}
			userCaseInput.setValue(withoutLastComma);
			
			String withoutLastComma = null;
			if (this.getSupportingDataList().size() > 0) {
				StringBuffer sb = new StringBuffer();
				for (String supportData : this.getSupportingDataList()) {
					sb.append(supportData).append(", ");
				}
				withoutLastComma = sb.substring(0, sb.length() - ", ".length());
			}
			*/
			
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
			userCaseInput.setPageInfo("Research");
			userCaseInput.setComments(this.getComments());
			userCaseInput.setSessionid(session.getId());
			
			// FIXME - temp disable
			UserDAO.SaveFeatureforOthers(userCaseInput);
			
			// save features that are set
			System.out.println("Saving features to usercaseinput1");
			String nwcode = this.networkCodeMap.get(this.activeNetwork);
		    if(!userInputs.isEmpty()) {
		    	for (Map.Entry<String, String> entry : userInputs.entrySet()) {
			    	if(!entry.getKey().contains("Diseases")) {
			    		if ( !entry.getValue().equals("[Clear]") ) {
					    	UserCaseInput caseinput = new UserCaseInput();
					    	caseinput.setPageInfo("Research");
					    	caseinput.setUserid(userid);
					    	caseinput.setCaseid(this.randomCaseNo);
					    	caseinput.setSessionid(session.getId());
					    	caseinput.setEventid(1001);
					    	String featureName = nodeNameDirectMapping.get(entry.getKey()).replace(" ",  "_");
					        caseinput.setValue("[" + nwcode + "]" + " " + featureName + "=" + entry.getValue());
					        
					        //System.out.println("Saving [" + nwcode + "]" + " " + featureName + "=" + entry.getValue());
					        UserDAO.SaveFeatureforOthers(caseinput);
			    		}
			    	}
		    	}	
		    }
			
			
			
			    //UserDAO.getCode(this.getNwNameforResearch());
	        //UserDAO.UpdateCaseList(nwcode,this.getCaseNo(),"Yes");
			
		}  catch(Exception e){
			e.printStackTrace();
		}
		
		
		userInputs.clear();
		//this.setNwNameforResearch("-select-");
		this.setFirstDx("");
    	this.setSecondDx("");
    	this.setThirdDx("");
		this.setCorrectDxList(new ArrayList<String>());
		this.setComments("");
		researchErrorMessages = new ArrayList<String>();
	   // session.invalidate();
		//this.setRandomCaseNo(CaseNo(this.getNwNameforResearch()));
		
		//this.clear(false);
		//this.randomCaseNo = 0;
		//changingResearchNetwork = true;
		
		//clearInput();
		//return "research?redirect=true";
		return clearInput();
		
	}

	public boolean caseListValidation() {
		System.out.println("--caseListValidation()");
		boolean flag = false;
		List<CaseList> caselists = null;
		caselists = UserDAO.getCaseList();
		System.out.println("--this.activeNetwork = " + this.activeNetwork);
		
		String nwcode = UserDAO.getCode(this.networkNameMap.get(this.activeNetwork));
		System.out.println("--nwcode: " + nwcode);
		
		
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
		System.out.println("saveUserCaseInput() - Not yet implemented");
		return "";
		
	}
	public String clear(boolean resetActiveNetwork) {
		System.out.println("clear()");
		
		if(errorMessages != null && errorMessages.size() > 0 )
			errorMessages.clear();
		this.qcCaseLoaded = 0;
		this.caseNoforQC = 0;
		this.caseNoforQCSetter = "-select-";
		
		this.randomCaseNo = 0;
    	this.setFirstDx("");
    	this.setSecondDx("");
    	this.setThirdDx("");
    	this.setComments("");
		
		this.currentFeature = "";
		this.loadingFeatures = false;
		this.loadingCase = false;
		this.setAccession("");
		this.setOrganization("");
		this.setDescription("");
		this.setModality("");
		this.setCorrectDx("");
		this.setPatientId("");
		this.setNwName("");
		//this.setNwNameforQC("");
		this.setCorrectDx("");
		this.setCorrectDxText("");
		this.setAge("");
		this.setGender("");
		this.setQcperson("");
		this.setSupportingDataList(new ArrayList<String>());
		//this.setQcFlag("false");
		
		if ( !userInputs.isEmpty() ) userInputs.clear();
		if (! probInputs.isEmpty() ) probInputs.clear();
		
		if ( !userInputs1.isEmpty() ) userInputs1.clear();
		if ( !userInputs2.isEmpty() ) userInputs2.clear();
		if ( !dbFeatures.isEmpty() ) dbFeatures.clear();
		
		if ( resetActiveNetwork ) {
		  this.prefixNodeListMapping.clear(); 
		  this.activeNetwork = "-select-";
		  //this.diseaseNames.clear();
		  diseaseNames = new ArrayList<String>();
		  if ( !diseaseNames.isEmpty() ) {
			  diseaseNames.clear();
		  }
		}

		this.unloadNeticaSession();
		
		return "";
	}
	
	public String getReviewCaseList(){
    	//this.setFeatureFlag("false");
    	this.setReviewCase("true");
      	return "caseInput_form?faces-redirect=true";
    }
	public String getReviewQCCaseList(){
    	//this.setFeatureFlag("false");
    	this.setReviewCase("true");
      	return "qualityControl?faces-redirect=true";
    }
	
	/**
	 * Skip research case
	 * @return
	 */
    public String clearInput(){
    	//this.setNwNameforResearch("");
    	//this.getRandomCaseNo();
    	userInputs.clear();
    	this.setFirstDx("");
    	this.setSecondDx("");
    	this.setThirdDx("");
    	this.setComments("");
		this.setCorrectDxList(new ArrayList<String>());
		researchErrorMessages = new ArrayList<String>();
		this.researchNetworkLoaded = false;
		this.randomCaseNo = 0;
		changingResearchNetwork = true;
		
		return "";
    }
    public String nextCase(){
    	//this.setNwNameforEducation("");
    	//this.getRandomCaseNo();
    	userInputs.clear();
    	userInputs1.clear();
    	userInputs2.clear();
    	dbFeatures.clear();
    	this.setFirstDx("");
    	this.setSecondDx("");
    	this.setThirdDx("");
    	educationErrorMessages = new ArrayList<String>();
    	this.setCorrectDxList(new ArrayList<String>());
    	this.setEducationCaseNo(CaseNo(this.getNwNameforEducation()));
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
	
	
	public void clearDefault(){
		if(errorMessages != null && errorMessages.size() >0)
			errorMessages.clear();
		this.setAccession("");
		this.setOrganization("");
		this.setDescription("");
		this.setModality("");
		this.setCorrectDx("");
		this.setPatientId("");
		this.setNwName("");
		//this.setNwNameforQC("");
		this.setCorrectDx("");
		this.setCorrectDxText("");
		this.setAge("");
		this.setGender("");
		//this.setQcperson("");
		this.setSupportingDataList(new ArrayList<String>());
		//this.setQcFlag("false");
	}
	
	/*
	 * Go to Home page
	 */
	public String getNavRuleHome() {
		this.clear(true);
		this.unloadNeticaSession();
		
		this.activePage = PageType.HOME;
		this.prefixNodeListMapping.clear();
		this.activeNetwork = null;

		return "home?faces-redirect=true";
	}

	/*
	 * Go to clinical page
	 */
	public String getNavRuleClinical() {
		this.clear(true);
		this.unloadNeticaSession();
		
		this.activePage = PageType.CLINICAL;
		//this.prefixNodeListMapping.clear();
		this.activeNetwork = availableNetworks.get(0);
		this.changingActiveNetwork = true;

		return "index?faces-redirect=true";
	}
	
	/*
	 * Go to Submit Case page
	 */
	public String getNavRuleCase() {
		System.out.println("getNavRuleCase()");
		
		this.clear(true);
		this.unloadNeticaSession();
		
		this.activePage = PageType.CASE;
		this.fromQcPage = "false";

		return "caseInput_form?faces-redirect=true";
	}
	
	/*
	 * Got to QC [age
	 */
	public String getNavRuleQC() {
		this.clear(true);
		this.activePage = PageType.QC;
		
		/*userInputsForQc.clear();
		this.setNwNameforQC("");
		clearDefault();*/
		//this.setCaseNoforQCSetter("");
		this.setFromQcPage("true");
		return "qualityControl?faces-redirect=true";
	}
	
	/*
	 * Go to Research page
	 */
	public String getNavRuleResearch() {
		this.clear(true);
		activePage = PageType.RESEARCH;
		if(dw!= null){
			dw.endSession();
			dw = null;
		}

		this.setActiveNetwork("-select-");
		//this.setResearchNetwork("-select-");
		/*userInputsForRs.clear();
		this.setNwNameforResearch("");
    	this.setFirstDx("");
    	this.setSecondDx("");
    	this.setThirdDx("");
    	this.setComments("");
    	researchErrorMessages = new ArrayList<String>();
		this.setCorrectDxList(new ArrayList<String>());*/
		this.setFirstDx("");
    	this.setSecondDx("");
    	this.setThirdDx("");
    	this.setComments("");
    	researchErrorMessages = new ArrayList<String>();
		this.setCorrectDxList(new ArrayList<String>());
		return "research?faces-redirect=true";
		}
	
	/* 
	 * Go to Education page
	 */
	public String getNavRuleEducation() {
		clear(true);
		
		activePage = PageType.EDUCATION;
		if(dw!= null){
			dw.endSession();
			dw = null;
		}
		if ( !userInputs.isEmpty() ) userInputs.clear();
		if ( !probInputs.isEmpty() ) probInputs.clear();
		this.setActiveNetwork("-select-");
		//this.setEducationNetwork("-select-");
		
		/*userInputs.clear();
		this.setNwNameforEducation("");*/
    	this.setFirstDx("");
    	this.setSecondDx("");
    	this.setThirdDx("");
    	educationErrorMessages = new ArrayList<String>();
    	this.setCorrectDxList(new ArrayList<String>());
		return "education?faces-redirect=true";
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

/*
	public void setNwNameforResearch(String nwNameforResearch) {
		
		if(nwNameforResearch != null && !nwNameforResearch.equalsIgnoreCase(this.getNwNameforResearch())){
			this.setErrorMsg("");
			//userInputsForRs.clear();
			System.out.println("Clearing User Inputs");
			}
		this.nwNameforResearch = nwNameforResearch;
	}
*/
	
	public String getDisease() {
		return currentDisease;
	}
	
	/**
	 * Called when the the "Prepopulate by Disease" menu is used
	 * 
	 * @param event holds info for previous and current value of menu
	 */
	public void getDiseaseAction(ValueChangeEvent event){
		if ( debugMode ) System.out.println("getDiseaseAction()");
		System.out.println("Event Change Value: '" + event.getOldValue() + "' -> '" + event.getNewValue()+"'" );
		String newValue = event.getNewValue().toString();
		
		String oldValue = "";
		if(event.getOldValue() != null){
			oldValue = event.getOldValue().toString();
		}
		
		currentDisease = newValue;
		if(newValue != null && !newValue.equals("-select-") && !newValue.equalsIgnoreCase(nullDisease) && !newValue.equalsIgnoreCase(oldValue)) {

			if(!userInputs.isEmpty())
				userInputs.clear();
			if(!probInputs.isEmpty())
				probInputs.clear();
			
			loadingFeatures = true;
			userInputs.put("Diseases", diseaseNameMap.get(newValue));
			
			getFeatureProb();
		}
		
		//updateDiagnosisNode1();
		//Map<String, Double> values = sortByValue(dw.getDiagnosisProbs(), -1);
	}
	
	public void getCorrectDxAction(ValueChangeEvent event) {
	
		if ( debugMode ) System.out.println("getCorrectDxAction()");
		

		String newValue = "";
		if (event.getNewValue() != null) {
			newValue= event.getNewValue().toString();
		}
		String oldValue = "";
		if (event.getOldValue() != null) {
			oldValue = event.getOldValue().toString();
		}
		
		currentDisease = newValue;
		if(newValue != null && 
		   !newValue.equals("-select-") && 
		   !newValue.equalsIgnoreCase(nullDisease) && 
		   !newValue.equalsIgnoreCase(oldValue) &&
		   !newValue.equalsIgnoreCase("Other")  ) {

			if(!userInputs.isEmpty())
				userInputs.clear();
			if(!probInputs.isEmpty())
				probInputs.clear();
			
			this.setCorrectDx(newValue);
			
			loadingFeatures = true;
			userInputs.put("Diseases", diseaseNameMap.get(newValue));
			getFeatureProb();
		}
		else {
			if (debugMode) System.out.println("No change found in 'correctDxAction()'");
		}
		
		//if(newValue != null && !newValue.equalsIgnoreCase("-select-") && !newValue.equalsIgnoreCase(oldValue)){
		//	userInputs.put("Diseases", newValue);
		//}
		
		if (newValue.equalsIgnoreCase("Other")) {
			if (debugMode) System.out.println("'Other' disease chosen");
			loadingFeatures = true; // ???
			setKnownDx(false);
			probInputs.clear();
			userInputs.clear();
		} else {
			setKnownDx(true);
		}
		
		//updateDiagnosisNode();
		//Map<String, Double> values = sortByValue(dw.getDiagnosisProbs(), -1);
	}

	public void setDisease(String disease) {
		if ( debugMode ) System.out.println( "setDisease(" + disease + ")");

			/*
			boolean flag = false;
			if (!disease.equals(currentDisease) && !disease.equals("--select--")) {
				flag = true;
			}
		
			currentDisease = disease;
			if (flag) {
				getFeatureProb();
			}
           */
		
	}
	 public String getFirstDx() {
			return firstDx;
		}

		public void setFirstDx(String firstDx) {
			if (!firstDx.equals(this.firstDx)) this.educationDxFlag1 = "";
			this.firstDx = firstDx;
		}

		public String getSecondDx() {
			return secondDx;
		}

		public void setSecondDx(String secondDx) {
			if (!secondDx.equals(this.secondDx)) this.educationDxFlag2 = "";
			this.secondDx = secondDx;
		}

		public String getThirdDx() {
			return thirdDx;
		}

		public void setThirdDx(String thirdDx) {
			if (!thirdDx.equals(this.thirdDx)) this.educationDxFlag3 = "";
			this.thirdDx = thirdDx;
		}

	public int CaseNo(String nwName) {
		int randomCaseNo = 0;
		String code = null;
		if(nwName != null && !"-select-".equalsIgnoreCase(nwName)){
			//code = UserDAO.getCode(nwName);
			code = networkCodeMap.get(nwName);
			System.out.println("code = " + code);
			randomCaseNo = UserDAO.getCaseId(code);
		}
		return randomCaseNo;
		
	}
	public String getAccessionNo() {
		String accessionNo = "";
		int caseid = this.getEducationCaseNo();
		accessionNo = UserDAO.getAccessionNo(caseid);
		HttpSession session = Util.getSession();
		String username = null;
		String password = null;
		if (session.getAttribute("username") != null) {
			username = session.getAttribute("username").toString();
		}
		if (session.getAttribute("password") != null) {
			password = session.getAttribute("password").toString();
		}
		int userid = UserDAO.getUserID(username, password);
		String[] input = new String[0];
		List<String> clearedkeylist = new ArrayList<String>();
		List<UserCaseInput> list = new ArrayList<UserCaseInput>();
		list = UserDAO.getUserCaseInput(caseid);
		String networkcode = UserDAO.getCode(this.getEducationNetwork());
		for (UserCaseInput userCaseInput : list) {
			input = userCaseInput.getValue().split("] ");
			// values.add(input[1]);
			if(input.length> 1 ){
			String[] val = input[1].split("=");
			if (val.length > 0 && nodeNameReverseMapping.get(val[0]).startsWith("CL_")){
				
				userInputs.put(nodeNameReverseMapping.get(val[0]), val[1]);
				
				UserCaseInput caseinput = new UserCaseInput();
				caseinput.setUserid(userid);
				caseinput.setCaseid(caseid);
				caseinput.setSessionid(session.getId());
				caseinput.setEventid(1001);
				caseinput.setPageInfo("Education");
				caseinput.setValue("[" + networkcode + "]" + " " + val[0]+ "=" + val[1]);
				UserDAO.SaveFeatureforOthers(caseinput);
			}
			}
			
		}
		if(!userInputs.isEmpty() && !userInputs.keySet().isEmpty()){
		for (Map.Entry<String, String> entry : userInputs.entrySet()){
			if(entry.getValue().contains("[Clear]"))
			{
				clearedkeylist.add(entry.getKey());
			}
		}
		}
		if(clearedkeylist != null && !clearedkeylist.isEmpty()){
		for (String key : clearedkeylist){
		userInputs.remove(key);
		}
		}
		return accessionNo;
	}
	
	public String getResearchAccessionNumber() {
		System.out.println("ServerModel.getResearchAccessionNumber()");
		if ( ! this.researchNetworkLoaded ) {
		//tring accessionNo = "";
		userInputs.clear();
		
	    int caseid = this.getRandomCaseNo();
	    HttpSession session = Util.getSession();
		String username = null;
		String password = null;
		if(session.getAttribute("username") != null) {
			username = session.getAttribute("username").toString();
		}
		if(session.getAttribute("password") != null) {
			password = session.getAttribute("password").toString();
		}
		int userid = UserDAO.getUserID(username, password);
		accessionNo = UserDAO.getAccessionNo(caseid,userid);
		
		System.out.println("accessionNo: " + accessionNo);
		System.out.println("caseid: " + caseid);
		
		
		String[] input = new String[0];
		List<UserCaseInput> list = new ArrayList<UserCaseInput>();
		list = UserDAO.getUserCaseInput(caseid);
		
		for(UserCaseInput userCaseInput:list) {
			
			System.out.println(userCaseInput.getValue());
			input = userCaseInput.getValue().split("] ");
			System.out.println("input: "+input[0]);
	
			if(input.length > 1) {
				String[] val = input[1].split("=");
				String nodeName = val[0].replace("_",  " ");
				System.out.println("nodeName: " + nodeName);
				System.out.println(val.length);
				System.out.println(nodeNameReverseMapping.get(nodeName));
				
				// FIXME - temp hack to figure out why this happens
				String fullNodeName = nodeNameReverseMapping.get(nodeName);
				if ( fullNodeName == null ) {
					System.out.println("Couldn't find mapping for node: " + nodeName);
					fullNodeName = "XX_";
				}
				
				if(val.length > 0 && fullNodeName.startsWith("CL_")) {
					System.out.println("val: " + val[0]);
					String networkcode = UserDAO.getCode(this.activeNetwork);
					System.out.println("network code: " + networkcode);
					userInputs.put(nodeNameReverseMapping.get(val[0]),val[1]);
					UserCaseInput caseinput = new UserCaseInput();
					caseinput.setUserid(userid);
					caseinput.setCaseid(caseid);
					caseinput.setSessionid(session.getId());
					caseinput.setEventid(1001);
					caseinput.setPageInfo("Research");
					caseinput.setValue("["+networkcode+"]"+" "+val[0]+"="+val[1]);
					UserDAO.SaveFeatureforOthers(caseinput);
			    }
				else {
					System.out.println("skipped this value");
				}
			}
		}
			this.researchNetworkLoaded = true;
		}
		 
		return this.accessionNo;
	}
	
	public String getEducationAccessionNumber() {
		System.out.println("ServerModel.getEducationAccessionNumber()");
		//String accessionNo ="";
		if ( !educationNetworkLoaded ) {
			System.out.println("Reload edu network");
			this.educationNetworkLoaded = true;
			
		userInputs.clear();
		
	    int caseid = this.getEducationCaseNo();
	    HttpSession session = Util.getSession();
		String username = null;
		String password = null;
		if(session.getAttribute("username") != null) {
			username = session.getAttribute("username").toString();
		}
		if(session.getAttribute("password") != null) {
			password = session.getAttribute("password").toString();
		}
		int userid = UserDAO.getUserID(username, password);
		this.accessionNo = UserDAO.getAccessionNo(caseid,userid);
		
		System.out.println("accessionNo: " + accessionNo);
		System.out.println("caseid: " + caseid);
		
		
		String[] input = new String[0];
		List<UserCaseInput> list = new ArrayList<UserCaseInput>();
		list = UserDAO.getUserCaseInput(caseid);
		for(UserCaseInput userCaseInput:list) {
			
			System.out.println(userCaseInput.getValue());
			input = userCaseInput.getValue().split("] ");
			System.out.println("input: "+input[0]);
	
			if(input.length > 1) {
				String[] val = input[1].split("=");
				String nodeName = val[0].replace("_",  " ");
				System.out.println("nodeName: " + nodeName);
				System.out.println(val.length);
				System.out.println(nodeNameReverseMapping.get(nodeName));
				
				// FIXME - temp fix
				String fullNodeName = nodeNameReverseMapping.get(nodeName);
				if ( fullNodeName == null ) {
					System.out.println("Unable to find mapping for node: " + nodeName);
					fullNodeName = "XX_";
				}
				
				if(val.length > 0 && fullNodeName.startsWith("CL_")) {
					System.out.println("val: " + val[0]);
					String networkcode = UserDAO.getCode(this.getEducationNetwork());
					System.out.println("network code: " + networkcode);
					userInputs.put(nodeNameReverseMapping.get(nodeName),val[1]);
					UserCaseInput caseinput = new UserCaseInput();
					caseinput.setUserid(userid);
					caseinput.setCaseid(caseid);
					caseinput.setSessionid(session.getId());
					caseinput.setEventid(1001);
					caseinput.setPageInfo("Research");
					caseinput.setValue("["+networkcode+"]"+" "+val[0]+"="+val[1]);
					UserDAO.SaveFeatureforOthers(caseinput);
			    }
				else {
					System.out.println("skipped this value");
				}
			}
		}
		}
				 
		return this.accessionNo;
	}


	 public List<String> getUsersList(){
		 List<String> users = new ArrayList<String>();
		 
		 //users = UserDAO.getUserName();
		 if(this.getNwName() != null && !"".equalsIgnoreCase(this.getNwName())){
			 String qcperson = UserDAO.getQcPerson(this.getNwName());
			 users.add(qcperson);
		 }
		 /*if(this.getNwName().equalsIgnoreCase("Neuro - Basal Ganglia")){
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
		 }*/
		return users;
		 
	 }
	 public List<String> getSelectCaseList(){
	    	/*
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
	    	} */
			return this.caseListforNw;
	    	
	    }
	 
	 public String doQC() {
		 
		 System.out.println("doQC()");
		 System.out.println("  -- for case: " + caseNoforQC);

		 this.setFromQcPage("true");
		 userInputs.clear();
		 
		 CaseList caselist = new CaseList();
		 
		 caselist = UserDAO.getCaseList(caseNoforQC);
		 if ( caselist != null ) {
			 this.qcCaseLoaded = 1;			 
			 
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
			// this.setQcFlag("true");
			// this.setFeatureFlag("false"); 
			// QualityForm qForm = new QualityForm();
			// qForm.setQcFlag("true"); 
			 
			 this.setEvent("FEATURE ENTERED");
			 String[] input = new String[0];
			// List<String> values = new ArrayList<String>();
			 
			 List<UserCaseInput> list = new ArrayList<UserCaseInput>();
			 list = UserDAO.getUserCaseInput(caseNoforQC);
			 
			 System.out.println("userInput size: " + userInputs.size() );
			 
			 for (UserCaseInput userCaseInput:list) {
				 System.out.println( "  -- case input value: " + userCaseInput.getValue());
				 input = userCaseInput.getValue().split("] ");
				// values.add(input[1]);
				 if ( input.length > 1 ) {
					 String[] val = input[1].split("=");
					 String nodeDisplayName = val[0].replace("_", " ");
					 userInputs.put(nodeNameReverseMapping.get(nodeDisplayName),val[1]);
				 }
				 else {
					 // FIXME - if disease set to correctDX ? 
				 }
			 }
		 } 
		 return "qualityControl";
		 
		 //return "caseInput_form?faces-redirect=true";
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
		if(nwNameforEducation != null && !nwNameforEducation.equalsIgnoreCase(this.getNwNameforEducation())){
			this.setEducationErrMsg("");
			userInputs.clear();
			userInputs1.clear();
			userInputs2.clear();
			dbFeatures.clear();
			System.out.println("Clearing User Inputs");
			}
		this.nwNameforEducation = nwNameforEducation;
	}

	public String getEducationErrMsg() {
		String errMsg;
		if(educationErrorMessages == null || educationErrorMessages.size() == 0) {
			errMsg ="";
		}
		else {
			errMsg = "<FONT COLOR=RED><B><UL>\n";
			for(String message: educationErrorMessages) {
				errMsg = errMsg + "<LI>" + message + "\n";
			}
			errMsg = errMsg + "</UL></B></FONT>\n";
			System.out.println("Education Err: "+errMsg);
		}
		
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
	 public String getComments() {
			return comments;
		}

		public void setComments(String comments) {
			this.comments = comments;
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
