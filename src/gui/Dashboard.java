package gui;

import java.util.LinkedList;
import java.util.List;

import org.json.*;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.css.CssMetaData;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import javafx.util.StringConverter;
import qobuz_api.QobuzApi;
import qobuz_api.QobuzApi.userInfo;
import supplement.Supplement;
import supplement.viewElements;
import javafx.geometry.Insets;

public class Dashboard extends Scene implements Supplement.sceneSupplement
{
	final String						name		= "Dashboard"	;
	final static double					height		= 500			,
										width		= 1000			;
	Thread								loginThread					;
	Scene								dashScene					;
	Pane								dashPane					,
										innerContent				;
	ScrollPane							contentPane					;
	TextField							findField					;
	TextArea							listInputArea				;
	Button								findButton					,
										settingsButton				,
										listSearchButton			,
										listUnfoldButton			,
										nextButton					,
										prevButton					;
	Label								currPage					,
										allPages					,
										delimetr					;
	ComboBox<String>					typeSearch					;
	ComboBox<Pair<String, Integer>>		audioFormats				;
	ComboBox<userInfo>					users						;
	List<Pane>							viewElems					;
	static Dashboard					single						;
	double								paddingCont					;

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
		currPage = new Label("0") ;
		delimetr = new Label("/") ;
		allPages = new Label("0") ;
		contentPane = new ScrollPane();
		audioFormats = new ComboBox<>();
		innerContent = new Pane();
		typeSearch = new ComboBox<>();
		users = new ComboBox<>();
		viewElems = new LinkedList<Pane>();
		listInputArea = new TextArea() ;
		listUnfoldButton = new Button("🡣") ;
		nextButton = new Button("🡢") ;
		prevButton = new Button("🡠") ;
		
		contentPane.setVmax(100) ;
		
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
		
		users.setOnShowing(Event -> {
			Platform.runLater(() -> users.setValue(null) ) ;
		});
		
		users.setOnHiding(Event -> {
			Platform.runLater(() -> users.getItems().forEach(x -> {
				if( x == null )
					return ;
				
				if(x.getStatus().getValue() == Supplement.statuses.LOGGED)
					users.setValue(x) ;
			})) ;
			
			if(users.getValue() != null && !users.getValue().getLogin().equals("+") )
			{
				if(loginThread != null)
				{
					Platform.runLater(() -> {
						loginThread.interrupt() ;
						loginThread = null ;
					} ) ;
				}
				
				users.getValue().getStatus().addListener((obs1, oldValue1, newValue1) -> {
						Platform.runLater(() -> {
							users.setEditable(true) ;
							users.setEditable(false) ; // Trick to force ComboBox redraw
						} ) ;
					}
				);
				
				Platform.runLater(() -> {
					loginThread = new Thread(() -> QobuzApi.loginUser(users.getValue()) ) ;
					loginThread.start();
				} ) ;
			}
		});
		
		users.valueProperty().addListener((obs, oldValue, newValue) -> {
					if(newValue == null)
						return ;
					
					if(newValue.getLogin().equals("+"))
					{
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
				});
		
		this.setRoot(dashPane);
		users.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0;");

		dashPane.getChildren().addAll(findField, findButton, contentPane, typeSearch, users, settingsButton, listInputArea, listUnfoldButton, audioFormats, nextButton, prevButton, currPage, delimetr, allPages) ;
		
		allPages.setFont( Font.font( 17 ) );
		currPage.setFont( Font.font( 17 ) );
		delimetr.setFont( Font.font( 17 ) );
		
		allPages.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0; -fx-text-fill: black");
		currPage.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0; -fx-text-fill: black");
		delimetr.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0; -fx-text-fill: black");
		prevButton.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0; -fx-background-insets: 0;");
		nextButton.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0; -fx-background-insets: 0;");
		
		dashPane.applyCss();
		dashPane.layout();
		
		findField.setPrefWidth(500);
		findField.setLayoutX(50);
		findField.setLayoutY(users.getLayoutY() + users.prefHeight( -1 ) + 10);
		findField.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0;");
		findField.setFont( Font.font( 17 ) );

		listUnfoldButton.setFont(Font.font( 17 ));
		listUnfoldButton.setLayoutX(findField.getLayoutX() + findField.getPrefWidth());
		listUnfoldButton.setLayoutY(findField.getLayoutY());
		listUnfoldButton.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0; -fx-background-insets: 0;");
		
		typeSearch.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0;");
		typeSearch.getItems().addAll(QobuzApi.typesSearch);
		typeSearch.setValue(typeSearch.getItems().get(0));
		typeSearch.setLayoutX(listUnfoldButton.getLayoutX() + listUnfoldButton.prefWidth(-1) + 10 );
		typeSearch.setLayoutY(findField.getLayoutY());
		
		contentPane.setPrefWidth( 715 ) ;
		contentPane.setPrefHeight( 300 ) ;
		contentPane.setLayoutX(findField.getLayoutX());
		contentPane.setLayoutY(findField.getLayoutY() + findField.prefHeight(-1) + 20);
		contentPane.setContent(innerContent);
		contentPane.setStyle("-fx-background-color: rgba(0,0,0,0.4);-fx-vbar-policy: never;");
		
		prevButton.setFont(Font.font( 17 ));
		prevButton.setLayoutX(contentPane.getLayoutX()) ;
		prevButton.setLayoutY(contentPane.getLayoutY() + contentPane.getPrefHeight()) ;
		
		currPage.setLayoutY(contentPane.getLayoutY() + contentPane.getPrefHeight()) ;
		currPage.setLayoutX(prevButton.getLayoutX() + prevButton.prefWidth(-1)) ;
		currPage.setPrefWidth(20);
		
		delimetr.setLayoutY(contentPane.getLayoutY() + contentPane.getPrefHeight()) ;
		delimetr.setLayoutX(currPage.getLayoutX() + currPage.prefWidth(-1)) ;
		delimetr.setPrefWidth(10);
		
		allPages.setLayoutY(contentPane.getLayoutY() + contentPane.getPrefHeight()) ;
		allPages.setLayoutX(delimetr.getLayoutX() + delimetr.prefWidth(-1)) ;
		allPages.setPrefWidth(20);
		
		nextButton.setFont(Font.font( 17 ));
		nextButton.setLayoutX(allPages.getLayoutX() + allPages.prefWidth(-1)) ;
		nextButton.setLayoutY(contentPane.getLayoutY() + contentPane.getPrefHeight()) ;
		
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
		audioFormats.setLayoutX(typeSearch.getLayoutX() );
		audioFormats.setLayoutY(typeSearch.getLayoutY() - audioFormats.prefHeight(-1) - 5);
		
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
		
		contentPane.vvalueProperty().addListener((x) -> {
			double allLength = innerContent.prefHeight(-1);
			double lastHeight = allLength - contentPane.getHeight() ;
			double neededItemsHeight = (viewElements.baseEntry.height + 5) * 5 + 5 ;
			double invisiblePart = neededItemsHeight - contentPane.getHeight() ;
			double beforePart = ( Integer.valueOf(currPage.getText()) - 1 ) == 0 ? 0 : 
				contentPane.getHeight() + ( Integer.valueOf(currPage.getText()) - 1 ) * ( (viewElements.baseEntry.height + 5) * 5 + 5 );
			double reqItems = (invisiblePart - 10 + paddingCont + contentPane.getHeight() ) * (Integer.valueOf(currPage.getText() ) -1) ;
			
			double vPerRow = contentPane.getVmax() / lastHeight ;
			
			if( contentPane.getVvalue() > vPerRow * ( reqItems + invisiblePart ))
				Platform.runLater(() -> contentPane.setVvalue( vPerRow * ( reqItems + invisiblePart ) ));
			else if( contentPane.getVvalue() < vPerRow * reqItems)
				Platform.runLater(() -> contentPane.setVvalue( vPerRow * reqItems ));
			
			
		});
		
		for(var CSSStyle : contentPane.getCssMetaData()) // Get padding for correct listing of items
		{
			if(CSSStyle.getProperty().equals("-fx-padding") )
			{
				CssMetaData<ScrollPane, ?> cssPane = (CssMetaData<ScrollPane, ?> )CSSStyle ;
				System.out.println(cssPane.getStyleableProperty(contentPane));
				ObjectProperty<Insets> rg = (ObjectProperty<Insets>)cssPane.getStyleableProperty(contentPane) ;
				
				paddingCont = rg.get().getBottom() + rg.get().getTop() ;
				
				break ;
			}
		}
		
		nextButton.setOnMouseClicked((Event) -> {
			if( currPage.getText().equals(allPages.getText()))
				return ;
			double allLength = innerContent.prefHeight(-1);
			double lastHeight = allLength - contentPane.getHeight() ;
			double neededItemsHeight = (viewElements.baseEntry.height + 5) * 5 + 5 ;
			double invisiblePart = neededItemsHeight - contentPane.getHeight() ;
			
			double reqItems = (invisiblePart - 10 + paddingCont + contentPane.getHeight() ) * Integer.valueOf(currPage.getText()) ;
			Platform.runLater(() -> currPage.setText( String.valueOf( Integer.valueOf(currPage.getText()) + 1) ));
			
			double vPerRow = contentPane.getVmax() / lastHeight ;
			System.out.println(reqItems + " " +  vPerRow * reqItems);
			
			Platform.runLater(() -> contentPane.setVvalue(reqItems * vPerRow ));
		}) ;
		
		prevButton.setOnMouseClicked((Event) -> {
			if( currPage.getText().equals("1"))
				return ;

			Platform.runLater(() -> currPage.setText( String.valueOf( Integer.valueOf(currPage.getText()) - 1) ));
			
			int prevPage =  Integer.valueOf(currPage.getText()) - 2 ;
			
			double allLength = innerContent.prefHeight(-1);
			double lastHeight = allLength - contentPane.getHeight() ;
			double neededItemsHeight = (viewElements.baseEntry.height + 5) * 5 + 5 ;
			double invisiblePart = neededItemsHeight - contentPane.getHeight() ;

			double reqItems = (invisiblePart - 10 + paddingCont + contentPane.getHeight() ) * prevPage ;
			
			double vPerRow = contentPane.getVmax() / lastHeight ;
			System.out.println(reqItems + " " +  vPerRow * reqItems);
			
			Platform.runLater(() -> contentPane.setVvalue(reqItems * vPerRow ));

		}) ;
		
		findButton.setOnMouseClicked((Event) -> {
			innerContent.getChildren().clear();
			new Thread(() -> {
				if( !(users.getItems().get(0) != null && users.getItems().get(0).getStatus().getValue() == Supplement.statuses.LOGGED))
					return ;
				
				Platform.runLater(() -> {
					innerContent.getChildren().clear();
					innerContent.setMinHeight(0);
					}) ;
				
				if(findButton.getText().equals("Find"))
				{
					if(findField.getText().isEmpty())
						return ;
					
					JSONObject jso ;
					if( findField.getText().contains("open.qobuz.com") && typeSearch.getValue().toLowerCase().equals("track") )
						jso = QobuzApi.getTrack(users.getValue(), findField.getText().split("/")[findField.getText().split("/").length - 1] );
					else
						jso = QobuzApi.search(users.getValue(), typeSearch.getValue().toLowerCase(), findField.getText() , 0, 30);
					
					int x = 0 ,
						y = 0 ;
					
					for( var elem : jso.getJSONObject(typeSearch.getValue().toLowerCase() + "s").getJSONArray("items"))
					{
						Pane elemView = Supplement.getViewPaneByType(typeSearch.getValue() , (JSONObject)elem ) ;
						
						if( x > 1 )
						{
							x = 0 ;
							++y ;
						}
						
						elemView.setOnMouseClicked(new EventHandler<MouseEvent>()
						{
							JSONObject self = (JSONObject)elem;
							viewElements.musicView mv = null ;
							@Override
							public void handle(MouseEvent arg0)
							{
								mv = new viewElements.musicView(self, contentPane);
							}
						});
						elemView.setLayoutY( ( elemView.getPrefHeight() + 5 ) * y + 5 ) ;
						elemView.setLayoutX( ( elemView.getPrefWidth() + 5 )* x + 5 ) ;
						Platform.runLater(()->innerContent.getChildren().add( elemView ) );
						
						++x ;
					}
					Platform.runLater(()->{
						innerContent.setMinHeight(innerContent.prefHeight(-1) + 5) ;
						currPage.setText("1") ;
						allPages.setText( String.valueOf( innerContent.getChildren().size() / 10 + ( innerContent.getChildren().size() % 10 != 0 ? 1 : 0)) ) ;
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
