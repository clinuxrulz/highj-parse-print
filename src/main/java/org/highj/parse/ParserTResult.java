package org.highj.parse;

import org.derive4j.Data;
import org.derive4j.Derive;
import org.derive4j.Flavour;
import org.derive4j.Visibility;
import org.highj.data.List;

import java.util.function.Function;

@Data(value = @Derive(inClass = "ParserTResultImpl", withVisibility = Visibility.Package), flavour = Flavour.HighJ)
public abstract class ParserTResult<E,T,M,A> {

    public interface Cases<R,E,T,M,A> {
        R Done(A result, List<T> unconsumedTokens);
        R More(ParserT<E,T,M,A> parserT);
        R Error(E error);
    }

    public abstract <R> R match(Cases<R,E,T,M,A> cases);

    public static <E,T,M,A> ParserTResult<E,T,M,A> done(A result, List<T> unconsumedTokens) {
        return ParserTResultImpl.Done(result, unconsumedTokens);
    }

    public static <E,T,M,A> ParserTResult<E,T,M,A> more(ParserT<E,T,M,A> parserT) {
        return ParserTResultImpl.More(parserT);
    }

    public static <E,T,M,A> ParserTResult<E,T,M,A> error(E error) {
        return ParserTResultImpl.Error(error);
    }

    public <B> ParserTResult<E,T,M,B> map(Function<A,B> fn) {
        return ParserTResultImpl
            .<E,T,M,A>cases()
            .Done((A result, List<T> unconsumedTokens) -> ParserTResult.<E,T,M,B>done(fn.apply(result), unconsumedTokens))
            .More((ParserT<E,T,M,A> k) -> ParserTResult.<E,T,M,B>more(k.map(fn)))
            .Error(ParserTResult::<E,T,M,B>error)
            .apply(this);
    }
}
