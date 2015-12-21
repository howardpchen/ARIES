package com.howardpchen.aries.network;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import norsys.netica.*;

public class DNETWrapper extends NetworkWrapper implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 223891435872906573L;
	Environ env;
	Net net;
	public DNETWrapper(String filename) throws NetworkLoadingException {
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
		
		return returnMap;
	}
	
	//added for CR102
	public String getHighestSensitiveNodeName(Map<String, String> userInputs)
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
	
}

