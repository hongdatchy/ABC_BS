package org.eclipse.om2m.hongdatchy;
 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;

import org.eclipse.om2m.hongdatchy.BsServer.MyHandler;
import org.eclipse.om2m.hongdatchy.common.HttpResponse;
import org.eclipse.om2m.hongdatchy.common.RestHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
 
public class NodeB {
 
	private static String originator="admin:admin";
	private static String cseProtocol="http";
	private static String cseIp = "127.0.0.1";
	private static int csePort = 8282;
	private static String cseId = "mn-cse-b";
	private static String cseName = "mn-name-b";
	private static String aeProtocol="http";
	private static String aeName = "node-b";
	private static String cntName1 = "data";
	private static String cntName2 = "command";
	private static String aeIp = "127.0.0.1";
	private static String csePoa = cseProtocol+"://"+cseIp+":"+csePort;
	private static String targetNodeBContainer = cseId + "/" + cseName + "/" + aeName + "/" + cntName2;
	private static String subName = "node-b-sub";
	private static int aePort = 1600;
	private static String appPoa = aeProtocol+"://"+aeIp+":"+aePort;
	private static String cseIdIn ="in-cse";
	private static String cseNameIn = "in-name";
	private static String aeNameIn = "bs-server";
	private static String cntNameIn = "result";
	
	public static void main(String[] args) throws FileNotFoundException {
		
		HttpServer server = null;
		try {
			server = HttpServer.create(new InetSocketAddress(aePort), 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		server.createContext("/", new MyHandler());
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();
		
		JSONArray array = new JSONArray();
		array.put(appPoa);
		JSONObject obj = new JSONObject();
		obj.put("rn", aeName);
		obj.put("api", 12345);
		obj.put("rr", true);
		obj.put("poa",array);
		JSONObject resource = new JSONObject();
		resource.put("m2m:ae", obj);
		RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName, resource.toString(), 2);
		
//		create cnt data
        obj = new JSONObject();
        obj.put("rn", cntName1);
        resource = new JSONObject();
        resource.put("m2m:cnt", obj);
		RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+aeName, resource.toString(), 3);
		
//		create cnt command
        obj = new JSONObject();
        obj.put("rn", cntName2);
        resource = new JSONObject();
        resource.put("m2m:cnt", obj);
		RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+aeName, resource.toString(), 3);

//		sub cnt bs with cnt service
		array = new JSONArray();
		array.put("/"+cseId+"/"+cseName+"/"+aeName);
		obj = new JSONObject();
		obj.put("nu", array);
		obj.put("rn", subName);
		obj.put("nct", 2);
		resource = new JSONObject();		
		resource.put("m2m:sub", obj);
		RestHttpClient.post(originator, csePoa+"/~/"+targetNodeBContainer, resource.toString(), 23);
		
//		
		File file = new File ("C:/Users/Microsoft Windows/OneDrive/Desktop/Project 3/DataNodeB.txt");
		List<String> oldRows = new ArrayList<String>();
		List<String> newRows = new ArrayList<String>();
		while (true){
			if(file.exists()) {
				oldRows.clear();
				Scanner myReader = new Scanner(file);
				while(myReader.hasNextLine()) {
					oldRows.add(myReader.nextLine());
				}
				myReader.close();
			}
		
	        if (!oldRows.equals(newRows)) {
	        	newRows.clear();
	        	newRows.addAll(oldRows);
		    	obj = new JSONObject();
				obj.put("cnf", "application/text");
				JSONObject con = new JSONObject();
				for (String row : newRows) {
					con.put(row.split(" ")[0], row.split(" ")[1]);
				}
		        obj.put("con", con.toString());
		        resource = new JSONObject();
				resource.put("m2m:cin", obj);
				RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+aeName+"/"+cntName1, resource.toString(), 4);
	        }
			
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	static class MyHandler implements HttpHandler {
		 
		public void handle(HttpExchange httpExchange)  {
			System.out.println("Event Recieved!");
 
			try{
				InputStream in = httpExchange.getRequestBody();
 
				String requestBody = "";
				int i;char c;
				while ((i = in.read()) != -1) {
					c = (char) i;
					requestBody = (String) (requestBody+c);
				}
 
				System.out.println(requestBody);
 
				JSONObject json = new JSONObject(requestBody);
				if (json.getJSONObject("m2m:sgn").has("m2m:vrq")) {
					System.out.println("Confirm subscription");
				} else {
					JSONObject cin = json.getJSONObject("m2m:sgn").getJSONObject("m2m:nev")
							.getJSONObject("m2m:rep").getJSONObject("m2m:cin");
					int ty = cin.getInt("ty");
					System.out.println("Resource type: "+ty);
 
					if (ty == 4) {
						JSONObject con = new JSONObject(cin.getString("con"));
						if(con.has("command")) {
							if(con.getString("command").equals("getsum")) {
								String parameter = con.getString("parameter");
								HttpResponse httpResponse = RestHttpClient.get(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+aeName+"/"+cntName1 + "/la");
								json = new JSONObject(httpResponse.getBody());
								int value =new JSONObject((String)json.getJSONObject("m2m:cin").get("con")).getInt(parameter);
								JSONObject obj = new JSONObject();
								obj.put("cnf", "application/text");
								JSONObject con1 = new JSONObject();
								con1.put("id", con.getInt("id"));
								con1.put("result", "getsum");
								con1.put("parameter", parameter);
								con1.put("destination", con.get("destination"));
								con1.put("value", value);
						        obj.put("con", con1.toString());
						        JSONObject resource = new JSONObject();
								resource.put("m2m:cin", obj);
								RestHttpClient.post(originator, csePoa+"/~/"+cseIdIn+"/"+cseNameIn+"/"+aeNameIn+"/"+cntNameIn, resource.toString(), 4);
							}
						}
					}
				}	
 
				String responseBudy ="";
				byte[] out = responseBudy.getBytes("UTF-8");
				httpExchange.sendResponseHeaders(200, out.length);
				OutputStream os = httpExchange.getResponseBody();
				os.write(out);
				os.close();
 
			} catch(Exception e){
				e.printStackTrace();
			}		
		}
	}
}
