package org.highj.parse;

import org.derive4j.hkt.__;
import org.highj.data.List;
import org.highj.typeclass0.group.Monoid;
import org.highj.typeclass1.monad.Monad;

import java.util.function.Function;
import java.util.function.Supplier;

public interface ParserT<E,TC,M,A> {

    __<M,ParserTResult<E,TC,M,A>> consume(Monoid<E> eMonoid, Monoid<TC> tcMonoid, Monad<M> mMonad, TC tokenChunk);

    static <E,TC,M,A> ParserT<E,TC,M,A> liftM(__<M,A> ma) {
        return (Monoid<E> eMonoid, Monoid<TC> tcMonoid, Monad<M> mMonad, TC tokenChunk) ->
            mMonad.map(
                (A a) ->
                    ParserTResult.done(a, tcMonoid.identity()),
                ma
            );
    }

    default <B> ParserT<E,TC,M,B> map(Function<A,B> fn) {
        return (Monoid<E> eMonoid, Monoid<TC> tcMonoid, Monad<M> mMonad, TC tokens) ->
            mMonad.map(
                (ParserTResult<E,TC,M,A> x) -> x.map(fn),
                ParserT.this.consume(eMonoid, tcMonoid, mMonad, tokens)
            );
    }

    default <B> ParserT<E,TC,M,B> apply(ParserT<E,TC,M,Function<A,B>> pf) {
        return (Monoid<E> eMonoid, Monoid<TC> tcMonoid, Monad<M> mMonad, TC tokenChunk) ->
            mMonad.bind(
                pf.consume(eMonoid, tcMonoid, mMonad, tokenChunk),
                ParserTResultImpl
                    .<E,TC,M,Function<A,B>>cases()
                    .Done(
                        (Function<A,B> fn, TC remainingTokenChunk) ->
                            ParserT.this.map(fn).consume(eMonoid, tcMonoid, mMonad, remainingTokenChunk)
                    )
                    .More(
                        (ParserT<E,TC,M,Function<A,B>> pf2) ->
                            mMonad.pure(ParserTResult.more(ParserT.this.apply(pf2)))
                    )
                    .Error(
                        e -> mMonad.pure(ParserTResult.error(e))
                    )
            );
    }

    static <E,TC,M,A> ParserT<E,TC,M,A> pure(A a) {
        return (Monoid<E> eMonoid, Monoid<TC> tcMonoid, Monad<M> mMonad, TC tokenChunk) ->
            mMonad.pure(ParserTResult.done(a, tokenChunk));
    }

    static <E,TC,M,A> ParserT<E,TC,M,A> lazy(Supplier<ParserT<E,TC,M,A>> p) {
        class Lazy {
            private final Object lock = new Object();
            private Supplier<ParserT<E,TC,M,A>> expression;
            private volatile ParserT<E,TC,M,A> evaluation;
            private ParserT<E,TC,M,A> eval() {
                ParserT<E,TC,M,A> _evaluation = this.evaluation;
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
        lazy.expression = p;
        return (Monoid<E> eMonoid, Monoid<TC> tcMonoid, Monad<M> mMonad, TC tokenChunk) ->
            lazy.eval().consume(eMonoid, tcMonoid, mMonad, tokenChunk);
    }

    default ParserT<E,TC,M,A> mplus(ParserT<E,TC,M,A> rhs) {
        return (Monoid<E> eMonoid, Monoid<TC> tcMonoid, Monad<M> mMonad, TC tokenChunk) ->
            mMonad.apply2(
                (ParserTResult<E,TC,M,A> lhsPr) -> (ParserTResult<E,TC,M,A> rhsPr) ->
                    ParserTResultImpl
                        .<E,TC,M,A>cases()
                        .Done(lhsPr)
                        .More(
                            (ParserT<E,TC,M,A> lhs2) ->
                                ParserTResult.more(
                                    ParserTResultImpl
                                        .<E,TC,M,A>cases()
                                        .Done(
                                            (A a, TC remainingTokenChunk) ->
                                                lhs2.mplus(
                                                    (Monoid<E> eMonoid2, Monoid<TC> tcMonoid2, Monad<M> mMonad2, TC tokenChunk2) ->
                                                        mMonad2.pure(
                                                            ParserTResult.<E,TC,M,A>done(
                                                                a,
                                                                tcMonoid2.apply(
                                                                    remainingTokenChunk,
                                                                    tokenChunk2
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
                                .<E,TC,M,A>cases()
                                .Done(rhsPr)
                                .More(
                                    (ParserT<E,TC,M,A> rhs2) ->
                                        ParserTResult.more(ParserTUtil.addErrorBeforeErrorOnError(rhs2, e))
                                )
                                .Error(e2 -> ParserTResult.error(eMonoid.apply(e, e2)))
                                .apply(rhsPr)
                        )
                        .apply(lhsPr),
                ParserT.this.consume(eMonoid, tcMonoid, mMonad, tokenChunk),
                rhs.consume(eMonoid, tcMonoid, mMonad, tokenChunk)
            );
    }

    static <E,TC,M,A> ParserT<E,TC,M,List<A>> many(ParserT<E,TC,M,A> p) {
        return some(p).mplus(ParserT.pure(List.Nil()));
    }

    static <E,TC,M,A> ParserT<E,TC,M,List<A>> some(ParserT<E,TC,M,A> p) {
        return ParserT.lazy(() -> many(p)).apply(p.map((A a) -> (List<A> x) -> List.Cons(a, x)));
    }
}
