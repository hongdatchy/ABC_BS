package org.eclipse.om2m.hongdatchy;
 
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.eclipse.om2m.hongdatchy.common.RestHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
 
public class BsServer {
	
	private static String originator="admin:admin";
	private static String cseProtocol="http";
	private static String cseIp = "127.0.0.1";
	private static int csePort = 8080;
	private static String cseId = "in-cse";
	private static String cseName = "in-name";
	private static int id = 0;
	private static String aeName = "bs-server";
	private static String aeProtocol="http";
	private static String aeIp = "127.0.0.1";
	private static int aePort = 1500;	
	private static String subName="bs-server-sub";
	private static String cntName1 = "service";
	private static String cntName2 = "command";
	private static String targetBsServerContainer= cseId + "/" + cseName + "/" + aeName + "/" + cntName1;
// HTTP POST http://127.0.0.1:8080/~/in-cse/in-name/bs-server/service
// HTTP POST http://127.0.0.1:8080/~/mn-cse-c/mn-name-c/node-c/data
	private static String csePoa = cseProtocol+"://"+cseIp+":"+csePort;
	private static String appPoa = aeProtocol+"://"+aeIp+":"+aePort;
 
	public static void main(String[] args) {
 
		HttpServer server = null;
		try {
			server = HttpServer.create(new InetSocketAddress(aePort), 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		server.createContext("/", new MyHandler());
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();
 
//		create bs service
		JSONArray array = new JSONArray();
		array.put(appPoa);
		JSONObject obj = new JSONObject();
		obj.put("rn", aeName);
		obj.put("api", 12346);
		obj.put("rr", true);
		obj.put("poa",array);
		JSONObject resource = new JSONObject();
		resource.put("m2m:ae", obj);
		RestHttpClient.post(originator, csePoa+"/~/"+cseId+"/"+cseName, resource.toString(), 2);
 
//		create cnt service
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
		RestHttpClient.post(originator, csePoa+"/~/"+targetBsServerContainer, resource.toString(), 23);
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
						if(con.has("service")) {
							if(con.get("service").toString().equals("getsum")) {
								String[] destinations = con.get("destination").toString().split(" ");
								JSONObject obj = new JSONObject();
								JSONObject resource = new JSONObject();
								for(String des: destinations) {
									obj = new JSONObject();
									obj.put("cnf", "application/text");
									JSONObject con1 = new JSONObject();
									con1.put("command", "getsum");
									con1.put("parameter", "totalslot");
									con1.put("id", id);
									obj.put("con", con1.toString());
									resource = new JSONObject();
									resource.put("m2m:cin", obj);
									System.out.println(getDestination(des));
									RestHttpClient.post(originator, csePoa+"/~/"+ getDestination(des) + "/"+"command", resource.toString(), 4);
								}
								id++;
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
		
		public String getDestination(String des) {
			return "mn-cse-"+des+"/"+"mn-name-"+des+"/"+"node-"+des;
		}
		
		
	}
}