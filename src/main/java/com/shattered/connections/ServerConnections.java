package com.shattered.connections;

import com.shattered.networking.NetworkBootstrap;
import com.shattered.networking.proto.PacketOuterClass;
import com.shattered.networking.proto.Sharding;
import com.shattered.networking.session.Session;
import com.shattered.system.SystemLogger;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author JTlr Frost <brradc@gmail.com> 6/13/2019
 */
public class ServerConnections {

    /**
     * Represents the Services
     */
    @Getter
    private static List<ShatteredServer> services = new CopyOnWriteArrayList<>();


    /**
     * Represents the World List Entries
     * This world list is seperate from Realm.getWorldList() -> Realm.module
     * This world list is used for world->central of updating their current status
     * Realm.getWorldList() -> Realm.module is used for requesting and filling information
     *      to send to the client.
     */
    @Getter
    private static Map<String, WorldListEntry> worldListEntries = new ConcurrentHashMap<>();


    /**
     * TODO ->
     *
     * Make it so every time a list entry is modified, removed, added
     * notify each and every single realm server with the updated lists.
     */

    /**
     *
     * @param connectionUid
     * @param type
     * @param session
     */
    public static void registerServer(String connectionUid, ServerType type, Session session) {
        synchronized (getServices()) {
            getServices().add(new ShatteredServer(connectionUid, type, session));
            SystemLogger.sendSystemMessage("ServerConnections -> Server Registered. Type=" + type.name() + ", ConnectionId=" + connectionUid);
        }
    }

    /**
     *
     * @param connectionUid
     */
    public static void unregisterServer(String connectionUid) {
        synchronized (getServices()) {
            ShatteredServer server = forUUID(connectionUid);
            if (server == null) return;

            //Checks if a World; If so it will unregister the entry.
            if (server.getServerType() == ServerType.WORLD)
                unregisterWorld(connectionUid);

            services.remove(server);
            SystemLogger.sendSystemErrMessage("ServerConnections -> Server Unregistered. Type=" + server.getServerType().name() + ", ConnectionId=" + connectionUid);
        }
    }

    /**
     * Fetches a Entry 
     * @param connectionUid
     * @return
     */
    public static WorldListEntry forEntryByUUID(String connectionUid) {
        for (WorldListEntry entry : getWorldListEntries().values()) {
            if (entry == null) continue;
            if (entry.getConnectionUuid().equals(connectionUid))
                return entry;
        }
        return null;
    }

    /**
     *
     * @param entry
     */
    public static void registerWorld(WorldListEntry entry) {
        synchronized (getWorldListEntries()) {

            if (!getWorldListEntries().containsKey(entry.getConnectionUuid()))
                SystemLogger.sendSystemMessage("ServerConnections -> World Registered {cuuid=" + entry.getConnectionUuid() + ", name=" + entry.getName() + "}");

            getWorldListEntries().put(entry.getConnectionUuid(), entry);

            //Sends Entry to the Realm
            for (ShatteredServer server : getServices()) {
                if (server == null) return;
                if (server.getServerType() != ServerType.REALM) continue;
                NetworkBootstrap.sendPacket(server.getSession().getChannel(), PacketOuterClass.Opcode.S_UpdateWorldEntry, Sharding.UpdateWorldEntry.newBuilder().setEntry(
                        Sharding.UpdateWorldList.Entry.newBuilder().setConnUuid(entry.getConnectionUuid()).setIndex(entry.getId()).setHost(entry.getSocket().getHostName())
                        .setPort(entry.getSocket().getPort()).setName(entry.getName()).setLocation(entry.getLocation()).setType(entry.getType()).setPopulation(entry.getPopulation()).build()
                ).build());
                SystemLogger.sendSystemMessage("S_UpdateWorldEntry -> Updating Entry for " + entry.getName() + " for Realm=" + server.getCuuid());
            }


        }
    }

    /**
     *
     * @param connectionUid
     */
    public static void unregisterWorld(String connectionUid) {
        synchronized (getWorldListEntries()) {
            for (WorldListEntry entry : getWorldListEntries().values()) {
                if (entry == null) continue;
                if (entry.getConnectionUuid().equals(connectionUid)) {
                    //Sends Entry to the Realm
                    for (ShatteredServer server : getServices()) {
                        if (server == null) return;
                        if (server.getServerType() != ServerType.REALM) continue;
                        NetworkBootstrap.sendPacket(server.getSession().getChannel(), PacketOuterClass.Opcode.S_UpdateWorldEntry, Sharding.UpdateWorldEntry.newBuilder().setEntry(
                                Sharding.UpdateWorldList.Entry.newBuilder().setConnUuid(entry.getConnectionUuid()).setIndex(-1).setName("Unavailable").build()
                        ).build());
                        SystemLogger.sendSystemMessage("S_UpdateWorldEntry -> Updating Entry for " + entry.getName() + " for Realm=" + server.getCuuid());
                    }
                    getWorldListEntries().remove(connectionUid);
                    SystemLogger.sendSystemMessage("ServerConnections -> World Unregistered {cuuid=" + connectionUid + ", name=" + entry.getName() + "}");
                }
            }
        }
    }

    /**
     * Finds a Server Service by their Connection UUID
     * @param uuid
     * @return
     */
    public static ShatteredServer forUUID(String uuid) {
        for(ShatteredServer service : getServices()) {
            if (service == null) continue;
            if (service.getCuuid().equals(uuid))
                return service;
        }
        return null;
    }

    /**
     * Returns a list of all connections for their type
     * @param type
     * @return
     */
    public static List<ShatteredServer> getServersForType(ServerType type) {
        List<ShatteredServer> servers = null;
        for (ShatteredServer service : getServices()) {
            if (service == null) continue;
            if (service.getServerType().equals(type)) {
                if (servers == null)
                    servers = new ArrayList<>();
                servers.add(service);
            }
        }
        return servers;
    }

    /**
     * Gets a Type of Service
     * @param type
     * @return
     */
    public static ShatteredServer getServerForType(ServerType type) {
        for (ShatteredServer service : getServices()) {
            if (service == null || service.getServerType() == null)  continue;
            if (service.getServerType().equals(type)) return service;
        }
        return null;
    }
}
