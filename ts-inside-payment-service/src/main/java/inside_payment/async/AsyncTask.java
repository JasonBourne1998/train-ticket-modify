package inside_payment.async;

import java.util.concurrent.Future;
import inside_payment.entity.OutsidePaymentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author fdse
 */
@Component  
public class AsyncTask {
    
    @Autowired
	private RestTemplate restTemplate;


    @Async("mySimpleAsync")
    public Future<Boolean> sendAsyncCallToPaymentService(OutsidePaymentInfo outsidePaymentInfo) {
        Boolean value = restTemplate.getForObject("http://rest-service-external:16100/greet", Boolean.class);
        return new AsyncResult<>(value);
    }
    
}  
