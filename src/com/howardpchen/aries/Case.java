package com.howardpchen.aries;

import com.howardpchen.aries.network.*;

public class Case {
	/*
	 * Need some way of keeping track of the images each case uses
	 */
	
	private NetworkWrapper network;
	
	public Case(String filename) {
		try {
			setNetwork(new DNETWrapper(filename));
		} catch (NetworkLoadingException e) {
			System.err.println("Error loading network in Case");
			//e.printStackTrace();
		}
	}
	
	public Case(NetworkWrapper w) {
		setNetwork(w);
	}

	public NetworkWrapper getNetwork() {
		return network;
	}

	public void setNetwork(NetworkWrapper w) {
		this.network = w;
	}
	
	
}
