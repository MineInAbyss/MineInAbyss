package com.derongan.minecraft.mineinabyss.util;

public class TickUtils {
    public static int milisecondsToTicks(int mili){
        return mili / 50;
    }
    public static int ticksToMilliseconds(int ticks){
        return ticks * 50;
    }
}
