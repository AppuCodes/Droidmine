package net.minecraft.optifine;

import java.lang.reflect.Field;

import net.minecraft.client.ClientEngine;

public class FieldLocatorName implements IFieldLocator
{
    private ReflectorClass reflectorClass = null;
    private String targetFieldName = null;

    public FieldLocatorName(ReflectorClass p_i38_1_, String p_i38_2_)
    {
        this.reflectorClass = p_i38_1_;
        this.targetFieldName = p_i38_2_;
    }

    public Field getField(ClientEngine mc)
    {
        Class oclass = this.reflectorClass.getTargetClass();

        if (oclass == null)
        {
            return null;
        }
        else
        {
            try
            {
                Field field = oclass.getDeclaredField(this.targetFieldName);
                field.setAccessible(true);
                return field;
            }
            catch (NoSuchFieldException var3)
            {
                return null;
            }
            catch (SecurityException securityexception)
            {
                securityexception.printStackTrace();
                return null;
            }
            catch (Throwable throwable)
            {
                throwable.printStackTrace();
                return null;
            }
        }
    }
}
