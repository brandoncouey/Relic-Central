package com.shattered.threads;

import com.shattered.ServerConstants;
import com.shattered.connections.ServerConnections;
import com.shattered.connections.ShatteredServer;

/**
 * @author JTlr Frost 8/29/2019 : 4:35 AM
 */
public class ServerResponding extends Thread {

    @Override
    public final void run() {
        while (true) {
            try {
                
                synchronized (ServerConnections.getServices()) {
                    for (int index = 0; index < ServerConnections.getServices().size(); index++) {
                        ShatteredServer server = ServerConnections.getServices().get(index);
                        if (server == null) continue;

                        if (server.getSession().getLastPingReceived() < System.currentTimeMillis() - (1000 * ServerConstants.SERVER_TIMEOUT)) {
                            ServerConnections.unregisterServer(server.getCuuid());
                            
                            if (server.getSession() != null && server.getSession().getChannel().isActive()) {
                                server.getSession().disconnect();
                            }
                        }
                    }
                }
                
                Thread.sleep(1000 * 10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
}
