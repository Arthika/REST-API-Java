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

public class positionStreaming {

	private static final String URL = "http://demo.arthikatrading.com:81/cgi-bin/IHFTRestStreamer/getPosition";
	
	public static class hftRequest {
		public getPositionRequest  getPosition;
		
		public hftRequest( String user, String token, List<String> asset, List<String> security, List<String> account ) {
			this.getPosition = new getPositionRequest(user, token, asset, security, account); 
		}
	}
	
	public static class hftResponse {
        public getPositionResponse getPositionResponse;
    }

	public static class getPositionRequest {
		public String        user;
		public String        token;
		public List<String>  asset;
		public List<String>  security;
		public List<String>  account;

		public getPositionRequest( String user, String token, List<String> asset, List<String> security, List<String> account ) {
			this.user = user;
			this.token = token;
			this.asset = asset;
			this.security = security;
			this.account = account;
		}
	}

	public static class getPositionResponse {
		public int              result;
		public String           message;
		public List<assetPositionTick>  assetposition;
		public List<securityPositionTick>  securityposition;
		public positionHeartbeat  heartbeat;
		public String           timestamp;
	}
	
	public static class assetPositionTick {
		public String  account;
		public String  asset;
		public double  exposure;
		public double  equity;
		public double  totalexposure;
		public double  freemargin;
	}
	
	public static class securityPositionTick {
		public String  account;
		public String  security;
		public double  exposure;
		public String  side;
		public double  price;
		public int     pips;
		public double  equity;
		public double  freemargin;
	}
	
	public static class positionHeartbeat {
		public List<String>  asset;
		public List<String>  security;
		public List<String>  account;
	}

    public static void main(String[] args) throws IOException {
		
		final ObjectMapper mapper = new ObjectMapper();
		List<Header> headers = new ArrayList<Header>();
		headers.add( new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") );
		headers.add( new BasicHeader(HttpHeaders.ACCEPT, "application/json") );
		CloseableHttpClient client = HttpClients.custom().setDefaultHeaders(headers).build();

        // -----------------------------------------
        // STEP 1 : Prepare and send a position request
        // -----------------------------------------

		hftRequest hftrequest = new hftRequest("fedenice", "fedenice", null, Arrays.asList("EUR_USD", "GBP_USD"), null);

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
                                
                                if (response.getPositionResponse.timestamp != null){
                                	System.out.println("Response timestamp: " + response.getPositionResponse.timestamp + " Contents:");
								}
								if (response.getPositionResponse.assetposition!= null){
									for (assetPositionTick tick : response.getPositionResponse.assetposition){
										System.out.println("Asset: " + tick.asset + " Account: " + tick.account + " Equity: " + tick.equity + " Exposure: " + tick.exposure);
                                    }
								}
								if (response.getPositionResponse.securityposition!= null){
									for (securityPositionTick tick : response.getPositionResponse.securityposition){
										System.out.println("Security: " + tick.security + " Account: " + tick.account + " Equity: " + tick.equity + " Exposure: " + tick.exposure + " Price: " + tick.price + " Pips: " + tick.pips);
                                    }
								}
								if (response.getPositionResponse.heartbeat!= null){
									System.out.println("Heartbeat!");
								}
								if (response.getPositionResponse.message != null){
									System.out.println("Message from server: " + response.getPositionResponse.message);
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

	public positionStreaming() {
		super();
	}

}
