package com.shattered.threads;

import com.shattered.ServerConstants;
import com.shattered.service.ServiceConnections;
import com.shattered.service.ServerService;

/**
 * @author JTlr Frost 8/29/2019 : 4:35 AM
 *
 * Loops every 10 seconds and checks if their last ping received doesnt exceed > {@link ServerConstants#SERVER_TIMEOUT}
 */
public class ServerTimeoutListener extends Thread {

    @Override
    public final void run() {
        for (;;) {
            try {
                
                synchronized (ServiceConnections.getServices()) {
                    for (int index = 0; index < ServiceConnections.getServices().size(); index++) {
                        ServerService server = ServiceConnections.getServices().get(index);
                        if (server == null) continue;

                        if (server.getSession().getLastPingReceived() < System.currentTimeMillis() - (1000 * ServerConstants.SERVER_TIMEOUT)) {
                            ServiceConnections.unregister(server.getCuuid());
                            
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
