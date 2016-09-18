package org.highj.parse;

import org.highj.data.List;
import org.highj.typeclass0.group.Monoid;
import org.highj.typeclass1.monad.Monad;

import java.util.function.Function;

class ParserTUtil {
    static <E,TC,M,A> ParserT<E,TC,M,List<A>> manyReversed(ParserT<E,TC,M,A> p) {
        return someReversed(p).mplus(ParserT.pure(List.Nil()));
    }

    static <E,TC,M,A> ParserT<E,TC,M,List<A>> someReversed(ParserT<E,TC,M,A> p) {
        return ParserT.lazy(() -> manyReversed(p)).apply(p.map((A a) -> (Function<List<A>,List<A>>)(List<A> x) -> List.Cons(a, x)));
    }

    static <E,TC,M,A> ParserT<E,TC,M,A> addErrorBeforeErrorOnError(ParserT<E,TC,M,A> p, E error) {
        return (Monoid<E> eMonoid, Monoid<TC> tcMonoid, Monad<M> mMonad, TC tokenChunk) ->
            mMonad.map(
                (ParserTResult<E,TC,M,A> pr) ->
                    ParserTResultImpl
                        .<E,TC,M,A>cases()
                        .Done(pr)
                        .More((ParserT<E,TC,M,A> p2) -> ParserTResult.more(addErrorBeforeErrorOnError(p2, error)))
                        .Error(e -> ParserTResult.error(eMonoid.apply(error, e)))
                        .apply(pr),
                p.consume(eMonoid, tcMonoid, mMonad, tokenChunk)
            );
    }

    static <E,TC,M,A> ParserT<E,TC,M,A> addErrorAfterErrorOnError(ParserT<E,TC,M,A> p, E error) {
        return (Monoid<E> eMonoid, Monoid<TC> tcMonoid, Monad<M> mMonad, TC tokenChunk) ->
            mMonad.map(
                (ParserTResult<E,TC,M,A> pr) ->
                    ParserTResultImpl
                        .<E,TC,M,A>cases()
                        .Done(pr)
                        .More((ParserT<E,TC,M,A> p2) -> ParserTResult.more(addErrorAfterErrorOnError(p2, error)))
                        .Error(e -> ParserTResult.error(eMonoid.apply(e, error)))
                        .apply(pr),
                p.consume(eMonoid, tcMonoid, mMonad, tokenChunk)
            );
    }
}
