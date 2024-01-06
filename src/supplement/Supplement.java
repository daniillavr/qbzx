package supplement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.ID3v23Tag;
import org.jaudiotagger.tag.images.*;
import org.json.*;

import gui.Dashboard;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import qobuz_api.QobuzApi;

public class Supplement {
	
	public interface sceneSupplement
	{
		String			getName()			;
		double			getWidth()			;
		double			getHeight()			;
		Scene			getScene()			;
	}
	
	public enum statuses
	{
		GETTING_SECRET("Getting secret"),
		GOT_SECRET("Got secret"),
		GETTING_USER_AUTH("Getting user auth"),
		GOT_USER_AUTH("Got user auth"),
		ERROR("Error"),
		NO_STATUS("No status"),
		LOGGED("logged"),
		NOT_LOGGED("Not logged");
		
		statuses(String status)
		{
			currentStatus = status ;
		}
		
		@Override
		public String toString()
		{
			return currentStatus ;
		}
		
		final String currentStatus ;
	}
	
	static public Pane getViewPaneByType( String type , JSONObject obj) throws InterruptedException
	{
		try {
			if( type.toLowerCase().equals("track") )
					return new viewElements.musicEntry( obj ,
							QobuzApi.getImage(Dashboard.createInstance().getCurrentUser(), type.toLowerCase(), ((Integer)obj.getInt("id")).toString(), "small") );
				else if(type.toLowerCase().equals("album"))
					return new viewElements.albumEntry( obj ,
							QobuzApi.getImage(Dashboard.createInstance().getCurrentUser(), type.toLowerCase(), obj.getString("id"), "small") );
				else if(type.toLowerCase().equals("artist"))
					return new viewElements.artistEntry( obj,
							QobuzApi.getImage(Dashboard.createInstance().getCurrentUser(), type.toLowerCase(), ((Integer)obj.getInt("id")).toString(), "small") );
		} catch (InterruptedException e) {
			throw e ;
		}
		return null ;
	}
	
	static public Byte[] byteToByte( byte[] bArray )
	{
		Byte[] bResult = new Byte[bArray.length] ;
		
		for(int i = 0 ; i < bResult.length ; ++i)
			bResult[i] = Byte.valueOf(bArray[i]) ;
		
		return bResult ;
	}
	
	static <Z> Z ignoreExc(Supplier<Z> func, Z ret)
	{
		try
		{
			return func.get() ;
		}
		catch( Exception ex )
		{
			System.out.println( "Exception " + ex.getClass() + " ignored. Exception message: " + ex.getMessage() ) ;
			return ret ;
		}
	}
	
	static public void createFileAndSet( JSONObject fileInfo , Path path , String format , String extension ) throws InterruptedException
	{
		byte[] musicFile = null ;
		try {
			musicFile = QobuzApi.getAudioFile(Dashboard.createInstance().getCurrentUser() , Integer.valueOf(fileInfo.getInt("id")).toString() , format ) ;
			Files.createDirectories(path) ;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String fileName = Supplement.ignoreExc(() -> fileInfo.getJSONObject("performer").getString("name") , "null") +
				" - " +
				Supplement.ignoreExc(() -> fileInfo.getString("title") , "null") +
				"." +
				extension ;
		
		fileName = fileName.replaceAll("<|>|:|\"|\\||\\?|\\*|\\\\", "_") ; // Because of jaudiotagger restriction of using File.toPath() there are cannot be symbols like "<>:\"|?*" in windows and '\' in linux ; currently just deleting all this symbols
		
		File fl = new File(
				path.toFile(),
				fileName
					);
		try(FileOutputStream  wf = new FileOutputStream (fl) )
		{
			wf.write(musicFile) ;
		}
		catch(Exception ex)
		{
			
		}
		Supplement.setMusicInfo(fl , fileInfo) ;
	}
	
	static public void setMusicInfo( File fl , JSONObject musicObj )
	{
		System.out.println(musicObj);
		try
		{
			AudioFile au = AudioFileIO.read(fl) ;
			Tag tags ;
			
			Artwork art = new StandardArtwork() ;
			art.setBinaryData(QobuzApi.getImage(musicObj, "track", "large"));
			
			if(fl.getPath().endsWith("mp3"))
				tags = new ID3v23Tag();
			else
				tags = au.getTag();
			
			tags.setField(art);
			au.setTag(tags);
			tags.setField(FieldKey.ARTIST, ignoreExc(() -> musicObj.getJSONObject("performer").getString("name") , ""));
			tags.setField(FieldKey.ALBUM_ARTIST, ignoreExc(() -> musicObj.getJSONObject("performer").getString("name") , ""));
			tags.setField(FieldKey.ALBUM, ignoreExc(() -> musicObj.getJSONObject("album").getString("title") , ""));
			tags.setField(FieldKey.GENRE, ignoreExc(() -> musicObj.getJSONObject("album").getJSONObject("genre").getString("name") , ""));
			tags.setField(FieldKey.COMPOSER, ignoreExc(() -> musicObj.getJSONObject("composer").getString("name") , ""));
			tags.setField(FieldKey.COPYRIGHT, ignoreExc(() -> musicObj.getString("copyright") , "")); // In new version COPYRIGHT is present, in oldest - no. So error because of documentation
			tags.setField(FieldKey.ISRC, ignoreExc(() -> musicObj.getString("isrc") , ""));
			tags.setField(FieldKey.TRACK, ignoreExc(() -> ((Integer)musicObj.getInt("track_number")).toString() , ""));
			tags.setField(FieldKey.TRACK_TOTAL, ignoreExc(() -> ((Integer)musicObj.getJSONObject("album").getInt("tracks_count")).toString() , ""));
			tags.setField(FieldKey.CUSTOM1, ignoreExc(() -> musicObj.getJSONObject("album").getString("id") , ""));
			tags.setField(FieldKey.TITLE, ignoreExc(() -> musicObj.getString("title") , ""));
			tags.setField(FieldKey.YEAR, ignoreExc(() -> musicObj.getString("release_date_original") , ""));

			au.commit();
		}
		catch(Exception ex)
		{
			System.out.println("Exception ID3: " + ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	public static class DraggableArea extends Pane
	{
		double	prevX	,
				prevY	;
		Window	window	;
		
		public DraggableArea(double x , double y , double w , double h , Scene windowNode )
		{
			super( ) ;
			System.out.println(x);
			setPrefSize( w , h ) ;
			setLayoutX( x ) ;
			setLayoutX( y ) ;
			setOnMousePressed( value -> {
				window = windowNode.getWindow() ;
				prevX = value.getScreenX() ;
				prevY = value.getScreenY() ;
			}) ;
			
			setOnMouseDragged( value -> {
				Platform.runLater( () -> {
					window.setX( window.getX() + value.getScreenX() - prevX ) ;
					window.setY( window.getY() + value.getScreenY() - prevY ) ;
					prevX = value.getScreenX() ;
					prevY = value.getScreenY() ;
				}) ;
			}) ;
		}
	}
}
