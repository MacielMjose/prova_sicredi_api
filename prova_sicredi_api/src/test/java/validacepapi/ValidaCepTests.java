package validacepapi;

import static io.restassured.RestAssured.given;
import static io.restassured.path.xml.XmlPath.CompatibilityMode.HTML;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;


import org.testng.annotations.*;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.ChartLocation;
import com.aventstack.extentreports.reporter.configuration.Theme;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;

public class ValidaCepTests {
	
	ExtentHtmlReporter htmlReport; 
    ExtentReports extent;
    ExtentTest test;
    
    @BeforeTest
	public void setup() {
		//reporting server initializing
        htmlReport = new ExtentHtmlReporter(System.getProperty("user.dir") +"/test-output/testReport.html");
        //initialize ExtentReports and attach the HtmlReporter
        extent = new ExtentReports();
        extent.attachReporter(htmlReport);
        //configuration items to change the look and feel
        //add content, manage tests etc
        htmlReport.config().setChartVisibilityOnOpen(true);
        htmlReport.config().setDocumentTitle("ValidaCep Tests - Report");
        htmlReport.config().setReportName("ValidaCep Api Tests");
        htmlReport.config().setTestViewChartLocation(ChartLocation.TOP);
        htmlReport.config().setTheme(Theme.STANDARD);
        htmlReport.config().setTimeStampFormat("EEEE, MMMM dd, yyyy, hh:mm a '('zzz')'");
	}
    
	@Test
	public void consulta_cep_valido() {
	test = extent.createTest("Test - consulta_cep_valido");
	String url = "https://viacep.com.br/ws/91060900/json/";

	Response response =	RestAssured.given().when().get(url);
		response.then().statusCode(200);
		JsonPath extractor = response.jsonPath();
						System.out.println(extractor.get());
	}
	
	@Test
	public void consulta_cep_inexistente() {
	test = extent.createTest("Test - consulta_cep_inexistente");
	String url = "https://viacep.com.br/ws/91919191/json/";
	
	//GIVEN,WHEN,THEN
	Response response =	RestAssured.given().contentType(ContentType.JSON).when().get(url);
		response.then().body("erro", equalTo(true)).statusCode(200);
		JsonPath extractor = response.jsonPath();
		System.out.println(extractor.get("erro"));
	}
	
	@Test
	public void consulta_cep_invalido() {
		test = extent.createTest("Test - consulta_cep_invalido");

		String url = "https://viacep.com.br/ws/950100100/json/";

		Response response = given().
				when().get(url).
				then().contentType(ContentType.HTML).extract().response();
		assertEquals(response.getStatusCode(), 400);
		XmlPath htmlPath = new XmlPath(HTML, response.getBody().asString());
		assertEquals(htmlPath.getString("html.head.title"), "ViaCEP 400");
		assertEquals(htmlPath.getString("html.body.h1"), "Erro 400");
		assertEquals(htmlPath.getString("html.body.h2"), "Ops!");
		assertEquals(htmlPath.getString("html.body.h3"), "Verifique a sua URL (Bad Request)");
	}
	
	@AfterMethod
	public void getResult(ITestResult result) {
		if(result.getStatus() == ITestResult.FAILURE) {
            test.log(Status.FAIL, MarkupHelper.createLabel(result.getName()+" FAILED ", ExtentColor.RED));
            test.fail(result.getThrowable());
        }
        else if(result.getStatus() == ITestResult.SUCCESS) {
            test.log(Status.PASS, MarkupHelper.createLabel(result.getName()+" PASSED ", ExtentColor.GREEN));
        }
        else {
            test.log(Status.SKIP, MarkupHelper.createLabel(result.getName()+" SKIPPED ", ExtentColor.ORANGE));
            test.skip(result.getThrowable());
        }
	}
	@AfterTest
    public void tearDown() {
    	//to write or update test information to reporter
        extent.flush();
    }
}
