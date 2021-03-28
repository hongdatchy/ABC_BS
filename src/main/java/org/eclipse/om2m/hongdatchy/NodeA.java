package org.eclipse.om2m.hongdatchy;

import org.eclipse.om2m.hongdatchy.common.RestHttpClient;
import org.json.JSONObject;
 
public class NodeA {
 
	private static String originator="admin:admin";
	private static String cseProtocol="http";
	private static String cseIp = "127.0.0.1";
	private static int csePort = 8181;
	private static String cseId = "mn-cse-a";
	private static String cseName = "mn-name-a";
	private static String aeName = "node-a";
	private static String cntName1 = "data";
	
	private static String csePoa = cseProtocol+"://"+cseIp+":"+csePort;
 
	
	private static String cseIdIn ="in-cse";
	private static String cseNameIn = "in-name";
	private static String aeNameIn = "bs-server";
	private static String cntNameIn = "service";
	public static void main(String[] args) {
		
		JSONObject obj = new JSONObject();
		obj.put("rn", aeName);
		obj.put("api", 12345);
		obj.put("rr", false);
		JSONObject resource = new JSONObject();
		resource.put("m2m:ae", obj);
		RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName, resource.toString(), 2);
		
//		create cnt data
        obj = new JSONObject();
        obj.put("rn", cntName1);
        resource = new JSONObject();
        resource.put("m2m:cnt", obj);
		RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+aeName, resource.toString(), 3);
		
		obj = new JSONObject();
		obj.put("cnf", "application/text");
		JSONObject con = new JSONObject();
		con.put("service", "getsum");
		con.put("parameter", "Total");
		con.put("destination", "c");
		obj.put("con", con.toString());
		resource = new JSONObject();
		resource.put("m2m:cin", obj);
		RestHttpClient.post(originator, csePoa+"/~/"+cseIdIn+"/"+cseNameIn+"/"+aeNameIn+"/"+cntNameIn, resource.toString(), 4);
	}
	
}