package com.github.tomodq.lifestyle;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class PlayerEvent implements Listener {
    private List<Player> players;
    private Map<String, Boolean> isSleeps;

    public PlayerEvent(JavaPlugin plugin) {
        players = new ArrayList<Player>();
        isSleeps = new HashMap<String, Boolean>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        players.add(player);
        setAwake(player);
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective sleepTimeObjective = sb.getObjective(player.getName());
        if(sleepTimeObjective == null) {
            sb.registerNewObjective(player.getName(), "dummy", "19,30,5,0");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        players.remove(event.getPlayer());
        isSleeps.remove(event.getPlayer().getName());
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
            setAwake(player);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Boolean isSleep = isSleeps.get(player.getName());
        if(isSleep) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Boolean isSleep = isSleeps.get(player.getName());
        if(isSleep) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if(!(event.getPlayer().getPotionEffect(PotionEffectType.CONFUSION) == null) || !(event.getPlayer().getPotionEffect(PotionEffectType.BLINDNESS) == null)) {
            event.setUseBed(Event.Result.ALLOW);
            return;
        }
        event.getPlayer().sendMessage("まだ眠くない");
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        if(!(event.getPlayer().getPotionEffect(PotionEffectType.CONFUSION) == null) || !(event.getPlayer().getPotionEffect(PotionEffectType.BLINDNESS) == null)) {
            event.getPlayer().sleep(event.getBed().getLocation(),true);
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


    public void setSleep(Player player) {
        isSleeps.put(player.getName(), true);
        player.removePotionEffect(PotionEffectType.CONFUSION);
        setBlindness(player);
    }

    public void setDizzy(Player player) {
        Boolean isSleep = isSleeps.get(player.getName());
        if(!(player.getPotionEffect(PotionEffectType.CONFUSION) == null)) {
            return;
        }
        if(isSleep) {
            return;
        }
        player.sendMessage("§1眠たい");
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION ,1200,0,false));
    }

    public void  setBlindness(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS ,24000,2,true));
    }

    public void setAwake(Player player) {
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.CONFUSION);
        isSleeps.put(player.getName(), false);
    }

    public  List<Player> getPlayers() {
        return players;
    }

    public List<Integer> getSleepAndAwakeTime(String playerName) {
        String objectiveDisplayName;
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        objectiveDisplayName = sb.getObjective(playerName).getDisplayName();
        List<Integer> sleepAndAwakeTime = new ArrayList<Integer>();
        Arrays.stream(objectiveDisplayName.split(",")).mapToInt(Integer::parseInt).forEach(value -> {
            sleepAndAwakeTime.add(value);
        });
        return sleepAndAwakeTime;
    }
}