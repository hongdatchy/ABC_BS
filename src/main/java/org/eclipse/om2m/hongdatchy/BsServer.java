package org.eclipse.om2m.hongdatchy;
 
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.eclipse.om2m.hongdatchy.common.HttpResponse;
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
	private static String cntName2 = "result";
	private static String targetBsServerContainer= cseId + "/" + cseName + "/" + aeName + "/" + cntName1;
	private static String targetBsServerContainer2= cseId + "/" + cseName + "/" + aeName + "/" + cntName2;
	private static String csePoa = cseProtocol+"://"+cseIp+":"+csePort;
	private static String appPoa = aeProtocol+"://"+aeIp+":"+aePort;
	private static List<String> list = new ArrayList<String>();
	
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
		
//		create cnt result
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
		
//		sub cnt bs with cnt result
		array = new JSONArray();
		array.put("/"+cseId+"/"+cseName+"/"+aeName);
		obj = new JSONObject();
		obj.put("nu", array);
		obj.put("rn", subName);
		obj.put("nct", 2);
		resource = new JSONObject();		
		resource.put("m2m:sub", obj);
		RestHttpClient.post(originator, csePoa+"/~/"+targetBsServerContainer2, resource.toString(), 23);
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
							if(con.getString("service").equals("getsum")) {
								String[] destinations = con.getString("destination").split(" ");
								JSONObject obj = new JSONObject();
								JSONObject resource = new JSONObject();
								for(String des: destinations) {
									obj = new JSONObject();
									obj.put("cnf", "application/text");
									JSONObject con1 = new JSONObject();
									con1.put("command", "getsum");
									con1.put("parameter", con.getString("parameter"));
									con1.put("destination", con.getString("destination"));
									con1.put("id", id);
									obj.put("con", con1.toString());
									resource = new JSONObject();
									resource.put("m2m:cin", obj);
									RestHttpClient.post(originator, csePoa+"/~/"+ "mn-cse-"+des+"/"+"mn-name-"+des+"/"+"node-"+des + "/"+"command", resource.toString(), 4);
								}
								id++;
							}
						}else if(con.has("result")) {	
							if(con.getString("result").equals("getsum")) {
								String id = String.valueOf(con.getInt("id"));
								list.add(con.getInt("id") + " " + cin.getString("rn"));
								String des = con.getString("destination");
								String parameter = con.getString("parameter");
								int numberOfDestination = des.split(" ").length;
								List<String> listFilter =  list.stream()
									.filter(x -> x.split(" ")[0].equals(id)).collect(Collectors.toList());
								if(listFilter.size() == numberOfDestination) {
									int s= 0;
									for(String str : listFilter) {
										HttpResponse httpResponse = RestHttpClient.get(originator, csePoa+"/~/"+cseId+"/"+cseName+"/"+aeName+"/"+cntName2 + "/" +str.split(" ")[1]);
										JSONObject cin1 = new JSONObject(httpResponse.getBody()).getJSONObject("m2m:cin");
										s += new JSONObject(cin1.getString("con")).getInt("value"); 
										list.remove(str);
									}
									JSONObject obj = new JSONObject();
									obj.put("cnf", "application/text");
									con = new JSONObject();
									con.put("parameter", parameter);
									con.put("destination", des);
									con.put("sum", s);
									obj.put("con", con.toString());
									JSONObject resource = new JSONObject();
									resource.put("m2m:cin", obj);
									RestHttpClient.post(originator, csePoa+"/~/"+"mn-cse-a"+"/"+"mn-name-a"+"/"+"node-a" + "/" + "data", resource.toString(), 4);
								}
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