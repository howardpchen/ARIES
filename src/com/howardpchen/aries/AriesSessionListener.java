package com.howardpchen.aries;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class AriesSessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent arg0) {
	
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent arg0) {
		System.out.println("Session ended.  Processing garbage collection for Netica...");
		//dw.endSession();
		System.out.println("Netica environment closed.");
		
		
	}


}
