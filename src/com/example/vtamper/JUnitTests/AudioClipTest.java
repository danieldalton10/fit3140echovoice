package com.example.vtamper.JUnitTests;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import android.test.AndroidTestCase;
import junit.framework.TestCase;
import com.example.vtamper.*;
import com.example.vtamper.AudioClip.EffectArguments;

/*
 * An AudioClipTest to test the method in AudioClip
 */

public class AudioClipTest extends AndroidTestCase {

	@Test
	/*
	 * To test the constructor method of audioClip();
	 */
	public void testConstructor() {
		AudioClip audioClip = new AudioClip();
		assertNotNull(audioClip);
	}
	
	public void testEffectArgumentsGetVolume() {
		
		AudioClip audioClip = new AudioClip();
		AudioClip.EffectArguments args = audioClip.new EffectArguments();
		float expectedOutput = 1;
		float actualOutput = args.getVolume();		
		assertEquals (expectedOutput, actualOutput);
	
	}
	
	public void testEffectArgumentsGetStart() {
		
		AudioClip audioClip = new AudioClip();
		AudioClip.EffectArguments args = audioClip.new EffectArguments();
		float expectedOutput = 0;
		float actualOutput = args.getStart();		
		assertEquals (expectedOutput, actualOutput);
	
	}
	
	public void testEffectArgumentsGetRepeat() {
		
		AudioClip audioClip = new AudioClip();
		AudioClip.EffectArguments args = audioClip.new EffectArguments();
		float expectedOutput = 1;
		float actualOutput = args.getRepeats();		
		assertEquals (expectedOutput, actualOutput);
	
	}
	
	public void testEffectArgumentsSetVolume() {
		
		AudioClip audioClip = new AudioClip();
		AudioClip.EffectArguments args = audioClip.new EffectArguments();
		args.setVolume(30);
		float expectedOutput = 30;
		float actualOutput = args.getVolume();	
		assertEquals (expectedOutput, actualOutput);
	
	}
	
	public void testEffectArgumentsSetRepeat() {
		
		AudioClip audioClip = new AudioClip();
		AudioClip.EffectArguments args = audioClip.new EffectArguments();
		args.setRepeats(2);
		float expectedOutput = 2;
		float actualOutput = args.getRepeats();	
		assertEquals (expectedOutput, actualOutput);
	
	}
	
	public void testLoadFile() {
		
		
		
		
	}
	
	public void testGetDuration() {
		AudioClip audioClip = new AudioClip();
	
		
	}
	
	public void testSelectEffect() {
		
		
	}
	
}