package fox.selinium.testsuite;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ShowTest {
	private WebDriver driver;
	private String baseUrl = "https://www.fox.com";
	HSSFWorkbook workbook;
	HSSFSheet sheet;
	Map<Integer, Object[]> testresultdata;
	Logger log = Logger.getLogger(ShowTest.class);
	int count = 0;
	@Before
	public void launchApp() {
		driver = new FirefoxDriver();
		driver.manage().window().maximize();
		driver.get(baseUrl);
		workbook = new HSSFWorkbook();
		sheet = workbook.createSheet("Test Result");
		testresultdata = new HashMap<Integer, Object[]>();
		testresultdata.put(count++, new Object[] {"Shows", "Record Status"});
	}

	@Test
	public void launchSiteAndLogin() {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 5);
			WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(@class, 'Header_userIcon_1VTS5')]")));
			element.click();
			WebElement signInElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(@class, 'Account_signIn_Q0B7n')]")));
			signInElement.click();
			driver.findElement(By.name("signinEmail")).clear();
			driver.findElement(By.name("signinEmail")).sendKeys("amyac66@laurenbt.com");
			driver.findElement(By.name("signinPassword")).clear();
			driver.findElement(By.name("signinPassword")).sendKeys("test1234");
			driver.findElement(By.xpath("//*[contains(@class, 'Account_signinButtonDesktop_2SO1g')]")).click();
			driver.findElement(By.xpath("//a[contains(@class, 'Header_categoryItem_2_wKp')]")).click();
			scrollPage();
			List<WebElement> elements = driver.findElements(By.xpath("//a[contains(@class, 'Tile_title_2XOxg MovieTile_title_1u6rs')]"));
			List<WebElement> filteredElements = elements.subList(elements.size()-4, elements.size());
			List<String> showNamesList = new ArrayList<String>();
			for(WebElement filteredElement: filteredElements){
				showNamesList.add(filteredElement.getText());
			}
			Map<String,String> duplicatedMap = checkDuplicated(showNamesList);
			for (int i=0;i<showNamesList.size();i++){
				testresultdata.put(count++, new Object[]{showNamesList.get(i), duplicatedMap.get(showNamesList.get(i))});
			}
			writeToExcel();
		} catch (Exception e) {
			log.error("Login Failed");
		}
	}
	
	private void scrollPage(){
		try {
		    Long lastHeight = (Long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");

		    while (true) {
		        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
		        Thread.sleep(2000);
		        Long newHeight = (Long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");
		        if (newHeight.longValue() == lastHeight.longValue()) {
		            break;
		        }
		        lastHeight = newHeight;
		    }
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	}
	
	public void writeToExcel() {
	    //write excel file and file name is TestResult.xls 
	    Set<Integer> keyset = testresultdata.keySet();
	    int rownum = 0;
	    for (Integer key : keyset) {
	        Row row = sheet.createRow(rownum++);
	        Object[] objArr = testresultdata.get(key);
	        int cellnum = 0;
	        for (Object obj : objArr) {
		        Cell cell = row.createCell(cellnum++);
		        cell.setCellValue((String)obj);
	        }
	    }
	    try {
	        FileOutputStream out =new FileOutputStream(new File("TestResult.xls"));
	        workbook.write(out);
	        out.close();
	        log.info("Excel written successfully..");
	         
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	  }
	
	private Map<String,String> checkDuplicated(List<String> showNamesList){
		Map<String,String> elementStatusMap =  new HashMap<String,String>();
		String[] tabValues = new String[]{"FX", "National Geographic", "FOX Sports", "All Shows"};
		for(String tab: tabValues)
		{
			driver.findElement(By.xpath("//a[contains(@class, 'Header_categoryItem_2_wKp')]")).click();
			driver.findElement(By.xpath("//a[contains(@class, 'PageHeaderBrowse_tab_19aN7 PageHeaderBrowseAltHeader_tab_2Lzol') and text()='"+tab+"']")).click();
			scrollPage();
			List<WebElement> elements = driver.findElements(By.xpath("//a[contains(@class, 'Tile_title_2XOxg MovieTile_title_1u6rs')]"));
			for (int i=0;i<showNamesList.size();i++){
				String searchData = showNamesList.get(i);
				if(elementStatusMap.get(searchData) == null)
				{
					for(WebElement elememnt: elements) {
					    if(elememnt.getText().trim().contains(searchData))
					    {
					    	elementStatusMap.put(searchData, "Duplicate Record");
					    	break;
					    }
					}
				}
			}
		}
		for (int i=0;i<showNamesList.size();i++){
			String searchData = showNamesList.get(i);
			if(elementStatusMap.get(searchData) == null){
				elementStatusMap.put(searchData, "");
			}
		}
		return elementStatusMap;
	}
}
