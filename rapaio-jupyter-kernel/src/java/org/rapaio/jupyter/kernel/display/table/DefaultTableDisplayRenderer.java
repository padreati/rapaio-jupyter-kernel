package org.rapaio.jupyter.kernel.display.table;

import java.util.Set;
import java.util.UUID;

import org.rapaio.jupyter.kernel.display.DisplayData;
import org.rapaio.jupyter.kernel.display.DisplayRenderer;
import org.rapaio.jupyter.kernel.display.MimeType;
import org.rapaio.jupyter.kernel.global.Global;

/**
 * Default renderer for table display model.
 */
public class DefaultTableDisplayRenderer implements DisplayRenderer {

    private static final Set<MimeType> supportedTypes = Set.of(
            MimeType.HTML, MimeType.MARKDOWN);

    @Override
    public Class<?> rendererClass() {
        return TableDisplayModel.class;
    }

    @Override
    public boolean canRender(String mimeType) {
        MimeType mime = MimeType.from(mimeType, Global.config().display().defaultMime());
        return supportedTypes.contains(mime);
    }

    @Override
    public DisplayData render(String mimeType, Object o) {
        if (!(o instanceof TableDisplayModel td)) {
            return null;
        }

        MimeType mime = MimeType.from(mimeType, Global.config().display().defaultMime());
        if (mime.equals(MimeType.HTML)) {
            return displayHtml(td);
        } else if (mime.equals(MimeType.MARKDOWN)) {
            return displayMarkdown(td);
        }
        return null;
    }

    private DisplayData displayHtml(TableDisplayModel td) {
        String id = UUID.randomUUID().toString();

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("<table border=\"1\" class=\"dataframe\" id=\"%s\">\n", id));
        sb.append("<thead>\n");
        for (int i = 0; i < td.headerRows(); i++) {
            sb.append("<tr style=\"text-align: right;\">");
            sb.append("<th></th>");
            for (int j = 0; j < td.getCols(); j++) {
                sb.append(String.format("<th>%s</th>", td.getValue(i, j)));
            }
            sb.append("</tr>\n");
        }
        sb.append("</thead>\n");
        sb.append("<tbody>\n");
        for (int i = td.headerRows(); i < td.getRows(); i++) {
            sb.append("<tr>");
            sb.append(String.format("<th>%d</th>", i - td.headerRows()));
            for (int j = 0; j < td.getCols(); j++) {
                sb.append(String.format("<td>%s</td>", td.getValue(i, j)));
            }
            sb.append("</tr>\n");
        }
        sb.append("</tbody>\n");
        sb.append("</table>");

        DisplayData dd = DisplayData.fromHtml(sb.toString());
        dd.setDisplayId(id);
        return dd;
    }

    private DisplayData displayMarkdown(TableDisplayModel td) {
        String id = UUID.randomUUID().toString();

        StringBuilder sb = new StringBuilder();
        if (td.headerRows() > 0) {
            for (int i = 0; i < td.headerRows(); i++) {
                sb.append("| ");
                sb.append(" |");
                for (int j = 0; j < td.getCols(); j++) {
                    sb.append(String.format("%s |", td.getValue(i, j)));
                }
                sb.append("\n");
            }
            sb.append("| ");
            sb.append("--- |".repeat(Math.max(0, td.getCols() + 1)));
            sb.append("\n");
        }

        for (int i = td.headerRows(); i < td.getRows(); i++) {
            sb.append("| ");
            sb.append(String.format("**%d** |", i - td.headerRows()));
            for (int j = 0; j < td.getCols(); j++) {
                sb.append(String.format("%s |", td.getValue(i, j)));
            }
            sb.append("\n");
        }

        DisplayData dd = DisplayData.withType(MimeType.MARKDOWN.longType(), sb.toString());
        dd.setDisplayId(id);
        return dd;
    }

    private DisplayData displayText(TableDisplayModel td) {
        return DisplayData.fromText("Not implemented yet.");
    }
}
