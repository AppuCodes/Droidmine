package net.minecraft.optifine;

import java.lang.reflect.Field;

import net.minecraft.client.ClientEngine;

public class FieldLocatorFixed implements IFieldLocator
{
    private Field field;

    public FieldLocatorFixed(Field p_i37_1_)
    {
        this.field = p_i37_1_;
    }

    public Field getField(ClientEngine mc)
    {
        return this.field;
    }
}
