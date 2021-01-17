package net.kunmc.lab.lifestyle;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class PlayerEvent implements Listener {
    private List<Player> players;
    private Map<String, Boolean> isSleeps;
    private Map<String, String> messages;
    private Long time;
    private World world;

    private static Long add = 2L;

    public PlayerEvent(JavaPlugin plugin) {
        players = new ArrayList<Player>();
        isSleeps = new HashMap<String, Boolean>();
        messages = new HashMap<String, String>();
        time = 0L;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        players.add(player);
        isSleeps.put(player.getName(), false);
        messages.put(player.getName(), "眠くない");
        setAwake(player);
        if(players.size() == 1) {
            world = player.getWorld();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        players.remove(event.getPlayer());
        isSleeps.remove(event.getPlayer().getName());
        messages.remove(event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if(!(entity.getType() == EntityType.PLAYER)) {
            return;
        }
        Player player = (Player) entity;
        Boolean isSleep = isSleeps.get(player.getName());
        if(isSleep) {
            player.setHealth(0);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        setAwake(event.getPlayer());
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Boolean isSleep = isSleeps.get(player.getName());
        if(isSleep != null && isSleep) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Boolean isSleep = isSleeps.get(player.getName());
        if(isSleep != null && isSleep) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if(isDizzyOrBlindness(event.getPlayer())) {
            event.setUseBed(Event.Result.ALLOW);
            return;
        }
        event.getPlayer().sendMessage("まだ眠くない");
        event.setCancelled(true);
    }


    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        if(isDizzyOrBlindness(event.getPlayer())) {
            event.getPlayer().sleep(event.getBed().getLocation(), true);
            return;
        }
        setAwake(event.getPlayer());
    }

    @EventHandler
    public void onWorldTimeSkip(TimeSkipEvent event) {
        if(event.getSkipReason() == TimeSkipEvent.SkipReason.NIGHT_SKIP) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onServerLoader(ServerLoadEvent event) {
        Bukkit.getServer().getOnlinePlayers().forEach(player -> {
            if(players.contains(player.getPlayer())) {
                return;
            }
            players.add(player);
            isSleeps.put(player.getName(), false);
            messages.put(player.getName(), "眠くない");
        });
    }


    public void setSleep(Player player) {
        isSleeps.put(player.getName(), true);
        messages.put(player.getName(), "Zzz");
        player.removePotionEffect(PotionEffectType.CONFUSION);
        setBlindness(player);
        if(!player.isSleeping()) {
            player.getLocation().getBlock().setType(Material.BLACK_BED);
            player.sleep(player.getLocation(), true);
        }
    }

    public void setDizzy(Player player) {
        Boolean isSleep = isSleeps.get(player.getName());
        if(!(player.getPotionEffect(PotionEffectType.CONFUSION) == null)) {
            return;
        }
        if(isSleep != null && isSleep) {
            return;
        }
        messages.put(player.getName(), "§1眠たい");
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION ,1200,0,false));
        //player.setPlayerTime(0,false);
    }

    public void  setBlindness(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS ,24000,2,true));
    }

    public void setAwake(Player player) {
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.CONFUSION);
        isSleeps.put(player.getName(), false);
        messages.put(player.getName(), "眠くない");
    }

    public  List<Player> getPlayers() {
        return players;
    }

    public String getMessage(String name) {
        return messages.get(name) != null ? messages.get(name) : "眠くない";
    }

    public boolean isDizzyOrBlindness(Player player) {
        return !(player.getPotionEffect(PotionEffectType.CONFUSION) == null) || !(player.getPotionEffect(PotionEffectType.BLINDNESS) == null);
    }

    public long getTime() {
        return time;
    }

    public void setTime() {
        if(this.time == 24000L) {
            this.time = 0L;
            return;
        }
        this.time += add;
    }

    public World getWorld() {
        return world;
    }

    public static void setSpeed(Long speed) {
        add = speed;
    }
}