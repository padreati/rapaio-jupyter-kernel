package org.rapaio.jupyter.kernel.core;

import com.google.gson.annotations.SerializedName;

public record LanguageInfo(
        // Language name
        @SerializedName("name") String name,
        // Language version
        @SerializedName("version") String version,
        // Mimetype for snippets written in this language
        @SerializedName("mimetype") String mimetype,
        // File extension for scripts written in this language. For as {@code .py} or {@code .java}
        @SerializedName("file_extension") String fileExtension,
         // {@code pygments} lexer for syntax highlighting, useful if it's different from language
        @SerializedName("pygments_lexer") String pygmentsLexer,
         // {@code codemirror} mode for syntax highlighting in the notebook as in
        //  <a href="https://codemirror.net/doc/manual.html#option_mode">language config</a>
        @SerializedName("codemirror_mode") Object codemirrorMode,
         //  the exported for scripts written in this language, by default is {@code "script"} exporter
        @SerializedName("nbconvert_exporter") String nbconvertExporter
) {

    public static LanguageInfo kernelLanguageInfo() {

        String name = "java";
        String version = Runtime.version().toString();
        String mimetype = "text/x-java-source";
        String fileExtension = ".jshell";
        String pygmentsLexer = "java";
        String codemirrorMode = "java";
        String nbconvertExporter = "script";

        return new LanguageInfo(name, version, mimetype, fileExtension, pygmentsLexer, codemirrorMode, nbconvertExporter);
    }

    public record Help(String text, String url) {
    }
}
