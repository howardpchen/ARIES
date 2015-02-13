package com.howardpchen.aries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import com.howardpchen.aries.network.DNETWrapper;
import com.howardpchen.aries.network.NetworkWrapper;

@ManagedBean
@SessionScoped
public class ServerModel {
	private Session currentSession;

	public ServerModel() {
		
	}
	
	public String getTestNodes() {
		NetworkWrapper dw = new DNETWrapper("../ARIES/WebContent/Neuro.dne");
		Map<String, Double> values = dw.getNodeProbs("T1");
		StringBuffer sb = new StringBuffer("");
		Iterator<String> it = values.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			sb.append(key).append(values.get(key));
		}
		return sb.toString();
	}
	
}
