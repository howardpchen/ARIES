package com.howardpchen.aries.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;



import norsys.netica.*;

public class DNETWrapper extends NetworkWrapper implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 223891435872906573L;
	Environ env;
	Net net;
	public DNETWrapper(String filename) throws NetworkLoadingException{
		try {
			
			env = new Environ("+BotzolakisE/UPenn/120,310-6-A/53080");
			net = new Net(new Streamer(filename));
			net.compile();
			
		} catch (NeticaException e) {
			System.err.println("Problem loading network on Netica.");
			e.printStackTrace();
			throw new NetworkLoadingException();
		}
	}
	
	@Override
	public Map<String, Double> getNodeProbs1(String nodeName) {
		Map<String, Double> returnMap = new HashMap<String, Double>();
		if(!nodeName.startsWith("CL_")){
		
		Node myNode = null;
		try {
			myNode = net.getNode(nodeName);
			int st = net.getNode(nodeName).getNumStates();
			for (int i = 0; i < st; i++) {
				String title = myNode.state(i).getName();
				double val = myNode.getBelief(title);
				returnMap.put(title, val);
			}
		} catch (NeticaException e) {
			System.err.println("Error getting node probabilities.");
			//e.printStackTrace();
		}
		}
		return returnMap;
	}
	
	@Override
	public Map<String, Double> getNodeProbs(String nodeName) {
		Map<String, Double> returnMap = new HashMap<String, Double>();
		
		Node myNode = null;
		try {
			myNode = net.getNode(nodeName);
			int st = net.getNode(nodeName).getNumStates();
			for (int i = 0; i < st; i++) {
				String title = myNode.state(i).getName();
				double val = myNode.getBelief(title);
				returnMap.put(title, val);
			}
		} catch (NeticaException e) {
			System.err.println("Error getting node probabilities.");
			//e.printStackTrace();
		}
		System.out.println(nodeName+" : "+returnMap);
		
		return returnMap;
	}
	
	//added for CR102
	public String getHighestSensitiveNodeName(Map<String, String> userInputs,TreeMap<String, List<String>> prefixNodeListMapping)
	{
		String highestSensitiveNodeName = null;
		try {
			
			Map<String, String> selectedNodeStateMapping = new HashMap<String, String>();
			for (Map.Entry<String, String> entry : userInputs.entrySet())
			{
				String state = entry.getValue();

				if("[Clear]".equals(state))
				{
					continue;
				}
				else
				{
					selectedNodeStateMapping.put(entry.getKey(), entry.getValue());
				}
			}			
			Node targetNode = net.getNode("Diseases");		
			NodeList nodeList = new NodeList(net);
			nodeList.clear();
			
			NodeList nl = net.getNodes();
			@SuppressWarnings("unchecked")
			Iterator<Node> it = nl.iterator();
			int current = 0;
			while (it.hasNext())
			{
				Node n = it.next();
				
				if(selectedNodeStateMapping.containsKey(n.getName()))
				{
					n.finding().setState(selectedNodeStateMapping.get(n.getName()));
				}
				
				if(!"Diseases".equals(n.getName()))
				{
					nodeList.add(n);
				}
			}
			System.out.println("nodeList .... "+nodeList.size());
			System.out.println(nodeList.toString());
			Sensitivity sensitivity = new Sensitivity(targetNode, nodeList, Sensitivity.ENTROPY_SENSV);
			Double highestSensitiveValue = 0.0d;
		
			
			for (String varyingNodeName : getNodeNames()) 
			{
				if("Diseases".equals(varyingNodeName))
				{
					continue;
				}
				
				Double nextValue = sensitivity.getMutualInfo(net.getNode(varyingNodeName));
				if(nextValue > highestSensitiveValue)
				{
					highestSensitiveValue = nextValue;
					highestSensitiveNodeName = varyingNodeName;
				}
				
				/*System.err.println( varyingNodeName + " : " + nextValue.toString());*/
			}
			
			sensitivity.finalize();
			
		} catch (NeticaException e) {
			System.err.println("Problem calculating the senstivity");
			//e.printStackTrace();
		}
		
		
		return highestSensitiveNodeName;
	}	

	@Override
	public String[] getStates(String nodeName) {
		String[] states = null;
		Node myNode = null;
		try {
			myNode = net.getNode(nodeName);
			int st = net.getNode(nodeName).getNumStates();
			states = new String[st];
			for (int i = 0; i < st; i++) {
				String title = myNode.state(i).getName();
				states[i] = title;
			}
		} catch (NeticaException e) {
			System.err.println("Problem getting node states for: " + nodeName);
			//e.printStackTrace();
		}
		return states;
	}
	
	@Override
	public String[] getNodeNames() {
		String[] names = null;
		
		try {
			names = new String[net.getNodes().size()];
			NodeList nl = net.getNodes();
			@SuppressWarnings("unchecked")
			Iterator<Node> it = nl.iterator();
			int current = 0;
			while (it.hasNext()){
				Node n = it.next();
				names[current++] = n.getName();
			}
		} catch (NeticaException e) {
			e.printStackTrace();
		}
		
		return names;
	}
	
	@Override
	public void setNodeState(String nodeName, String state) {
		Node myNode;
		try {
			myNode = net.getNode(nodeName);
			if (myNode != null) myNode.finding().setState(state);
			
		} catch (NeticaException e) {
			System.err.println("Error: Node not found!");
		}
		
	}
	
	public void clearNodeState(String nodeName) {
		Node myNode;
		try {
			myNode = net.getNode(nodeName);
			if (myNode != null) myNode.finding().clear();
			
		} catch (NeticaException e) {
			System.err.println("Error: Node not found!");
		}
		
	}
	@Override
	public void endSession() {
		try {
			net.finalize();
			env.finalize();
		} catch (NeticaException e) {
			System.err.println("Error finalizing existing Network session");
			//e.printStackTrace();
		}
	}
	@Override
	public List<String> getSINodeNames() {
		List<String> SINames = new ArrayList<String>();
		for(String SINodeName:getNodeNames()){
			if(SINodeName.startsWith("SI_")){
				SINames.add(SINodeName);
			}
		}
		return SINames;
		}
	@Override
	public List<String> getSPNodeNames() {
		List<String> SPNames = new ArrayList<String>();
		for(String SPNodeName:getNodeNames()){
			if(SPNodeName.startsWith("SP_")){
				SPNames.add(SPNodeName);
			}
		}
		return SPNames;
	}
	@Override
	public List<String> getCLNodeNames() {
		List<String> CLNames = new ArrayList<String>();
		for(String CLNodeName:getNodeNames()){
			if(CLNodeName.startsWith("CL_")){
				CLNames.add(CLNodeName);
			}
		}
		return CLNames;
	}
	@Override
	public List<String> getMSNodeNames() {
		List<String> MSNames = new ArrayList<String>();
		for(String MSNodeName:getNodeNames()){
			if(!MSNodeName.startsWith("SI_") && !MSNodeName.startsWith("SP_") && !MSNodeName.startsWith("CL_") && !("Diseases").equals(MSNodeName)){
				MSNames.add(MSNodeName);
			}
		}
		return MSNames;
	}
	
	// added for CR103
		@SuppressWarnings("unchecked")
		public String getSensitiveForDisease(String disease,Map<String, String> userInputs) {
			String sensitiveForDisease = null;
			try {
				Map<String, String> userSIInputs = new HashMap<String, String>();
				Map<String, String> selectedSINodeStateMapping = new HashMap<String, String>();
				System.out.println("userInputs.entrySet().." + userInputs.entrySet());
				for (Map.Entry<String, String> entry : userInputs.entrySet()) {
					if (entry.getKey().contains("SI_")) {
						userSIInputs.put(entry.getKey(), entry.getValue());
					}
				}
				System.out.println("userSIInputs SIMAP : " + userSIInputs.entrySet());
				for (Map.Entry<String, String> entry : userSIInputs.entrySet()) {
					String state = entry.getValue();

					if ("[Clear]".equals(state)) {
						continue;
					} else {
						selectedSINodeStateMapping.put(entry.getKey(), entry.getValue());
					}
				}
				/*String[] states = null;
				Node targetNode = net.getNode("Diseases");
				int st=targetNode.getNumStates();
				targetNode.remove
				states = new String[st];
			   
					for (int i = 0; i < st; i++) {
						String title = targetNode.state(i).getName();
						states[i] = title;
					}*/
			    
				//targetNode.getNumStates()
				//System.out.println(""+targetNode.state(disease));
				//System.out.println("dkjhd "+targetNode.getNumStates());
				//Node targetNode = new Node("Disease",disease, net);
				//targetNode.setStateFuncTable(arg0, arg1);
				Node targetNode = net.getNode("Diseases");
				
				NodeList nodeList = new NodeList(net);
				nodeList.clear();

				NodeList nl = net.getNodes();

				Iterator<Node> it = nl.iterator();
				int current = 0;
				while (it.hasNext()) {
					Node n = it.next();

					if (selectedSINodeStateMapping.containsKey(n.getName())) {
						n.finding().setState(selectedSINodeStateMapping.get(n.getName()));
					}

					// if(!"Diseases".equals(n.getName()))
					System.out.println("n.getName : " + n.getName());
					if (n.getName().startsWith("SI_")) {
						nodeList.add(n);
					}

				}
				System.out.println("nodeList .... " + nodeList.size());
				System.out.println(nodeList.toString());
                System.out.println(" .... "+targetNode.getNumStates());
				Sensitivity sensitivity = new Sensitivity(targetNode, nodeList, Sensitivity.ENTROPY_SENSV);
				Double highestSensitiveValue = 0.0d;

				for (String varyingNodeName : getSINodeNames()) {
					/*
					 * if("Diseases".equals(varyingNodeName)) { continue; }
					 */

					Double nextValue = sensitivity.getMutualInfo(net.getNode(varyingNodeName));
					if (nextValue > highestSensitiveValue) {
						highestSensitiveValue = nextValue;
						sensitiveForDisease = varyingNodeName;
					}

					/*
					 * System.err.println( varyingNodeName + " : " +
					 * nextValue.toString());
					 */
				}
				targetNode.finalize();
				sensitivity.finalize();

			} catch (NeticaException e) {
				System.err.println("Problem calculating the senstivity");
				e.printStackTrace();
			}

			return sensitiveForDisease;
		}


	// added for CR103
	@SuppressWarnings("unchecked")
	public String getHighestSISensitiveNodeName(Map<String, String> userInputs) {
		String highestSISensitiveNodeName = null;
		try {
			Map<String, String> userSIInputs = new HashMap<String, String>();
			Map<String, String> selectedSINodeStateMapping = new HashMap<String, String>();
			System.out.println("userInputs.entrySet().." + userInputs.entrySet());
			for (Map.Entry<String, String> entry : userInputs.entrySet()) {
				if (entry.getKey().contains("SI_")) {
					userSIInputs.put(entry.getKey(), entry.getValue());
				}
			}
			System.out.println("userSIInputs SIMAP : " + userSIInputs.entrySet());
			for (Map.Entry<String, String> entry : userSIInputs.entrySet()) {
				String state = entry.getValue();

				if ("[Clear]".equals(state)) {
					continue;
				} else {
					selectedSINodeStateMapping.put(entry.getKey(), entry.getValue());
				}
			}
			Node targetNode = net.getNode("Diseases");
			NodeList nodeList = new NodeList(net);
			nodeList.clear();

			NodeList nl = net.getNodes();

			Iterator<Node> it = nl.iterator();
			int current = 0;
			while (it.hasNext()) {
				Node n = it.next();

				if (selectedSINodeStateMapping.containsKey(n.getName())) {
					n.finding().setState(selectedSINodeStateMapping.get(n.getName()));
				}

				// if(!"Diseases".equals(n.getName()))
				System.out.println("n.getName : " + n.getName());
				if (n.getName().startsWith("SI_")) {
					nodeList.add(n);
				}

			}
			System.out.println("nodeList .... " + nodeList.size());
			System.out.println(nodeList.toString());

			Sensitivity sensitivity = new Sensitivity(targetNode, nodeList, Sensitivity.ENTROPY_SENSV);
			Double highestSensitiveValue = 0.0d;

			for (String varyingNodeName : getSINodeNames()) {
				/*
				 * if("Diseases".equals(varyingNodeName)) { continue; }
				 */

				Double nextValue = sensitivity.getMutualInfo(net.getNode(varyingNodeName));
				if (nextValue > highestSensitiveValue) {
					highestSensitiveValue = nextValue;
					highestSISensitiveNodeName = varyingNodeName;
				}

				/*
				 * System.err.println( varyingNodeName + " : " +
				 * nextValue.toString());
				 */
			}

			sensitivity.finalize();

		} catch (NeticaException e) {
			System.err.println("Problem calculating the senstivity");
			// e.printStackTrace();
		}

		return highestSISensitiveNodeName;
	}

	@Override
	public String getHighestSPSensitiveNodeName(Map<String, String> userInputs) {
		String highestSPSensitiveNodeName = null;
		try {
			Map<String, String> userSPInputs = new HashMap<String, String>();
			Map<String, String> selectedSPNodeStateMapping = new HashMap<String, String>();
			System.out.println("userInputs.entrySet().." + userInputs.entrySet());
			for (Map.Entry<String, String> entry : userInputs.entrySet()) {
				if (entry.getKey().contains("SP_")) {
					userSPInputs.put(entry.getKey(), entry.getValue());
				}
			}
			System.out.println("userSIInputs SPMAP : " + userSPInputs.entrySet());
			for (Map.Entry<String, String> entry : userSPInputs.entrySet()) {
				String state = entry.getValue();

				if ("[Clear]".equals(state)) {
					continue;
				} else {
					selectedSPNodeStateMapping.put(entry.getKey(), entry.getValue());
				}
			}
			Node targetNode = net.getNode("Diseases");
			NodeList nodeList = new NodeList(net);
			nodeList.clear();

			NodeList nl = net.getNodes();

			Iterator<Node> it = nl.iterator();
			int current = 0;
			while (it.hasNext()) {
				Node n = it.next();

				if (selectedSPNodeStateMapping.containsKey(n.getName())) {
					n.finding().setState(selectedSPNodeStateMapping.get(n.getName()));
				}

				// if(!"Diseases".equals(n.getName()))
				if (n.getName().startsWith("SP_")) {
					nodeList.add(n);
				}

			}
			System.out.println("nodeList .... " + nodeList.size());
			System.out.println(nodeList.toString());

			Sensitivity sensitivity = new Sensitivity(targetNode, nodeList, Sensitivity.ENTROPY_SENSV);
			Double highestSensitiveValue = 0.0d;

			for (String varyingNodeName : getSPNodeNames()) {
				/*
				 * if("Diseases".equals(varyingNodeName)) { continue; }
				 */

				Double nextValue = sensitivity.getMutualInfo(net.getNode(varyingNodeName));
				if (nextValue > highestSensitiveValue) {
					highestSensitiveValue = nextValue;
					highestSPSensitiveNodeName = varyingNodeName;
				}

				/*
				 * System.err.println( varyingNodeName + " : " +
				 * nextValue.toString());
				 */
			}

			sensitivity.finalize();

		} catch (NeticaException e) {
			System.err.println("Problem calculating the senstivity");
			// e.printStackTrace();
		}

		return highestSPSensitiveNodeName;
	}

	@Override
	public String getHighestCLSensitiveNodeName(Map<String, String> userInputs) {
		String highestCLSensitiveNodeName = null;
		try {
			Map<String, String> userCLInputs = new HashMap<String, String>();
			Map<String, String> selectedCLNodeStateMapping = new HashMap<String, String>();
			System.out.println("userInputs.entrySet().." + userInputs.entrySet());
			for (Map.Entry<String, String> entry : userInputs.entrySet()) {
				if (entry.getKey().contains("CL_")) {
					userCLInputs.put(entry.getKey(), entry.getValue());
				}
			}
			System.out.println("userCLInputs SIMAP : " + userCLInputs.entrySet());
			for (Map.Entry<String, String> entry : userCLInputs.entrySet()) {
				String state = entry.getValue();

				if ("[Clear]".equals(state)) {
					continue;
				} else {
					selectedCLNodeStateMapping.put(entry.getKey(), entry.getValue());
				}
			}
			Node targetNode = net.getNode("Diseases");
			NodeList nodeList = new NodeList(net);
			nodeList.clear();

			NodeList nl = net.getNodes();

			Iterator<Node> it = nl.iterator();
			int current = 0;
			while (it.hasNext()) {
				Node n = it.next();

				if (selectedCLNodeStateMapping.containsKey(n.getName())) {
					n.finding().setState(selectedCLNodeStateMapping.get(n.getName()));
				}

				// if(!"Diseases".equals(n.getName()))
				if (n.getName().startsWith("CL_")) {
					nodeList.add(n);
				}

			}
			System.out.println("nodeList .... " + nodeList.size());
			System.out.println(nodeList.toString());

			Sensitivity sensitivity = new Sensitivity(targetNode, nodeList, Sensitivity.ENTROPY_SENSV);
			Double highestSensitiveValue = 0.0d;

			for (String varyingNodeName : getCLNodeNames()) {
				/*
				 * if("Diseases".equals(varyingNodeName)) { continue; }
				 */

				Double nextValue = sensitivity.getMutualInfo(net.getNode(varyingNodeName));
				if (nextValue > highestSensitiveValue) {
					highestSensitiveValue = nextValue;
					highestCLSensitiveNodeName = varyingNodeName;
				}

				/*
				 * System.err.println( varyingNodeName + " : " +
				 * nextValue.toString());
				 */
			}

			sensitivity.finalize();

		} catch (NeticaException e) {
			System.err.println("Problem calculating the senstivity");
			// e.printStackTrace();
		}

		return highestCLSensitiveNodeName;
	}

	@Override
	public String getHighestMSSensitiveNodeName(Map<String, String> userInputs) {
		String highestMSSensitiveNodeName = null;
		try {
			Map<String, String> userMSInputs = new HashMap<String, String>();
			Map<String, String> selectedMSNodeStateMapping = new HashMap<String, String>();
			System.out.println("userInputs.entrySet().." + userInputs.entrySet());
			for (Map.Entry<String, String> entry : userInputs.entrySet()) {
				if (!(entry.getKey().startsWith("SI_")) && !(entry.getKey().startsWith("SP_"))
						&& !(entry.getKey().startsWith("CL_"))) {
					userMSInputs.put(entry.getKey(), entry.getValue());
				}
			}
			System.out.println("userMSInputs MSMAP : " + userMSInputs.entrySet());
			for (Map.Entry<String, String> entry : userMSInputs.entrySet()) {
				String state = entry.getValue();

				if ("[Clear]".equals(state)) {
					continue;
				} else {
					selectedMSNodeStateMapping.put(entry.getKey(), entry.getValue());
				}
			}
			Node targetNode = net.getNode("Diseases");
			NodeList nodeList = new NodeList(net);
			nodeList.clear();

			NodeList nl = net.getNodes();

			Iterator<Node> it = nl.iterator();
			int current = 0;
			while (it.hasNext()) {
				Node n = it.next();

				if (selectedMSNodeStateMapping.containsKey(n.getName())) {
					n.finding().setState(selectedMSNodeStateMapping.get(n.getName()));
				}

				// if(!"Diseases".equals(n.getName()))
				if ((!"Diseases".equals(n.getName()) && !(n.getName().startsWith("SI_"))
						&& !(n.getName().startsWith("SP_")) && !(n.getName().startsWith("CL_")))) {
					nodeList.add(n);
				}

			}
			System.out.println("nodeList .... " + nodeList.size());
			System.out.println(nodeList.toString());
			if (nodeList.size() > 0) {
				Sensitivity sensitivity = new Sensitivity(targetNode, nodeList, Sensitivity.ENTROPY_SENSV);
				Double highestSensitiveValue = 0.0d;

				for (String varyingNodeName : getMSNodeNames()) {
					if ("Diseases".equals(varyingNodeName)) {
						continue;
					}

					Double nextValue = sensitivity.getMutualInfo(net.getNode(varyingNodeName));
					if (nextValue > highestSensitiveValue) {
						highestSensitiveValue = nextValue;
						highestMSSensitiveNodeName = varyingNodeName;
					}

					/*
					 * System.err.println( varyingNodeName + " : " +
					 * nextValue.toString());
					 */
				}

				sensitivity.finalize();
			}
		} catch (NeticaException e) {
			System.err.println("Problem calculating the senstivity");
			// e.printStackTrace();
		}

		return highestMSSensitiveNodeName;

	}
	
}

