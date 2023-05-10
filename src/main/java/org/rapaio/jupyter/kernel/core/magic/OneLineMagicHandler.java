package org.rapaio.jupyter.kernel.core.magic;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.rapaio.jupyter.kernel.channels.Channels;
import org.rapaio.jupyter.kernel.core.CompleteMatches;
import org.rapaio.jupyter.kernel.core.display.DisplayData;

public record OneLineMagicHandler(
        String syntaxMatcher,
        String syntaxHelp,
        List<String> documentation,
        Predicate<MagicSnippet> canHandlePredicate,
        OneLineMagicEvalFunction evalFunction,
        BiFunction<Channels, MagicSnippet, DisplayData> inspectFunction,
        BiFunction<Channels, MagicSnippet, CompleteMatches> completeFunction
) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String syntaxMatcher;
        private String syntaxHelp;
        private List<String> documentation;
        private Predicate<MagicSnippet> canHandlePredicate;
        private OneLineMagicEvalFunction evalFunction;
        private BiFunction<Channels, MagicSnippet, DisplayData> inspectFunction;
        private BiFunction<Channels, MagicSnippet, CompleteMatches> completeFunction;

        public Builder syntaxMatcher(String syntaxMatcher) {
            this.syntaxMatcher = syntaxMatcher;
            return this;
        }

        public Builder syntaxHelp(String syntaxHelp) {
            this.syntaxHelp = syntaxHelp;
            return this;
        }

        public Builder documentation(List<String> documentation) {
            this.documentation = documentation;
            return this;
        }

        public Builder canHandlePredicate(Predicate<MagicSnippet> canHandlePredicate) {
            this.canHandlePredicate = canHandlePredicate;
            return this;
        }

        public Builder evalFunction(OneLineMagicEvalFunction evalFunction) {
            this.evalFunction = evalFunction;
            return this;
        }

        public Builder inspectFunction(BiFunction<Channels, MagicSnippet, DisplayData> inspectFunction) {
            this.inspectFunction = inspectFunction;
            return this;
        }

        public Builder completeFunction(BiFunction<Channels, MagicSnippet, CompleteMatches> completeFunction) {
            this.completeFunction = completeFunction;
            return this;
        }

        public OneLineMagicHandler build() {
            return new OneLineMagicHandler(syntaxMatcher, syntaxHelp, documentation, canHandlePredicate, evalFunction, inspectFunction,
                    completeFunction);
        }
    }
}
