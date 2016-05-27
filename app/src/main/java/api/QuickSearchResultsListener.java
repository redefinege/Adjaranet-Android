package api;

import java.util.List;

public interface QuickSearchResultsListener {
    void onSuccess(List<QuickSearchModel> searchModelList);

    void onError();
}
