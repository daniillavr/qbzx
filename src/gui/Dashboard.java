package gui;

import java.util.AbstractMap.SimpleEntry;
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
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Pair;
import javafx.util.StringConverter;
import qobuz_api.QobuzApi;
import qobuz_api.QobuzApi.userInfo;
import supplement.Supplement;
import supplement.Supplement.DraggableArea;
import supplement.viewElements;
import javafx.geometry.Insets;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;

public class Dashboard extends Scene implements Supplement.sceneSupplement
{
	final String							name		= "Dashboard"	;
	final static double						height		= 500			,
											width		= 1100			;
	Thread									loginThread					,
											searchThread				;
	Scene									dashScene					;
	Pane									dashPane					,
											innerContent				;
	DraggableArea							draggableArea				;
	ScrollPane								contentPane					;
	TextField								findField					;
	TextArea								listInputArea				;
	Button									settingsButton				,
											listSearchButton			,
											listUnfoldButton			,
											nextButton					,
											prevButton					,
											exitButton					;
	Label									currPage					,
											allPages					,
											delimiter					;
	ComboBox<String>						typeSearch					;
	ComboBox<Pair<String, Integer>>			audioFormats				,
											findButton					;
	ComboBox<userInfo>						users						;
	List<Pane>								viewElems					;
	static Dashboard						single						;
	double									paddingCont					;

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
	
	void configCss()
	{
		dashPane.getStylesheets().add( MainWindow.class.getResource("/css/main.css").toExternalForm()) ;
		
		dashPane.getStyleClass().add("dashboard-pane") ;
		
		allPages.getStyleClass().add("all-pages-label") ;
		currPage.getStyleClass().add("current-page-label") ;
		delimiter.getStyleClass().add("delimiter-page-label") ;
		
		settingsButton.getStyleClass().add("settings-button") ;
		nextButton.getStyleClass().add("next-page-button") ;
		prevButton.getStyleClass().add("prev-page-button") ;
		listUnfoldButton.getStyleClass().add("list-infold-button") ;
		exitButton.getStyleClass().add("exit-button") ;
		
		audioFormats.getStyleClass().add("audio-formats-combobox") ;
		typeSearch.getStyleClass().add("type-seacrh-combobox") ;
		users.getStyleClass().add("users-combobox") ;
		findButton.getStyleClass().add("find-combobox") ;
		
		findField.getStyleClass().add("find-field") ;
		listInputArea.getStyleClass().add("list-input-field") ;
		
		dashPane.getStyleClass().add("dash-pane") ;
		contentPane.getStyleClass().add("content-pane") ;
		innerContent.getStyleClass().add("inner-pane") ;
	}
	
	void tryLogin()
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
			loginThread = new Thread(() -> {
				try {
					QobuzApi.loginUser(users.getValue());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} ) ;
			loginThread.start();
		} ) ;
	}
	
	void InitUsersCombobox()
	{
		for( var user : supplement.Settings.users)
			users.getItems().add(user);
		users.getItems().add(new userInfo("+","","","","")) ;
		users.setValue(users.getItems().get(0));
		
		users.setCellFactory(new Callback<ListView<QobuzApi.userInfo>,ListCell<QobuzApi.userInfo>>() {
			@Override
			public ListCell<userInfo> call(ListView<userInfo> param) {
				return new ListCell<userInfo>() {
					@Override
					public void updateItem( userInfo ui , boolean empty )
					{
						if( empty )
						{
							setText(null);
							setGraphic(null);
							return ;
						}
						
						if(ui.getLogin().equals("+"))
						{
							setText(ui.getLogin());
							setGraphic(null);
						}
						else
						{
							setText(ui.getLogin());
							Button bt = new Button("↷") ;
							Label lb = new Label(ui.getLogin());
							
							HBox hb = new HBox( lb, bt) ;
							
							setGraphic(hb);
							
							hb.setHgrow(lb, Priority.ALWAYS);
							
							bt.setPrefWidth(25) ;
							bt.setFont(this.getFont());
							bt.getStyleClass().add("users-change-button") ;
							
							lb.setFont(this.getFont());
							lb.setMaxWidth(Double.POSITIVE_INFINITY);
							
							bt.setOnMousePressed(ev -> {
								LoginWindow.createInstance().setLoginPass(ui.getLogin(), ui.getPassword(), ui) ;
								LoginWindow.createInstance().show(); ;
							}) ;
							
							setGraphic(hb);
							setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
						}
						
						super.updateItem(ui, empty) ;
					}
				};
			}
			
		}) ;
		
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
				tryLogin() ;
			}
		});
		
		users.valueProperty().addListener((obs, oldValue, newValue) -> {
					if(newValue == null)
						return ;
					
					if(newValue.getLogin().equals("+"))
					{
						Platform.runLater(() -> users.setValue(null) ) ;
						LoginWindow.createInstance().setLoginPass( "" , "" , null ) ;
						LoginWindow.createInstance().show() ;
					}
					else if(!users.isArmed())
					{
						tryLogin() ;
					}
				});
		
		supplement.Settings.users.addListener(new ListChangeListener<QobuzApi.userInfo>() {
			@Override
			public void onChanged(Change<? extends userInfo> c) {
				Platform.runLater(() -> {
					users.getItems().clear() ;
					users.getItems().setAll(supplement.Settings.users) ;
					users.getItems().add(new userInfo("+","","","",""));
					
					users.setValue(supplement.Settings.users.get(supplement.Settings.users.size() - 1));
				} ) ;
			}
		}) ;
	}
	
	ComboBox<Pair<String, Integer>> customFindButtonComboBox()
	{
		return new ComboBox<Pair<String, Integer>>( ) {
			double	xMouseClicked			;
			double	showX			= 56.0	;
			
			{
				xMouseClicked = 0 ;
				
				super.setOnMousePressed( event -> {
					xMouseClicked = event.getX() ;
					
					if(xMouseClicked > showX)
						return ;
					
					if(this.getValue().getKey().equals("Stop"))
					{
						if(searchThread != null && searchThread.isAlive())
						{
							searchThread.interrupt() ;
							try {
								searchThread.join() ;
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						
						return ;
					}
					
					if( !(users.getItems().get(0) != null && users.getItems().get(0).getStatus().getValue() == Supplement.statuses.LOGGED))
						return ;
					
					if(findButton.getItems().get(0).getValue() == 1)
						Platform.runLater(() -> this.setItem(0 ,new Pair<String, Integer>("Stop", 1)) ) ;
					else if(findButton.getItems().get(0).getValue() == 2)
						Platform.runLater(() -> this.setItem(0, new Pair<String, Integer>("Stop", 2)) ) ;
					
					clearInnerContent();
					
					searchThread = new Thread(() -> {
						try
						{	
							if(findButton.getItems().get(0).getValue() == 1)
							{
								if(findField.getText().isEmpty())
									throw new Exception("Empty field for searching") ;
								
								JSONObject jso ;
								if( findField.getText().contains("open.qobuz.com") && typeSearch.getValue().toLowerCase().equals("track") )
									jso = QobuzApi.getTrack(users.getValue(), findField.getText().split("/")[findField.getText().split("/").length - 1] );
								else
									jso = QobuzApi.search(users.getValue(), typeSearch.getValue().toLowerCase(), findField.getText() , 0, 30);
								
								Platform.runLater(()->{
									currPage.setText("1") ;
									allPages.setText( String.valueOf( Integer.valueOf(jso.getJSONObject(typeSearch.getValue().toLowerCase() + "s").getJSONArray("items").length()) / 10 + 
											( Integer.valueOf(jso.getJSONObject(typeSearch.getValue().toLowerCase() + "s").getJSONArray("items").length()) % 10 != 0 ? 1 : 0)) ) ;
									innerContent.setMinHeight(Integer.valueOf( allPages.getText() ) * 5 * (viewElements.baseEntry.height + 5) + 5 );
								});
								
								int x = 0 ,
									y = 0 ;
								
								for( var elem : jso.getJSONObject(typeSearch.getValue().toLowerCase() + "s").getJSONArray("items"))
								{
									Thread.sleep(1);
									
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
							}
							else if(findButton.getItems().get(0).getValue() == 2)
							{
								if(listInputArea.getText().isEmpty())
									throw new Exception("Empty field for downloading") ;
								for(var music : listInputArea.getText().split("\\n"))
								{
									Thread.sleep(1);
									
									String type = music.contains("track") ? "track" : (music.contains("album") ? "album" : "") ;
									JSONObject jso = QobuzApi.getInfo(users.getValue(), type, music.split("/")[music.split("/").length - 1] );
									
									if(jso == null)
										continue ;

									Supplement.getViewPaneByType(type , jso ).getChildren().get(0).getOnMouseClicked().handle(null) ;
								}
							}
						}
						catch(InterruptedException ex)
						{
							System.out.println("Find button exception: " + ex.getMessage());
						}
						catch(Exception e)
						{
							System.out.println("Find button exception: " + e.getMessage());
						}
						finally
						{
							Platform.runLater(() -> this.setItem(0 , new Pair<String, Integer>("Find", 1))) ;
						}
					});
					Platform.runLater(() -> searchThread.start());
					
				});
				
				super.valueProperty().addListener((obs, oldValue, newValue) -> {
					if(newValue.getKey().equals("Clear"))
					{
						clearInnerContent();
						Platform.runLater(() -> super.setValue(super.getItems().get(0)) ) ;
					}
				});
				
				super.setCellFactory( new Callback<ListView<Pair<String, Integer>>, ListCell<Pair<String, Integer>>>() {

					@Override
					public ListCell<Pair<String, Integer>> call(ListView<Pair<String, Integer>> param) {
						return new ListCell<Pair<String, Integer>>() {
							public void updateItem(Pair<String, Integer> item, boolean empty)
							{	
								if(!empty && item.getKey().equals("Clear"))
								{
									setPrefHeight(-1) ;
									setText(item.getKey());
								}
								else
								{
									setPrefHeight(1) ;
									setMinHeight(1) ;
									setHeight(1) ;
									setText(null);
									setGraphic(null);
								}
								
								super.updateItem(item, empty) ;
							}
						};
					}} ) ;
				
				super.setButtonCell(new ListCell<Pair<String, Integer>>() {
					public void updateItem(Pair<String, Integer> item, boolean empty)
					{	
						if(!empty)
						{
							setPrefHeight(-1) ;
							setText(item.getKey());
						}
						else
						{
							setText(null);
							setGraphic(null);
						}
						
						super.updateItem(item, empty) ;
					}
				});
			}
			
			@Override
			public void show()
			{
				if( xMouseClicked > showX )
					super.show();
			}
			
			public void setItem( int index , Pair<String, Integer> val )
			{
				this.getItems().set(index, val) ;
				this.setValue(this.getItems().get(index));
			}
		} ;
	}
	
	void InitObject()
	{
		dashPane		= new Pane()		;
		contentPane		= new ScrollPane()	;
		innerContent	= new Pane()		;
		
		findField		= new TextField()	;
		listInputArea	= new TextArea()	;
		
		listUnfoldButton	= new Button("🡣")			;
		nextButton			= new Button("🡢")			;
		prevButton			= new Button("🡠")			;
		settingsButton		= new Button( "Settings" )	;
		exitButton			= new Button()				;
		
		currPage	= new Label("0") ;
		delimiter	= new Label("/") ;
		allPages	= new Label("0") ;
		
		viewElems	= new LinkedList<Pane>()	;
		
		audioFormats	= new ComboBox<>()				;
		typeSearch		= new ComboBox<>()				;
		users			= new ComboBox<>()				;
		findButton		= customFindButtonComboBox()	;
		
		dashPane.getChildren().addAll(findField, findButton, audioFormats, contentPane, typeSearch, users, settingsButton, listInputArea, listUnfoldButton, nextButton, prevButton, currPage, delimiter, allPages, exitButton) ;
		this.setRoot(dashPane);
	}
	
	void configContentPaneWithControls()
	{
		dashPane.applyCss();
		dashPane.layout();
		
		for(var CSSStyle : contentPane.getCssMetaData()) // Get padding for correct listing of items
		{
			if(CSSStyle.getProperty().equals("-fx-padding") )
			{
				CssMetaData<ScrollPane, ?> cssPane = (CssMetaData<ScrollPane, ?> )CSSStyle ;
				System.out.println(cssPane.getStyleableProperty(contentPane));
				Insets rg = ((ObjectProperty<Insets>)cssPane.getStyleableProperty(contentPane)).get() ;
				
				paddingCont = rg.getBottom() + rg.getTop() ;
			}
		}
		
		contentPane.vvalueProperty().addListener((x) -> {
			double	allLength = innerContent.getMinHeight() ;
			double	lastHeight = allLength - contentPane.getHeight() ;
			double	neededItemsHeight = 5 + (viewElements.baseEntry.height + 5) * 5 + paddingCont ;
			double	invisiblePart = neededItemsHeight - contentPane.getHeight() ;
			double	reqItems = (invisiblePart - 10 + paddingCont + contentPane.getHeight() ) * (Integer.valueOf(currPage.getText() ) -1) ;
			double	vPerRow = contentPane.getVmax() / lastHeight ;
			
			if( contentPane.getVvalue() > vPerRow * ( reqItems + invisiblePart ))
				Platform.runLater(() -> contentPane.setVvalue( vPerRow * ( reqItems + invisiblePart ) ));
			else if( contentPane.getVvalue() < vPerRow * reqItems)
				Platform.runLater(() -> contentPane.setVvalue( vPerRow * reqItems ));
			
			
		});
		
		nextButton.setOnMouseClicked((Event) -> {
			if( currPage.getText().equals(allPages.getText()))
				return ;
			
			double	allLength = innerContent.getMinHeight() ;
			double	lastHeight = allLength - contentPane.getHeight() ;
			double	neededItemsHeight = (viewElements.baseEntry.height + 5) * 5 + 5 + paddingCont ;
			double	invisiblePart = neededItemsHeight - contentPane.getHeight() ;
			double	reqItems = (invisiblePart - 10 + paddingCont + contentPane.getHeight() ) * Integer.valueOf(currPage.getText()) ;
			double	vPerRow = contentPane.getVmax() / lastHeight ;
			
			Platform.runLater(() -> currPage.setText( String.valueOf( Integer.valueOf(currPage.getText()) + 1) ));
			
			Platform.runLater(() -> contentPane.setVvalue(reqItems * vPerRow ));
		}) ;
		
		prevButton.setOnMouseClicked((Event) -> {
			if( Integer.valueOf( currPage.getText() ) <= 1)
				return ;

			Platform.runLater(() -> currPage.setText( String.valueOf( Integer.valueOf(currPage.getText()) - 1) ));
			
			int		prevPage =  Integer.valueOf(currPage.getText()) - 2 ;
			
			double	allLength = innerContent.getMinHeight() ;
			double	lastHeight = allLength - contentPane.getHeight() ;
			double	neededItemsHeight = (viewElements.baseEntry.height + 5) * 5 + 5 + paddingCont ;
			double	invisiblePart = neededItemsHeight - contentPane.getHeight() ;
			double	reqItems = (invisiblePart - 10 + paddingCont + contentPane.getHeight() ) * prevPage ;
			double	vPerRow = contentPane.getVmax() / lastHeight ;
			
			Platform.runLater(() -> contentPane.setVvalue(reqItems * vPerRow ));

		}) ;
	}
	
	Dashboard( )
	{
		super(new Group(), width, height);
		
		InitObject() ;
		configCss() ;
		InitUsersCombobox() ;
		configContentPaneWithControls() ;
		
		contentPane.setVmax(100) ;

		findButton.getItems().addAll( new Pair<>("Find" , 1), new Pair<>("Clear" , 0) ) ;
		findButton.setValue(findButton.getItems().get(0)) ;
		
		dashPane.applyCss();
		dashPane.layout();
		
		findField.setPrefWidth(500);
		findField.setLayoutX(50);
		findField.setLayoutY(users.getLayoutY() + users.prefHeight( -1 ) + 10);
		findField.setPromptText("Rammstein...");
		
		listUnfoldButton.setLayoutX(findField.getLayoutX() + findField.getPrefWidth());
		listUnfoldButton.setLayoutY(findField.getLayoutY());
		
		typeSearch.getItems().addAll(QobuzApi.typesSearch);
		typeSearch.setValue(typeSearch.getItems().get(0));
		typeSearch.setLayoutX(listUnfoldButton.getLayoutX() + listUnfoldButton.prefWidth(-1) + 10 );
		typeSearch.setLayoutY(findField.getLayoutY());
		typeSearch.setPrefWidth( typeSearch.prefWidth(-1) + 25 ); 
		
		contentPane.setPrefWidth( 740 ) ;
		contentPane.setPrefHeight( 300 ) ;
		contentPane.setLayoutX(findField.getLayoutX());
		contentPane.setLayoutY(findField.getLayoutY() + findField.prefHeight(-1) + 20);
		contentPane.setContent(innerContent);
		contentPane.requestFocus();
		
		prevButton.setLayoutX(contentPane.getLayoutX()) ;
		prevButton.setLayoutY(contentPane.getLayoutY() + contentPane.getPrefHeight()) ;
		
		currPage.setLayoutY(contentPane.getLayoutY() + contentPane.getPrefHeight()) ;
		currPage.setLayoutX(prevButton.getLayoutX() + prevButton.prefWidth(-1)) ;
		currPage.setPrefWidth(20);
		
		delimiter.setLayoutY(contentPane.getLayoutY() + contentPane.getPrefHeight()) ;
		delimiter.setLayoutX(currPage.getLayoutX() + currPage.prefWidth(-1)) ;
		delimiter.setPrefWidth(10);
		
		allPages.setLayoutY(contentPane.getLayoutY() + contentPane.getPrefHeight()) ;
		allPages.setLayoutX(delimiter.getLayoutX() + delimiter.prefWidth(-1)) ;
		allPages.setPrefWidth(20);
		
		nextButton.setLayoutX(allPages.getLayoutX() + allPages.prefWidth(-1)) ;
		nextButton.setLayoutY(contentPane.getLayoutY() + contentPane.getPrefHeight()) ;
		
		findButton.setLayoutX(typeSearch.getLayoutX() + typeSearch.prefWidth(-1) + 10 );
		findButton.setLayoutY(findField.getLayoutY());
		findButton.setPrefWidth(contentPane.getLayoutX() + contentPane.prefWidth(-1) - findButton.getLayoutX());
		
		exitButton.setPrefSize(users.prefHeight(-1), users.prefHeight(-1)) ;
		exitButton.setLayoutX(this.getWidth() - exitButton.prefWidth(-1) );
		
		exitButton.setOnMouseClicked(ev -> {
			Platform.exit() ;
		});
		
		settingsButton.setLayoutX(exitButton.getLayoutX() - settingsButton.prefWidth(-1) );
		
		listInputArea.setPrefWidth(findField.prefWidth(-1));
		listInputArea.setPrefHeight(300);
		listInputArea.setVisible(false);
		listInputArea.setLayoutX(findField.getLayoutX());
		listInputArea.setLayoutY(findField.getLayoutY());
		listInputArea.setTooltip(new Tooltip());
		listInputArea.getTooltip().setText("Insert URL's to albums/tracks.\nSeparate them with new line(Enter button)");
		
		focusOwnerProperty().addListener(node -> {
			if(!listInputArea.isVisible())
				return ;
			
			if(	listUnfoldButton.isFocused() ||
				findButton.isFocused() ||
				audioFormats.isFocused() ||
				listInputArea.isFocused() )
			{
			}
			else
			{
				listInputArea.setVisible(false);
				audioFormats.setVisible(false) ;
				typeSearch.setVisible(true) ;
				listUnfoldButton.setText("🡣");
			}
		}) ;
		
		listUnfoldButton.setOnMousePressed((Event) -> {
						listInputArea.setVisible(!listInputArea.isVisible());
						if( listInputArea.isVisible() )
						{
							findButton.getItems().set(0, new Pair<>("Download", 2)) ;
							typeSearch.setVisible(false) ;
							audioFormats.setVisible(true) ;
							listInputArea.requestFocus();
							listUnfoldButton.setText("🡡");
						}
						else
						{
							findButton.getItems().set(0, new Pair<>("Find", 1)) ;
							typeSearch.setVisible(true) ;
							audioFormats.setVisible(false) ;
							listUnfoldButton.setText("🡣");
						}
				});
		
		settingsButton.setOnMouseClicked((Event) -> {
						new Settings() ;
				});

		audioFormats.getItems().addAll(QobuzApi.audioFormats);
		audioFormats.setValue(audioFormats.getItems().get(0));
		audioFormats.setLayoutX(typeSearch.getLayoutX() );
		audioFormats.setLayoutY(typeSearch.getLayoutY() );
		audioFormats.setPrefWidth( typeSearch.prefWidth(-1) ) ;
		audioFormats.setVisible(false) ;
		audioFormats.setTooltip(new Tooltip());
		audioFormats.getTooltip().setText("MP3 = MP3 320\nFLAC = FLAC Lossless\nFLAC+ = FLAC Hi-Res 24 bit =< 96kHz\nFLAC++ = FLAC Hi-Res 24 bit >96 kHz & =< 192 kHz") ;
		
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
		
		contentPane.applyCss() ;
		contentPane.layout() ;
		
		draggableArea = new DraggableArea( users.getLayoutX() + users.prefWidth( -1 ) , users.getLayoutY() , settingsButton.getLayoutX() - users.getLayoutX() - users.prefWidth( -1 ) , users.prefHeight( -1 ) ,
				this ) ;
		draggableArea.setLayoutX(users.getLayoutX() + users.prefWidth( -1 ));
		draggableArea.setLayoutY(users.getLayoutY());
		draggableArea.getStyleClass().add("draggable-pane") ;
		dashPane.getChildren().add( draggableArea ) ;
		
	}
	
	void clearInnerContent()
	{
		Platform.runLater(() -> {
			innerContent.getChildren().clear();
			innerContent.setMinHeight(0);
			innerContent.setPrefHeight(0);
		}) ;
	}
}
