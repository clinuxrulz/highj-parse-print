package org.highj.parse;

import org.derive4j.hkt.__;
import org.highj.data.List;

public interface ParserT<E,T,M,A> {

    __<M,ParserResultT<E,T,M,A>> consume(List<T> tokens);
}
