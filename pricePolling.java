import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

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

public class pricePolling {

	private static final String URL = "/fcgi-bin/IHFTRestAPI/getPrice";
	private static String domain;
	private static String user;
	private static String password;
	private static String authentication_port;
	private static String request_port;
	
	public static class hftRequest {
		public getPriceRequest  getPrice;
		
		public hftRequest( String user, String token, List<String> security, List<String> tinterface, String granularity, int levels ) {
			this.getPrice = new getPriceRequest(user, token, security, tinterface, granularity, levels); 
		}
	}
	
	public static class hftResponse {
        public getPriceResponse getPriceResponse;
    }

    public static class getPriceRequest {
        public String        user;
        public String        token;
        public List<String>  security;
        public List<String>  tinterface;
        public String        granularity;
		public int           levels;
        
        public getPriceRequest( String user, String token, List<String> security, List<String> tinterface, String granularity, int levels ) {
        	this.user = user;
        	this.token = token;
        	this.security = security;
        	this.tinterface = tinterface;
        	this.granularity = granularity;
			this.levels = levels;
        }
    }

    public static class getPriceResponse {
        public int              result;
        public String           message;
        public List<priceTick>  tick;
        public priceHeartbeat   heartbeat;
        public String           timestamp;
    }

    public static class priceTick {
        public String  security;
        public String  tinterface;
        public double  price;
        public int     pips;
        public int     liquidity;
        public String  side;
    }

    public static class priceHeartbeat {
        public List<String>  security;
        public List<String>  tinterface;
    }

    public static void main(String[] args) throws IOException {
    	
    	// get properties from file
    	getProperties();
		
		final ObjectMapper mapper = new ObjectMapper();
		List<Header> headers = new ArrayList<Header>();
		headers.add( new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") );
		headers.add( new BasicHeader(HttpHeaders.ACCEPT, "application/json") );
		CloseableHttpClient client = HttpClients.custom().setDefaultHeaders(headers).build();

        // -----------------------------------------
        // STEP 1 : Prepare and send a price request
        // -----------------------------------------

		hftRequest hftrequest = new hftRequest(user, password, Arrays.asList("EUR_USD", "GBP_USD"), null, "tob", 1);

		try {
			mapper.setSerializationInclusion(Inclusion.NON_NULL);
			mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
			StringEntity request = new StringEntity(mapper.writeValueAsString(hftrequest));
			System.out.println(mapper.writeValueAsString(hftrequest));
			HttpPost httpRequest = new HttpPost(domain + ":" + request_port + URL);
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
                            	
                                if (response.getPriceResponse.tick != null){
                                    for (priceTick tick : response.getPriceResponse.tick){
                                    	System.out.println("Security: " + tick.security + " Price: " + tick.price + " Side: " + tick.side + " Liquidity: " + tick.liquidity);
                                    }
                                }
                                if (response.getPriceResponse.message != null){
									System.out.println("Message from server: " + response.getPriceResponse.message);
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
    
    public static void getProperties(){
    	Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("config.properties");
			prop.load(input);
			domain = prop.getProperty("domain");
			user = prop.getProperty("user");
			password = prop.getProperty("password");
			authentication_port = prop.getProperty("authentication-port");
			request_port = prop.getProperty("request-port");
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

	public pricePolling() {
		super();
	}

}

