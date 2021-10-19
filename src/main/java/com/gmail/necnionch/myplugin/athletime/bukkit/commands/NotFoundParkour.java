package com.gmail.necnionch.myplugin.athletime.bukkit.commands;

import com.gmail.necnionch.myplugin.athletime.bukkit.command.errors.CommandError;
import org.jetbrains.annotations.NotNull;

public class NotFoundParkour extends CommandError {
    @Override
    public @NotNull String getMessage() {
        return "そのパルクールは存在しません。";
    }

}
