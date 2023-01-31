package neatlogic.module.report.config;

import java.awt.Color;
import java.awt.Paint;

public class ReportConfig {

	public static String EXPORT_TMP_PATH;
	public static int REPORT_MAXROW;
	public static int REPORT_TIMEOUT;
	public static String ARCHIVE_PATH;

	public static final int JFREECHART_FONTSIZE;

	public static Paint[] CHART_COLOR = {

			Color.decode("#D18CBD"), 
			Color.decode("#FFBA5A"),
			Color.decode("#78D8DE"),
			Color.decode("#A78375"), 
			Color.decode("#B9D582"), 
			Color.decode("#F3E67B"), 
			Color.decode("#527CA6"), 
			Color.decode("#50BFF2"), 
			Color.decode("#43CB9A"),
			Color.decode("#FF8484") };

	static {
		JFREECHART_FONTSIZE = 12;
	}
}
