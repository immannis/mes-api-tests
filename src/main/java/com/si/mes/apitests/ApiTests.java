package com.si.mes.apitests;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;

import org.apache.log4j.Logger;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ApiTests {

	public static String name = "TestPosts";
	public String instanceName = "TestPosts Instance";
	private static Logger logger = Logger.getLogger(ApiTests.class);
	public String epUrl = "http://ec2-54-174-213-136.compute-1.amazonaws.com:3000";
	public String usersUrl = epUrl + "/users";
	public String postsUrl = epUrl + "/posts";
	public String commentsUrl = epUrl + "/comments";
	public String cType = "application/json";
	public String userReqBody = "{  \"name\": \"Soujanya Imm\", \"username\": \"soujanyai\", \"email\": \"si@test.com\", \"address\": { \"street\": \"123 dummy ave\", \"suite\": \"101\", \"city\": \"Atlanta\"},    \"phone\": \"1-770-736-8031 x56442\",    \"website\": \"testsi.org\",    \"company\": {  \"name\": \"MES\", \"bs\": \"harness real-time e-markets\"}}";
	public String postReqBody = "{\"userId\":\"1\", \"title\": \"Test Post Title\",\"body\": \"Test Post Body\"}";
	public String commentsReqBody = "{\"postId\": 1,  \"name\": \"test name\", \"email\": \"Eliseo@gardner.biz\", \"body\": \"This is test body\"}";
	public String modifiedBody = null;

	@Test
	public void testUsersValidInputWithJsonPath() {

		logger.info("Verify for a given user - username, city, lang and bs values are correct");
		given().contentType(cType).when().get(usersUrl + "/1").then().statusCode(200).assertThat().body("username",
				equalTo("Bret"), "address.city", equalTo("Gwenborough"), "address.geo.lng", equalTo("81.1496"),
				"company.bs", containsString("e-markets"));

		logger.info("Verify no.of users from specified city are greater than 50 ");
		given().contentType(cType).when().get(usersUrl).then().statusCode(200).assertThat()
				.body("address.findAll{it.city =='Lebsackbury'}.size()", greaterThan(50));

		logger.info(
				"Verify if users with specified website are less than 100 and if all of them have specified string in catchphrase ");
		given().contentType(cType).when().get(usersUrl).then().statusCode(200).assertThat().body(
				"findAll{it.website =='ambrose.net' && it.company.catchPhrase == 'task-force'}.size()", lessThan(100));

	}

	@Test
	public void testUsersErrorsforbadActions() {

		String urlWithUser = null;
		logger.info("Verify Insert new User via POST yields success");
		int userIDFromRes = given().contentType(cType).body(userReqBody).when().post(usersUrl).then().statusCode(201)
				.extract().path("id");

		urlWithUser = usersUrl + "/" + userIDFromRes;

		String insertedPost = given().contentType(cType).body(userReqBody).when().get(urlWithUser).then().extract()
				.response().print();

		logger.info("Verify Inserting existing post  yields error");
		given().contentType(cType).body(insertedPost).when().post(usersUrl).then().statusCode(500);

		logger.info("Verify Insert duplicate via PUT yields suceess");
		given().contentType(cType).body(userReqBody).when().put(urlWithUser).then().statusCode(200);

		modifiedBody = userReqBody.replace("Soujanya Imm", "Soujanya Mi Imm");

		logger.info("Verify Upsert via PUT yields suceess");
		given().contentType(cType).body(modifiedBody).when().put(urlWithUser).then().statusCode(200).assertThat()
				.body("name", equalTo("Soujanya Mi Imm"));

		logger.info("Verify deleting existing user yields succes");
		given().when().delete(urlWithUser).then().statusCode(200);

		logger.info("Verify deleting already deleted user yields error");
		given().when().get(urlWithUser).then().statusCode(404);

	}

	// @Test
	// Negetive tests for assumed data validations. Please uncomment the Test annotation to run the testcase
	/**
	 * @TODO ErrorReorter to collect all failures and continue
	 */
	public void ztestUsersErrorsforbadInputs() {

		modifiedBody = userReqBody.replace("si@test.com", "sitestcom");
		assertFalse("User got Created with Invalid email format", given().contentType(cType).body(modifiedBody).when()
				.post(usersUrl).then().extract().statusCode() == 201);

		modifiedBody = userReqBody.replace("sitestcom", "si@test.com");
		assertFalse("User got created with invalid website", given().contentType(cType).body(modifiedBody).when()
				.post(usersUrl).then().extract().statusCode() == 201);

		modifiedBody = userReqBody.replace("\"Atlanta\"", "1234");
		logger.info(modifiedBody);
		assertFalse("Integer is being accepted for City", given().contentType(cType).body(modifiedBody).when()
				.post(usersUrl).then().extract().statusCode() == 201);

		/**
		 * @TODO tests for html, JS, SQL injections Optional/conditional/mandatory
		 *       fields
		 * 
		 **/

	}

	@Test
	public void testPostsValidInputs() {

		String urlWithPostId = null;

		logger.info("Verify Post is inserted successfully for valid data");
		int postidFromRes = given().contentType(cType).body(postReqBody).when().post(postsUrl).then().statusCode(201)
				.extract().path("id");
		urlWithPostId = postsUrl + "/" + postidFromRes;

		logger.info("Verify for a given Post title ends with specified string");
		given().contentType(cType).when().get(postsUrl + "/1").then().statusCode(200).assertThat().body("title",
				endsWith("reprehenderit"));

		logger.info("Verify no.of posts posted by specified user  is greater than equal to 5");
		given().contentType(cType).when().get(usersUrl + "/1/posts").then().statusCode(200).assertThat().body("size()",
				greaterThanOrEqualTo(5));

		logger.info("Verify no.of posts that contains given string in Title posted by a given user is greater than 0");
		given().contentType(cType).when().get(usersUrl + "/1/posts").then().statusCode(200).assertThat()
				.body("title.grep(~/Test Post.*/).size()", greaterThan(0));

		modifiedBody = postReqBody.replace("Post Body", "Post Body by Soujanya Immani");
		logger.info("Verify for a given existing Post upsert is successfull and assert the updated text");
		logger.info("Verify Upsert via PUT yields suceess");
		given().contentType(cType).body(modifiedBody).when().put(urlWithPostId).then().statusCode(200).assertThat()
				.body("body", endsWith("Soujanya Immani"));

		logger.info("Verify deleting existing post yields success");
		given().when().delete(urlWithPostId).then().statusCode(200);

		logger.info("Verify deleting already deleted post yields error");
		given().when().get(urlWithPostId).then().statusCode(404);

	}

	// @Test
	// Negetive tests for assumed data validations. Please uncomment the Test annotation to run the testcase
	public void testPostsErrorsforbadInputs() {

		modifiedBody = postReqBody.replace("\"Test Post Body\"", "1234");
		assertFalse("Integer is being accepted for Title", given().contentType(cType).body(modifiedBody).when()
				.post(postsUrl).then().extract().statusCode() == 201);

		modifiedBody = postReqBody.replace("expedita", "<script>javascript:alert()</script>");
		assertFalse("Data is not sanitized:Javascript injected", given().contentType(cType).body(modifiedBody).when()
				.post(postsUrl).then().extract().statusCode() == 201);

	}

	@Test
	public void testCommentsValidInputs() {

		String urlWithCommentId = null;

		logger.info("Verify Comment is inserted successfully for valid data");
		int commentidFromRes = given().contentType(cType).body(commentsReqBody).when().post(commentsUrl).then()
				.statusCode(201).extract().path("id");
		urlWithCommentId = commentsUrl + "/" + commentidFromRes;

		logger.info("Verify for a given Comment - email has @");
		given().contentType(cType).when().get(commentsUrl + "/1").then().statusCode(200).assertThat().body("email",
				containsString("@"));

		logger.info("Verify for a given post no.of comments are less than or greter than 5");
		logger.info("Verify for a given Comment - email has @");
		given().contentType(cType).when().get(postsUrl + "/1/comments").then().statusCode(200).assertThat()
				.body("$.size()", greaterThanOrEqualTo(5));

		logger.info("Verify for a given post no.of comments that has illegallanguage word less than or equal to 10");
		given().contentType(cType).when().get(postsUrl + "/1/comments").then().statusCode(200).assertThat()
				.body("body.grep(~/.*nonoword.*/).size()", lessThanOrEqualTo(10));

		modifiedBody = commentsReqBody.replace("test name", "test name updated by put");
		logger.info("Verify for a given existing comment upsert is successfull and assert the updated text");
		given().contentType(cType).body(modifiedBody).when().put(urlWithCommentId).then().statusCode(200).assertThat()
				.body("name", equalTo("test name updated by put"));

		logger.info("Verify deleting existing comment yields succes");
		given().when().delete(urlWithCommentId).then().statusCode(200);

		logger.info("Verify deleting already deleted comment yields error");
		given().when().get(urlWithCommentId).then().statusCode(404);

	}

	// @Test
	// Negetive tests for assumed data validations. Please uncomment the Test annotation to run the testcase
	public void testCommentsErrorsforbadInputs() {

		modifiedBody = commentsReqBody.replace("1", "\"postid as String\"");
		assertFalse("String is being accepted for PostID", given().contentType(cType).body(modifiedBody).when()
				.post(commentsUrl).then().extract().statusCode() == 201);

	}

}
