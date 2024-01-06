package gui;

import java.io.File;
import java.io.IOException;

import javafx.application.Platform;
import javafx.stage.DirectoryChooser;
import javafx.beans.property.ObjectProperty;
import javafx.css.CssMetaData;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import supplement.Supplement;
import supplement.Supplement.DraggableArea;

public class Settings extends Stage
{
	Scene			settingsScene		;
	Pane			settingsPane		,
					contentPane			;
	DraggableArea	draggablePane		;
	Label			downloadPathLabel	;
	TextField		downloadPathField	;
	Button			saveButton			,
					chooseDirButton		,
					exitButton			;
	
	final double	windowHeight		,
					windowWidth			,
					valuesPaneHeight	,
					valuesPaneWidth		;
	
	
	{
		windowHeight		= 500	;
		windowWidth			= 400	;
		valuesPaneHeight	= 400	;
		valuesPaneWidth		= 350	;
	}
	
	void InitObject()
	{
		setResizable( false ) ;
		initStyle( StageStyle.TRANSPARENT );
		
		settingsPane		= new Pane()						;
		contentPane			= new Pane()						;
		settingsScene		= new Scene( settingsPane )			;
		saveButton			= new Button("Save")				;
		chooseDirButton		= new Button("↳")					;
		exitButton			= new Button("")					;
		downloadPathLabel	= new Label("Download path")		;
		downloadPathField	= new TextField()					;
		
		settingsPane.setPrefSize(windowWidth, windowHeight);
		setHeight(windowHeight);
		setWidth(windowWidth);
		setScene( settingsScene );
		
		settingsPane.getChildren().addAll( contentPane , saveButton, exitButton ) ;
		contentPane.getChildren().addAll( downloadPathLabel , downloadPathField , chooseDirButton ) ;
	}
	
	void configCss()
	{
		settingsScene.getStylesheets().add( MainWindow.class.getResource("/css/main.css").toExternalForm() ) ;
		
		settingsPane.getStyleClass().add("settings-pane") ;
		contentPane.getStyleClass().add("settings-content-pane") ;
		
		saveButton.getStyleClass().add("save-button") ;
		chooseDirButton.getStyleClass().add("choose-dir-button") ;
		exitButton.getStyleClass().add("exit-button") ;
		
		downloadPathLabel.getStyleClass().add("download-path-label") ;
		
		downloadPathField.getStyleClass().add("download-path-field") ;
	}
	
	public Settings()
	{
		super();
		InitObject() ;
		configCss();
		
		contentPane.setPrefSize(valuesPaneWidth, valuesPaneHeight);
		contentPane.setLayoutX((windowWidth - valuesPaneWidth) / 2);
		contentPane.setLayoutY((windowHeight - valuesPaneHeight) / 2);
		
		settingsPane.applyCss();
		settingsPane.layout();
		
		downloadPathLabel.setLayoutX(10);
		downloadPathLabel.setLayoutY(10);
		downloadPathField.setLayoutX(contentPane.getWidth() / 2 + 10);
		downloadPathField.setLayoutY(10);
		
		chooseDirButton.setMaxSize(downloadPathField.prefHeight(-1), downloadPathField.prefHeight(-1)) ;
		chooseDirButton.setPrefSize(downloadPathField.prefHeight(-1), downloadPathField.prefHeight(-1)) ;
		chooseDirButton.setLayoutX(contentPane.getWidth() - 10 - chooseDirButton.getPrefWidth()) ;
		chooseDirButton.setLayoutY(10);
		
		chooseDirButton.setOnMouseClicked( ev -> {
			DirectoryChooser dc = new DirectoryChooser() ;
			dc.setInitialDirectory(new File(".")) ;
			File file = dc.showDialog(getOwner());
			try {
				downloadPathField.setText( file.getCanonicalPath().toString() );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}) ;
		
		downloadPathField.setPrefWidth(chooseDirButton.getLayoutX() - downloadPathField.getLayoutX() - 1) ;
		
		saveButton.setLayoutX(this.getWidth() / 2 - saveButton.prefWidth(-1) / 2);
		saveButton.setLayoutY(this.getHeight() - saveButton.prefHeight(-1) - 10);
		
		saveButton.setOnMouseClicked((Event) -> {
						supplement.Settings.downloadPath = downloadPathField.getText() ;
						supplement.Settings.writeConfig() ;
						Platform.runLater( () -> this.close() );
				});
		
		downloadPathField.setText(supplement.Settings.downloadPath);
		
		exitButton.setPrefSize(saveButton.prefHeight(-1), saveButton.prefHeight(-1)) ;
		exitButton.setLayoutX(this.getWidth() - exitButton.prefWidth(-1)) ;
		exitButton.setOnMousePressed(ev -> {
			this.close();
		}) ;
		
		draggablePane = new DraggableArea(0, 0, exitButton.getLayoutX(), exitButton.prefHeight(-1), settingsScene) ;
		draggablePane.getStyleClass().add("draggable-pane") ;
		settingsPane.getChildren().add( draggablePane ) ;
		
		settingsPane.applyCss();
		settingsPane.layout();
		
		this.show();
		
	}
}
