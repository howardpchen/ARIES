package com.howardpchen.aries;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSessionBindingEvent;

import com.howardpchen.aries.network.DNETWrapper;
import com.howardpchen.aries.network.NetworkLoadingException;
import com.howardpchen.aries.network.NetworkWrapper;

@ManagedBean
@SessionScoped
public class ServerModel {
	/**
	 * 
	 */
//	private static final long serialVersionUID = -9026586621419425189L;
	
	private Map<String, String> userInputs;
	private NetworkWrapper dw;
	private String pageLoad = ""; 
	
			
	public ServerModel() {
		userInputs = new HashMap<String, String>();
		registerSession();
		System.out.println("Called constructor.");
	}
	
	public void setNodeInput(String s) {
		String[] inputs = s.split(":");
		if (inputs.length == 2)	userInputs.put(inputs[0], inputs[1]);
	}
	
	public String getNodeInput() {
		return "";
	}
	
	public String getNodeInputString() {
		if (userInputs.size() == 0) return "None so far.";
		StringBuffer sb = new StringBuffer("");
		Set<String> s = userInputs.keySet();
		Iterator<String> it = s.iterator();
		sb.append("<table>");
		while (it.hasNext()) {
			String key = it.next();
			sb.append("<tr><td>").append(key).append("<td>").append(userInputs.get(key)).append("</tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}
	
	public String nodeInputHandler() {
	    return("index");  // return to index or refresh index
	}
	
	public String getTestNodes() {
		System.out.println("Called getTestNodes()");
		String[] nodeNames = dw.getNodeNames();
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < nodeNames.length; i++) {
			if (nodeNames[i].equals("Disease")) continue;
			sb.append("<p class='node-title'>" + nodeNames[i] + "</p>");
			sb.append("<table>");
			Map<String, Double> values = dw.getNodeProbs(nodeNames[i]);
			Set<String> s = values.keySet();
			Iterator<String> it = s.iterator();
			while (it.hasNext()) {
				String key = it.next();
				sb.append("<tr><td>" + key).append("<td>" + values.get(key)).append("</tr>");
			}
			sb.append("</table>");
		}
		return sb.toString();
	}

	public List<String> getSelectMenuInputs() {
		List<String> returnString = new ArrayList<String>();
		returnString.add("-- Click to Select --");
		String[] nodeNames = dw.getNodeNames();
		for (int i = 0; i < nodeNames.length; i++) {
			if (nodeNames[i].equals("Disease")) continue;
			Map<String, Double> values = dw.getNodeProbs(nodeNames[i]);
			Set<String> s = values.keySet();
			Iterator<String> it = s.iterator();
			while (it.hasNext()) {
				String key = it.next();
				returnString.add(nodeNames[i] + ":" + key);
			}			
			returnString.add(nodeNames[i] + ":[Clear]");
			returnString.add("------------------------------");
		}
		
		return returnString;
	}
	
	public String getDiagnosisNode() {
		// Update the diagnosis node first
		
		Set<String> s = userInputs.keySet();
		Iterator<String> it = s.iterator();
		while (it.hasNext()) {
			String key = it.next();
			String response = userInputs.get(key);
			if (response.equals("[Clear]")) {
				userInputs.remove(key);
				dw.clearNodeState(key);
			}
			else dw.setNodeState(key, response);
		}
		
		// Then produce the node output
		
		StringBuffer sb = new StringBuffer("");
		sb.append("<table>");
		Map<String, Double> values = dw.getDiagnosisProbs();
		s = values.keySet();
		it = s.iterator();
		while (it.hasNext()) {
			String key = it.next();
			sb.append("<tr><td>" + key).append("<td>").append(convertToPercentage(values.get(key))).append("%</tr>");
		}			
		sb.append("</table>");
		return sb.toString();
	}


	public void setPrePageLoad(String pl) {
		this.pageLoad = pl;
	}
	
	public String getPrePageLoad() {
	    System.out.println("DNET Wrapper session started");
		try {
			dw = new DNETWrapper("WebContent/Neuro.dne");
		} catch (NetworkLoadingException e) {
			System.out.println ("Error loading the network.");
			
		}
		return this.pageLoad;
	}

	public void setPostPageLoad(String pl) {
		this.pageLoad = pl;
	}
	
	public String getPostPageLoad() {
	    dw.endSession();
	    System.out.println("DNET Wrapper session ended");
		return this.pageLoad;
	}
	
	public static double convertToPercentage(Double d) {
		double db = d.doubleValue();
		return Math.round(db * 1000)/10d;
	}


	public void valueBound(HttpSessionBindingEvent event) {
	    System.out.println("valueBound:" + event.getName() + " session:" + event.getSession().getId() );
	}

	public void registerSession() {
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put( "sessionBindingListener", this  );
		System.out.println( "registered sessionBindingListener"  );
	}

	public void valueUnbound(HttpSessionBindingEvent event) {
		System.out.println("valueUnBound:" + event.getName() + " session:" + event.getSession().getId() );
	}

	
}
