package io.quarkiverse.langchain4j.sample;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import org.mvnpm.importmap.Aggregator;

/**
 * Dynamically create the import map
 */
@ApplicationScoped
@Path("/_importmap")
public class ImportmapResource {
    private String importmap;

    // See https://github.com/WICG/import-maps/issues/235
    // This does not seem to be supported by browsers yet...
    @GET
    @Path("/dynamic.importmap")
    @Produces("application/importmap+json")
    public String importMap() {
        return this.importmap;
    }

    @GET
    @Path("/dynamic-importmap.js")
    @Produces("application/javascript")
    public String importMapJson() {
        return JAVASCRIPT_CODE.formatted(this.importmap);
    }

    @PostConstruct
    void init() {
        Aggregator aggregator = new Aggregator();
        // Add our own mappings
        aggregator.addMapping("icons/", "/icons/");
        aggregator.addMapping("components/", "/components/");
        aggregator.addMapping("fonts/", "/fonts/");
        this.importmap = aggregator.aggregateAsJson();
    }

    private static final String JAVASCRIPT_CODE = """
            const im = document.createElement('script');
            im.type = 'importmap';
            im.textContent = JSON.stringify(%s);
            document.currentScript.after(im);
            """;
}
