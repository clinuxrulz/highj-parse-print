# highj-parse-print

- [x] A iteratee parser ```ParserT```.
- [ ] A pretty printer ```PrintT```.
- [ ] A profunctor interface to perform parsing and pretty printing with the same code (almost like ```purescript-typeable```).

## Parser Example (yes messy at the moment, but works)

```
public class App {
    public static void main(String[] params) {
        Monoid<List<String>> eMonoid = Monoid.create(List.Nil(), (a, b) -> List.append(a, b));
        Monoid<String> tcMonoid = Monoid.create("", (a, b) -> a + b);
        Either<String,Integer> errorOrResult = T1.narrow(
            ParserTStringUtil.<List<String>,T1.µ>eof(List.of("Expected eof")).apply(
                ParserTStringUtil.<List<String>,T1.µ>int_(List.of("Expected int")).map((Integer x) -> (Function<T0,Integer>)(T0 unused) -> x)
            ).consume(
                eMonoid,
                tcMonoid,
                T1.monad,
                "12345\0"
            )
        )._1().match(new ParserTResult.Cases<Either<String,Integer>,List<String>,String,T1.µ,Integer>() {
            @Override
            public Either<String, Integer> Done(Integer result, String unconsumedTokenChunk) {
                return Either.Right(result);
            }
            @Override
            public Either<String, Integer> More(ParserT<List<String>, String, T1.µ, Integer> parserT) {
                return Either.Left("Unexpected end of file.");
            }
            @Override
            public Either<String, Integer> Error(List<String> error) {
                return Either.Left(tcMonoid.fold(error.intersperse("\r\n")));
            }
        });
        errorOrResult.either(
            (String error) -> (Effect0)() -> {
                System.out.println("Error: " + error);
            },
            (Integer result) -> (Effect0)() -> {
                System.out.println("Result: " + result.toString());
            }
        ).run();
    }
}
```
