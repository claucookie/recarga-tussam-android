  package es.claucookie.recarga.logic;

  import org.apache.http.HttpResponse;
  import org.apache.http.client.CookieStore;
  import org.apache.http.client.methods.HttpRequestBase;
  import org.apache.http.impl.client.DefaultHttpClient;
  import org.apache.http.protocol.HttpContext;
  import org.json.JSONObject;

/**
* Main filter for all services. This class won't be overwritten.
* @author Service Generator 
*/
public class RecargaTussamHelper implements com.mobivery.utils.FilterInterface {

  	private static RecargaTussamHelper instance=new RecargaTussamHelper();

  	private RecargaTussamHelper() {
  	}

  	public static RecargaTussamHelper getInstance() {
  		return instance;
  	}
  	public String preInjectURLParameters(String logic,String method,String url,Object request) {
  		return url;
  	}
  	public String postInjectURLParameters(String logic,String method,String url,Object request) {
  		return url;
  	}
  	public Object cacheHit(String logic, String method, Object request, Object response) {
        return response;
  	}
  	public void preExecute(String logic,String method,DefaultHttpClient client, HttpRequestBase request, CookieStore cookieStore, HttpContext context) {

  	}
  	public void postExecute(String logic,String method,DefaultHttpClient client, HttpRequestBase request, HttpResponse response, CookieStore cookieStore) {

  	}	
  	public String preprocessResponse(String logic,String method,String responseString) {
  		return responseString;
  	}
  	public JSONObject preProcessJSON(String logic,String method,JSONObject jsonObject){
  		return jsonObject;
  	}
  }
