package net.kunmc.lab.lifestyle;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class LifeStyle extends JavaPlugin{
    private World world;
    private Long time = 0L;
    private Long add = 2L;

    @Override
    public void onEnable() {
        // Plugin startup logic
        PlayerEvent playerEvent = new PlayerEvent(this);
        BukkitScheduler scheduler = getServer().getScheduler();
        getServer().getOnlinePlayers().forEach(player -> {
            if(world == null) {
                world = player.getWorld();
            }
            playerEvent.getIsSleep(player);
            playerEvent.getMessage(player);
            playerEvent.getIsInvalid(player);
        });
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                if (world != null) {
                    world.setTime(14000L);
                }
                Scoreboard sb = getServer().getScoreboardManager().getMainScoreboard();
                Objective sleep = sb.getObjective("sleep");
                Objective awake = sb.getObjective("awake");
                getServer().getOnlinePlayers().forEach(player -> {
                    if(world == null) {
                        world = player.getWorld();
                        world.setTime(14000L);
                    }
                    playerEvent.getIsSleep(player);
                    playerEvent.getMessage(player);
                    playerEvent.getIsInvalid(player);
                    List<Integer> nowTime = castTime(getTime());
                    player.setPlayerTime(getTime(), false);
                    int sleepTime = 22;
                    int awakeTime = 5;
                    if(sleep != null && awake != null) {
                        sleepTime = sleep.getScore(player.getName()).getScore();
                        awakeTime = awake.getScore(player.getName()).getScore();
                        if((sleepTime == awakeTime) || 0 > sleepTime || sleepTime > 23  || 0 > awakeTime || awakeTime > 23) {
                            sleepTime = 22;
                            awakeTime = 5;
                        }
                    }
                    setTime();
                    if(player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
                        setActionBar(nowTime, "サバイバル・アドベンチャーモードでのみ有効", player);
                        return;
                    }
                    if(playerEvent.isNightVision(player)) {
                        playerEvent.setIsInvalid(player, '2');
                    } else if(playerEvent.getIsInvalid(player) != '1') {
                        playerEvent.setIsInvalid(player, '0');
                    }
                    if((playerEvent.getIsInvalid(player) == '1'  && isSleepTime(nowTime.get(0), sleepTime, awakeTime)) || playerEvent.getIsInvalid(player) == '2') {
                        playerEvent.setAwake(player);
                        setActionBar(nowTime, playerEvent.getMessage(player) + "§6★無敵", player);
                        return;
                    }
                    if(isSleepTime(nowTime.get(0), sleepTime, awakeTime)) {
                        playerEvent.setSleep(player);
                        setActionBar(nowTime, playerEvent.getMessage(player), player);
                        return;
                    }
                    if(nowTime.get(1) >= 30) {
                        if(nowTime.get(0) == 23  && sleepTime == 0) {
                            playerEvent.setDizzy(player);
                            setActionBar(nowTime, playerEvent.getMessage(player), player);
                            return;
                        }
                        if(nowTime.get(0)  == sleepTime - 1) {
                            playerEvent.setDizzy(player);
                            setActionBar(nowTime, playerEvent.getMessage(player), player);
                            return;
                        }
                    }
                    playerEvent.setAwake(player);
                    playerEvent.setIsInvalid(player, '0');
                    setActionBar(nowTime, playerEvent.getMessage(player), player);
                });

            }
        }, 0L, 2L);
    }

    public List<Integer> castTime(Long time) {
        int hours = 0;
        int minutes = 0;
        while (true) {
            if(time >= 1000) {
                time -= 1000;
                hours += 1;
                continue;
            }
            if(time >= 17) {
                time -= 17;
                minutes += 1;
                continue;
            }
            break;
        }
        hours = hours >= 18 ? hours - 18 : hours + 6;
        List<Integer> times = new ArrayList<Integer>();
        times.add(hours);
        times.add(minutes);
        return times;
    }

    public boolean isSleepTime(int now, int start, int end) {
        if(start > end && (now >= start || end > now)) {
            return true;
        }
        if(end > start && (now >= start && end > now)) {
            return true;
        }
        return false;
    }

    public void setActionBar(List<Integer> nowTime, String message, Player player) {
        TextComponent component = new TextComponent();
        component.setText(nowTime.get(0) + "時" + nowTime.get(1) + "分" + " <" + message +"§f>");
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
    }

    public void setTime() {
        if(time >= 24000L) {
            time = 0L;
            return;
        }
        time += add;
    }

    public void setTimeAbout(Long timeAbout) {
        time = timeAbout;
    }

    public Long getTime() {
        return time;
    }

    public void setSpeed(Long speed) {
        if(speed > 100L) {
            speed = 100L;
        }
        add = 2L * speed;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("speed-up")) {
            if (args.length != 1) {
                return false;
            }
            try {
                setSpeed(Long.parseLong(args[0]));
            } catch (NumberFormatException e) {
                return false;
            }
            sender.sendMessage("経過速度を" + args[0] + "倍に設定しました");
            return true;
        }
        if(cmd.getName().equalsIgnoreCase("time")) {
            if(args.length < 2) {
                return false;
            }
            if(args[0].equalsIgnoreCase("set")) {
                if(args[1].equalsIgnoreCase("day")) {
                    setTimeAbout(1000L);
                    return true;
                }
                if(args[1].equalsIgnoreCase("sunset")) {
                    setTimeAbout(12000L);
                    return true;
                }
                if(args[1].equalsIgnoreCase("night")) {
                    setTimeAbout(13000L);
                    return true;
                }
                if(args[1].equalsIgnoreCase("sunrise")) {
                    setTimeAbout(23000L);
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("speed-up")) {
            if(args.length == 1) {
                return Collections.singletonList("数値");
            }
        }
        if(cmd.getName().equalsIgnoreCase("time")) {
            if(args.length == 1) {
                return Collections.singletonList("set");
            }
            if(args.length == 2 && args[0].equalsIgnoreCase("set")) {
                if(args[1].length() == 0) {
                    return Arrays.asList("day", "sunset", "night", "sunrise");
                } else {
                    if("day".startsWith(args[1])) {
                        return Collections.singletonList("day");
                    }
                    if("night".startsWith(args[1])) {
                        return Collections.singletonList("night");
                    }
                    if(args[1].length() == 4) {
                        if("sunset".startsWith(args[1])) {
                            return Collections.singletonList("sunset");
                        }
                        if("sunrise".startsWith(args[1])) {
                            return Collections.singletonList("sunrise");
                        }
                    }
                }
            }
        }
        return super.onTabComplete(sender, cmd, label, args);
    }

}
