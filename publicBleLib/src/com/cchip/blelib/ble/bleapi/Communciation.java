package com.cchip.blelib.ble.bleapi;

public interface Communciation<T> {
		  
    public T getCommunicationChannel(String addr);

    
    public void commucateInitAall();
    
    public void commucateInit(String addr);
    
    public boolean getCommunication(String addr);
    
    public boolean isCommunicte(String macAddr);
}
