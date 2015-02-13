package com.howardpchen.aries.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import norsys.netica.*;

public class DNETWrapper extends NetworkWrapper {
	Environ env;
	Net net;
	public DNETWrapper(String filename) {
		try {
			env = new Environ(null);
			net = new Net(new Streamer(filename));
		} catch (NeticaException e) {
			System.err.println("Problem loading network.");
			e.printStackTrace();
		}
	}
	@Override
	public Map<String, Double> getNodeProbs(String nodeName) {
		HashMap<String, Double> returnMap = new HashMap();
		Node myNode = null;
		try {
			myNode = net.getNode(nodeName);
			int st = net.getNode(nodeName).getNumStates();
			for (int i = 0; i < st; i++) {
				String title = myNode.state(i).getTitle();
				double val = myNode.state(i).getNumeric();
				returnMap.put(title, val);
			}
		} catch (NeticaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
}

