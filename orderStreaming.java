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
import org.apache.http.util.EntityUtils;
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

public class orderStreaming {

	private static final String URL = "http://demo.arthikatrading.com:81/cgi-bin/IHFTRestStreamer/getOrder";
	
	public static class hftRequest {
		public getOrderRequest  getOrder;
		
		public hftRequest( String user, String token, List<String> security, List<String> tinterface, List<String> type ) {
			this.getOrder = new getOrderRequest(user, token, security, tinterface, type); 
		}
	}
	
	public static class hftResponse{
        public getOrderResponse getOrderResponse;
    }

	public static class getOrderRequest {
		public String        user;
		public String        token;
		public List<String>  security;
		public List<String>  tinterface;
		public List<String>  type;

		public getOrderRequest( String user, String token, List<String> security, List<String> tinterface, List<String> type ) {
			this.user = user;
			this.token = token;
			this.security = security;
			this.tinterface = tinterface;
			this.type = type;
		}
	}
	
	public static class getOrderResponse {
		public int              result;
		public String           message;
		public List<orderTick>  order;
		public orderHeartbeat   heartbeat;
		public String           timestamp;
		
	}
	
	public static class orderTick {
		public int     tempid;
		public String  orderid;
		public String  fixid;
		public String  account;
		public String  tinterface;
		public String  security;
		public int     pips;
		public int     quantity;
		public String  side;
		public String  type;
		public double  limitprice;
		public int     maxshowquantity;
		public String  timeinforce;
		public int     seconds;
		public int     milliseconds;
		public String  expiration;
		public double  finishedprice;
		public int     finishedquantity;
		public String  commcurrency;
		public double  commission;
		public double  priceatstart;
		public int     userparam;
		public String  status;
		public String  reason;
	}
	
	public static class orderHeartbeat {
		public List<String>  security;
		public List<String>  tinterface;
	}

    public static void main(String[] args) throws IOException {
		
		final ObjectMapper mapper = new ObjectMapper();
		List<Header> headers = new ArrayList<Header>();
		headers.add( new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") );
		headers.add( new BasicHeader(HttpHeaders.ACCEPT, "application/json") );
		CloseableHttpClient client = HttpClients.custom().setDefaultHeaders(headers).build();

        // -----------------------------------------
        // STEP 1 : Prepare and send a price request
        // -----------------------------------------

		hftRequest hftrequest = new hftRequest("fedenice", "fedenice", Arrays.asList("EUR_USD", "GBP_JPY", "GBP_USD"), null, null);

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
                        // STEP 2 : Wait for continuous responses from server (streaming)
                        // --------------------------------------------------------------

                        try {
                        	InputStreamReader stream = new InputStreamReader(entity.getContent());
                        	BufferedReader bufferedReader = new BufferedReader(stream);
                            String line = null;
                            
                            while ((line = bufferedReader.readLine()) != null) {
                            	hftResponse response = mapper.readValue(line, hftResponse.class);
								
								if (response.getOrderResponse.timestamp != null){
									System.out.println("Response timestamp: " + response.getOrderResponse.timestamp + " Contents:");
								}
								if (response.getOrderResponse.order!= null){
									for (orderTick tick : response.getOrderResponse.order){
										System.out.println("TempId: " + tick.tempid + " OrderId: " + tick.orderid + " Security: " + tick.security + " Account: " + tick.account + " Quantity: " + tick.quantity + " Type: " + tick.type + " Side: " + tick.side + " Status: " + tick.status);
                                    }
								}
								if (response.getOrderResponse.heartbeat!= null){
									System.out.println("Heartbeat!");
								}
								if (response.getOrderResponse.message != null){
									System.out.println("Message from server: " + response.getOrderResponse.message);
								}
                                
                            }
                        }
                        catch (IOException e) { e.printStackTrace(); }
                        catch (Exception e) { e.printStackTrace(); }
                        
                        return entity != null ? EntityUtils.toString(entity) : null;
                        
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

	public orderStreaming() {
		super();
	}

}

