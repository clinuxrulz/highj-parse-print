package org.highj.parse;

import org.highj.data.List;
import org.highj.typeclass0.group.Monoid;
import org.highj.typeclass1.monad.Monad;

class ParserTUtil {
    static <E,T,M,A> ParserT<E,T,M,A> addErrorBeforeErrorOnError(ParserT<E,T,M,A> p, E error) {
        return (Monoid<E> eMonoid, Monad<M> mMonad, List<T> tokens) ->
            mMonad.map(
                (ParserTResult<E,T,M,A> pr) ->
                    ParserTResultImpl
                        .<E,T,M,A>cases()
                        .Done(pr)
                        .More((ParserT<E,T,M,A> p2) -> ParserTResult.more(addErrorBeforeErrorOnError(p2, error)))
                        .Error(e -> ParserTResult.error(eMonoid.apply(error, e)))
                        .apply(pr),
                p.consume(eMonoid, mMonad, tokens)
            );
    }

    static <E,T,M,A> ParserT<E,T,M,A> addErrorAfterErrorOnError(ParserT<E,T,M,A> p, E error) {
        return (Monoid<E> eMonoid, Monad<M> mMonad, List<T> tokens) ->
            mMonad.map(
                (ParserTResult<E,T,M,A> pr) ->
                    ParserTResultImpl
                        .<E,T,M,A>cases()
                        .Done(pr)
                        .More((ParserT<E,T,M,A> p2) -> ParserTResult.more(addErrorAfterErrorOnError(p2, error)))
                        .Error(e -> ParserTResult.error(eMonoid.apply(e, error)))
                        .apply(pr),
                p.consume(eMonoid, mMonad, tokens)
            );
    }
}
