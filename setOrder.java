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

public class setOrder {

	private static final String URL = "http://demo.arthikatrading.com:81/fcgi-bin/IHFTRestAPI/setOrder";
	
	public static class hftRequest {
		public setOrderRequest  setOrder;
		
		public hftRequest( String user, String token, List<orderRequest> order ) {
			this.setOrder = new setOrderRequest(user, token, order); 
		}
	}
	
	public static class hftResponse{
        public setOrderResponse setOrderResponse;
    }

	public static class setOrderRequest {
		public String        user;
		public String        token;
		public List<orderRequest>  order;

		public setOrderRequest( String user, String token, List<orderRequest> order ) {
			this.user = user;
			this.token = token;
			this.order = order;
		}
	}

	public static class setOrderResponse {
		public int              result;
		public String           message;
		public List<orderRequest>    order;
		public String           timestamp;
	}
	
	public static class orderRequest {
		public String  security;
		public String  tinterface;
		public int     quantity;
		public String  side;
		public String  type;
		public String  timeinforce;
		public double  price;
		public int     expiration;
		public int     userparam;
		public int     tempid;
		public String  result;
	}

    public static void main(String[] args) throws IOException {
		
		final ObjectMapper mapper = new ObjectMapper();
		List<Header> headers = new ArrayList<Header>();
		headers.add( new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") );
		headers.add( new BasicHeader(HttpHeaders.ACCEPT, "application/json") );
		CloseableHttpClient client = HttpClients.custom().setDefaultHeaders(headers).build();

        // -----------------------------------------
        // STEP 1 : Prepare and send a setOrder request with two orders
        // -----------------------------------------

		orderRequest order1 = new orderRequest();
		order1.security = "EUR_USD";
		order1.tinterface = "Baxter_CNX";
		order1.quantity = 500000;
		order1.side = "sell";
		order1.type = "market";
		
		orderRequest order2 = new orderRequest();
		order2.security = "GBP_USD";
		order2.tinterface = "Baxter_CNX";
		order2.quantity = 600000;
		order2.side = "sell";
		order2.type = "limit";
		order2.timeinforce = "day";
		order2.price = 1.47389;
		hftRequest hftrequest = new hftRequest("fedenice", "fedenice", Arrays.asList(order1, order2));

		try {
			mapper.setSerializationInclusion(Inclusion.NON_NULL);
			mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
			StringEntity request = new StringEntity(mapper.writeValueAsString(hftrequest));
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
                            	hftResponse response = mapper.readValue(line, hftResponse.class);
								
								if (response.setOrderResponse.order!= null){
									for (orderRequest tick : response.setOrderResponse.order){
										System.out.println("TempId: " + tick.tempid + " Security: " + tick.security + " Quantity: " + tick.quantity + " Type: " + tick.type + " Side: " + tick.side + " Price: " + tick.price + " Result: " + tick.result);
                                    }
								}
								if (response.setOrderResponse.message != null){
									System.out.println("Message from server: " + response.setOrderResponse.message);
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

	public setOrder() {
		super();
	}

}

