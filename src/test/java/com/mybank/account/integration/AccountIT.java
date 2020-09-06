/*
package com.mybank.account.integration;

import com.mybank.account.Application;
import io.restassured.RestAssured;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.config.JsonConfig;
import io.restassured.config.RedirectConfig;
import io.restassured.http.ContentType;
import io.restassured.path.json.config.JsonPathConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource("classpath:application-test.properties")
public class AccountIT {

    @LocalServerPort
    private int randomPort;

    @Value("${api.guest.user:user1}")
    private String testUserName;

    @Value("${api.guest.password:user1}")
    private String testUserPassword;

    @Before
    public void setUp(){
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.urlEncodingEnabled = false;
        RestAssured.config = RestAssured.config()
                .jsonConfig(JsonConfig.jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.DOUBLE))
                .redirect(RedirectConfig.redirectConfig().followRedirects(false));
        RestAssured.baseURI = "http://localhost:"+ randomPort;
    }

    protected RequestSpecification givenBaseSpec(){
        return RestAssured.given().accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .auth().basic(testUserName, testUserPassword);
    }

    protected Response makePOSTRequest(String url, String payload){
        return givenBaseSpec()
                .body(payload)
                .when()
                .post(url)
                .then().extract().response();

    }

    @Test
    @DirtiesContext
    public void checkIfHealthChekWOrks(){
        Response response = RestAssured.given()
                .baseUri("http://localhost:"+ randomPort)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .auth().basic(testUserName, testUserPassword)
                .when()
                .get("/actuator/health")
                .then().extract().response();

        ResponseSpecification OKSPEC =
                new ResponseSpecBuilder()
                .expectStatusCode(200)
                .build();

        response.then().spec(OKSPEC);
    }
}
*/
