# Server side setup with Maven to run example on Vespa Cloud

1. Do steps 1 - 4 from https://cloud.vespa.ai/en/getting-started-java.html
2. Create new Maven Java application and go to it
3. Add following dependency to `pom.xml`, so the latest Vespa release is used on each build:

```xml

<parent>
    <groupId>com.yahoo.vespa</groupId>
    <artifactId>cloud-tenant-base</artifactId>
    <version>[8,9)</version>
    <relativePath/>
</parent>
```

4. Create `src/main/application/schemas/langchain4j.sd` with following content:

```sd
schema langchain4j {
  document langchain4j {
    field text_segment type string {
      indexing: summary | index
    }
    field vector type tensor<float>(x[384]) {
      indexing: summary | attribute
      attribute {
        distance-metric: prenormalized-angular
      }
    }
  }

  rank-profile cosine_similarity {
    inputs {
      query(q) tensor<float>(x[384])
    }
    function cosine() {
      expression: 1.0 - distance(field, vector)
    }
    first-phase {
      expression: if (cosine() < query(threshold), -1, cosine())
      rank-score-drop-limit: 0.0
    }
  }
}
```

5. Create `src/main/application/services.xml` with following content:

```xml
<?xml version="1.0" encoding="utf-8" ?>
<services version="1.0">
    <container version="1.0" id="default">
        <document-api/>
        <search/>
    </container>

    <content id="langchain4j" version="1.0">
        <redundancy>1</redundancy>
        <documents>
            <document type="langchain4j" mode="index"/>
        </documents>
    </content>
</services>
```

6. Do steps 6 from https://cloud.vespa.ai/en/getting-started-java.html. This will generate SSL private key and
   public certificate and store them locally. Their path will be used in client example. Certificate will be added
   to the project under `src/main/application/security/clients.pem`.

7. Now you can build and deploy your application: do steps 7 & 8 from https://cloud.vespa.ai/en/getting-started.html.
   Alternatively you can deploy your build application by using [Cloud UI](https://console.vespa-cloud.com) and
   uploading `target/application.zip` there.