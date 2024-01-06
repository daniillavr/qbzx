package supplement;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import qobuz_api.QobuzApi;
import qobuz_api.QobuzApi.userInfo;

public class Settings
{
	public static String downloadPath = "" ;
	static String configFile = "config.cfg" ;
	public static ObservableList<QobuzApi.userInfo> users ;
	
	static void setDefaultValues()
	{
		try {
			downloadPath = new File(".").getCanonicalPath().toString() ;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} ;
	}
	
	static
	{
		users = FXCollections.observableArrayList(new ArrayList<QobuzApi.userInfo>());
		try
		{
			File fl = new File(configFile) ;
			
			if( fl.exists() )
			{
				FileReader fr = new FileReader( fl ) ;
				JSONObject js = new JSONObject(new JSONTokener(fr)) ;
				fr.close();
				downloadPath = js.getString("DownloadPath") ;
				
				for(var ui : js.getJSONArray("Users"))
				{
					JSONObject user = (JSONObject)ui ;

					userInfo li = new userInfo(
							user.getString("login") ,
							user.getString("password") ,
							user.getString("appid") ,
							user.getString("appsecret") ,
							user.getString("userauth"));
					
					users.add(li) ;
				}
				
			}
			else
			{
				setDefaultValues() ;
				JSONObject js = new JSONObject() ; 
				js.put("DownloadPath", downloadPath) ;
				js.put("Users", new JSONArray()) ;
				
				FileWriter fw = new FileWriter( fl ) ;
				
				fw.write(js.toString());
				fw.close();
			}
			
		}
		catch(Exception ex)
		{
			System.out.println( "Exception " + ex.getClass() + " in Settings reported: " + ex.getMessage() ) ;
			ex.printStackTrace();
		}
	}
	
	public static void addUser( userInfo li )
	{
		if(!users.contains(li))
			users.add(li) ;
	}
	
	public static void writeConfig()
	{
		try
		{
			File fl = new File(configFile) ;
			FileReader fr = new FileReader( fl ) ;
			JSONObject js = new JSONObject(new JSONTokener(fr)) ;
			js.put("DownloadPath", downloadPath);
			fr.close();
			for(var ui : users)
			{
				if(ui.getLogin().equals("+"))
					continue ;
				
				if( ui.getSave() )
				{	
					for( int i = 0 ; i < js.getJSONArray("Users").length() ; ++i)
						if( js.getJSONArray("Users").getJSONObject(i).getString("login") == ui.getLogin() )
							js.getJSONArray("Users").remove(i) ;
					
					JSONObject userJson = new JSONObject() ;
					userJson.put("login" , ui.getLogin()) ;
					userJson.put("password" , ui.getPassword()) ;
					userJson.put("appid" , ui.getAppID()) ;
					userJson.put("appsecret" , ui.getAppSecret()) ;
					userJson.put("userauth" , ui.getUserAuth()) ;
					
					js.getJSONArray("Users").put(userJson) ;
				}
			}
			
			FileWriter fw = new FileWriter( fl ) ;
			fw.write(js.toString());
			fw.close();
		}
		catch(Exception ex)
		{
			System.out.println( "Exception " + ex.getClass() + " in Settings reported: " + ex.getMessage() ) ;
		}
	}
}
