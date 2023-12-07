package gui;

import java.util.LinkedList;
import java.util.List;

import org.json.*;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;
import qobuz_api.QobuzApi;
import qobuz_api.QobuzApi.userInfo;
import supplement.Supplement;

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
	TextArea				listInputArea				;
	Button					findButton					,
							settingsButton				,
							listSearchButton			,
							listUnfoldButton			;
	ComboBox<String>		typeSearch					;
	ComboBox<Integer>		audioFormats				;
	ComboBox<userInfo>		users						;
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
	
	public userInfo getCurrentUser()
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
		listInputArea = new TextArea() ;
		listUnfoldButton = new Button("🡣") ;
		
		for( var user : supplement.Settings.users)
			users.getItems().add(user);
		users.getItems().add(new userInfo("+","","","","")) ;
		users.setValue(users.getItems().get(0));
		
		users.setConverter(new StringConverter<userInfo>() {
			@Override
			public String toString(userInfo object) {
				if(object == null)
					return "" ;
				if(object.toString().equals("+"))
					return object.toString();
				else
					return object.toString() + "(" + object.getStatus().getValue() + ")";
			}
			@Override
			public userInfo fromString(String string) {return null;}
		});
		
		users.setOnShowing((Event) -> {
			Platform.runLater(() -> users.setValue(null) ) ;
		});
		
		users.valueProperty().addListener((obs, oldValue, newValue) -> {
					if(newValue == null)
						return ;
					
					if(newValue.getLogin().equals("+"))
					{
						System.out.println("Here");
						Platform.runLater(() -> users.setValue(null) ) ;
						Stage loginStage = new Stage() ;
						loginStage.setScene(LoginWindow.createInstance());
						loginStage.show();
						
						loginStage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, (Event) -> {
							Platform.runLater(() -> {
								users.getItems().clear() ;
								for( var user : supplement.Settings.users)
									users.getItems().add(user);
								users.getItems().add(new userInfo("+","","","",""));
								
								users.setValue(supplement.Settings.users.get(supplement.Settings.users.size() - 1));
							} ) ;
						});
					}
					else if(oldValue == null || !oldValue.getLogin().equals("+"))
					{
						newValue.getStatus().addListener((obs1, oldValue1, newValue1) -> {
								Platform.runLater(() -> {
									users.setEditable(true) ;
									users.setEditable(false) ; // Trick to force ComboBox redraw
								} ) ;
							}
						);
						new Thread(() -> QobuzApi.loginUser(newValue) ).start();
					}
				});
		
		this.setRoot(dashPane);
		users.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0;");

		dashPane.getChildren().addAll(findField, findButton, contentPane, typeSearch, users, settingsButton, listInputArea, listUnfoldButton) ;
		
		findField.setPrefWidth(500);
		findField.setLayoutX(50);
		findField.setLayoutY(50);
		findField.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0;");
		findField.setFont( Font.font( 17 ) );

		listUnfoldButton.setFont(Font.font( 17 ));
		listUnfoldButton.setLayoutX(findField.getLayoutX() + findField.getPrefWidth());
		listUnfoldButton.setLayoutY(findField.getLayoutY());
		listUnfoldButton.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0; -fx-background-insets: 0;");
		
		dashPane.applyCss();
		dashPane.layout();
		
		typeSearch.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0;");
		typeSearch.getItems().addAll(QobuzApi.typesSearch);
		typeSearch.setValue(typeSearch.getItems().get(0));
		typeSearch.setLayoutX(listUnfoldButton.getLayoutX() + listUnfoldButton.prefWidth(-1) + 10 );
		typeSearch.setLayoutY(findField.getLayoutY());
		
		contentPane.setPrefWidth( 700 ) ;
		contentPane.setPrefHeight( 300 ) ;
		contentPane.setLayoutX(findField.getLayoutX());
		contentPane.setLayoutY(findField.getLayoutY() + findField.prefHeight(-1) + 20);
		contentPane.setContent(innerContent);
		contentPane.setStyle("-fx-background-color: rgba(0,0,0,0.4);-fx-vbar-policy: never; -fx-background-insets: 0;");
		
		findButton.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0; -fx-background-insets: 0;");
		findButton.setFont( Font.font( 17 ) );
		findButton.setLayoutX(typeSearch.getLayoutX() + typeSearch.prefWidth(-1) + 10 );
		findButton.setLayoutY(findField.getLayoutY());
		findButton.setPrefWidth(contentPane.getLayoutX() + contentPane.prefWidth(-1) - findButton.getLayoutX());
		
		settingsButton.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0; -fx-background-insets: 0;");
		settingsButton.setFont( Font.font( 17 ) );
		settingsButton.setLayoutX(this.getWidth() - settingsButton.prefWidth(-1) );
		
		listInputArea.setPrefWidth(findField.prefWidth(-1));
		listInputArea.setPrefHeight(300);
		listInputArea.setVisible(false);
		listInputArea.setLayoutX(findField.getLayoutX());
		listInputArea.setLayoutY(findField.getLayoutY());
		
		listInputArea.focusedProperty().addListener((obs, oldValue, newValue ) -> {
						if(newValue == false )
						{
							if( !listUnfoldButton.focusedProperty().get() && !findButton.focusedProperty().get() )
							{
								findButton.setText("Find");
								listInputArea.setVisible(false);
								listUnfoldButton.setText("🡣");
							}
					}});
		
		listUnfoldButton.setOnMousePressed((Event) -> {
						listInputArea.setVisible(!listInputArea.isVisible());
						if( listInputArea.isVisible() )
						{
							findButton.setText("Download");
							listInputArea.requestFocus();
							listUnfoldButton.setText("🡡");
						}
						else
						{
							findButton.setText("Find");
							listUnfoldButton.setText("🡣");
						}
				});
		
		settingsButton.setOnMouseClicked((Event) -> {
						new Settings() ;
				});

		audioFormats.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0;");
		audioFormats.getItems().addAll(QobuzApi.audioFormats);
		audioFormats.setValue(audioFormats.getItems().get(0));
		audioFormats.setLayoutX(typeSearch.getLayoutX() + typeSearch.getPrefWidth() + 20 );
		audioFormats.setLayoutY(typeSearch.getLayoutY());
		
		findButton.setOnMouseClicked((Event) -> {
			innerContent.getChildren().clear();
			new Thread(() -> {
				
				if(findButton.getText().equals("Find"))
				{
					if(findField.getText().isEmpty())
						return ;
					
					JSONObject jso ;
					if( findField.getText().contains("open.qobuz.com") && typeSearch.getValue().toLowerCase().equals("track") )
						jso = QobuzApi.getTrack(users.getValue(), findField.getText().split("/")[findField.getText().split("/").length - 1] );
					else
						jso = QobuzApi.search(users.getValue(), typeSearch.getValue().toLowerCase(), findField.getText() , 0, 6);
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
				else
				{
					if(listInputArea.getText().isEmpty())
						return ;
					for(var music : listInputArea.getText().split("\\n"))
					{
						JSONObject jso = QobuzApi.getTrack(users.getValue(), music.split("/")[music.split("/").length - 1] );
						
						for( var elem : jso.getJSONObject(typeSearch.getValue().toLowerCase() + "s").getJSONArray("items"))
						{
							Supplement.getViewPaneByType(typeSearch.getValue() , (JSONObject)elem ).getChildren().get(0).getOnMouseClicked().handle(null) ;
						}
					}
				}
			}).start();
			
		});
	}
}
