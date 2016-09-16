package org.highj.parse;

import org.derive4j.Data;
import org.derive4j.Derive;
import org.derive4j.Flavour;
import org.derive4j.Visibility;
import org.highj.data.List;

@Data(value = @Derive(inClass = "ParserResultTImpl", withVisibility = Visibility.Package), flavour = Flavour.HighJ)
public abstract class ParserResultT<E,T,M,A> {

    public interface Cases<R,E,T,M,A> {
        R Done(A result, List<T> unconsumedTokens);
        R More(ParserT<E,T,M,A> parserT);
        R Error(E error);
    }

    public abstract <R> R match(Cases<R,E,T,M,A> cases);

    public static <E,T,M,A> ParserResultT<E,T,M,A> done(A result, List<T> unconsumedTokens) {
        return ParserResultTImpl.Done(result, unconsumedTokens);
    }

    public static <E,T,M,A> ParserResultT<E,T,M,A> more(ParserT<E,T,M,A> parserT) {
        return ParserResultTImpl.More(parserT);
    }

    public static <E,T,M,A> ParserResultT<E,T,M,A> error(E error) {
        return ParserResultTImpl.Error(error);
    }
}
