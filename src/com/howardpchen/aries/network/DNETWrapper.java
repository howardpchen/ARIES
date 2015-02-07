package com.howardpchen.aries.network;

import java.util.ArrayList;

import norsys.netica.*;

public class DNETWrapper implements NetworkWrapper {
	Environ env;
	Net net;
	public DNETWrapper(String filename) {
		try {
			env = new Environ(null);
			net = new Net(new Streamer(filename));
		} catch (NeticaException e) {
			e.printStackTrace();
		}
		
				
	}
	
	@Override
	public Vertex getDiagnosisNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Vertex> getVertices() {
		// TODO Auto-generated method stub
		return null;
	}
	
}

