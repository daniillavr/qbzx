package gui;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import supplement.Supplement;

public class Settings extends Stage
{
	Scene		settingsScene		;
	Pane		settingsPane		;
	Label		downloadPathLabel	;
	TextField	downloadPathField	;
	Button		saveButton			;
	
	public Settings()
	{
		super();
		
		this.setResizable( false ) ;
		this.initStyle( StageStyle.TRANSPARENT );
		
		settingsPane		= new Pane()						;
		settingsScene		= new Scene( settingsPane )			;
		saveButton			= new Button("Save")				;
		downloadPathLabel	= new Label("Download path")		;
		downloadPathField	= new TextField()					;
		
		settingsPane.setPrefSize(300, 300);
		this.setHeight(300);
		this.setWidth(300);
		this.setScene( settingsScene );
		
		settingsPane.getChildren().addAll( downloadPathLabel , downloadPathField , saveButton ) ;
		
		settingsPane.applyCss();
		settingsPane.layout();
		
		downloadPathLabel.setLayoutX(this.getWidth() / 2 - downloadPathLabel.prefWidth(-1) - 10);
		downloadPathField.setLayoutX(this.getWidth() / 2 + 10);
		saveButton.setLayoutX(this.getWidth() / 2 - saveButton.prefWidth(-1) / 2);
		saveButton.setLayoutY(this.getHeight() - 15);
		
		
		downloadPathLabel.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0;");
		downloadPathField.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0;");
		saveButton.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0; -fx-padding: 0; -fx-background-insets: 0;");
		
		Stage self = this ;
		saveButton.setOnMouseClicked(new EventHandler<MouseEvent>()
				{
					Stage settingsStage = self ; 
					@Override
					public void handle(MouseEvent event) {
						Supplement.settingsValue.downloadPath = downloadPathField.getText() ;
						settingsStage.close() ;
					}
				});
		
		downloadPathField.setText(Supplement.settingsValue.downloadPath);
		
		this.show();
		
	}
}
