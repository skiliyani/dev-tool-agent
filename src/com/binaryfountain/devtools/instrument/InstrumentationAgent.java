package com.binaryfountain.devtools.instrument;

import java.lang.instrument.Instrumentation;

import com.binaryfountain.devtools.instrument.transformer.DBMetedataTransformer;
import com.binaryfountain.devtools.instrument.transformer.DBTableTransformer;

/**
 * Java Agent used for instrumenting/transforming application classes.
 * 
 * @author Kiliyani
 *
 */
public class InstrumentationAgent {
	public static void premain(String agentArgs, Instrumentation inst) {
		System.out.println("[Agent] Dev Tool Agent Starting. Args: " + agentArgs);
		
		String url = null;
		String username = null;
		String password = null;

		if (agentArgs != null) {
			String[] args = agentArgs.split("&");
			for (int i = 0; i < args.length; i++) {
				String arg = args[i];
				String[] keyValue = arg.split("=");
				if (keyValue.length == 2) {
					if (keyValue[0].equals("url")) {
						url = keyValue[1];
						System.out.println("[Agent] Using MySQL URL:" + url);
					} else if (keyValue[0].equals("username")) {
						username = keyValue[1];
						System.out.println("[Agent] Using MySQL username:" + username);
					} else if (keyValue[0].equals("password")) {
						username = keyValue[1];
						System.out.println("[Agent] Using MySQL password:" + username);
					}
				}
			}
		}

		inst.addTransformer(new DBMetedataTransformer(url, username, password), true);
		inst.addTransformer(new DBTableTransformer(), true);
	}

}
