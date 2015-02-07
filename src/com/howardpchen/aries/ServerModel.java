package com.howardpchen.aries;

import java.util.ArrayList;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean
@SessionScoped
public class ServerModel {
	private AriesModule currentModule;
	private ArrayList<AriesCase> cases;
	private AriesCase currentCase;
	
	public ServerModel() {
		
	}
	
}
