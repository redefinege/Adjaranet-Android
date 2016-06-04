package api;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import model.Movie;
import model.Series;
import network.GsonRequest;

@SuppressWarnings("unused")
public class AdjaranetAPI {
    private static final String URL_BASE = "http://adjaranet.com/";
    private static final String URL_STATIC = "http://static.adjaranet.com/";
    private static final String URL_QUICK_SEARCH =
            URL_BASE + "/Home/quick_search?ajax=1&search={query}";
    private static final String URL_PLAYBACK_INFO =
            URL_BASE + "req/jsondata/req.php?id={movie_id}&reqId=getLangAndHd";
    private static final String URL_RELATED =
            URL_BASE + "Movie/BuildSliderRelated?ajax=1&movie_id={movie_id}&isepisode=0&type=related&order=top&period=day&limit=25";
    private static final String URL_SERIES_INFO =
            URL_BASE + "req/jsondata/req.php?id={id}&reqId=getInfo";

    private static AdjaranetAPI sInstance;

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    private AdjaranetAPI(RequestQueue requestQueue, ImageLoader imageLoader) {
        mRequestQueue = requestQueue;
        mImageLoader = imageLoader;
    }

    /**
     * Singleton initializer
     */
    public static void init(ShouldProvideVolleyInstance volleyProvider) {
        sInstance = new AdjaranetAPI(volleyProvider.getRequestQueue(), volleyProvider.getImageLoader());
    }

    /**
     * Getter for Singleton
     */
    public static AdjaranetAPI getInstance() {
        return sInstance;
    }

    /**
     * Cancel all volley requests
     */
    public void cancelAllRequests() {
        mRequestQueue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
    }

    /**
     * Get movie thumbnail image as Bitmap object
     */
    public void getMovieThumbnailBitmap(Movie movie, ImageLoader.ImageListener imageListener) {
        String thumbnailUrl = URL_STATIC
                + "moviecontent/" + movie.getId()
                + "/covers/980x530-" + movie.getId()
                + ".jpg";

        mImageLoader.get(thumbnailUrl, imageListener);
    }

    /**
     * Get advanced search results
     */
    public void getAdvancedSearchResults(final api.SearchBuilder searchBuilder,
                                         final Response.Listener<List<Movie>> listener,
                                         final Response.ErrorListener errorListener) {
        final CountDownLatch lLatch = new CountDownLatch(2);
        final List<Movie> lResponse = new ArrayList<>();

        final Response.Listener<List<Movie>> lMovieListener = new Response.Listener<List<Movie>>() {
            @Override
            public void onResponse(List<Movie> response) {
                lResponse.addAll(response);
                lLatch.countDown();
            }
        };

        final Response.Listener<List<Movie>> lSeriesListener = new Response.Listener<List<Movie>>() {
            @Override
            public void onResponse(List<Movie> response) {
                for (Movie movie : response) {
                    movie.setSeason("1");
                    movie.setEpisode("1");
                }
                lResponse.addAll(response);
                lLatch.countDown();
            }
        };

        final Response.ErrorListener lErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                lLatch.countDown();
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                searchBuilder.setEpisode("1");
                sendMovieRequest(searchBuilder.getUrl(),
                        lSeriesListener,
                        lErrorListener
                );
                searchBuilder.setEpisode("0");
                sendMovieRequest(searchBuilder.getUrl(),
                        lMovieListener,
                        lErrorListener
                );

                try {
                    lLatch.await();
                } catch (InterruptedException ignored) {
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        long seed = System.nanoTime();
                        Collections.shuffle(lResponse, new Random(seed));
                        listener.onResponse(lResponse);
                    }
                });
            }
        }).start();
    }

    /**
     * Get quick search results
     */
    public void getQuickSearchResults(String query, final QuickSearchResultsListener listener) {
        StringRequest request = new StringRequest(Request.Method.GET,
                URL_QUICK_SEARCH.replace("{query}", query),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JsonElement responseElement = new JsonParser().parse(response);
                        if (!responseElement.isJsonObject()) {
                            listener.onError();
                            return;
                        }

                        JsonElement moviesElement = responseElement.getAsJsonObject().get("movies");
                        if (!moviesElement.isJsonObject()) {
                            listener.onError();
                            return;
                        }

                        JsonElement dataElement = moviesElement.getAsJsonObject().get("data");
                        if (!dataElement.isJsonArray()) {
                            listener.onError();
                            return;
                        }

                        JsonArray dataArray = dataElement.getAsJsonArray();
                        List<QuickSearchModel> responseList = new ArrayList<>();
                        int dataLimit = dataArray.size() < 5 ? dataArray.size() : 5;

                        for (int i = 0; i < dataLimit; i++) {
                            JsonObject dataObject = dataArray.get(i).getAsJsonObject();
                            QuickSearchModel searchModel = new QuickSearchModel();

                            searchModel.setId(GsonUtils.getNullStringAsEmpty(dataObject, "id"));
                            searchModel.setTitleEn(GsonUtils.getNullStringAsEmpty(dataObject, "title_en"));
                            searchModel.setTitleKa(GsonUtils.getNullStringAsEmpty(dataObject, "title_ge"));
                            responseList.add(searchModel);
                        }
                        listener.onSuccess(responseList);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onError();
                    }
                }
        );

        mRequestQueue.add(request);
    }

    /**
     * Get movie playback info
     * Contains available languages, quality and playback url
     */
    public void getMoviePlaybackInfo(final Movie movie, final Response.Listener<String> successListener,
                                     final Response.ErrorListener errorListener) {
        StringRequest request = new StringRequest(Request.Method.GET,
                URL_PLAYBACK_INFO.replace("{movie_id}", movie.getId()),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JsonElement parsedResponse = new JsonParser().parse(response);
                        if (!parsedResponse.isJsonObject() && !parsedResponse.isJsonArray()) {
                            errorListener.onErrorResponse(null);
                            return;
                        }

                        JsonElement zeroElement;
                        if (parsedResponse.isJsonArray()
                                && parsedResponse.getAsJsonArray().size() > 0) {

                            zeroElement = parsedResponse.getAsJsonArray().get(0);
                        } else if (parsedResponse.isJsonObject()
                                && parsedResponse.getAsJsonObject().has("0")) {

                            zeroElement = parsedResponse.getAsJsonObject().get("0");
                        } else {
                            errorListener.onErrorResponse(null);
                            return;
                        }

                        if (!zeroElement.isJsonObject()) {
                            errorListener.onErrorResponse(null);
                            return;
                        }

                        JsonObject zeroObject = zeroElement.getAsJsonObject();
                        List<String> languages = GsonUtils.getCommaSeparatedList(zeroObject, "lang");
                        List<String> quality = GsonUtils.getCommaSeparatedList(zeroObject, "quality");
                        String url = GsonUtils.getNullStringAsEmpty(zeroObject, "url");
                        movie.setLanguages(languages);
                        movie.setQualities(quality);
                        movie.setPlaybackUrl(url);

                        successListener.onResponse("");
                    }
                },
                errorListener);

        mRequestQueue.add(request);
    }

    /**
     * Get movies related to passed one
     */
    public GsonRequest getRelatedMovies(Movie movie, Response.Listener<List<Movie>> listener,
                                        Response.ErrorListener errorListener) {
        return sendMovieRequest(
                URL_RELATED.replace("{movie_id}", movie.getId()),
                listener,
                errorListener
        );
    }

    /**
     * Get Georgian movies for Home screen
     */
    public GsonRequest getHomeGeorgianMovies(Response.Listener<List<Movie>> listener,
                                             Response.ErrorListener errorListener) {
        String uri = new CachedDataUriBuilder()
                .setType(CachedDataUriBuilder.TYPE__GEOMOVIES)
                .setLimit(25)
                .orderIsNew()
                .periodIsDay()
                .build().toString();

        return sendMovieRequest(
                uri,
                listener,
                errorListener
        );
    }

    /**
     * Get new movies for Home screen
     */
    public GsonRequest getHomeNewMovies(Response.Listener<List<Movie>> listener,
                                        Response.ErrorListener errorListener) {
        String uri = new CachedDataUriBuilder()
                .setType(CachedDataUriBuilder.TYPE__MOVIES)
                .setLimit(25)
                .orderIsNew()
                .periodIsWeek()
                .build().toString();

        return sendMovieRequest(
                uri,
                listener,
                errorListener
        );
    }

    /**
     * Get premiere movies for Home screen
     */
    public GsonRequest getHomePremiereMovies(Response.Listener<List<Movie>> listener,
                                             Response.ErrorListener errorListener) {
        String uri = new CachedDataUriBuilder()
                .setType(CachedDataUriBuilder.TYPE__PREMIERE)
                .setLimit(25)
                .orderIsNew()
                .periodIsWeek()
                .build().toString();

        return sendMovieRequest(
                uri,
                listener,
                errorListener
        );
    }

    /**
     * Get top series/episodes for Home screen
     */
    public GsonRequest getHomeTopSeries(Response.Listener<List<Movie>> listener,
                                        Response.ErrorListener errorListener) {
        String uri = new CachedDataUriBuilder()
                .setType(CachedDataUriBuilder.TYPE__SERIES)
                .setLimit(25)
                .orderIsTop()
                .periodIsWeek()
                .build().toString();

        return sendMovieRequest(
                uri,
                listener,
                errorListener
        );
    }

    /**
     * Get series info
     */
    public GsonRequest getSeriesInfo(String id, Response.Listener<Series> listener,
                                     Response.ErrorListener errorListener) {
        Type type = new TypeToken<Series>() {
        }.getType();
        GsonRequest<Series> request = new GsonRequest<>(
                URL_SERIES_INFO.replace("{id}", id),
                type,
                getSeriesGson(type),
                listener,
                errorListener
        );
        request.setPriority(Request.Priority.HIGH);
        mRequestQueue.add(request);

        return request;
    }

    /**
     * Send and deserialize request, which expects Movie list to be returned
     */
    private synchronized GsonRequest sendMovieRequest(String url, Response.Listener<List<Movie>> listener,
                                                      Response.ErrorListener errorListener) {
        Type type = new TypeToken<List<Movie>>() {
        }.getType();
        GsonRequest<List<Movie>> request = new GsonRequest<>(
                url,
                type,
                getMovieGson(type),
                listener,
                errorListener
        );
        request.setPriority(Request.Priority.HIGH);
        mRequestQueue.add(request);

        return request;
    }

    /**
     * Build Gson object with Movie deserializer
     */
    private Gson getMovieGson(Type type) {
        return new GsonBuilder()
                .registerTypeAdapter(type, new MovieListDeserializer())
                .create();
    }

    /**
     * Build Gson object with Series deserializer
     */
    private Gson getSeriesGson(Type type) {
        return new GsonBuilder()
                .registerTypeAdapter(type, new SeriesDeserializer())
                .create();
    }

    public static api.SearchBuilder newSearchBuilder() {
        return new SearchBuilder();
    }

    public static class SearchBuilder extends api.SearchBuilder {
        SearchBuilder() {
            super();

            setSearchUrl(URL_BASE + "Search/SearchResults");
        }
    }

    public static class CachedDataUriBuilder {
        public static final String TYPE__GEOMOVIES = "geomovies";
        public static final String TYPE__PREMIERE = "premiere";
        public static final String TYPE__MOVIES = "movies";
        public static final String TYPE__SERIES = "series";
        private static final String ORDER__NEW = "new";
        private static final String ORDER__TOP = "top";
        private static final String PERIOD__DAY = "day";
        private static final String PERIOD__WEEK = "week";

        private int dataLimit = 25;
        private String dataType = TYPE__GEOMOVIES;
        private String dataOrder = ORDER__NEW;
        private String dataPeriod = PERIOD__WEEK;

        public CachedDataUriBuilder setLimit(int limit) {
            this.dataLimit = limit;
            return this;
        }

        public CachedDataUriBuilder setType(String type) {
            this.dataType = type;
            return this;
        }

        public CachedDataUriBuilder orderIsNew() {
            this.dataOrder = ORDER__NEW;
            return this;
        }

        public CachedDataUriBuilder orderIsTop() {
            this.dataOrder = ORDER__TOP;
            return this;
        }

        public CachedDataUriBuilder periodIsDay() {
            this.dataPeriod = PERIOD__DAY;
            return this;
        }

        public CachedDataUriBuilder periodIsWeek() {
            this.dataPeriod = PERIOD__WEEK;
            return this;
        }

        public Uri build() {
            String path = "/cache/cached_home_{type}.php";
            path = path.replace("{type}", dataType);

            Uri uri = new Uri.Builder()
                    .scheme("http")
                    .authority("adjaranet.com")
                    .path(path)
                    .appendQueryParameter("type", dataType)
                    .appendQueryParameter("order", dataOrder)
                    .appendQueryParameter("period", dataPeriod)
                    .appendQueryParameter("limit", String.valueOf(dataLimit))
                    .build();

            return uri;
        }
    }
}
