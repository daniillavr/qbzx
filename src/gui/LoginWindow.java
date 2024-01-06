package gui;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.css.CssMetaData;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import qobuz_api.*;
import qobuz_api.QobuzApi.QobuzError;
import qobuz_api.QobuzApi.userInfo;
import supplement.Supplement;
import supplement.Supplement.DraggableArea;

public class LoginWindow extends Stage implements Supplement.sceneSupplement
{
	Button							loginButton							,
									exitButton							;
	Pane							loginPane							;
	DraggableArea					draggablePane						;
	Label							loginLabel							,
									passwordLabel						;
	TextField						loginField							,
									passwordField						;
	Scene							loginScene							;
	Label							saveUserLabel						;
	CheckBox						saveUserCheck						;
	final String					name		= "Login window Qobuz"	;
	static final double				width		= 300					,
									height		= 500					;
	static LoginWindow				single								;
	userInfo						user								;
	static Boolean					saveUserInfo						;
	
	static
	{
		single = null ;
		saveUserInfo = false ;
	}
	
	{
		user = null ;
	}
	
	@Override
	public String getName()
	{
		return name ;
	}
	
	static public LoginWindow createInstance()
	{
		if(single != null)
			return single ;
		return ( single = new LoginWindow( ) );
	}
	
	void InitObject()
	{
		loginPane		= new Pane()							;
		
		loginButton		= new Button( "Login" )					;
		exitButton		= new Button("")						;
		
		loginLabel		= new Label("Login:")					;
		passwordLabel	= new Label("Password:")				;
		saveUserLabel	= new Label("Save login/password")		;
		
		loginField		= new TextField()						;
		passwordField	= new TextField()						;
		
		saveUserCheck	= new CheckBox()						;
		
		loginScene		= new Scene(loginPane, width, height)	;
		
		loginPane.getChildren().addAll(loginLabel, passwordLabel, loginField, passwordField, loginButton, exitButton, saveUserCheck, saveUserLabel);
		
		setScene(loginScene) ;
		initStyle( StageStyle.TRANSPARENT );
	}
	
	void configCss()
	{
		loginScene.getStylesheets().add( MainWindow.class.getResource("/css/main.css").toExternalForm()) ;
		
		loginPane.getStyleClass().add("login-pane") ;
		
		loginField.getStyleClass().add("login-field") ;
		passwordField.getStyleClass().add("login-field") ;
		
		loginLabel.getStyleClass().add("login-label") ;
		passwordLabel.getStyleClass().add("login-label") ;
		saveUserLabel.getStyleClass().add("save-user-label") ;
		
		loginButton.getStyleClass().add("login-button") ;
		exitButton.getStyleClass().add("exit-button") ;
		
		saveUserCheck.getStyleClass().add("save-user-checkbox") ;
	}
	
	LoginWindow( )
	{
		super();
		InitObject();
		configCss();
		
		loginButton.setPrefWidth(100);
		
		loginPane.applyCss();
		loginPane.layout();
		
		loginPane.setStyle("-fx-background-color: black");
		
		loginButton.setLayoutX( loginPane.getWidth() / 2 - loginButton.getWidth() / 2 ) ;
		loginButton.setLayoutY( loginPane.getHeight() - 100 );
		
		loginLabel.setLayoutX( 20 ) ;
		loginLabel.setLayoutY( 160 );
		
		passwordLabel.setLayoutX( 20 ) ;
		passwordLabel.setLayoutY( loginLabel.getLayoutY() + 40 );
		
		loginField.setLayoutX( loginScene.getWidth() / 2 ) ;
		loginField.setLayoutY( loginLabel.getLayoutY() );
		loginField.setPrefWidth( loginScene.getWidth() / 2 - loginLabel.getLayoutX() ) ;
		
		passwordField.setLayoutX( loginScene.getWidth() / 2 ) ;
		passwordField.setLayoutY( passwordLabel.getLayoutY() );
		passwordField.setPrefWidth( loginScene.getWidth() / 2 - loginLabel.getLayoutX() ) ;
		
		saveUserLabel.setLayoutX(loginScene.getWidth() / 2 - ( saveUserLabel.getWidth() + 20 + saveUserCheck.getWidth() ) / 2 );
		saveUserLabel.setLayoutY(passwordLabel.getLayoutY() + 40 );
		
		saveUserCheck.setLayoutX( saveUserLabel.getLayoutX() + saveUserLabel.getWidth() + 20 );
		saveUserCheck.setLayoutY(passwordLabel.getLayoutY() + 40 );
		
		saveUserCheck.setOnAction( (Event) -> {
					LoginWindow.saveUserInfo = !LoginWindow.saveUserInfo ;
				});
		
		loginButton.setOnMouseClicked( (Event) -> {
					QobuzApi.userInfo newUser ;
					
					if( user != null )
					{
						newUser = user ;
						newUser.setLogin(loginField.getText());
						newUser.setPassword(passwordField.getText());
						newUser.setSave(saveUserCheck.isArmed());
						
						newUser = new QobuzApi.userInfo();
						supplement.Settings.users.add(newUser);
						supplement.Settings.users.remove(newUser) ; // Need to trigger event handler in Dashboard. Don't wanna make user as property.
					}
					else
					{
						newUser = new userInfo() ;
						newUser.setLogin(loginField.getText());
						newUser.setPassword(passwordField.getText());
						newUser.setSave(saveUserCheck.isArmed());
						supplement.Settings.addUser(newUser);
					}
					
					user = null ;
					
					Platform.runLater( () -> this.hide() );
		});
		
		exitButton.setOnMouseClicked(Event -> {
			this.hide();
		});
		
		loginPane.applyCss();
		loginPane.layout();
		
		exitButton.setPrefSize(loginButton.prefHeight(-1), loginButton.prefHeight(-1));
		exitButton.setLayoutX(loginPane.getWidth() - exitButton.prefWidth(-1));
		
		draggablePane = new DraggableArea(0, 0, loginScene.getWidth() - exitButton.getPrefWidth(), exitButton.getPrefHeight(), loginScene) ;
		draggablePane.getStyleClass().add("draggable-pane") ;
		
		loginPane.getChildren().add(draggablePane) ;
		
	}
	
	public void setLoginPass(String login, String password, QobuzApi.userInfo userEdit)
	{
		loginField.setText(login);
		passwordField.setText(password);
		
		user = userEdit ;
	}
	
}