package gui;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import qobuz_api.QobuzApi;
import qobuz_api.QobuzApi.loginInfo;
import supplement.Supplement;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;

import java.io.ByteArrayInputStream;

public class Dashboard extends Scene implements Supplement.sceneSupplement
{
	final String			name		= "Dashboard"	;
	final static double		height		= 500			,
							width		= 1000			;
	Scene					dashScene					;
	Pane					dashPane					,
							innerContent				;
	ScrollPane				contentPane					;
	TextField				findField					;
	Button					findButton					,
							settingsButton				;
	ComboBox<String>		typeSearch					;
	ComboBox<Integer>		audioFormats				;
	ComboBox<loginInfo>		users						;
	List<Pane>				viewElems					;
	static Dashboard		single						;

	@Override
	public String getName()
	{
		return name ;
	}
	
	@Override
	public Scene getScene()
	{
		return this ;
	}
	
	@Override
	public <E> Property<E> getProperty(E a)
	{
		return null;
	}
	
	public loginInfo getCurrentUser()
	{
		return users.getValue() ;
	}
	
	static public Dashboard createInstance( )
	{
		if(single != null)
			return single ;
		return ( single = new Dashboard( ) );
	}
	
	Dashboard( )
	{
		super(new Group(), width, height);
		
		dashPane = new Pane();
		findField = new TextField() ;
		findButton = new Button( "Find" ) ;
		settingsButton = new Button( "Settings" ) ;
		contentPane = new ScrollPane();
		audioFormats = new ComboBox<>();
		innerContent = new Pane();
		typeSearch = new ComboBox<>();
		users = new ComboBox<>();
		viewElems = new LinkedList<Pane>();
		
		for( var user : supplement.Settings.users)
			users.getItems().add(user);
		users.setValue(users.getItems().get(0));
		
		this.setRoot(dashPane);
		users.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0;");

		dashPane.getChildren().addAll(findField, findButton, contentPane, typeSearch, users, settingsButton) ;
		
		findField.setPrefWidth(500);
		findField.setLayoutX(50);
		findField.setLayoutY(50);
		findField.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0;");
		findField.setFont( Font.font( 17 ) );

		typeSearch.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0;");
		typeSearch.getItems().addAll(QobuzApi.typesSearch);
		typeSearch.setValue(typeSearch.getItems().get(0));
		typeSearch.setLayoutX(findField.getLayoutX() + findField.getPrefWidth() + 20 );
		typeSearch.setLayoutY(findField.getLayoutY());
		
		dashPane.applyCss();
		dashPane.layout();
		
		findButton.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0; -fx-background-insets: 0;");
		findButton.setFont( Font.font( 17 ) );
		findButton.setLayoutX(typeSearch.getLayoutX() + typeSearch.prefWidth(-1) + 20 );
		findButton.setLayoutY(findField.getLayoutY());
		
		settingsButton.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0; -fx-background-insets: 0;");
		settingsButton.setFont( Font.font( 17 ) );
		settingsButton.setLayoutX(this.getWidth() - settingsButton.prefWidth(-1) );
		
		settingsButton.setOnMouseClicked(new EventHandler<MouseEvent>()
				{
					@Override
					public void handle(MouseEvent event) {
						new Settings() ;
					}
				});

		audioFormats.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0;");
		audioFormats.getItems().addAll(QobuzApi.audioFormats);
		audioFormats.setValue(audioFormats.getItems().get(0));
		audioFormats.setLayoutX(typeSearch.getLayoutX() + typeSearch.getPrefWidth() + 20 );
		audioFormats.setLayoutY(typeSearch.getLayoutY());
		
		findButton.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent arg0) {
				innerContent.getChildren().clear();
				
				
				new Thread(new Runnable() {

					@Override
					public void run() {
						JSONObject jso = QobuzApi.search(users.getValue(), typeSearch.getValue().toLowerCase(), findField.getText() , 0, 6);
						Pane prevElem = innerContent ;
						
						Platform.runLater(()->{
							innerContent.setMinHeight(0) ;
						});
						
						for( var elem : jso.getJSONObject(typeSearch.getValue().toLowerCase() + "s").getJSONArray("items"))
						{
							Pane elemView = Supplement.getViewPaneByType(typeSearch.getValue() , (JSONObject)elem ) ;
							
							elemView.setOnMouseClicked(new EventHandler<MouseEvent>()
							{
								JSONObject self = (JSONObject)elem;
								Supplement.musicView mv = null ;
								@Override
								public void handle(MouseEvent arg0)
								{
									System.out.println("here");
									mv = new Supplement.musicView(self, contentPane);
								}
							});
							elemView.setLayoutY( prevElem.getLayoutY() + prevElem.getHeight() + 5 ) ;
							elemView.setLayoutX( 5 ) ;
							Platform.runLater(()->innerContent.getChildren().add( elemView ) );
							prevElem = elemView ;
						}
						Platform.runLater(()->{
							innerContent.setMinHeight(innerContent.prefHeight(-1) + 5) ;
						});
					}
				}).start();
			}
			
		});
		
		contentPane.setPrefWidth( 700 ) ;
		contentPane.setPrefHeight( 300 ) ;
		contentPane.setLayoutX(findField.getLayoutX());
		contentPane.setLayoutY(findField.getLayoutY() + findField.prefHeight(-1) + 20);
		contentPane.setContent(innerContent);
		contentPane.setStyle("-fx-background-color: rgba(0,0,0,0.4);-fx-vbar-policy: never; -fx-background-insets: 0;");
	}
}
