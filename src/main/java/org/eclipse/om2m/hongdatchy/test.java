package org.eclipse.om2m.hongdatchy;

import org.eclipse.om2m.hongdatchy.common.RestHttpClient;

public class test {
	
	private static String originator="admin:admin";
	private static String cseProtocol="http";
	private static String cseIp = "127.0.0.1";
	private static int csePort = 8080;
	private static String cseId = "mn-cse-b";
	private static String cseName = "mn-name-b";
 
	private static String aeName = "node-b";
	private static String cntName1 = "data";
 
	private static String csePoa = cseProtocol+"://"+cseIp+":"+csePort;

	public static void main(String[] args) {
		RestHttpClient.get(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+aeName+"/"+cntName1 + "/la");
//		RestHttpClient.get(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+aeName+"/"+cntName1 + "/cin_809572660");
//		System.out.println(httpResponse.getBody());
	}
}
