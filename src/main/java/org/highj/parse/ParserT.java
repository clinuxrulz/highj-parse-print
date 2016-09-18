package org.highj.parse;

import org.derive4j.hkt.__;
import org.highj.data.List;
import org.highj.typeclass0.group.Monoid;
import org.highj.typeclass1.monad.Monad;

import java.util.function.Function;
import java.util.function.Supplier;

public interface ParserT<E,T,M,A> {

    __<M,ParserTResult<E,T,M,A>> consume(Monoid<E> eMonoid, Monad<M> mMonad, List<T> tokens);

    static <E,T,M,A> ParserT<E,T,M,A> liftM(__<M,A> ma) {
        return (Monoid<E> eMonoid, Monad<M> mMonad, List<T> tokens) ->
            mMonad.map(
                (A a) ->
                    ParserTResult.done(a, List.Nil()),
                ma
            );
    }

    default <B> ParserT<E,T,M,B> map(Function<A,B> fn) {
        return (Monoid<E> eMonoid, Monad<M> mMonad, List<T> tokens) ->
            mMonad.map(
                (ParserTResult<E,T,M,A> x) -> x.map(fn),
                ParserT.this.consume(eMonoid, mMonad, tokens)
            );
    }

    default <B> ParserT<E,T,M,B> apply(ParserT<E,T,M,Function<A,B>> pf) {
        return (Monoid<E> eMonoid, Monad<M> mMonad, List<T> tokens) ->
            mMonad.bind(
                pf.consume(eMonoid, mMonad, tokens),
                ParserTResultImpl
                    .<E,T,M,Function<A,B>>cases()
                    .Done(
                        (Function<A,B> fn, List<T> remainingTokens) ->
                            ParserT.this.map(fn).consume(eMonoid, mMonad, remainingTokens)
                    )
                    .More(
                        (ParserT<E,T,M,Function<A,B>> pf2) ->
                            mMonad.pure(ParserTResult.more(ParserT.this.apply(pf2)))
                    )
                    .Error(
                        e -> mMonad.pure(ParserTResult.error(e))
                    )
            );
    }

    static <E,T,M,A> ParserT<E,T,M,A> pure(A a) {
        return (Monoid<E> eMonoid, Monad<M> mMonad, List<T> tokens) ->
            mMonad.pure(ParserTResult.done(a, tokens));
    }

    static <E,T,M,A> ParserT<E,T,M,A> lazy(Supplier<ParserT<E,T,M,A>> p) {
        class Lazy {
            private final Object lock = new Object();
            private Supplier<ParserT<E,T,M,A>> expression;
            private volatile ParserT<E,T,M,A> evaluation;
            private ParserT<E,T,M,A> eval() {
                ParserT<E,T,M,A> _evaluation = this.evaluation;
                if (_evaluation == null) {
                    synchronized (this.lock) {
                        _evaluation = this.evaluation;
                        if (_evaluation == null) {
                            this.evaluation = _evaluation = expression.get();
                            this.expression = null;
                        }
                    }
                }
                return _evaluation;
            }
        }
        final Lazy lazy = new Lazy();
        return (Monoid<E> eMonoid, Monad<M> mMonad, List<T> tokens) ->
            lazy.eval().consume(eMonoid, mMonad, tokens);
    }

    default ParserT<E,T,M,A> mplus(ParserT<E,T,M,A> rhs) {
        return (Monoid<E> eMonoid, Monad<M> mMonad, List<T> tokens) ->
            mMonad.apply2(
                (ParserTResult<E,T,M,A> lhsPr) -> (ParserTResult<E,T,M,A> rhsPr) ->
                    ParserTResultImpl
                        .<E,T,M,A>cases()
                        .Done(lhsPr)
                        .More(
                            (ParserT<E,T,M,A> lhs2) ->
                                ParserTResult.more(
                                    ParserTResultImpl
                                        .<E,T,M,A>cases()
                                        .Done(
                                            (A a, List<T> remainingTokens) ->
                                                lhs2.mplus(
                                                    (Monoid<E> eMonoid2, Monad<M> mMonad2, List<T> tokens2) ->
                                                        mMonad2.pure(
                                                            ParserTResult.<E,T,M,A>done(
                                                                a,
                                                                List.append(
                                                                    remainingTokens,
                                                                    tokens2
                                                                )
                                                            )
                                                        )
                                                )
                                        )
                                        .More(lhs2::mplus)
                                        .Error(e -> ParserTUtil.addErrorAfterErrorOnError(lhs2, e))
                                        .apply(rhsPr)
                                )
                        )
                        .Error(e ->
                            ParserTResultImpl
                                .<E,T,M,A>cases()
                                .Done(rhsPr)
                                .More(
                                    (ParserT<E,T,M,A> rhs2) ->
                                        ParserTResult.more(ParserTUtil.addErrorBeforeErrorOnError(rhs2, e))
                                )
                                .Error(e2 -> ParserTResult.error(eMonoid.apply(e, e2)))
                                .apply(rhsPr)
                        )
                        .apply(lhsPr),
                ParserT.this.consume(eMonoid, mMonad, tokens),
                rhs.consume(eMonoid, mMonad, tokens)
            );
    }
}
