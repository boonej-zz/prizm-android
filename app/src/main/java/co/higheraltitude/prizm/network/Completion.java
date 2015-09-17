package co.higheraltitude.prizm.network;

/**
 * Created by boonej on 8/21/15.
 */
public interface Completion {
    void onSuccess(String result);
    void onError(String result);
}
