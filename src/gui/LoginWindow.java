package gui;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.stage.Stage;
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

public class LoginWindow extends Scene implements Supplement.sceneSupplement
{
	Button							loginButton							;
	Pane							loginPane							;
	ImageView 						exitImage							;
	LableTextField					login								,
									password							;
	Label							status								,
									saveUserLabel						;
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
	
	static public LoginWindow createInstance()
	{
		if(single != null)
			return single ;
		return ( single = new LoginWindow( ) );
	}
	
	LoginWindow( )
	{
		super(new Group(), width, height);
		InitObject();
		
		status.setText("Start");
		
		exitImage.setFitHeight(30);
		exitImage.setFitWidth(30);
		loginButton.setPrefWidth(100);
		status.setPrefWidth(150);
		
		saveUserLabel.setFont( Font.font( 17 ) );
		saveUserCheck.setFont( Font.font( 17 ) );
		
		loginPane.applyCss();
		loginPane.layout();
		
		exitImage.setLayoutX(loginPane.getWidth() - exitImage.getFitWidth());
		loginPane.setStyle("-fx-background-color:black");
		loginButton.setFont( Font.font( 17 ) );
		loginButton.setStyle("-fx-background-color: rgba(255,255,255,0.4); -fx-background-radius: 0; -fx-padding: 0; -fx-background-insets: 0;");
		loginButton.setLayoutX( loginPane.getWidth() / 2 - loginButton.getWidth() / 2 ) ;
		loginButton.setLayoutY( loginPane.getHeight() - 100 );
		login.setLayoutX( loginPane.getWidth() / 2 - login.getWidth() / 2 ) ;
		login.setLayoutY( 160 );
		password.setLayoutX( loginPane.getWidth() / 2 - password.getWidth() / 2 ) ;
		password.setLayoutY( login.getLayoutY() + 40 );
		
		saveUserLabel.setLayoutX(this.getWidth() / 2 - ( saveUserLabel.getWidth() + 20 + saveUserCheck.getWidth() ) / 2 );
		saveUserLabel.setLayoutY(password.getLayoutY() + 40 );
		saveUserLabel.setStyle("-fx-text-fill: white; -fx-background-radius: 0; -fx-padding: 0;");
		
		saveUserCheck.setLayoutX( saveUserLabel.getLayoutX() + saveUserLabel.getWidth() + 20 );
		saveUserCheck.setLayoutY(password.getLayoutY() + 40 );
		saveUserCheck.setStyle("-fx-background-color: rgba(255,255,255,0.4); -fx-background-radius: 0; -fx-padding: 0; -fx-background-insets: 0;");
		
		status.setFont( Font.font( 17 ) );
		status.setLayoutX(loginPane.getWidth() / 2 - status.getPrefWidth() / 2);
		status.setLayoutY(loginButton.getLayoutY() + loginButton.prefHeight(-1) + 10 );
		status.setStyle("-fx-text-fill: white; -fx-alignment:center;");
		
		saveUserCheck.setOnAction( (Event) -> {
					LoginWindow.saveUserInfo = !LoginWindow.saveUserInfo ;
				});
		
		loginButton.setOnMouseClicked( (Event) -> {
					user = new userInfo() ;
					
					user.setLogin(login.getText());
					user.setPassword(password.getText());
					user.setSave(saveUserCheck.isArmed());
					
					supplement.Settings.addUser(user);
					Platform.runLater( () -> this.getRoot().getScene().windowProperty().get().fireEvent( new WindowEvent(this.getRoot().getScene().windowProperty().get() , WindowEvent.WINDOW_CLOSE_REQUEST) ) );
		});
		
		exitImage.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0)
			{
				Platform.exit();
			}
		});
	}
	
	void InitObject()
	{
		loginButton = new Button( "Login" ) ;
		loginPane = new Pane() ;
		login = new LableTextField("Login:", 150, 17, LableTextField.POSITION.LEFT);
		password = new LableTextField("Password:", 150, 17, LableTextField.POSITION.LEFT);
		exitImage = new ImageView( new Image(MainWindow.class.getResourceAsStream("/exit.png")) );
		status = new Label("") ;
		saveUserLabel = new Label("Save login/password") ;
		saveUserCheck = new CheckBox() ;
		
		loginPane.getChildren().addAll(login, password, loginButton, exitImage, status, saveUserCheck, saveUserLabel);
		this.setRoot(loginPane) ;
	}
}

class LableTextField extends Pane
{
	TextField	field	;
	Label		text	;
	
	public enum POSITION
	{
		LEFT , UP
	}
	
	public String getText() { return field.getText() ; }
	public void setText( String str ) { field.setText(str); ; }
	
	LableTextField( String labelText , int fw , int fonth , POSITION pos )
	{
		super();
		text = new Label( labelText ) ;
		field = new TextField();
		
		this.getChildren().addAll(text, field);
		Scene s = new Scene(this);
		
		text.setStyle("-fx-text-fill: white;");
		field.setStyle("""
				-fx-background-color: rgba(255,255,255,0.4);
				-fx-background-radius: 0;
				-fx-padding: 1;
				-fx-text-fill: white;
				-fx-background-insets: 0;
				""");
		this.setStyle("-fx-background-color: rgba(0,0,0,0);");
		
		text.setFont(new Font(fonth));
		field.setFont(new Font(fonth));
		
		this.applyCss() ;
		this.layout();
		
		field.setPrefSize(fw, text.prefHeight(-1));
		switch( pos )
		{
			case LEFT ->
				field.setLayoutX(text.prefWidth(-1) + 5 );
			case UP ->
			{
				field.setLayoutY(text.prefHeight(-1) + 5 );
				text.setLayoutX(this.prefWidth(-1) / 2 - text.prefWidth(-1)/2);
			}
		}
	}
}