import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

// Note: Following libraries are required:
//
// 1) 'jackson-all-xxx.jar'       with MAVEN dependency: groupId 'org.codehaus.jackson', artifactId 'jackson' and version 1.9.9 
//                         or download from main project at 'http://www.java2s.com/Code/Jar/j/Downloadjacksonall199jar.htm'
//
// 2) 'httpclient-xxx.jar' with MAVEN dependency: groupId 'org.apache.httpcomponents', artifactId 'fluent-hc' and version 4.5
//                         or download from main project at 'https://hc.apache.org'

public class modifyOrder {

	private static final String URL = "http://demo.arthikatrading.com:81/fcgi-bin/IHFTRestAPI/modifyOrder";
	
	public static class hftRequest {
		public modifyOrderRequest  modifyOrder;
		
		public hftRequest( String user, String token, List<modOrder> order ) {
			this.modifyOrder = new modifyOrderRequest(user, token, order); 
		}
	}
	
	public static class hftResponse{
        public modifyOrderResponse modifyOrderResponse;
    }

	public static class modifyOrderRequest {
		public String        user;
		public String        token;
		public List<modOrder>   order;

		public modifyOrderRequest( String user, String token, List<modOrder> order ) {
			this.user = user;
			this.token = token;
			this.order = order;
		}
	}
	
	public static class modOrder {
		public String  fixid;
        public double  price;
        public int     quantity;
    }

	public static class modifyOrderResponse {
		public List<modifyTick> order;
		public String           message;
		public String           timestamp;
	}
	
	public static class modifyTick {
		public String  fixid;
		public String  result;
	}

    public static void main(String[] args) throws IOException {
		
		final ObjectMapper mapper = new ObjectMapper();
		List<Header> headers = new ArrayList<Header>();
		headers.add( new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") );
		headers.add( new BasicHeader(HttpHeaders.ACCEPT, "application/json") );
		CloseableHttpClient client = HttpClients.custom().setDefaultHeaders(headers).build();

        // -----------------------------------------
        // STEP 1 : Prepare and send a modifyOrder request for two pending orders
        // -----------------------------------------

		modOrder order1 = new modOrder();
		order1.fixid = "TRD_20151007112351168_0128";
		order1.price = 1.11005;
		order1.quantity = 20000;
		modOrder order2 = new modOrder();
		order2.fixid = "TRD_20151007112401904_0127";
		order2.price = 1.11006;
		order2.quantity = 30000;
		hftRequest hftrequest = new hftRequest("fedenice", "fedenice", Arrays.asList(order1, order2));

		try {
			mapper.setSerializationInclusion(Inclusion.NON_NULL);
			mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
			StringEntity request = new StringEntity(mapper.writeValueAsString(hftrequest));
			System.out.println(mapper.writeValueAsString(hftrequest));
			HttpPost httpRequest = new HttpPost(URL);
			httpRequest.setEntity(request);
			
			// Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
            	
                @Override
                public String handleResponse(final HttpResponse httpresponse) throws ClientProtocolException, IOException {
                    int status = httpresponse.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = httpresponse.getEntity();
                        
                        // --------------------------------------------------------------
                        // STEP 2 : Wait for response from server
                        // --------------------------------------------------------------

                        try {
                        	InputStreamReader stream = new InputStreamReader(entity.getContent());
                        	BufferedReader bufferedReader = new BufferedReader(stream);
                            String line = null;
                            
                            while ((line = bufferedReader.readLine()) != null) {
                            	System.out.println(line);
                            	hftResponse response = mapper.readValue(line, hftResponse.class);
								
                            	for (modifyTick tick : response.modifyOrderResponse.order){
									System.out.println("Result from server: " + tick.fixid + "-" + tick.result);
                                }
								if (response.modifyOrderResponse.message != null){
									System.out.println("Message from server: " + response.modifyOrderResponse.message);
								}
                                
                            }
                        }
                        catch (IOException e) { e.printStackTrace(); }
                        catch (Exception e) { e.printStackTrace(); }
                        
                        return null;
                        
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }
            };
            
			client.execute(httpRequest, responseHandler);
		} finally {
			client.close();
		}
	
	}

	public modifyOrder() {
		super();
	}

}

