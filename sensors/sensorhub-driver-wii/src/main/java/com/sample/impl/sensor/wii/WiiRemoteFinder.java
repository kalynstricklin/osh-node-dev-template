package com.sample.impl.sensor.wii;

import motej.Mote;
import motej.MoteFinder;
import motej.MoteFinderListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WiiRemoteFinder implements MoteFinderListener {
    private static final Logger logger = LoggerFactory.getLogger(WiiRemoteFinder.class);
    MoteFinder finder;
    Mote mote;
    Object lock = new Object();
    @Override
    public void moteFound(Mote mote) {
        logger.info("Remote found");
        this.mote = mote;
        finder.removeMoteFinderListener(this);
        synchronized (lock) {
            lock.notifyAll();
        }
    }
    public Mote findRemote(){
        if (finder == null) {
            logger.debug("finding wii remote");
            finder = MoteFinder.getMoteFinder();
            finder.addMoteFinderListener(this);
        }
        logger.debug("starting discovery");
        finder.startDiscovery();
        try{
            synchronized (lock){
                lock.wait();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return mote;
    }
}
