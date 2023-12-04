package main;

import java.util.function.Supplier;

import gui.* ;
import supplement.Supplement;

public class Main
{

	public static void main( String[] args )
	{
		MainWindow mw = new MainWindow( args , new Supplier[] {LoginWindow::createInstance , Dashboard::createInstance} ) ;
		
		//MainWindow mw = new MainWindow( args ) ;
	}

}
