/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyanogenmod.settings.device;

import android.content.Context;
import android.os.UEventObserver;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
	private int	mState = 0;	
	
	private static final int	DOCK_DESK 		= 1 << 0;
	private static final int	DOCK_KEYBD 		= 1 << 1;
	private static final int	ACC_CARMOUNT 	= 1 << 2;
	private static final int	ACC_TVOUT 		= 1 << 3;
	private static final int	ACC_LINEOUT 	= 1 << 4;
	private static final int	HDMI_CONNECTED 	= 1 << 5;
	
	public C30Observer()
	{
		FileReader	curState;
		
		try
		{
			// Read current driver state of 30-pin connector
			// The state bits describe current status:
			// 0 - DOCK_DESK connected
			// 1 - DOCK_KEYBD connected
			// 2 - ACC_CARMOUNT connected
			// 3 - ACC_TVOUT connected
			// 4 - ACC_LINEOUT connected
			// 5 - HDMI cable is connected ?
			
			curState = new FileReader(stateFile);
			
			char[]	rawData = new char[128];
			
			if (curState.read(rawData, 0, 128) > 0)
			{
				String	state = new String(rawData);
				Integer	val = new Integer(state.trim());
			
				if (val != null)
					mState = val.intValue();
					
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
        		listener.onStateChange(accessory, state);
        }
    
        if (dock != null && "desk".equals(dock))
        {
        	String	state = event.get("STATE");
        
        	Log.v("SGT7", "Dock connection state: " + state);
        	
        	if (listener != null)
        		listener.onStateChange(dock, state);
        }
    }
    
    /**
     * Returns current state of C30 connector
     * @return true if connected
     */
    public boolean getConnectionState() {
		return (mState > 0);
	}
    
    public boolean isTVoutConnected() {
    	return (mState & ACC_TVOUT) != 0;
    }
    
    public boolean isDockDeskConnected() {
    	return (mState & DOCK_DESK) != 0;
    }
}
