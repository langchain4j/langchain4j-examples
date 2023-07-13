package devoxx;

import devoxx.model.Talk;
import retrofit2.Call;
import retrofit2.http.GET;

import java.util.List;

public interface DevoxxApi {

    @GET("/api/public/talks")
    Call<List<Talk>> talks();
}
