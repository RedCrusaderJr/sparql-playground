package swiss.sib.sparql.playground.Performance;

import java.io.File;
import java.io.IOException;

import com.gembox.spreadsheet.CellRange;
import com.gembox.spreadsheet.ExcelCell;
import com.gembox.spreadsheet.ExcelFile;
import com.gembox.spreadsheet.ExcelWorksheet;
import com.gembox.spreadsheet.LengthUnit;
import com.gembox.spreadsheet.SpreadsheetInfo;
import com.gembox.spreadsheet.tables.BuiltInTableStyleName;
import com.gembox.spreadsheet.tables.Table;
import com.gembox.spreadsheet.tables.TableColumn;
import com.gembox.spreadsheet.tables.TotalsRowFunction;

public class ExcelTracer {

	public static final String DEFAULT = "defaultRepository";
	public static final String NATIVE = "nativeRepository";
	public static final String MARKLOGIC_RDF4J = "marklogic_rdf4j";
	public static final String APPROACH_1 = "approach1";
	public static final String APPROACH_2 = "approach2";

	public static final String BASIC = "basic";
	public static final String DRAW_FEEDER = "drawFeeder";
	public static final String DRAW_FEEDER_OPT = "drawFeederOpt";
	public static final String DRAW_HAZARD = "drawHazard";
	public static final String DRAW_BUFFER = "drawBuffer";
	public static final String DRAW_BUFFER_OPT = "drawBufferOpt";
	public static final String DRAW_HEALTHY = "drawHealthy";
	public static final String DRAW_HEALTHY_OPT = "drawHealthyOpt";
	public static final String DRAW_AFFECTED = "drawAffected";
	public static final String DRAW_AFFECTED_OPT = "drawAffectedOpt";

	private Boolean started = false;
	private File excelFilePath;
	private ExcelFile excelFile;
	private String sheetName;

	private Integer rowTracker;

	public ExcelTracer() {
		// If using Profssional version, put your serial key below.
		SpreadsheetInfo.setLicense("FREE-LIMITED-KEY");
		rowTracker = 0;
	}

	public void startExcelFile(String fileName) throws Exception {
		this.excelFilePath = new File("test_folder/results/" + fileName + ".xlsx");

		if (!this.excelFilePath.exists()) {
			throw new Exception("!this.excelFilePath.exists()");
		} else {
			this.excelFile = ExcelFile.load(excelFilePath.getAbsolutePath());
			started = true;
		}
	}

	public Boolean isStarted() {
		return started;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

	public void resetRowTracker() {
		this.rowTracker = 0;
	}

	public void nextRow() {
		this.rowTracker++;
	}

	public void traceToTable(String tableName, String columnName, long data) throws Exception {
		if (!started) {
			return;
		}

		if (this.excelFile == null) {
			throw new Exception("this.excelFile == null");
		}

		try {
			ExcelWorksheet sheet = excelFile.getWorksheet(this.sheetName);
			if (this.sheetName == null) {
				throw new Exception("sheet == null");
			}

			Table table = sheet.getTable(tableName + "_" + sheetName);
			if (table == null) {
				throw new Exception("table == null");
			}

			TableColumn column = table.getColumn(columnName);
			if (column == null) {
				throw new Exception("clolumn == null");
			}

			CellRange range = column.getDataRange();
			ExcelCell cell = range.get(rowTracker);
			cell.setValue(data);

		} catch (Exception e) {
			throw e;
		}

	}

	public void saveExcelFile() throws IOException {
		this.excelFile.save(excelFilePath.getAbsolutePath());
	}

	private void excel() throws java.io.IOException {

		ExcelFile workbook = new ExcelFile();
		ExcelWorksheet worksheet = workbook.addWorksheet("Tables");

		// Add some data.
		Object[][] data = { { "Worker", "Hours", "Price" }, { "John Doe", 25, 35.0 }, { "Jane Doe", 27, 35.0 },
				{ "Jack White", 18, 32.0 }, { "George Black", 31, 35.0 } };

		for (int i = 0; i < 5; i++)
			for (int j = 0; j < 3; j++)
				worksheet.getCell(i, j).setValue(data[i][j]);

		// Set column widths.
		worksheet.getColumn(0).setWidth(100, LengthUnit.PIXEL);
		worksheet.getColumn(1).setWidth(70, LengthUnit.PIXEL);
		worksheet.getColumn(2).setWidth(70, LengthUnit.PIXEL);
		worksheet.getColumn(3).setWidth(70, LengthUnit.PIXEL);
		worksheet.getColumn(2).getStyle().setNumberFormat("\"$\"#,##0.00");
		worksheet.getColumn(3).getStyle().setNumberFormat("\"$\"#,##0.00");

		// Create table and enable totals row.
		Table table = worksheet.addTable("Table1", "A1:C5", true);
		table.setHasTotalsRow(true);

		// Add new column.
		TableColumn column = table.addColumn();
		column.setName("Total");

		// Populate column.
		for (ExcelCell cell : column.getDataRange())
			cell.setFormula("=Table1[Hours] * Table1[Price]");

		// Set totals row function for newly added column and calculate it.
		column.setTotalsRowFunction(TotalsRowFunction.SUM);
		column.getRange().calculate();

		// Set table style.
		table.setBuiltInStyle(BuiltInTableStyleName.TABLE_STYLE_MEDIUM_2);

		workbook.save("test_folder/results/TestData.xlsx");
	}
}
