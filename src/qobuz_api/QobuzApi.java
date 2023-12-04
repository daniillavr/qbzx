package qobuz_api;

import java.io.StringReader;
import java.net.URI;
import java.net.http.*;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;
import org.json.JSONTokener;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import supplement.*;
import supplement.Supplement.statuses;

public class QobuzApi 
{
	static String													qobuzURL		=	"https://play.qobuz.com"					;
	static String													qobuzAPI		=	"https://www.qobuz.com/api.json/0.2/"		;
	public final static List<String>								imageSizes		=	List.of( "small" , "thumbnail" , "large" )	;
	public final static List<Integer>								audioFormats	=	List.of( 5 , 6 , 7 , 27 )					;
	public final static List<String>								typesSearch		=	
			List.of( "Track" , "Artist" , "Album" , "Article" )																		;
	final static Map<String, Function<JSONObject, JSONObject>>		imagePath		=	Map.ofEntries(
			Map.entry("track", jsonObj -> {
				return jsonObj.getJSONObject("album").getJSONObject("image")													;
			}),
			Map.entry("album", jsonObj -> {
				return jsonObj.getJSONObject("image")																				;
			}),
			Map.entry("artist", jsonObj -> {
				return jsonObj.getJSONObject("image")																				;
			})
			)																														;
	
	static public class QobuzError extends Throwable
	{
		private static final long serialVersionUID = 1L;
		
		public QobuzError( String error )
		{
			super(error) ;
		}
		
	}
	
	static public void getSecret( loginInfo info ) throws QobuzError
	{
		try
		{
			info.getStatus().setValue(Supplement.statuses.GETTING_SECRET.currentStatus);
			HttpClient cli = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(5))
					.version(Version.HTTP_2)
					.followRedirects(Redirect.NORMAL)
					.build();
			HttpRequest req = HttpRequest.newBuilder()
					.timeout(Duration.ofSeconds(5))
					.uri(new URI(qobuzURL + "/login"))
					.GET()
					.build();
			
			HttpResponse<String> resp = cli.send(req, BodyHandlers.ofString());
			Matcher find = Pattern.compile("<script src=\"(\\/resources\\/[\\.\\-\\w]+\\/bundle\\.js)\">").matcher(resp.body());
			if( !find.find() )
				throw new Exception("Something changed on Qobuz site, please change regex or something to fix it!");
			
			req = HttpRequest.newBuilder().uri(new URI(qobuzURL + find.group(1))).GET().build();
			resp = cli.send(req, BodyHandlers.ofString());
			
			find = Pattern.compile("production:\\{api:\\{appId:\"(.*?)\",appSecret:").matcher(resp.body());
			if( !find.find() )
				throw new Exception("Something changed in Qobuz resources...");
			var id = find.group(1);
			
			info.setAppID(id);
			
			find = Pattern.compile("name:\"[A-Za-z\\/]+\\/Berlin\",info:\"([\\w=]+)\",extras:\"([\\w=]+)\"").matcher(resp.body());
			if( !find.find() )
				throw new Exception("Something changed in Qobuz resources...");
            var bundleInfo = find.group(1);
            
			find = Pattern.compile("[a-z]\\.initialSeed\\(\"([\\w=]+)\",window\\.utimezone\\.berlin\\)").matcher(resp.body());
			if( !find.find() )
				throw new Exception("Something changed in Qobuz resources...");
			var bundleSeed = find.group(1);
			
			String B64step1 = bundleSeed + bundleInfo.split("\\=")[0] + "=";
			
			byte[] bts = B64step1.getBytes("UTF-8");
			
			byte[] encoded = Base64.getDecoder().decode(bts);
			
			System.out.println(info.getAppID());
			
			info.setAppSecret(new String(encoded, "UTF-8" ));
			info.getStatus().setValue(Supplement.statuses.GOT_SECRET.currentStatus);
		}
		catch( Exception ex )
		{
			info.getStatus().setValue(Supplement.statuses.ERROR.currentStatus);
			throw new QobuzError("Something went wrong when getting secret(Qobuz changed patterns...).");
		}
	}
	
	static public void getUserToken( loginInfo info ) throws QobuzError
	{
		info.getStatus().setValue(Supplement.statuses.GETTING_USER_AUTH.currentStatus);
		try
		{
			String str = new StringBuilder()
					.append(qobuzAPI)
					.append("user/login?email=")
					.append(info.getLogin())
					.append("&password=")
					.append(info.getPassword())
					.append("&app_id=")
					.append(info.getAppID())
					.toString();
			HttpClient cli = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(5))
					.version(Version.HTTP_2)
					.followRedirects(Redirect.NORMAL).build();
			HttpRequest req = HttpRequest.newBuilder()
					.timeout(Duration.ofSeconds(5))
					.uri(new URI(str))
					.GET().build();
			
			HttpResponse<String> resp = cli.send(req, BodyHandlers.ofString());
			
			JSONObject jsonObject = new JSONObject(new JSONTokener(new StringReader(resp.body())));
			System.out.println(jsonObject);
			info.setUserAuth((String)jsonObject.get("user_auth_token"));
		}
		catch(Exception ex)
		{
			info.getStatus().setValue(Supplement.statuses.ERROR.currentStatus);
			throw new QobuzError("Something went wrong when getting user token.");
		}
		info.getStatus().setValue(Supplement.statuses.GOT_USER_AUTH.currentStatus);
	}
	
	static public byte[] getImage(loginInfo info , String typeSearch , String id , String imageSize )
	{
		System.out.println(typeSearch + " " + id + " " + imageSize);
		byte[] str = null ;
		JSONObject	jsonObject = null ;
		
		if( !imageSizes.contains(imageSize) )
			return str ;
		
		try
		{
			String searchStr = new StringBuilder()
					.append(qobuzAPI)
					.append(typeSearch)
					.append("/get?")
					.append(typeSearch.toLowerCase() + "_id=")
					.append(id.toString())
					.append("&app_id=")
					.append(info.getAppID())
					.append("&user_auth_token=")
					.append(info.getUserAuth())
					.toString();
			System.out.println(searchStr);
			HttpClient cli = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).version(Version.HTTP_2).followRedirects(Redirect.NORMAL).build();
			HttpRequest req = HttpRequest.newBuilder()
					.uri(new URI(searchStr))
				.GET().build();
			HttpResponse<String> resp = cli.send(req, BodyHandlers.ofString());
			
			jsonObject = new JSONObject(new JSONTokener(new StringReader(resp.body())));
			
			System.out.println(typeSearch);
			
			JSONObject arr = imagePath.get(typeSearch).apply(jsonObject);
			
			try(var iss = new URI((String)arr.get( imageSize )).toURL().openStream();)
			{
				str = iss.readAllBytes();
			}
		}
		catch(Exception ex)
		{
			jsonObject = null ;
			System.out.println("Excp:" + ex.getMessage());
		}

		return str ;
	}
	
	static public byte[] getImage( JSONObject entity , String typeSearch , String imageSize )
	{
		byte[] str = null ;
		
		if( !imageSizes.contains(imageSize) )
			return str ;
		
		try
		{
			JSONObject arr = imagePath.get(typeSearch).apply(entity);
			
			try(var iss = new URI((String)arr.get( imageSize )).toURL().openStream();)
			{
				str = iss.readAllBytes();
			}
		}
		catch(Exception ex)
		{
			System.out.println("Excp:" + ex.getMessage());
			return null ;
		}

		return str ;
	}
	
	static public byte[] getFile( loginInfo info , String typeSearch , String id , String format )
	{
		byte[] str = null ;
		JSONObject	jsonObject = null ;
		
		try
		{
			Long nowTime = Instant.now().toEpochMilli() / 1000 ;
			
			String request_sig = "trackgetFileUrlformat_id" + format + "track_id" + id + nowTime.toString() + info.appSecret ;
			
			byte[] encoded = MessageDigest.getInstance("MD5").digest(request_sig.getBytes("UTF-8")) ;
			
			String result = Arrays.toString( Arrays.asList(Supplement.byteToByte(encoded)).stream().map(x -> String.valueOf(Integer.toHexString((x >> 4) & 0xf) + Integer.toHexString(x& 0xf ))).toArray()).replace("[", "").replace("]", "").replace(",", "").replace(" " , "");
			
			
			System.out.println("") ;
			String searchStr = new StringBuilder()
					.append(qobuzAPI)
					.append(typeSearch)
					.append("/getFileUrl?")
					.append("track_id=")
					.append(id.toString())
					.append("&app_id=")
					.append(info.getAppID())
					.append("&format_id=")
					.append(format)
					.append("&request_ts=")
					.append(nowTime)
					.append("&request_sig=")
					.append(result) 
					.append("&user_auth_token=")
					.append(info.getUserAuth())
					.toString();
			
			System.out.println(searchStr);
			
			HttpClient cli = HttpClient.newBuilder().version(Version.HTTP_2).followRedirects(Redirect.NORMAL).build();
			HttpRequest req = HttpRequest.newBuilder()
					.uri(new URI(searchStr))
				.GET().build();
			HttpResponse<String> resp = cli.send(req, BodyHandlers.ofString());
			
			jsonObject = new JSONObject(new JSONTokener(new StringReader(resp.body())));
			
			try(var iss = new URI(jsonObject.getString("url")).toURL().openStream();)
			{
				str = iss.readAllBytes();
			}
		}
		catch(Exception ex)
		{
			jsonObject = null ;
			System.out.println("Excp:" + ex.getMessage());
		}

		return str ;
	}
	
	static public JSONObject search(loginInfo info , String typeSearch , String textSearch , Integer offset , Integer limitSearch )
	{
		JSONObject jsonObject = null ;
		try
		{
			String searchStr = new StringBuilder()
					.append(qobuzAPI)
					.append(typeSearch)
					.append("/search?app_id=")
					.append(info.getAppID())
					.append("&offset=")
					.append(offset.toString())
					.append("&query=")
					.append(textSearch.replace(" ", "%20"))
					.append("&limit=")
					.append(limitSearch.toString())
					.append("&user_auth_token=")
					.append(info.getUserAuth())
					.toString();
			System.out.println(searchStr);
			HttpClient cli = HttpClient.newBuilder().version(Version.HTTP_2).followRedirects(Redirect.NORMAL).build();
			HttpRequest req = HttpRequest.newBuilder()
					.uri(new URI(searchStr))
				.GET().build();
			HttpResponse<String> resp = cli.send(req, BodyHandlers.ofString());
			
			jsonObject = new JSONObject(new JSONTokener(new StringReader(resp.body())));
			System.out.println(jsonObject);
		}
		catch(Exception ex)
		{
			jsonObject = null ;
			System.out.println("Excp:" + ex.getMessage());
		}
		
		return jsonObject ;
	}
	
	static public class loginInfo
	{
		String						login		,
									password	,
									appID		,
									appSecret	,
									userAuth	;
		boolean						saveInfo	;
		public Property<String>		statusCode	;
		
		public loginInfo()
		{
			login = password = appID = appSecret = userAuth = "";
			statusCode = new SimpleStringProperty(statuses.NO_STATUS.currentStatus);
		}
		
		public loginInfo( String l , String p , String id , String secret , String us )
		{
			login = l ;
			password = p ;
			appID = id ;
			appSecret = secret ;
			userAuth = us ;
			statusCode = new SimpleStringProperty(statuses.NO_STATUS.currentStatus);
		}
		
		public void setLogin(String l) { login = l ; }
		public void setPassword(String p) { password = p ; }
		public void setAppID(String id) { appID = id ; }
		public void setAppSecret(String secret) { appSecret = secret ; }
		public void setUserAuth(String us) { userAuth = us ; }
		public void setSave(boolean save) { saveInfo = save ; }
		
		public String 			getLogin() { return login ; }
		public String 			getPassword() { return password ; }
		public String 			getAppID() { return appID ; }
		public String 			getAppSecret() { return appSecret ; }
		public String 			getUserAuth() { return userAuth ; }
		public boolean			getSave() { return saveInfo ; }
		public Property<String>	getStatus() { return statusCode ; }
		
		@Override
		public String toString()
		{
			return login ;
		}
		
		@Override
		public boolean equals( Object o )
		{
			if( o == null || this.getClass() != o.getClass() )
				return false ;
			
			loginInfo li = (loginInfo)o ;
			
			return li.getLogin().equals( this.getLogin() ) && li.getPassword().equals( this.getPassword() ) ;
		}
		
	}
}