package supplement;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.*;
import org.json.*;

import gui.Dashboard;
import gui.MainWindow;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import qobuz_api.QobuzApi;
import qobuz_api.QobuzApi.loginInfo;

public class Supplement {
	
	public interface sceneSupplement
	{
		String			getName()			;
		double			getWidth()			;
		double			getHeight()			;
		Scene			getScene()			;
		<E> Property<E>	getProperty(E a)	;
	}
	
	public enum statuses
	{
		GETTING_SECRET("Getting secret"),
		GOT_SECRET("Got secret"),
		GETTING_USER_AUTH("Getting user auth"),
		GOT_USER_AUTH("Got user auth"),
		ERROR("Error"),
		NO_STATUS("No status");
		
		statuses(String status)
		{
			currentStatus = status ;
		}
		
		public final String currentStatus ;
	}
	
	static public Pane getViewPaneByType( String type , JSONObject obj)
	{
		if( type.equals("Track") )
			return new musicEntry( obj ,
					QobuzApi.getImage(Dashboard.createInstance().getCurrentUser(), type.toLowerCase(), ((Integer)obj.getInt("id")).toString(), "large") );
		else if(type.equals("Album"))
			return new albumEntry( obj ,
					QobuzApi.getImage(Dashboard.createInstance().getCurrentUser(), type.toLowerCase(), ((Integer)obj.getInt("id")).toString(), "large") );
		else if(type.equals("Artist"))
			return new artistEntry( obj,
					QobuzApi.getImage(Dashboard.createInstance().getCurrentUser(), type.toLowerCase(), ((Integer)obj.getInt("id")).toString(), "large") );
		
		return null ;
	}
	
	static public Byte[] byteToByte( byte[] bArray )
	{
		Byte[] bResult = new Byte[bArray.length] ;
		
		for(int i = 0 ; i < bResult.length ; ++i)
			bResult[i] = Byte.valueOf(bArray[i]) ;
		
		return bResult ;
	}
	
	static class musicEntry extends Pane
	{
		Label		title		,
					performer	;
		ImageView	image		;
		Pane		imageBorder	;
		String		id			;
		Button		download	;
		JSONObject	self		;
		
		musicEntry( JSONObject jsonMusic , byte[] image )
		{
			super();
			self = jsonMusic ;
			this.title = new Label( jsonMusic.getString("title") ) ;
			this.id = jsonMusic.get("id").toString() ;
			this.download = new Button("Download");
			this.performer = new Label( jsonMusic.getJSONObject("performer").getString("name") ) ;
			if(image != null)
				this.image = new ImageView(new Image( new ByteArrayInputStream( image ) ) );
			else
				this.image = new ImageView() ;
			this.imageBorder = new Pane();
			this.getChildren().addAll(this.title, this.performer, this.image, this.imageBorder, this.download);
			
			Scene s = new Scene(this);
			
			this.setStyle("-fx-border-color: black;");
			
			this.imageBorder.setPrefHeight(72);
			this.imageBorder.setPrefWidth(72);
			this.imageBorder.setLayoutX(4);
			this.imageBorder.setLayoutY(4);
			this.imageBorder.setStyle("-fx-border-color: black; -fx-border-style: solid; -fx-border-width: 1;");
			this.imageBorder.applyCss();
			
			this.download.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 5; -fx-background-insets: 0;");
			
			this.title.setTooltip(new Tooltip(jsonMusic.getString("title")) );
			this.title.getTooltip().setStyle("-fx-background-color: rgba(0,0,0,0); -fx-background-radius: 0; -fx-padding: 0; -fx-text-fill:black; -fx-show-duration: 120s");
			
			this.setPrefHeight( 80 );
			this.setPrefWidth( 290 );
			this.image.setFitHeight(70);
			this.image.setFitWidth(70);
			this.image.setLayoutX(5);
			this.image.setLayoutY(5);
			
			this.performer.setLayoutX( this.image.getFitWidth() + 10 );
			this.performer.setLayoutY( 10 );
			
			//this.performer.setStyle("-fx-padding: 0");
			this.title.setStyle("-fx-text-fill: rgba(150 ,150, 150 , 1.0) ;");
			
			this.applyCss();
			this.layout();

			this.download.setLayoutX(this.prefWidth(-1) - this.download.prefWidth(-1));
			this.download.setLayoutY(this.prefHeight(-1) - this.download.prefHeight(-1));
			
			this.title.setLayoutX( this.performer.getLayoutX() + this.performer.prefWidth(-1) +  5 );
			this.title.setLayoutY( 10 );
			this.title.setMaxWidth(this.prefWidth(-1) - this.title.getLayoutX() - 5);
			
			this.download.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent arg0) {
					byte[] musicFile = QobuzApi.getFile(Dashboard.createInstance().getCurrentUser() , "track" , id , "27" ) ;
					
					File fl = new File("music.flac") ;
					try(FileOutputStream  wf = new FileOutputStream (fl) )
					{
						wf.write(musicFile) ;
					}
					catch(Exception ex)
					{
						
					}
					setMusicInfo(fl , self) ;
					
				}
			});
		}
		
		public String getTitle()
		{
			return title.getText() ;
		}
	}
	
	static class albumEntry extends musicEntry
	{
		albumEntry( JSONObject json , byte[] image )
		{
			super( json , image );
		}
	}
	
	static class artistEntry extends Pane
	{
		Label		performer	;
		ImageView	image		;
		Pane		imageBorder	;
		String		id			;
		
		artistEntry( JSONObject json , byte[] image  )
		{
			super();
			this.id = ((Integer)json.getInt("id")).toString() ;
			this.performer = new Label( json.getString("name") ) ;
			if(image != null)
				this.image = new ImageView(new Image( new ByteArrayInputStream( image ) ) );
			else
				this.image = new ImageView() ;
			this.imageBorder = new Pane();
			this.getChildren().addAll(this.performer, this.image, this.imageBorder);
			
			Scene s = new Scene(this);
			
			this.setStyle("-fx-border-color: black;");
			
			this.imageBorder.setPrefHeight(72);
			this.imageBorder.setPrefWidth(72);
			this.imageBorder.setLayoutX(4);
			this.imageBorder.setLayoutY(4);
			this.imageBorder.setStyle("-fx-border-color: black; -fx-border-style: solid; -fx-border-width: 1;");
			this.imageBorder.applyCss();
			
			this.setPrefHeight( 80 );
			this.setPrefWidth( 290 );
			this.image.setFitHeight(70);
			this.image.setFitWidth(70);
			this.image.setLayoutX(5);
			this.image.setLayoutY(5);
			
			this.performer.setLayoutX( this.image.getFitWidth() + 10 );
			this.performer.setLayoutY( 10 );
			
			this.applyCss();
			this.layout();
		}
	}
	
	public static class musicView extends Pane
	{
		Label					performer	;
		ImageView				image		;
		Button					backButton	;
		Pane					parent		;
		ScrollPane				scrollPane	;
		double					vPos		;
		ObservableList<Node>	pChilds		;
		JSONObject				musicInfo	;
		Pane					infoPane	;
		
		public musicView(JSONObject music, ScrollPane contentPane)
		{
			super();
			
			this.backButton = new Button( "<-" ) ;
			
			this.musicInfo = music;
			this.parent = (Pane)contentPane.getContent() ;
			this.scrollPane = contentPane;
			this.pChilds = FXCollections.observableArrayList(this.parent.getChildren()) ;
			this.vPos = scrollPane.getVvalue() ;
			this.image = new ImageView( new Image( new ByteArrayInputStream( QobuzApi.getImage(this.musicInfo , "track", "large") ) )) ;
			this.infoPane = new Pane() ;
			
			this.getChildren().clear();
			this.getChildren().addAll( this.backButton, this.image);
			
			this.image.setLayoutX(20);
			this.image.setLayoutY(20);
			this.image.setFitHeight(140);
			this.image.setFitWidth(140);
			
			this.infoPane.setLayoutX(this.image.getLayoutX() + this.image.getFitWidth() + 50);
			this.infoPane.setLayoutY(this.image.getLayoutY());
			this.infoPane.setPrefWidth(this.scrollPane.getWidth() - this.infoPane.getLayoutX() - 20);
			initInfoPane();
			this.getChildren().add(this.infoPane) ;
			
			this.backButton.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0; -fx-background-insets: 0;");
			
			this.backButton.setOnMouseClicked( new EventHandler<MouseEvent>()
					{
						@Override
						public void handle(MouseEvent arg0)
						{
							Platform.runLater(() -> {
								parent.getChildren().clear();
								parent.getChildren().addAll(pChilds);
								scrollPane.setVvalue(vPos);
							});
						}
				
					});
			
			Platform.runLater(() -> {
				parent.getChildren().clear();
				parent.getChildren().addAll(this);
				scrollPane.setVvalue(0);
			});
			
			this.applyCss();
			this.layout();
		}
		
		void initInfoPane()
		{
			Label	artist	, artistValue	,
					genre	, genreValue	,
					album	, albumValue	
					;
			
			artist = new Label("Artist");
			artistValue = new Label(musicInfo.getJSONObject("performer").getString("name"));
			genre = new Label("Genre");
			genreValue = new Label(musicInfo.getJSONObject("album").getJSONObject("genre").getString("name"));
			album = new Label("Album");
			albumValue = new Label(musicInfo.getJSONObject("album").getString("title"));
			
			
			artist.setFont(Font.font( 17 ));
			artistValue.setFont(artist.getFont());
			genre.setFont(artist.getFont());
			genreValue.setFont(artist.getFont());
			album.setFont(artist.getFont());
			albumValue.setFont(artist.getFont());
			
			this.infoPane.getChildren().addAll(artist, artistValue, genre, genreValue, album, albumValue) ;
			Scene s = new Scene( this.infoPane) ;
			
			this.infoPane.applyCss();
			this.infoPane.layout();
			
			artist.setLayoutX(10);
			
			artistValue.setLayoutX(artist.getLayoutX() + artist.getWidth() + 30);
			artistValue.setLayoutY(artist.getLayoutY());
			
			genre.setLayoutX(artist.getLayoutX());
			genre.setLayoutY(artist.getLayoutY() + artist.getHeight() + 5 );
			genreValue.setLayoutX(artistValue.getLayoutX());
			genreValue.setLayoutY(genre.getLayoutY());
			
			album.setLayoutX(genre.getLayoutX());
			album.setLayoutY(genre.getLayoutY() + genre.getHeight() + 5 );
			albumValue.setLayoutX(artistValue.getLayoutX());
			albumValue.setLayoutY(album.getLayoutY());
			
		}
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
	
	static void setMusicInfo( File fl , JSONObject musicObj )
	{
		System.out.println(musicObj);
		try
		{
			AudioFile au = AudioFileIO.read(fl) ;
			Tag tags = au.getTag() ;
			Artwork art = new StandardArtwork() ;
			art.setBinaryData(QobuzApi.getImage(musicObj, "track", "large"));
			tags.setField(art);
			tags.setField(FieldKey.ARTIST, ignoreExc(() -> musicObj.getJSONObject("performer").getString("name") , ""));
			tags.setField(FieldKey.ALBUM, ignoreExc(() -> musicObj.getJSONObject("album").getString("title") , ""));
			tags.setField(FieldKey.GENRE, ignoreExc(() -> musicObj.getJSONObject("album").getJSONObject("genre").getString("name") , ""));
			tags.setField(FieldKey.COMPOSER, ignoreExc(() -> musicObj.getJSONObject("composer").getString("name") , ""));
			tags.setField(FieldKey.COPYRIGHT, ignoreExc(() -> musicObj.getString("copyright") , "")); // In new version COPYRIGHT present, in oldest - no. So error because of documentation
			tags.setField(FieldKey.ISRC, ignoreExc(() -> musicObj.getString("isrc") , ""));
			tags.setField(FieldKey.TRACK, ignoreExc(() -> ((Integer)musicObj.getInt("track_number")).toString() , ""));
			tags.setField(FieldKey.TRACK_TOTAL, ignoreExc(() -> ((Integer)musicObj.getJSONObject("album").getInt("tracks_count")).toString() , ""));
			tags.setField(FieldKey.ALBUM_ARTISTS, ignoreExc(() -> musicObj.getString("performers") , ""));
			tags.setField(FieldKey.CUSTOM1, ignoreExc(() -> musicObj.getJSONObject("album").getString("id") , ""));
			tags.setField(FieldKey.TITLE, ignoreExc(() -> musicObj.getString("title") , ""));
			au.commit();
			fl.renameTo( new File( supplement.Settings.downloadPath + ignoreExc(() -> musicObj.getJSONObject("performer").getString("name") , "null") + " - " + ignoreExc(() -> musicObj.getString("title") , "null") + "." + fl.getName().split("\\.")[1] ) ) ;
		}
		catch(Exception ex)
		{
			System.out.println("Exception ID3: " + ex.getMessage());
		}
	}
}
