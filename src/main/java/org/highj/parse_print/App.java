package org.highj.parse_print;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        java.util.function.Function<Integer,Integer> f = x -> x + 1;
        System.out.println( f.apply(3).toString() );
    }
}
