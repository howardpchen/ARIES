package com.howardpchen.aries;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import com.howardpchen.aries.network.DNETWrapper;
import com.howardpchen.aries.network.NetworkWrapper;

@ManagedBean
@SessionScoped
public class ServerModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9026586621419425189L;
	private NetworkWrapper dw; 
			
	public ServerModel() {
		dw = new DNETWrapper("WebContent/Neuro.dne");
		System.out.println("Called constructor.");
	}
	
	public String getTestNodes() {
		System.out.println("Called getTestNodes()");
		String[] nodeNames = dw.getNodeNames();
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < nodeNames.length; i++) {
			sb.append("<P>Node Name: " + nodeNames[i]);
			Map<String, Double> values = dw.getNodeProbs(nodeNames[i]);
			Set<String> s = values.keySet();
			Iterator<String> it = s.iterator();
			while (it.hasNext()) {
				String key = it.next();
				sb.append("<LI>[" + key).append("] " + values.get(key));
			}			
		}
		return sb.toString();
	}
	
	public String getDiagnosisNode() {
		StringBuffer sb = new StringBuffer("");
		sb.append("<P>DIFFERENTIAL DIAGNOSIS: ");
		Map<String, Double> values = dw.getDiagnosisProbs();
		Set<String> s = values.keySet();
		Iterator<String> it = s.iterator();
		while (it.hasNext()) {
			String key = it.next();
			sb.append("<LI>[" + key).append("] " + values.get(key));
		}			
		return sb.toString();
	}
	

}
