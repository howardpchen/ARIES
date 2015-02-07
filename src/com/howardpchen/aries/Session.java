package com.howardpchen.aries;

import java.util.ArrayList;

public class Session {
	private ArrayList<Case> myCases;
	private int caseIndex;

	public Session() {
		myCases = new ArrayList<Case>();
		caseIndex = 0;
	}
	
	public Case getCurrentCase() {
		return myCases.get(caseIndex);
	}
	
	public void nextCase() {
		caseIndex = (caseIndex + 1) % myCases.size();
	}
	
	public boolean isLastCase() {
		return (caseIndex == myCases.size()-1) ? true:false;
	}
	
}

