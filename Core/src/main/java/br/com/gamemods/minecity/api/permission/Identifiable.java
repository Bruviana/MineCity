package br.com.gamemods.minecity.api.permission;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public interface Identifiable<T extends Serializable>
{
    @NotNull
    Identity<T> getIdentity();
}
