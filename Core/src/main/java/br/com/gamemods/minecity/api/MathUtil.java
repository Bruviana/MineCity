package br.com.gamemods.minecity.api;

public class MathUtil
{
    private MathUtil() {}

    public static <C extends Comparable<C>, T extends C> T min(T a, T b)
    {
        return (a.compareTo(b) <= 0)? a : b;
    }

    public static <C extends Comparable<C>, T extends C> T max(T a, T b)
    {
        return (a.compareTo(b) >= 0)? a : b;
    }
}
