package com.wl;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ExcelUtil {

    public Object[][] readData(String filePath, String sheetName) {
        Object[][] objects = null;

        // 默认从classpath中找文件(文件放在resources目录下)，name不能带“/”，否则会抛空指针
        InputStream resourceStream = this.getClass().getClassLoader().getResourceAsStream(filePath);

        //创建Workbook工作薄对象，表示整个excel.
        Workbook workbook = null;
        try {
            if (filePath.endsWith("xls")) {
                workbook = new HSSFWorkbook(resourceStream);
            }
            else if (filePath.endsWith("xlsx")) {
                workbook = new XSSFWorkbook(resourceStream);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (workbook != null) {
            Sheet sheet = workbook.getSheet("LocationSearch");
            if (sheet != null) {
                //获得当前sheet的开始行
                int firstRowNum  = sheet.getFirstRowNum();
                Row firstRow = sheet.getRow(0);

                //获得当前sheet的结束行
                int lastRowNum = sheet.getLastRowNum();

                objects = new Object[lastRowNum][firstRow.getLastCellNum()-1];
                for (int i = firstRowNum+1;i<=lastRowNum;i++) {
                    //获得当前行
                    Row row = sheet.getRow(i);

                    //获得当前行的开始列
                    int firstCellNum = row.getFirstCellNum();
                    //获得当前行的列数
                    int lastCellNum = row.getPhysicalNumberOfCells();
                    Map<String, String> cells = new HashMap<String, String>();
                    //循环当前行
                    for(int cellNum = firstCellNum; cellNum < lastCellNum;cellNum++){
                        Cell cell = row.getCell(cellNum);
                        cells.put(firstRow.getCell(cellNum).getStringCellValue(), getCellValue(cell));
                    }

                    objects[i-1][0] = cells;
                }
            }
        }

        return objects;
    }

    protected static String getCellValue(Cell cell){
        String cellValue = "";
        if(cell == null){
            return cellValue;
        }
        //把数字当成String来读，避免出现1读成1.0的情况
        if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
            cell.setCellType(Cell.CELL_TYPE_STRING);
        }
        //判断数据的类型
        switch (cell.getCellType()){
            case Cell.CELL_TYPE_NUMERIC: //数字
                cellValue = String.valueOf(cell.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_STRING: //字符串
                cellValue = String.valueOf(cell.getStringCellValue());
                break;
            case Cell.CELL_TYPE_BOOLEAN: //Boolean
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_FORMULA: //公式
                cellValue = String.valueOf(cell.getCellFormula());
                break;
            case Cell.CELL_TYPE_BLANK: //空值
                cellValue = "";
                break;
            case Cell.CELL_TYPE_ERROR: //故障
                cellValue = "非法字符";
                break;
            default:
                cellValue = "未知类型";
                break;
        }
        return cellValue;
    }
}
