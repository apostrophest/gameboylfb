package com.github.reisnera.gameboylfb;

import org.testng.annotations.*;

public class HelloWorldTest {
	
	@Test
	public void doTest() {
		HelloWorld world = new HelloWorld();
		assert world.getClass().getName().equals("com.github.reisnera.gameboylfb.HelloWorld");
	}
	
}
