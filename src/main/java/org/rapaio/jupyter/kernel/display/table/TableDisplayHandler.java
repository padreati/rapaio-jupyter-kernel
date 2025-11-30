package org.rapaio.jupyter.kernel.display.table;

import java.util.UUID;

import org.rapaio.jupyter.kernel.display.DisplayData;
import org.rapaio.jupyter.kernel.display.DisplayHandler;
import org.rapaio.jupyter.kernel.display.MIMEType;

public class TableDisplayHandler implements DisplayHandler {

    @Override
    public boolean canRender(Object o) {
        return o instanceof TableDisplay;
    }

    @Override
    public String defaultMIMEType() {
        return MIMEType.HTML.toString();
    }

    @Override
    public DisplayData render(String mimeType, Object o) {
        if (!(o instanceof TableDisplay td)) {
            return null;
        }

        String mime = mimeType != null ? mimeType : defaultMIMEType();

        if (mime.equals(MIMEType.HTML.toString())) {
            return displayHtml(td);
        }
        if (mime.equals(MIMEType.MARKDOWN.toString())) {
            return displayMarkdown(td);
        }
        if (mime.equals(MIMEType.TEXT.toString())) {
            return displayText(td);
        }
        return null;
    }

    private DisplayData displayHtml(TableDisplay td) {
        String id = UUID.randomUUID().toString();

        StringBuilder sb = new StringBuilder();
        
        // Add CSS styles
        sb.append("<style>\n");
        sb.append(String.format("#%s {\n", id));
        sb.append("  border-collapse: collapse;\n");
        sb.append("  width: 100%;\n");
        sb.append("  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;\n");
        sb.append("  font-size: 14px;\n");
        sb.append("}\n");
        sb.append(String.format("#%s th, #%s td {\n", id, id));
        sb.append("  border: 1px solid #ddd;\n");
        sb.append("  padding: 8px 12px;\n");
        sb.append("  text-align: left;\n");
        sb.append("}\n");
        sb.append(String.format("#%s th {\n", id));
        sb.append("  background-color: #f8f9fa;\n");
        sb.append("  font-weight: 600;\n");
        sb.append("  color: #495057;\n");
        sb.append("}\n");
        sb.append(String.format("#%s tr:nth-child(even) {\n", id));
        sb.append("  background-color: #f8f9fa;\n");
        sb.append("}\n");
        sb.append(String.format("#%s tr:hover {\n", id));
        sb.append("  background-color: #e9ecef;\n");
        sb.append("}\n");
        sb.append("</style>\n");
        
        sb.append(String.format("<table id=\"%s\">\n", id));
        sb.append("<thead>\n");
        for (int i = 0; i < td.headerRows(); i++) {
            sb.append("<tr>");
            for (int j = 0; j < td.getCols(); j++) {
                sb.append(String.format("<th>%s</th>", td.getValue(i, j)));
            }
            sb.append("</tr>\n");
        }
        sb.append("</thead>\n");
        sb.append("<tbody>\n");
        for (int i = td.headerRows(); i < td.getRows(); i++) {
            sb.append("<tr>");
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

    private DisplayData displayMarkdown(TableDisplay td) {
        String id = UUID.randomUUID().toString();

        StringBuilder sb = new StringBuilder();
        if (td.headerRows() > 0) {
            for (int i = 0; i < td.headerRows(); i++) {
                sb.append("| ");
                for (int j = 0; j < td.getCols(); j++) {
                    sb.append(String.format("%s |", td.getValue(i, j)));
                }
                sb.append("\n");
            }
            sb.append("| ");
            sb.append("--- |".repeat(Math.max(0, td.getCols())));
            sb.append("\n");
        }

        for (int i = td.headerRows(); i < td.getRows(); i++) {
            sb.append("| ");
            for (int j = 0; j < td.getCols(); j++) {
                sb.append(String.format("%s |", td.getValue(i, j)));
            }
            sb.append("\n");
        }

        DisplayData dd = DisplayData.withType(MIMEType.MARKDOWN.toString(), sb.toString());
        dd.setDisplayId(id);
        return dd;
    }

    private DisplayData displayText(TableDisplay td) {
        return DisplayData.fromText("Not implemented yet.");
    }
}
