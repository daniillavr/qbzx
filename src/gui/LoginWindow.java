package gui;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import qobuz_api.*;
import qobuz_api.QobuzApi.QobuzError;
import qobuz_api.QobuzApi.loginInfo;
import supplement.Supplement;

public class LoginWindow extends Scene implements Supplement.sceneSupplement
{
	Button							loginButton							;
	Pane							loginPane							;
	ImageView 						exitImage							;
	LableTextField					login								,
									password							;
	Label							status								;
	
	final String					name		= "Login window Qobuz"	;
	static public IntegerProperty	statusCode							;
	static final double				width		= 300					,
									height		= 500					;
	static LoginWindow				single								;
	loginInfo						user								;
	
	static
	{
		single = null ;
		statusCode = new SimpleIntegerProperty(0);
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
	
	@Override
	public <E> Property<E> getProperty(E a)
	{
		return (Property<E>)statusCode ;
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
		password.setLayoutY( 200 );
		
		status.setFont( Font.font( 17 ) );
		status.setLayoutX(loginPane.getWidth() / 2 - status.getPrefWidth() / 2);
		status.setLayoutY(loginButton.getLayoutY() + loginButton.prefHeight(-1) + 10 );
		status.setStyle("-fx-text-fill: white; -fx-alignment:center;");
		
		
		loginButton.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent arg0)
			{
				user = new loginInfo() ;
				
				user.getStatus().addListener(new ChangeListener<String>() {
					@Override
					public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2)
					{
						Platform.runLater(() -> status.setText(arg2));
					}
				});
				
				
				user.setLogin(login.getText());
				user.setPassword(password.getText());
				new Thread(new Runnable() {
						public void run() {
							try
							{
								QobuzApi.getSecret(user);
								QobuzApi.getUserToken(user);
								Thread.sleep(1000);
							}
							catch( QobuzError qe ) { System.out.println("Exception: " + qe.getMessage() ) ; return ; }
							catch (InterruptedException e) { e.printStackTrace(); }

							Platform.runLater(()->statusCode.setValue(1));

						}
					}).start();
				
				MainWindow.users.add(user);
			}
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
		
		loginPane.getChildren().addAll(login, password, loginButton, exitImage, status);
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
		this.getChildren().forEach(x -> x.applyCss());
		
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