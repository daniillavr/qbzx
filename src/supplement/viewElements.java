package supplement;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.json.JSONException;
import org.json.JSONObject;

import gui.Dashboard;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.util.Pair;
import javafx.util.StringConverter;
import qobuz_api.QobuzApi;
import qobuz_api.QobuzApi.userInfo;

public class viewElements
{
	public abstract static sealed class baseEntry extends Pane permits musicEntry, albumEntry
	{
		public final static Integer						width			,
														height			;
		protected Label									title			,
														performer		;
		protected ImageView								image			;
		protected Pane									imageBorder		;
		protected String								id				;
		protected Button								download		;
		protected JSONObject							info			;
		protected ComboBox<Pair<String, Integer>>		audioFormats	;
		
		static
		{
			width = 350 ;
			height = 80 ;
		}
		
		void configCss()
		{
			getStyleClass().add("view-element") ;
			
			title.getStyleClass().add("title-label") ;
			performer.getStyleClass().add("performer-label") ;
			
			image.getStyleClass().add("image-imageview") ;
			
			imageBorder.getStyleClass().add("image-border-pane") ;
			
			download.getStyleClass().add("download-button") ;
			
			audioFormats.getStyleClass().add("audio-formats-combobox") ;
		}
		
		baseEntry( JSONObject json , byte[] img , String per , String label , String _id , Integer quality )
		{
			super();
			
			info			= json					;
			title			= new Label( label )	;
			id				= _id					;
			audioFormats	= new ComboBox<>()		;
			imageBorder		= new Pane()			;
			download		= new Button("Download");
			performer		= new Label( per )		;
			
			if(img != null)
				image = new ImageView(new Image( new ByteArrayInputStream( img ) ) );
			else
				image = new ImageView() ;
			
			setPrefHeight( height );
			setPrefWidth( width );
			
			configCss() ;
			
			getChildren().addAll( download, title, performer, image, imageBorder, audioFormats);
			
			Scene s = new Scene(this);
			
			audioFormats.setConverter(new StringConverter<Pair<String, Integer>>() {
				@Override
				public String toString(Pair<String, Integer> object) {
					if(object == null)
						return "" ;
					
					return object.getKey() ;
				}
				@Override
				public Pair<String, Integer> fromString(String string) {return null;}
			});
			
			imageBorder.setPrefHeight(72);
			imageBorder.setPrefWidth(72);
			imageBorder.setLayoutX(4);
			imageBorder.setLayoutY(4);
			
			title.setTooltip(new Tooltip(json.getString("title")) );
			
			image.setFitHeight(70);
			image.setFitWidth(70);
			image.setLayoutX(5);
			image.setLayoutY(5);
			
			performer.setLayoutX( this.image.getFitWidth() + 10 );
			performer.setLayoutY( 10 );
			
			audioFormats.getItems().addAll(QobuzApi.audioFormats.stream().filter(x -> x.getValue() <= quality).toList());
			audioFormats.setValue(this.audioFormats.getItems().get(this.audioFormats.getItems().size() - 1));
			
			applyCss();
			layout();

			download.setLayoutX(this.prefWidth(-1) - this.download.prefWidth(-1));
			download.setLayoutY(this.prefHeight(-1) - this.download.prefHeight(-1));
			
			audioFormats.setPrefHeight(this.download.prefHeight(-1));
			audioFormats.setLayoutX( this.download.getLayoutX() - this.audioFormats.prefWidth(-1) - 10 );
			audioFormats.setLayoutY( this.prefHeight(-1) - this.audioFormats.prefHeight(-1) );
			audioFormats.setTooltip(new Tooltip());
			audioFormats.getTooltip().setText("MP3 = MP3 320\nFLAC = FLAC Lossless\nFLAC+ = FLAC Hi-Res 24 bit =< 96kHz\nFLAC++ = FLAC Hi-Res 24 bit >96 kHz & =< 192 kHz") ;
			
			
			title.setLayoutX( this.performer.getLayoutX() + this.performer.prefWidth(-1) +  5 );
			title.setLayoutY( 10 );
			title.setMaxWidth(this.prefWidth(-1) - this.title.getLayoutX() - 5);
		}
		
		public String getTitle()
		{
			return title.getText() ;
		}
	}
	
	static non-sealed class musicEntry extends baseEntry
	{	
		musicEntry( JSONObject jsonMusic , byte[] image ) throws InterruptedException
		{
			super(	jsonMusic, 
					image, 
					jsonMusic.getJSONObject("performer").getString("name"), 
					jsonMusic.getString("title"),
					Integer.valueOf(jsonMusic.getInt("id")).toString(),
					(Integer)QobuzApi.audioFormats.stream().filter(x -> x.getValue() == QobuzApi.FormatByArg.apply( jsonMusic.getDouble("maximum_bit_depth") , jsonMusic.getDouble("maximum_sampling_rate") ) ).map(x -> x.getValue()).toArray()[0]
					);
			
			super.download.setOnMouseClicked((Event) -> {
				try {
					Supplement.createFileAndSet(info, Path.of(supplement.Settings.downloadPath, info.getJSONObject("performer").getString("name"), "tracks"), Integer.valueOf( super.audioFormats.getValue().getValue() ).toString() , super.audioFormats.getValue().getKey().split(" ")[0] ) ;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
		}
	}
	
	static non-sealed class albumEntry extends baseEntry
	{
		albumEntry( JSONObject jsonAlbum , byte[] image )
		{
			super(	jsonAlbum, 
					image, 
					jsonAlbum.getJSONObject("artist").getString("name"), 
					jsonAlbum.getString("title"),
					jsonAlbum.getString("id"),
					(Integer)QobuzApi.audioFormats.stream().filter(x -> x.getValue() == QobuzApi.FormatByArg.apply( jsonAlbum.getDouble("maximum_bit_depth") , jsonAlbum.getDouble("maximum_sampling_rate") ) ).map(x -> x.getValue()).toArray()[0]
					);
			
			super.download.setOnMouseClicked((Event) -> {
				JSONObject album;
				try {
					album = QobuzApi.getInfo(Dashboard.createInstance().getCurrentUser(), "album", id);
				
				for(var elem : album.getJSONObject("tracks").getJSONArray("items"))
				{
					JSONObject music = (JSONObject)elem ;
					music.put("album", info);
					Supplement.createFileAndSet(music, Path.of(supplement.Settings.downloadPath, info.getJSONObject("artist").getString("name"), info.getString("title")), Integer.valueOf( super.audioFormats.getValue().getValue() ).toString() , super.audioFormats.getValue().getKey().split(" ")[0] ) ;
				}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
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
			this.image = new ImageView( new Image( new ByteArrayInputStream( QobuzApi.getImage(this.musicInfo , "track", "small") ) )) ;
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
			
			this.backButton.setOnMouseClicked( (Event) -> {
						Platform.runLater(() -> {
							parent.getChildren().clear();
							parent.getChildren().addAll(pChilds);
							scrollPane.setVvalue(vPos);
						});
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
}
