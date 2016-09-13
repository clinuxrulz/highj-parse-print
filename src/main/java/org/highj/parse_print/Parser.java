package org.highj.parse_print;

import org.derive4j.Data;
import org.derive4j.Derive;
import org.derive4j.Flavour;
import org.derive4j.Visibility;
import org.highj.data.List;

import java.util.function.Function;

@Data(value = @Derive(inClass = "ParserImpl", withVisibility = Visibility.Package), flavour = Flavour.HighJ)
public abstract class Parser<E,T,A> {
    public interface Cases<R,E,T,A> {
        R Await(Function<T,Parser<E,T,A>> await);
        R Done(A result, List<T> unconsumed);
        R Error(E error);
    }

    public abstract <R> R match(Cases<R,E,T,A> cases);

    public static <E,T,A> Parser<E,T,A> await(Function<T,Parser<E,T,A>> k) {
        return ParserImpl.Await(k);
    }

    public static <E,T,A> Parser<E,T,A> done(A result, List<T> unconsumed) {
        return ParserImpl.Done(result, unconsumed);
    }

    public static <E,T,A> Parser<E,T,A> error(E error) {
        return ParserImpl.Error(error);
    }

    public Parser<E,T,A> consume(List<T> t) {
        return ParserImpl.<E,T,A>cases()
            .Await(k -> {
                if (t.isEmpty()) {
                    return await(k);
                } else {
                    return k.apply(t.head()).consume(t.tail());
                }
            })
            .Done((res, unconsumed) -> Parser.done(res, List.append(unconsumed, t)))
            .Error(Parser::error)
            .apply(this);
    }

    public <B> Parser<E,T,B> map(Function<A,B> f) {
        return ParserImpl.<E,T,A>cases()
            .Await(k -> await((T t) -> k.apply(t).map(f)))
            .Done((res, unconsumed) -> Parser.done(f.apply(res), unconsumed))
            .Error(Parser::error)
            .apply(this);
    }

    public <B> Parser<E,T,B> apply(Parser<E,T,Function<A,B>> p) {
        return ParserImpl.<E,T,Function<A,B>>cases()
            .Await(k -> await((T t) -> Parser.this.apply(k.apply(t))))
            .Done((res, unconsumed) -> Parser.this.apply(p.consume(unconsumed)))
            .Error(Parser::error)
            .apply(p);
    }

    public static <E,T,A> Parser<E,T,A> pure(A a) {
        return Parser.done(a, List.Nil());
    }

    public <B> Parser<E,T,B> bind(Function<A,Parser<E,T,B>> f) {
        return ParserImpl.<E,T,A>cases()
            .Await(k -> await((T t) -> k.apply(t).bind(f)))
            .Done((res, unconsumed) -> f.apply(res).consume(unconsumed))
            .Error(Parser::error)
            .apply(this);
    }
}
