import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
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

public class cancelOrder {

	private static final String URL = "/cancelOrder";
	private static String domain;
	//private static String url_stream;
	private static String url_polling;
	private static String url_challenge;
	private static String url_token;
	private static String user;
	private static String password;
	private static String authentication_port;
	private static String request_port;
	private static String challenge;
	private static String token;
	//private static int interval;
	
	public static class hftRequest {
		public getAuthorizationChallengeRequest getAuthorizationChallenge;
		public getAuthorizationTokenRequest getAuthorizationToken;
		public cancelOrderRequest  cancelOrder;
		
		public hftRequest( String user) {
			this.getAuthorizationChallenge = new getAuthorizationChallengeRequest(user); 
		}
		
		public hftRequest( String user, String challengeresp ) {
			this.getAuthorizationToken = new getAuthorizationTokenRequest(user, challengeresp); 
		}
		
		public hftRequest( String user, String token, List<String> fixid ) {
			this.cancelOrder = new cancelOrderRequest(user, token, fixid); 
		}
	}
	
	public static class hftResponse{
		public getAuthorizationChallengeResponse getAuthorizationChallengeResponse;
        public getAuthorizationTokenResponse getAuthorizationTokenResponse;
        public cancelOrderResponse cancelOrderResponse;
    }
	
	public static class getAuthorizationChallengeRequest {
        public String        user;
        
        public getAuthorizationChallengeRequest( String user ) {
        	this.user = user;
        }
    }
	
	public static class getAuthorizationChallengeResponse {
        public String        challenge;
        public String        timestamp;
    }
	
	public static class getAuthorizationTokenRequest {
        public String        user;
        public String        challengeresp;
        
        public getAuthorizationTokenRequest( String user, String challengeresp ) {
        	this.user = user;
        	this.challengeresp = challengeresp;
        }
    }
	
	public static class getAuthorizationTokenResponse {
        public String        token;
        public String        timestamp;
    }

	public static class cancelOrderRequest {
		public String        user;
		public String        token;
		public List<String>  fixid;

		public cancelOrderRequest( String user, String token, List<String> fixid ) {
			this.user = user;
			this.token = token;
			this.fixid = fixid;
		}
	}

	public static class cancelOrderResponse {
		public List<cancelTick> order;
		public String           message;
		public String           timestamp;
	}
	
	public static class cancelTick {
		public String  fixid;
		public String  result;
	}

    public static void main(String[] args) throws IOException, DecoderException {
    	
    	// get properties from file
    	getProperties();
    	
    	
    	final ObjectMapper mapper = new ObjectMapper();
		List<Header> headers = new ArrayList<Header>();
		headers.add( new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") );
		headers.add( new BasicHeader(HttpHeaders.ACCEPT, "application/json") );
		CloseableHttpClient client = HttpClients.custom().setDefaultHeaders(headers).build();
    	
    	// Create a custom response handler
        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
        	
            @Override
            public String handleResponse(final HttpResponse httpresponse) throws ClientProtocolException, IOException {
                int status = httpresponse.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = httpresponse.getEntity();
                    
                    // --------------------------------------------------------------
                    // Wait for response from server (polling)
                    // --------------------------------------------------------------

                    try {
                    	InputStreamReader stream = new InputStreamReader(entity.getContent());
                    	BufferedReader bufferedReader = new BufferedReader(stream);
                        String line = null;
                        
                        while ((line = bufferedReader.readLine()) != null) {
                        	hftResponse response = mapper.readValue(line, hftResponse.class);
                        	
                        	if (response.getAuthorizationChallengeResponse != null){
                        		challenge = response.getAuthorizationChallengeResponse.challenge;
                        		return null;
                        	}
                        	if (response.getAuthorizationTokenResponse != null){
                        		token = response.getAuthorizationTokenResponse.token;
                        		return null;
                        	}
                        	if (response.cancelOrderResponse != null){
                        		if (response.cancelOrderResponse.order != null){
									for (cancelTick tick : response.cancelOrderResponse.order){
										System.out.println("Result from server: " + tick.fixid + "-" + tick.result);
                                    }
								}
								if (response.cancelOrderResponse.message != null){
									System.out.println("Message from server: " + response.cancelOrderResponse.message);
								}
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
        
        try {
        	hftRequest hftrequest;
        	StringEntity request;
        	HttpPost httpRequest;
        	
        	// get challenge
        	hftrequest = new hftRequest(user);
        	mapper.setSerializationInclusion(Inclusion.NON_NULL);
			mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
			request = new StringEntity(mapper.writeValueAsString(hftrequest));
			System.out.println(mapper.writeValueAsString(hftrequest));
			httpRequest = new HttpPost(domain + ":" + authentication_port + url_challenge);
			httpRequest.setEntity(request);
			client.execute(httpRequest, responseHandler);
			
			// create challenge response
			byte[] a = Hex.decodeHex(challenge.toCharArray());
			byte[] b = password.getBytes();
			byte[] c = new byte[a.length + b.length];
			System.arraycopy(a, 0, c, 0, a.length);
			System.arraycopy(b, 0, c, a.length, b.length);
			byte[] d = DigestUtils.sha1(c);
			String challengeresp = Hex.encodeHexString(d);
			
			// get token with challenge response
			hftrequest = new hftRequest(user, challengeresp);
			mapper.setSerializationInclusion(Inclusion.NON_NULL);
			mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
			request = new StringEntity(mapper.writeValueAsString(hftrequest));
			System.out.println(mapper.writeValueAsString(hftrequest));
			httpRequest = new HttpPost(domain + ":" + authentication_port + url_token);
			httpRequest.setEntity(request);
			client.execute(httpRequest, responseHandler);
        	
			// -----------------------------------------
	        // Prepare and send a cancelOrder request for two pending orders
	        // -----------------------------------------
			String order1 = "TRD_20151006145512719_0125";
			String order2 = "TRD_20151006145524148_0124";
			hftrequest = new hftRequest(user, token, Arrays.asList(order1, order2));
			mapper.setSerializationInclusion(Inclusion.NON_NULL);
			mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
			request = new StringEntity(mapper.writeValueAsString(hftrequest));
			System.out.println(mapper.writeValueAsString(hftrequest));
			httpRequest = new HttpPost(domain + ":" + request_port + url_polling + URL);
			httpRequest.setEntity(request);
			client.execute(httpRequest, responseHandler);
		} finally {
			client.close();
		}
	
	}
    
    public static void getProperties(){
    	Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("config.properties");
			prop.load(input);
			domain = prop.getProperty("domain");
			//url_stream = prop.getProperty("url-stream");
			url_polling = prop.getProperty("url-polling");
			url_challenge = prop.getProperty("url-challenge");
			url_token = prop.getProperty("url-token");
			user = prop.getProperty("user");
			password = prop.getProperty("password");
			authentication_port = prop.getProperty("authentication-port");
			request_port = prop.getProperty("request-port");
			//interval = Integer.parseInt(prop.getProperty("interval"));
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		finally {
			if (input != null) {
				try {
					input.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    }

	public cancelOrder() {
		super();
	}

}
