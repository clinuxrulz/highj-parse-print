package org.highj.parse;

import org.derive4j.Data;
import org.derive4j.Derive;
import org.derive4j.Flavour;
import org.derive4j.Visibility;
import org.highj.data.List;

import java.util.function.Function;

@Data(value = @Derive(inClass = "ParserTResultImpl", withVisibility = Visibility.Package), flavour = Flavour.HighJ)
public abstract class ParserTResult<E,TC,M,A> {

    public interface Cases<R,E,TC,M,A> {
        R Done(A result, TC unconsumedTokenChunk);
        R More(ParserT<E,TC,M,A> parserT);
        R Error(E error);
    }

    public abstract <R> R match(Cases<R,E,TC,M,A> cases);

    public static <E,TC,M,A> ParserTResult<E,TC,M,A> done(A result, TC unconsumedTokens) {
        return ParserTResultImpl.Done(result, unconsumedTokens);
    }

    public static <E,TC,M,A> ParserTResult<E,TC,M,A> more(ParserT<E,TC,M,A> parserT) {
        return ParserTResultImpl.More(parserT);
    }

    public static <E,TC,M,A> ParserTResult<E,TC,M,A> error(E error) {
        return ParserTResultImpl.Error(error);
    }

    public <B> ParserTResult<E,TC,M,B> map(Function<A,B> fn) {
        return ParserTResultImpl
            .<E,TC,M,A>cases()
            .Done((A result, TC unconsumedTokenChunk) -> ParserTResult.<E,TC,M,B>done(fn.apply(result), unconsumedTokenChunk))
            .More((ParserT<E,TC,M,A> k) -> ParserTResult.<E,TC,M,B>more(k.map(fn)))
            .Error(ParserTResult::<E,TC,M,B>error)
            .apply(this);
    }
}
