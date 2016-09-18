package org.highj.parse;

import org.highj.typeclass0.group.Monoid;
import org.highj.typeclass1.monad.Monad;

public class ParserTStringUtil {

    public static <E,M> ParserT<E,String,M,Character> char_(Character c, E noMatchError) {
        return (Monoid<E> eMonoid, Monoid<String> tcMonoid, Monad<M> mMonad, String tokenChunk) -> {
            if (tokenChunk.isEmpty()) {
                return mMonad.pure(ParserTResult.<E,String,M,Character>more(char_(c, noMatchError)));
            } else {
                char c2 = tokenChunk.charAt(0);
                String remainingTokenChunk = tokenChunk.substring(1);
                if (c.equals(c2)) {
                    return mMonad.pure(ParserTResult.<E,String,M,Character>done(c2, remainingTokenChunk));
                } else {
                    return mMonad.pure(ParserTResult.<E,String,M,Character>error(noMatchError));
                }
            }
        };
    }

    public static <E,M> ParserT<E,String,M,String> string(String str, E noMatchError) {
        return (Monoid<E> eMonoid, Monoid<String> tcMonoid, Monad<M> mMonad, String tokenChunk) -> {
            String str2 = tokenChunk.substring(0, Math.min(tokenChunk.length(), str.length()));
            String remainingTokenChunk = tokenChunk.substring(str2.length());
            if (str2.length() < str.length()) {
                if (str.substring(0, str2.length()).equals(str2)) {
                    return mMonad.pure(ParserTResult.<E,String,M,String>more(string(str.substring(str2.length()), noMatchError)));
                } else {
                    return mMonad.pure(ParserTResult.<E,String,M,String>error(noMatchError));
                }
            } else if (str.equals(str2)) {
                return mMonad.pure(ParserTResult.<E,String,M,String>done(str2, remainingTokenChunk));
            } else {
                return mMonad.pure(ParserTResult.<E,String,M,String>error(noMatchError));
            }
        };
    }
}
