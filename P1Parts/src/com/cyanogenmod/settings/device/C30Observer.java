package com.cyanogenmod.settings.device;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.content.Context;
import android.os.UEventObserver;
import android.util.Log;

/**
 * UEVENT: {SUBSYSTEM=platform, STATE=online, DEVPATH=/devices/platform/acc_con, SEQNUM=1508, ACTION=change, DRIVER=acc_con, MODALIAS=platform:acc_con, ACCESSORY=TV} 
 * UEVENT: {SUBSYSTEM=platform, STATE=offline, DEVPATH=/devices/platform/acc_con, SEQNUM=1509, ACTION=change, DRIVER=acc_con, MODALIAS=platform:acc_con, ACCESSORY=TV}
 * UEVENT: {SUBSYSTEM=platform, STATE=online, DEVPATH=/devices/platform/acc_con, DOCK=desk, SEQNUM=1536, ACTION=change, DRIVER=acc_con, MODALIAS=platform:acc_con}
 *
 */

public class C30Observer extends UEventObserver {
	
	private static final String	uEventInfo = "DEVPATH=/devices/platform/acc_con";
	private static final String	stateFile = "/sys/devices/platform/acc_con/acc_file";
	private C30StateListener	listener;
	private boolean	mState = false;	
	
	public C30Observer()
	{
		FileReader	curState;
		
		try
		{
			// Read current driver state of TVOUT
			curState = new FileReader(stateFile);
			
			char[]	rawData = new char[128];
			
			if (curState.read(rawData, 0, 128) > 0)
			{
				String	state = new String(rawData);
				Integer	val = new Integer(state.trim());
			
				if (val != null)
					mState = val.intValue() > 0 ? true : false;
					
				Log.v("SGT7", "C30 initial state: " + mState);
			}
			
			curState.close();
		}
		catch(FileNotFoundException e) { }
		catch (IOException e) { }
	}
	
	/**
	 * Starts observing for kernel user events
	 */
	public void start()
	{
		//this.startObserving("");
		this.startObserving(uEventInfo);
		
		Log.v("SGT7", "C30 UEVENT Observer started");
	}
	
	public void setOnStateChangeListener(C30StateListener l)
	{
		listener = l;
	}
	
	/**
	 * Handles user event from kernel
	 * 
	 * @param event kernel event
	 */
    @Override
    public void onUEvent(UEventObserver.UEvent event) {
        Log.v("SGT7", "C30 UEVENT: " + event.toString());
        
        String accessory = event.get("ACCESSORY");
        String dock = event.get("DOCK");
        
        if (accessory != null && "TV".equals(accessory))
        {
        	String	state = event.get("STATE");

        	Log.v("SGT7", "TVout 30-pin connection state: " + state);
        	
        	if (listener != null)
        		listener.onStateChange(state);
        }
    
        if (dock != null && "desk".equals(dock))
        {
        	String	state = event.get("STATE");
        
        	Log.v("SGT7", "Dock connection state: " + state);
        	
        	if (listener != null)
        		listener.onStateChange(state);
        }
    }
    
    /**
     * Returns current state of C30 connector
     * @return true if connected
     */
    public boolean getConnectionState() {
		return mState;
	}
}
