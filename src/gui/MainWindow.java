package gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javafx.application.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import qobuz_api.QobuzApi;
import supplement.Supplement;

public class MainWindow extends Application
{
	static Stage stage ;
	static Supplement.sceneSupplement currentScene ;
	static ArrayList<Supplier<Supplement.sceneSupplement>> scenesSuppliers ;
	static List<QobuzApi.loginInfo> users ;
	
	
	static
	{
		scenesSuppliers = new ArrayList<Supplier<Supplement.sceneSupplement>>();
		users = new LinkedList<QobuzApi.loginInfo>() ;
	}
	
	public MainWindow()
	{
		super();
		
	}
	
	public MainWindow(String[] args , Supplier<Supplement.sceneSupplement>[] suppliers )
	{
		scenesSuppliers.addAll(Arrays.asList(suppliers));
		super.launch(args);
	}
	
	void initStage( Stage stage )
	{
		stage.setResizable( false ) ;
		stage.initStyle( StageStyle.TRANSPARENT );

		stage.show( ) ;
	}
	
	private void chageScene( )
	{
		stage.setTitle( currentScene.getName() ) ;
		stage.setHeight( currentScene.getHeight() );
		stage.setWidth( currentScene.getWidth() );
		stage.setScene( currentScene.getScene() ) ;
		//stage.initStyle( StageStyle.TRANSPARENT );

		stage.show( ) ;
	}
	
	@Override
	public void start( Stage arg0 ) throws Exception
	{
		stage = arg0 ;
		initStage( arg0 ) ;
		setScene(0);
		chageScene();
		addChangeListenerTo(0, new ChangeListener<Integer>()
		{
			@Override
			public void changed(ObservableValue<? extends Integer> arg0, Integer arg1, Integer arg2)
			{
				setScene(1);
				chageScene();
			}
			
		});
	}
	
	public void addSceneObject( Supplier<Supplement.sceneSupplement> supplier )
	{
		scenesSuppliers.add(supplier) ;
	}
	
	void setScene( int index )
	{
		currentScene = scenesSuppliers.get(index).get();
	}
	
	void addChangeListenerTo(int index1 , ChangeListener el )
	{
		currentScene.getProperty(index1).addListener(el);
	}
}
