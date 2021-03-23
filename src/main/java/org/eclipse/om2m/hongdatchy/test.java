package org.eclipse.om2m.hongdatchy;

import org.eclipse.om2m.hongdatchy.common.HttpResponse;
import org.eclipse.om2m.hongdatchy.common.RestHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

public class test {
	
	private static String originator="admin:admin";
	private static String cseProtocol="http";
	private static String cseIp = "127.0.0.1";
	private static int csePort = 8080;
	private static String cseId = "mn-cse-b";
	private static String cseName = "mn-name-b";
	private static String aeName = "node-b";
	private static String cntName = "x";
	private static String csePoa = cseProtocol+"://"+cseIp+":"+csePort;
	private static String targetNodeBContainer = cseId + "/" + cseName + "/" + aeName + "/" + cntName;
	private static String subName = "node-b-sub-x";

	public static void main(String[] args) {
		JSONArray array = new JSONArray();
		JSONObject obj = new JSONObject();
		JSONObject resource = new JSONObject();
		array.put("/"+cseId+"/"+cseName+"/"+aeName);
		obj = new JSONObject();
		obj.put("nu", array);
		obj.put("rn", subName);
		obj.put("nct", 2);
		resource = new JSONObject();		
		resource.put("m2m:sub", obj);
		RestHttpClient.post(originator, csePoa+"/~/"+targetNodeBContainer, resource.toString(), 23);
	}
}
