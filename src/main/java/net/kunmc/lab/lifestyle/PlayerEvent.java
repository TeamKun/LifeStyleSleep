package net.kunmc.lab.lifestyle;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
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

import java.util.*;

public class PlayerEvent implements Listener {
    private HashMap<String,Boolean> isSleeps;
    private HashMap<String, String> messages;
    private HashMap<String, Character> isInvalids;

    public PlayerEvent(JavaPlugin plugin) {
        isSleeps = new HashMap<String, Boolean>();
        messages = new HashMap<String, String>();
        isInvalids = new HashMap<String, Character>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageEvent event) {
        EntityType entityType = event.getEntity().getType();
        if(entityType != EntityType.PLAYER) {
            return;
        }
        Player player = (Player) event.getEntity();
        char isInvalid = getIsInvalid(player);
        if(isInvalid == '1' || isInvalid == '2') {
            event.setCancelled(true);
            return;
        }
        Boolean isSleep = getIsSleep(player);
        if(isSleep) {
            setIsInvalid(player, '1');
            player.setHealth(0);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        setAwake(player);
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Boolean isSleep = getIsSleep(player);
        if(isSleep) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Boolean isSleep = getIsSleep(player);
        if(isSleep) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        if(isDizzy(player) || getIsSleep(player)) {
            event.setUseBed(Event.Result.ALLOW);
            return;
        }
        event.getPlayer().sendMessage("まだ眠くない");
        event.setCancelled(true);
    }


    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        try {
            Player player = event.getPlayer();
            if (getIsSleep(player)) {
                player.sleep(player.getBedLocation(), true);
                event.setSpawnLocation(false);
                return;
            }
            event.setSpawnLocation(false);
            setAwake(player);
        } catch (Exception e) {
            return;
        }
    }

    @EventHandler
    public void onWorldTimeSkip(TimeSkipEvent event) {
        if(event.getSkipReason() == TimeSkipEvent.SkipReason.NIGHT_SKIP) {
            event.setCancelled(true);
        }
    }

    public void setSleep(Player player) {
        setIsSleep(player, true);
        setMessage(player, "§3Zzz");
        player.removePotionEffect(PotionEffectType.CONFUSION);
        deleteBed(player.getLocation(), 10);
        player.getLocation().getBlock().setType(Material.BLACK_BED);
        player.sleep(player.getLocation(), true);
    }

    public void setDizzy(Player player) {
        Boolean isSleep = getIsSleep(player);
        if(isDizzy(player)) {
            return;
        }
        if(isSleep) {
            return;
        }
        setMessage(player, "§3眠たい");
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION ,1200,0,false));
    }

    public void setAwake(Player player) {
        player.removePotionEffect(PotionEffectType.CONFUSION);
        setIsSleep(player, false);
        setMessage(player,"§a眠くない");
        deleteBed(player.getLocation(), 10);
    }


    public void setIsSleep(Player player, boolean isSleep) {
        isSleeps.put(player.getName(), isSleep);
    }

    public boolean getIsSleep(Player player) {
        if(isSleeps.containsKey(player.getName())) {
            return  isSleeps.get(player.getName());
        }
        setIsSleep(player,false);
        return false;
    }

    public String getMessage(Player player) {
       if(messages.containsKey(player.getName())) {
           return messages.get(player.getName());
       }
       setMessage(player, "§a眠くない");
       return "§a眠くない";
    }
    public void setMessage(Player player, String message) {
        messages.put(player.getName(), message);
    }

    public void setIsInvalid(Player player, char isInvalid) {
        isInvalids.put(player.getName(), isInvalid);
    }

    public char getIsInvalid(Player player) {
        if(isInvalids.containsKey(player.getName())) {
            return isInvalids.get(player.getName());
        }
        setIsInvalid(player, '0');
        return '0';
    }

    public boolean isDizzy(Player player) {
        return !(player.getPotionEffect(PotionEffectType.CONFUSION) == null);
    }

    public boolean isNightVision(Player player) {
        return !(player.getPotionEffect(PotionEffectType.NIGHT_VISION) == null);
    }

    public void deleteBed(Location loc, int length) {
        int x1 = loc.getBlockX();
        int y1 = loc.getBlockY();
        int z1 = loc.getBlockZ();

        int x2 = x1 + length;
        int y2 = y1 + length;
        int z2 = z1 + length;

        World world = loc.getWorld();
        for (int xPoint = x1; xPoint <= x2; xPoint++) {
            for (int yPoint = y1; yPoint <= y2; yPoint++) {
                for (int zPoint = z1; zPoint <= z2; zPoint++) {
                    assert world != null;
                    Block block = world.getBlockAt(x1, y1, z1);
                    deleteBedExe(block);
                    block = world.getBlockAt(x1 - length, y1, z1);
                    deleteBedExe(block);
                    block = world.getBlockAt(x1 - length, y1 - length, z1);
                    deleteBedExe(block);
                    block = world.getBlockAt(x1 - length, y1, z1 - length);
                    deleteBedExe(block);
                    block = world.getBlockAt(x1, y1 - length, z1);
                    deleteBedExe(block);
                    block = world.getBlockAt(x1, y1 - length, z1 - length);
                    deleteBedExe(block);
                    block = world.getBlockAt(x1, y1, z1 - length);
                    deleteBedExe(block);
                    block = world.getBlockAt(x1 - length, y1 - length, z1 - length);
                    deleteBedExe(block);
                }
            }
        }
    }

    public void deleteBedExe(Block block) {
        if(block.getType() == Material.BLACK_BED) {
            block.setType(Material.AIR);
        }
    }

}
