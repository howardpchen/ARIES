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
	public DNETWrapper(String filename) {
		try {
			env = new Environ(null);
			net = new Net(new Streamer(filename));
			net.compile();
		} catch (NeticaException e) {
			System.err.println("Problem loading network.");
			e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return returnMap;
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
			e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return names;
	}
	
}

