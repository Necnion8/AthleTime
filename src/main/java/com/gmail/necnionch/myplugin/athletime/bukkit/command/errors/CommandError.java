package com.gmail.necnionch.myplugin.athletime.bukkit.command.errors;

import org.jetbrains.annotations.NotNull;

public abstract class CommandError extends Error {
    public abstract @NotNull String getMessage();
}
