package com.gmail.necnionch.myplugin.athletime.bukkit.commands;

import com.gmail.necnionch.myplugin.athletime.bukkit.AthleTimePlugin;
import com.gmail.necnionch.myplugin.athletime.bukkit.command.Command;
import com.gmail.necnionch.myplugin.athletime.bukkit.command.CommandSender;
import com.gmail.necnionch.myplugin.athletime.bukkit.command.RootCommand;
import com.gmail.necnionch.myplugin.athletime.bukkit.command.errors.CommandError;
import com.gmail.necnionch.myplugin.athletime.bukkit.command.errors.NotFoundCommandError;
import com.gmail.necnionch.myplugin.athletime.bukkit.command.errors.PermissionCommandError;
import com.gmail.necnionch.myplugin.athletime.bukkit.hooks.CitizensNPC;
import com.gmail.necnionch.myplugin.athletime.bukkit.parkour.Parkour;
import com.gmail.necnionch.myplugin.athletime.bukkit.parkour.ParkourContainer;
import com.gmail.necnionch.myplugin.athletime.bukkit.listeners.SetupHandler;
import com.gmail.necnionch.myplugin.athletime.bukkit.parkour.ParkourPoint;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

import static com.gmail.necnionch.myplugin.athletime.bukkit.AthleTimePlugin.makeMessage;


public class MainCommand extends RootCommand {
    private final RootCommand setupCommands = new RootCommand() {};
    private final ParkourContainer container;
    private final AthleTimePlugin owner;
    private final CitizensNPC citizens;

    public MainCommand(AthleTimePlugin owner, ParkourContainer container) {
        this.owner = owner;
        this.container = container;
        citizens = owner.getCitizens();

        setDefault(addCommand("help", null, this::sendHelp));
        addCommand("list", "athletime.command.list", this::execList);
        addCommand("add", "athletime.command.add", this::execAdd);
        addCommand("remove", "athletime.command.remove", this::execRemove, this::completeParkours);
        addCommand("setup", "athletime.command.setup", this::execSetup, this::genSetup);
        addCommand("refreshnpc", "athletime.command.refreshnpc", this::execRefreshNPC);
        setupCommands.addCommand("startpoint", null, this::execSetupStartPoint);
        setupCommands.addCommand("checkpoint", null, this::execSetupCheckPoint);
        setupCommands.addCommand("endpoint", null, this::execSetupEndPoint);
        setupCommands.addCommand("clearpoints", null, this::execSetupClearPoints);
        setupCommands.addCommand("setranknpc", null, this::execSetupSetRankNPC, this::genSetupSetRankNPC);
        setupCommands.addCommand("clearranknpcs", null, this::execSetupClearRankNPCs);

        /*
        /athletime help
        /athletime list
        /athletime add (parkourName)
        /athletime remove (parkourName)
        /athletime setup (parkourName) startpoint
        /athletime setup (parkourName) checkpoint
        /athletime setup (parkourName) endpoint
        /athletime setup (parkourName) clearpoints
        /athletime setup (parkourName) setnpc
        /athletime setup (parkourName) clearnpcs
         */
    }


    private void sendHelp(CommandSender s, List<String> args) {
        s.sendMessage(makeMessage(ChatColor.RED, "/athleTime <list/refreshNPC>"));
        s.sendMessage(makeMessage(ChatColor.RED, "/athleTime [add/remove/setup] (parkourName)"));
    }

    private void sendSetupHelp(CommandSender s, List<String> args) {
        for (Command command : setupCommands.getCommands()) {
            if (command.getName().equalsIgnoreCase("setranknpc")) {
                s.sendMessage(makeMessage(ChatColor.RED, "/athleTime setup (parkourName) setranknpc (npcId)"));
            } else {
                s.sendMessage(makeMessage(ChatColor.RED, "/athleTime setup (parkourName) " + command.getName()));
            }
        }
    }


    private void execList(CommandSender s, List<String> args) {
        ComponentBuilder builder = new ComponentBuilder("パルクール: ").color(ChatColor.YELLOW);

        Parkour[] parkours = container.getAll();
        for (int i = 0; i < parkours.length; i++) {
            Parkour parkour = parkours[i];
            builder.append(parkour.getName()).color(ChatColor.WHITE);
            if (i+1 < parkours.length)
                builder.append(", ").color(ChatColor.GRAY);
        }

        s.sendMessage(builder.create());
    }

    private void execAdd(CommandSender s, List<String> args) {
        if (args.isEmpty())
            throw new NotSpecifiedParkour();

        String name = args.get(0);
        if (container.get(name) != null) {
            s.sendMessage(makeMessage(ChatColor.RED, "そのパルクール名は存在します。"));
            return;
        }

        container.put(new Parkour(name));
        s.sendMessage(makeMessage(ChatColor.GREEN, "パルクール " + name + " を追加しました。"));
    }

    private void execRemove(CommandSender s, List<String> args) {
        if (args.isEmpty())
            throw new NotSpecifiedParkour();

        Parkour parkour = container.remove(args.get(0));
        if (parkour == null)
            throw new NotFoundParkour();

        owner.getRecords().removeAll(parkour.getName());
        s.sendMessage(makeMessage(ChatColor.GOLD, parkour.getName() + "を削除しました。"));
    }

    private void execRefreshNPC(CommandSender s, List<String> args) {
        if (!owner.getCitizens().isHooked()) {
            s.sendMessage(makeMessage(ChatColor.RED, "CitizensNPCと連携されていません。"));
            return;
        }

        boolean changed = false;
        for (Parkour parkour : container.getAll()) {
            if (owner.getCitizens().updateNPC(parkour))
                changed = true;
        }
        s.sendMessage(makeMessage(ChatColor.GOLD, "すべてのNPCを更新しました。"));

        if (changed)
            container.saveAll();
    }

    @NotNull
    private List<String> completeParkours(CommandSender s, String label, List<String> args) {
        if (args.isEmpty())
            return Collections.emptyList();
        return generateSuggests(args.get(0), Stream.of(container.getAll()).map(Parkour::getName).toArray(String[]::new));
    }


    private void execSetup(CommandSender s, List<String> args) {
        if (!(s.getSender() instanceof Player)) {
            s.sendMessage(makeMessage(ChatColor.RED, "プレイヤーのみ実行できるコマンドです。"));
            return;
        }

        if (args.isEmpty())
            throw new NotSpecifiedParkour();

        String parkour = args.remove(0);
        Command command;
        try {
            command = setupCommands.getCommand(args.remove(0));
        } catch (IndexOutOfBoundsException e) {
            sendSetupHelp(s, args);
            return;
        }
        if (command == null) {
            s.sendMessage(makeMessage(ChatColor.RED, "そのコマンドはありません。"));
            return;
        }

        args.add(0, parkour);
        command.getExecutor().execute(s, args);
    }

    @NotNull
    private List<String> genSetup(CommandSender s, String label, List<String> args) {
        if (args.size() <= 1) {
            return completeParkours(s, "", args);

        } else if (args.size() == 2) {
            return generateSuggests(args.get(1), Stream.of(setupCommands.getCommands())
                    .map(Command::getName)
                    .toArray(String[]::new));

        } {
            args.remove(0);
            Command command = setupCommands.getCommand(args.remove(0));
            return (command != null && command.getCompleter() != null) ? command.getCompleter().tabComplete(s, label, args) : Collections.emptyList();
        }
    }

    private void execSetupStartPoint(CommandSender s, List<String> args) {
        Parkour parkour = container.get(args.remove(0));
        if (parkour == null)
            throw new NotFoundParkour();

        Player player = (Player) s.getSender();
        Map<String, ParkourPoint> points = (parkour.getWorldName() != null) ? parkour.getPoints() : Maps.newHashMap();

        s.sendMessage(makeMessage(ChatColor.GOLD, "感圧版を左クリックで追加し、右クリックで削除。( / で終了)"));

        new SetupHandler(owner, player, (result) -> {
            parkour.startPoints().clear();
            points.forEach((key, value) -> {
                if (ParkourPoint.Type.START.equals(value.getType())) {
                    parkour.startPoints().add(value.getLocation());
                }
            });
            container.put(parkour);
            s.sendMessage(makeMessage(ChatColor.GOLD, "変更を保存しました。"));

        }) {
            @Override
            public void onInteract(PlayerInteractEvent event, Block block) {
                String loc = Parkour.transLocationKey(block.getWorld(), block.getLocation());
                ParkourPoint point = points.get(loc);

                if (Action.LEFT_CLICK_BLOCK.equals(event.getAction())) {
                    // add
                    if (point == null) {
                        parkour.setWorldName(block.getWorld().getName());
                        ParkourPoint newPoint = new ParkourPoint(parkour, ParkourPoint.Type.START, cloneXYZYaw(block.getLocation(), player.getLocation().getYaw()));
                        points.put(loc, newPoint);
                        s.sendMessage(makeMessage(ChatColor.YELLOW, "ポイントを追加しました。"));

                    } else {
                        s.sendMessage(makeMessage(ChatColor.RED, "すでに" + point.getType().name + "として設定されています。"));
                    }

                } else if (Action.RIGHT_CLICK_BLOCK.equals(event.getAction())) {
                    // remove
                    if (point != null) {
                        points.remove(loc, point);
                        s.sendMessage(makeMessage(ChatColor.YELLOW, point.getType().name + "を削除しました。"));

                    } else {
                        s.sendMessage(makeMessage(ChatColor.RED, "ポイントは設定されていません。"));
                    }
                }

            }
        };

    }

    private void execSetupCheckPoint(CommandSender s, List<String> args) {
        Parkour parkour = container.get(args.remove(0));
        if (parkour == null)
            throw new NotFoundParkour();

        Player player = (Player) s.getSender();
        Map<String, ParkourPoint> points = (parkour.getWorldName() != null) ? parkour.getPoints() : Maps.newHashMap();

        s.sendMessage(makeMessage(ChatColor.GOLD, "感圧版を左クリックで追加し、右クリックで削除。( / で終了)"));

        new SetupHandler(owner, player, (result) -> {
            parkour.checkPoints().clear();
            points.forEach((key, value) -> {
                if (ParkourPoint.Type.CHECK.equals(value.getType())) {
                    parkour.checkPoints().add(value.getLocation());
                }
            });
            container.put(parkour);
            s.sendMessage(makeMessage(ChatColor.GOLD, "変更を保存しました。"));

        }) {
            @Override
            public void onInteract(PlayerInteractEvent event, Block block) {
                String loc = Parkour.transLocationKey(block.getWorld(), block.getLocation());
                ParkourPoint point = points.get(loc);

                if (Action.LEFT_CLICK_BLOCK.equals(event.getAction())) {
                    // add
                    if (point == null) {
                        parkour.setWorldName(block.getWorld().getName());
                        ParkourPoint newPoint = new ParkourPoint(parkour, ParkourPoint.Type.CHECK, cloneXYZYaw(block.getLocation(), player.getLocation().getYaw()));
                        points.put(loc,newPoint);
                        s.sendMessage(makeMessage(ChatColor.YELLOW, "ポイントを追加しました。"));

                    } else {
                        s.sendMessage(makeMessage(ChatColor.RED, "すでに" + point.getType().name + "として設定されています。"));
                    }

                } else if (Action.RIGHT_CLICK_BLOCK.equals(event.getAction())) {
                    // remove
                    if (point != null) {
                        points.remove(loc, point);
                        s.sendMessage(makeMessage(ChatColor.YELLOW, point.getType().name + "を削除しました。"));

                    } else {
                        s.sendMessage(makeMessage(ChatColor.RED, "ポイントは設定されていません。"));
                    }
                }

            }
        };
    }

    private void execSetupEndPoint(CommandSender s, List<String> args) {
        Parkour parkour = container.get(args.remove(0));
        if (parkour == null)
            throw new NotFoundParkour();

        Player player = (Player) s.getSender();
        Map<String, ParkourPoint> points = (parkour.getWorldName() != null) ? parkour.getPoints() : Maps.newHashMap();

        s.sendMessage(makeMessage(ChatColor.GOLD, "感圧版を左クリックで追加し、右クリックで削除。( / で終了)"));

        new SetupHandler(owner, player, (result) -> {
            parkour.endPoints().clear();
            points.forEach((key, value) -> {
                if (ParkourPoint.Type.END.equals(value.getType())) {
                    parkour.endPoints().add(value.getLocation());
                }
            });
            container.put(parkour);
            s.sendMessage(makeMessage(ChatColor.GOLD, "変更を保存しました。"));

        }) {
            @Override
            public void onInteract(PlayerInteractEvent event, Block block) {
                String loc = Parkour.transLocationKey(block.getWorld(), block.getLocation());
                ParkourPoint point = points.get(loc);

                if (Action.LEFT_CLICK_BLOCK.equals(event.getAction())) {
                    // add
                    if (point == null) {
                        parkour.setWorldName(block.getWorld().getName());
                        ParkourPoint newPoint = new ParkourPoint(parkour, ParkourPoint.Type.END, cloneXYZYaw(block.getLocation(), player.getLocation().getYaw()));
                        points.put(loc, newPoint);
                        s.sendMessage(makeMessage(ChatColor.YELLOW, "ポイントを追加しました。"));

                    } else {
                        s.sendMessage(makeMessage(ChatColor.RED, "すでに" + point.getType().name + "として設定されています。"));
                    }

                } else if (Action.RIGHT_CLICK_BLOCK.equals(event.getAction())) {
                    // remove
                    if (point != null) {
                        points.remove(loc, point);
                        s.sendMessage(makeMessage(ChatColor.YELLOW, point.getType().name + "を削除しました。"));

                    } else {
                        s.sendMessage(makeMessage(ChatColor.RED, "ポイントは設定されていません。"));
                    }
                }

            }
        };
    }

    private void execSetupClearPoints(CommandSender s, List<String> args) {
        Parkour parkour = container.get(args.remove(0));
        if (parkour == null)
            throw new NotFoundParkour();

        parkour.startPoints().clear();
        parkour.checkPoints().clear();
        parkour.endPoints().clear();

        container.put(parkour);
        s.sendMessage(makeMessage(ChatColor.GOLD, "すべてのポイントを削除しました。"));
    }

    private void execSetupSetRankNPC(CommandSender s, List<String> args) {
        if (!owner.getCitizens().isHooked()) {
            s.sendMessage(makeMessage(ChatColor.RED, "CitizensNPCと連携されていません。"));
            return;
        }

        Parkour parkour = container.get(args.remove(0));
        if (parkour == null)
            throw new NotFoundParkour();

        Player player = (Player) s.getSender();

        NPC npc;

        if (args.isEmpty()) {
            List<NPC> targets = Lists.newArrayList();
            for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
                npc = citizens.getByEntity(entity);
                if (npc != null && npc.getEntity() != null)
                    targets.add(npc);
            }
            targets.sort(Comparator.comparingDouble(target -> player.getLocation().distance(target.getEntity().getLocation())));

            if (targets.isEmpty()) {
                s.sendMessage(makeMessage(ChatColor.RED, "最寄りのNPCが見つかりません。"));
                return;
            }
            npc = targets.get(0);

        } else {
            try {
                npc = citizens.getById(Integer.parseInt(args.get(0)));
            } catch (NumberFormatException e) {
                s.sendMessage(makeMessage(ChatColor.RED, "NPCの数値IDを指定してください。"));
                return;
            }
            if (npc == null) {
                s.sendMessage(makeMessage(ChatColor.RED, "指定されたNPCが見つかりません。"));
                return;
            }
        }

        if (!parkour.rankNPCs().containsKey(npc.getId())) {
            parkour.newRankNPC(npc.getId());
            container.put(parkour);
            s.sendMessage(makeMessage(ChatColor.GOLD, "NPC-" + npc.getId() + " " + npc.getName() + " を追加しました。"));
            citizens.updateNPC(parkour);
        } else {
            s.sendMessage(makeMessage(ChatColor.RED, "すでに追加されています。"));
        }
    }

    @NotNull
    private List<String> genSetupSetRankNPC(CommandSender s, String label, List<String> args) {
        if (citizens.isHooked() && args.size() == 1)
            return generateSuggests(args.get(0), Lists.newArrayList(citizens.getNPCs()).stream()
                    .map(npc -> String.valueOf(npc.getId()))
                    .toArray(String[]::new));
        return Collections.emptyList();
    }

    private void execSetupClearRankNPCs(CommandSender s, List<String> args) {
        if (!owner.getCitizens().isHooked()) {
            s.sendMessage(makeMessage(ChatColor.RED, "CitizensNPCと連携されていません。"));
            return;
        }

        Parkour parkour = container.get(args.remove(0));
        if (parkour == null)
            throw new NotFoundParkour();

        parkour.rankNPCs().clear();
        container.put(parkour);
        s.sendMessage(makeMessage(ChatColor.GOLD, "NPC設定を削除しました。"));
    }






    private Location cloneXYZYaw(Location location, float yaw) {
        return new Location(null, location.getX(), location.getY(), location.getZ(), roundYaw(yaw), 0);
    }

    private float roundYaw(float yaw) {
        if (yaw < 0.0F)
            yaw += 360.0F;
        yaw %= 360.0F;
        int i = (int)((yaw + 45.0F) / 90.0F);
        if (i == 1)
            return 90;
        if (i == 2)
            return 180;
        if (i == 3)
            return -90;
        return 0;
    }

    @Override
    public void onError(@NotNull CommandSender sender, @Nullable Command command, @NotNull CommandError error) {
        String message;
        if (error instanceof NotFoundCommandError) {
            message = "そのコマンドはありません。";
        } else if (error instanceof PermissionCommandError) {
            message = "権限がありません。";
        } else {
            message = error.getMessage();
        }
        sender.sendMessage(makeMessage(ChatColor.RED, "エラー: " + message));
    }


}
