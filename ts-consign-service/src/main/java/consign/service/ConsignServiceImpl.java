package consign.service;

import consign.entity.ConsignRecord;














import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import consign.entity.Consign;
import consign.repository.ConsignRepository;
import edu.fudan.common.util.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author fdse
 */
@Service
public class ConsignServiceImpl implements ConsignService { 
    private static final Logger logger = LoggerFactory.getLogger(ConsignServiceImpl.class);















    @Autowired
    ConsignRepository repository;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    private String getServiceUrl(String serviceName) {
        logger.info("[function name:{}][serviceName:{}]","getServiceUrl",serviceName);
        return "http://" + serviceName;
    }


    @Override
    public Response insertConsignRecord(Consign consignRequest, HttpHeaders headers) {
        logger.info("[function name:{}][consignRequest:{}, headers:{}]","insertConsignRecord",(consignRequest != null ? consignRequest.toString(): null), (headers != null ? headers.toString(): null));

        ConsignRecord consignRecord = new ConsignRecord();
        //Set the record attribute
        consignRecord.setId(UUID.randomUUID().toString());
        consignRecord.setOrderId(consignRequest.getOrderId().toString());
        consignRecord.setAccountId(consignRequest.getAccountId().toString());
        consignRecord.setHandleDate(consignRequest.getHandleDate());
        consignRecord.setTargetDate(consignRequest.getTargetDate());
        consignRecord.setFrom(consignRequest.getFrom());
        consignRecord.setTo(consignRequest.getTo());
        consignRecord.setConsignee(consignRequest.getConsignee());
        consignRecord.setPhone(consignRequest.getPhone());
        consignRecord.setWeight(consignRequest.getWeight());

        //get the price
        HttpEntity requestEntity = new HttpEntity(null, headers);
        String consign_price_service_url = getServiceUrl("ts-consign-price-service");
        ResponseEntity<Response<Double>> re = restTemplate.exchange(
                consign_price_service_url + "/api/v1/consignpriceservice/consignprice/" + consignRequest.getWeight() + "/" + consignRequest.isWithin(),
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<Response<Double>>() {
                });
        logger.info("[status code:{}, url:{}, type:{}, headers:{}]",re.getStatusCode(),
                consign_price_service_url + "/api/v1/consignpriceservice/consignprice/" + consignRequest.getWeight() + "/" + consignRequest.isWithin(),"GET",headers);
        consignRecord.setPrice(re.getBody().getData());
        ConsignRecord result = repository.save(consignRecord);
        return new Response<>(1, "You have consigned successfully! The price is " + result.getPrice(), result);
    }

    @Override
    public Response updateConsignRecord(Consign consignRequest, HttpHeaders headers) {
        logger.info("[function name:{}][consignRequest:{}, headers:{}]","updateConsignRecord",(consignRequest != null ? consignRequest.toString(): null), (headers != null ? headers.toString(): null));

        if (!repository.findById(consignRequest.getId()).isPresent()) {
        logger.info("[Optional<ConsignRecord>:{},headers:{}]", (repository.findById(consignRequest.getId()) != null ? repository.findById(consignRequest.getId()) : null),headers);
            return insertConsignRecord(consignRequest, headers);
        }
        ConsignRecord originalRecord = repository.findById(consignRequest.getId()).get();
        originalRecord.setAccountId(consignRequest.getAccountId().toString());
        originalRecord.setHandleDate(consignRequest.getHandleDate());
        originalRecord.setTargetDate(consignRequest.getTargetDate());
        originalRecord.setFrom(consignRequest.getFrom());
        originalRecord.setTo(consignRequest.getTo());
        originalRecord.setConsignee(consignRequest.getConsignee());
        originalRecord.setPhone(consignRequest.getPhone());
        //Recalculate price
        if (originalRecord.getWeight() != consignRequest.getWeight()) {
            HttpEntity requestEntity = new HttpEntity<>(null, headers);
            String consign_price_service_url = getServiceUrl("ts-consign-price-service");
            ResponseEntity<Response<Double>> re = restTemplate.exchange(
                    consign_price_service_url + "/api/v1/consignpriceservice/consignprice/" + consignRequest.getWeight() + "/" + consignRequest.isWithin(),
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<Response<Double>>() {
                    });
        logger.info("[status code:{}, url:{}, type:{}, headers:{}]",re.getStatusCode(),
                    consign_price_service_url + "/api/v1/consignpriceservice/consignprice/" + consignRequest.getWeight() + "/" + consignRequest.isWithin(),"GET",headers);

            originalRecord.setPrice(re.getBody().getData());
        } else {
            originalRecord.setPrice(originalRecord.getPrice());
        }
        originalRecord.setConsignee(consignRequest.getConsignee());
        originalRecord.setPhone(consignRequest.getPhone());
        originalRecord.setWeight(consignRequest.getWeight());
      
      logger.info("[originalRecord:{},headers:{}]", (originalRecord != null ? originalRecord : null));
      repository.save(originalRecord);
        return new Response<>(1, "Update consign success", originalRecord);
    }

    @Override
    public Response queryByAccountId(UUID accountId, HttpHeaders headers) {
        logger.info("[function name:{}][accountId:{}, headers:{}]","queryByAccountId",(accountId != null ? accountId.toString(): null), (headers != null ? headers.toString(): null));
        List<ConsignRecord> consignRecords = repository.findByAccountId(accountId.toString());
      logger.info("[consignRecords:{},headers:{}]", (consignRecords != null ? consignRecords : null));
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
        if (consignRecords != null && !consignRecords.isEmpty()) {
            return new Response<>(1, "Find consign by account id success", consignRecords);
        }else {
            logger.warn("[queryByAccountId][No Content according to accountId][accountId: {}]", accountId);
            return new Response<>(0, "No Content according to accountId", null);
        }
    }

    @Override
    public Response queryByOrderId(UUID orderId, HttpHeaders headers) {
        logger.info("[function name:{}][orderId:{}, headers:{}]","queryByOrderId",(orderId != null ? orderId.toString(): null), (headers != null ? headers.toString(): null));
        ConsignRecord consignRecords = repository.findByOrderId(orderId.toString());
      logger.info("[consignRecords:{},headers:{}]", (consignRecords != null ? consignRecords : null));
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
        if (consignRecords != null ) {
            return new Response<>(1, "Find consign by order id success", consignRecords);
        }else {
            logger.warn("[queryByOrderId][No Content according to orderId][orderId: {}]", orderId);
            return new Response<>(0, "No Content according to order id", null);
        }
    }

    @Override
    public Response queryByConsignee(String consignee, HttpHeaders headers) {
        logger.info("[function name:{}][consignee:{}, headers:{}]","queryByConsignee",consignee, (headers != null ? headers.toString(): null));
        List<ConsignRecord> consignRecords = repository.findByConsignee(consignee);
      logger.info("[consignRecords:{},headers:{}]", (consignRecords != null ? consignRecords : null));
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
        if (consignRecords != null && !consignRecords.isEmpty()) {
            return new Response<>(1, "Find consign by consignee success", consignRecords);
        }else {
            logger.warn("[queryByConsignee][No Content according to consignee][consignee: {}]", consignee);
            return new Response<>(0, "No Content according to consignee", null);
        }
    }
}
