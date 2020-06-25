package org.folio.marccat.resources;

import org.folio.marccat.StorageTestSuite;
import org.folio.marccat.TestConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import java.io.File;
import static io.restassured.RestAssured.given;


@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class LoadFromFileTest extends TestConfiguration {

  @Test
  public void loadRecords() throws Exception {

    String url = getURI("/marccat/load-from-file");
    String path = this.getClass().getResource("/bibliographic/record.mrc").getFile().toString();

    given()
      .headers("X-Okapi-Tenant", StorageTestSuite.TENANT_ID)
      .queryParam("view", "1")
      .queryParam("startRecord", "1")
      .queryParam("numberOfRecords", "1")
      .multiPart("files", new File(path))
      .when()
      .post(url)
      .then()
      .statusCode(201);
  }



}