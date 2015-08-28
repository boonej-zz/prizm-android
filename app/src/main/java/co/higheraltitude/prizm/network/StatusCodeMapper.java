package co.higheraltitude.prizm.network;

import com.joanzapata.android.asyncservice.api.ErrorMapper;

import org.springframework.web.client.HttpStatusCodeException;


/**
 * Created by boonej on 8/25/15.
 */
public class StatusCodeMapper implements ErrorMapper {
    @Override
    public int mapError(Throwable throwable) {
        if (throwable instanceof HttpStatusCodeException) {
            return ((HttpStatusCodeException) throwable).getStatusCode().value();
        } else {
            return SKIP;
        }
    }
}
