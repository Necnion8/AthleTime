package com.gmail.necnionch.myplugin.athletime.bukkit.command.errors;

import org.jetbrains.annotations.NotNull;

public class PermissionCommandError extends CommandError {
    @Override
    public @NotNull String getMessage() {
        return "not have perms";
    }

}
