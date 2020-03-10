package com.shattered.connections;

import com.shattered.networking.session.Session;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.net.InetSocketAddress;

/**
 * @author JTlr Frost <brradc@gmail.com> 6/13/2019
 */
@RequiredArgsConstructor
public class ShatteredServer {

    /**
     * Represents the Connection UUID
     */
    @Getter
    private final String cuuid;

    /**
     * Represents the Shard Type
     */
    @Getter
    private final ServerType serverType;

    /**
     * Represents the Current Session
     */
    @Getter
    private final Session session;

    /**
     * Represents the Server Socket Address
     */
    @Getter
    @Setter
    private InetSocketAddress address;
}
