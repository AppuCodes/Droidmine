package net.minecraft.optifine;

import java.lang.reflect.Field;

import net.minecraft.client.ClientEngine;

public interface IFieldLocator
{
    Field getField(ClientEngine mc);
}
