package api;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;

public interface ShouldProvideVolleyInstance {
    RequestQueue getRequestQueue();
    ImageLoader getImageLoader();
}
