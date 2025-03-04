package net.minecraft.optifine;

import java.lang.reflect.Field;
import java.util.*;

import net.minecraft.client.ClientEngine;

public class FieldLocatorActionKeyF3 implements IFieldLocator
{
    public Field getField(ClientEngine mc)
    {
        Class oclass = ClientEngine.class;
        Field field = this.getFieldRenderChunksMany(mc);

        if (field == null)
        {
            return null;
        }
        else
        {
            Field field1 = ReflectorRaw.getFieldAfter(ClientEngine.class, field, Boolean.TYPE, 0);

            if (field1 == null)
            {
                return null;
            }
            else
            {
                return field1;
            }
        }
    }

    private Field getFieldRenderChunksMany(ClientEngine mc)
    {
        ClientEngine minecraft = mc;
        boolean flag = minecraft.renderChunksMany;
        Field[] afield = ClientEngine.class.getDeclaredFields();
        minecraft.renderChunksMany = true;
        Field[] afield1 = ReflectorRaw.getFields(minecraft, afield, Boolean.TYPE, Boolean.TRUE);
        minecraft.renderChunksMany = false;
        Field[] afield2 = ReflectorRaw.getFields(minecraft, afield, Boolean.TYPE, Boolean.FALSE);
        minecraft.renderChunksMany = flag;
        Set<Field> set = new HashSet(Arrays.asList(afield1));
        Set<Field> set1 = new HashSet(Arrays.asList(afield2));
        Set<Field> set2 = new HashSet(set);
        set2.retainAll(set1);
        Field[] afield3 = (Field[])((Field[])set2.toArray(new Field[set2.size()]));
        return afield3.length != 1 ? null : afield3[0];
    }
}
