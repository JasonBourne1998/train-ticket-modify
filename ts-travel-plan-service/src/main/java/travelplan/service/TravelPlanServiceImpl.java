package travelplan.service;

import edu.fudan.common.util.JsonUtils;











import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.fudan.common.util.Response;
import edu.fudan.common.util.StringUtils;

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
import edu.fudan.common.entity.*;
import travelplan.entity.TransferTravelInfo;
import travelplan.entity.TransferTravelResult;
import travelplan.entity.TravelAdvanceResultUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author fdse
 */
@Service
public class TravelPlanServiceImpl implements TravelPlanService { 
    private static final Logger logger = LoggerFactory.getLogger(TravelPlanServiceImpl.class);













    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private DiscoveryClient discoveryClient;

    String success = "Success";
    String cannotFind = "Cannot Find";

    private String getServiceUrl(String serviceName) {
        logger.info("[function name:{}][serviceName:{}]","getServiceUrl",serviceName);
        return "http://" + serviceName;
    }

    @Override
    public Response getTransferSearch(TransferTravelInfo info, HttpHeaders headers) {
        logger.info("[function name:{}][info:{}, headers:{}]","getTransferSearch",(info != null ? info.toString(): null), (headers != null ? headers.toString(): null));

        TripInfo queryInfoFirstSection = new TripInfo();
        queryInfoFirstSection.setDepartureTime(StringUtils.Date2String(info.getTravelDate()));
        queryInfoFirstSection.setStartPlace(info.getStartStation());
        queryInfoFirstSection.setEndPlace(info.getViaStation());

        List<TripResponse> firstSectionFromHighSpeed;
        List<TripResponse> firstSectionFromNormal;
        firstSectionFromHighSpeed = tripsFromHighSpeed(queryInfoFirstSection, headers);
        firstSectionFromNormal = tripsFromNormal(queryInfoFirstSection, headers);

        TripInfo queryInfoSecondSectoin = new TripInfo();
        queryInfoSecondSectoin.setDepartureTime(StringUtils.Date2String(info.getTravelDate()));
        queryInfoSecondSectoin.setStartPlace(info.getViaStation());
        queryInfoSecondSectoin.setEndPlace(info.getEndStation());

        List<TripResponse> secondSectionFromHighSpeed;
        List<TripResponse> secondSectionFromNormal;
        secondSectionFromHighSpeed = tripsFromHighSpeed(queryInfoSecondSectoin, headers);
        secondSectionFromNormal = tripsFromNormal(queryInfoSecondSectoin, headers);

        List<TripResponse> firstSection = new ArrayList<>();
        firstSection.addAll(firstSectionFromHighSpeed);
        firstSection.addAll(firstSectionFromNormal);

        List<TripResponse> secondSection = new ArrayList<>();
        secondSection.addAll(secondSectionFromHighSpeed);
        secondSection.addAll(secondSectionFromNormal);

        TransferTravelResult result = new TransferTravelResult();
        result.setFirstSectionResult(firstSection);
        result.setSecondSectionResult(secondSection);

        return new Response<>(1, "Success.", result);
    }

    @Override
    public Response getCheapest(TripInfo info, HttpHeaders headers) {
        logger.info("[function name:{}][info:{}, headers:{}]","getCheapest",(info != null ? info.toString(): null), (headers != null ? headers.toString(): null));
        RoutePlanInfo routePlanInfo = new RoutePlanInfo();
        routePlanInfo.setNum(5);
        routePlanInfo.setStartStation(info.getStartPlace());
        routePlanInfo.setEndStation(info.getEndPlace());
        routePlanInfo.setTravelDate(info.getDepartureTime());
        ArrayList<RoutePlanResultUnit> routePlanResultUnits = getRoutePlanResultCheapest(routePlanInfo, headers);

        if (!routePlanResultUnits.isEmpty()) {
            ArrayList<TravelAdvanceResultUnit> lists = new ArrayList<>();
            for (int i = 0; i < routePlanResultUnits.size(); i++) {
                RoutePlanResultUnit tempUnit = routePlanResultUnits.get(i);
                TravelAdvanceResultUnit newUnit = new TravelAdvanceResultUnit();
                newUnit.setTripId(tempUnit.getTripId());
                newUnit.setEndStation(tempUnit.getEndStation());
                newUnit.setTrainTypeId(tempUnit.getTrainTypeName());
                newUnit.setStartStation(tempUnit.getStartStation());

                List<String> stops = tempUnit.getStopStations();
                newUnit.setStopStations(stops);
                newUnit.setPriceForFirstClassSeat(tempUnit.getPriceForFirstClassSeat());
                newUnit.setPriceForSecondClassSeat(tempUnit.getPriceForSecondClassSeat());
                newUnit.setStartTime(tempUnit.getStartTime());
                newUnit.setEndTime(tempUnit.getEndTime());

                TrainType trainType = queryTrainTypeByName(tempUnit.getTrainTypeName(), headers);
                int firstClassTotalNum = trainType.getConfortClass();
                int secondClassTotalNum = trainType.getEconomyClass();

                int first = getRestTicketNumber(info.getDepartureTime(), tempUnit.getTripId(),
                        tempUnit.getStartStation(), tempUnit.getEndStation(), SeatClass.FIRSTCLASS.getCode(), firstClassTotalNum, tempUnit.getStopStations(), headers);

                int second = getRestTicketNumber(info.getDepartureTime(), tempUnit.getTripId(),
                        tempUnit.getStartStation(), tempUnit.getEndStation(), SeatClass.SECONDCLASS.getCode(), secondClassTotalNum, tempUnit.getStopStations(), headers);
                newUnit.setNumberOfRestTicketFirstClass(first);
                newUnit.setNumberOfRestTicketSecondClass(second);
                lists.add(newUnit);
            }

            return new Response<>(1, success, lists);
        } else {
            TravelPlanServiceImpl.logger.warn("[getCheapest][Get cheapest trip warn][Route Plan Result Units: {}]","No Content");
            return new Response<>(0, cannotFind, null);
        }
    }

    @Override
    public Response getQuickest(TripInfo info, HttpHeaders headers) {
        logger.info("[function name:{}][info:{}, headers:{}]","getQuickest",(info != null ? info.toString(): null), (headers != null ? headers.toString(): null));
        RoutePlanInfo routePlanInfo = new RoutePlanInfo();
        routePlanInfo.setNum(5);
        routePlanInfo.setStartStation(info.getStartPlace());
        routePlanInfo.setEndStation(info.getEndPlace());
        routePlanInfo.setTravelDate(info.getDepartureTime());
        ArrayList<RoutePlanResultUnit> routePlanResultUnits = getRoutePlanResultQuickest(routePlanInfo, headers);


        if (!routePlanResultUnits.isEmpty()) {

            ArrayList<TravelAdvanceResultUnit> lists = new ArrayList<>();
            for (int i = 0; i < routePlanResultUnits.size(); i++) {
                RoutePlanResultUnit tempUnit = routePlanResultUnits.get(i);
                TravelAdvanceResultUnit newUnit = new TravelAdvanceResultUnit();
                newUnit.setTripId(tempUnit.getTripId());
                newUnit.setTrainTypeId(tempUnit.getTrainTypeName());
                newUnit.setEndStation(tempUnit.getEndStation());
                newUnit.setStartStation(tempUnit.getStartStation());

                List<String> stops = tempUnit.getStopStations();
                newUnit.setStopStations(stops);

                newUnit.setPriceForFirstClassSeat(tempUnit.getPriceForFirstClassSeat());
                newUnit.setPriceForSecondClassSeat(tempUnit.getPriceForSecondClassSeat());
                newUnit.setStartTime(tempUnit.getStartTime());
                newUnit.setEndTime(tempUnit.getEndTime());

                TrainType trainType = queryTrainTypeByName(tempUnit.getTrainTypeName(), headers);
                int firstClassTotalNum = trainType.getConfortClass();
                int secondClassTotalNum = trainType.getEconomyClass();
                int first = getRestTicketNumber(info.getDepartureTime(), tempUnit.getTripId(),
                        tempUnit.getStartStation(), tempUnit.getEndStation(), SeatClass.FIRSTCLASS.getCode(), firstClassTotalNum, tempUnit.getStopStations(), headers);

                int second = getRestTicketNumber(info.getDepartureTime(), tempUnit.getTripId(),
                        tempUnit.getStartStation(), tempUnit.getEndStation(), SeatClass.SECONDCLASS.getCode(), secondClassTotalNum, tempUnit.getStopStations(),headers);
                newUnit.setNumberOfRestTicketFirstClass(first);
                newUnit.setNumberOfRestTicketSecondClass(second);
                lists.add(newUnit);
            }
            return new Response<>(1, success, lists);
        } else {
            TravelPlanServiceImpl.logger.warn("[getQuickest][Get quickest trip warn][Route Plan Result Units: {}]","No Content");
            return new Response<>(0, cannotFind, null);
        }
    }

    @Override
    public Response getMinStation(TripInfo info, HttpHeaders headers) {
        logger.info("[function name:{}][info:{}, headers:{}]","getMinStation",(info != null ? info.toString(): null), (headers != null ? headers.toString(): null));
        RoutePlanInfo routePlanInfo = new RoutePlanInfo();
        routePlanInfo.setNum(5);
        routePlanInfo.setStartStation(info.getStartPlace());
        routePlanInfo.setEndStation(info.getEndPlace());
        routePlanInfo.setTravelDate(info.getDepartureTime());
        ArrayList<RoutePlanResultUnit> routePlanResultUnits = getRoutePlanResultMinStation(routePlanInfo, headers);

        if (!routePlanResultUnits.isEmpty()) {

            ArrayList<TravelAdvanceResultUnit> lists = new ArrayList<>();
            for (int i = 0; i < routePlanResultUnits.size(); i++) {
                RoutePlanResultUnit tempUnit = routePlanResultUnits.get(i);
                TravelAdvanceResultUnit newUnit = new TravelAdvanceResultUnit();
                newUnit.setTripId(tempUnit.getTripId());
                newUnit.setTrainTypeId(tempUnit.getTrainTypeName());
                newUnit.setStartStation(tempUnit.getStartStation());
                newUnit.setEndStation(tempUnit.getEndStation());

                List<String> stops = tempUnit.getStopStations();
                newUnit.setStopStations(stops);

                newUnit.setPriceForFirstClassSeat(tempUnit.getPriceForFirstClassSeat());
                newUnit.setPriceForSecondClassSeat(tempUnit.getPriceForSecondClassSeat());
                newUnit.setEndTime(tempUnit.getEndTime());
                newUnit.setStartTime(tempUnit.getStartTime());

                TrainType trainType = queryTrainTypeByName(tempUnit.getTrainTypeName(), headers);
                int firstClassTotalNum = trainType.getConfortClass();
                int secondClassTotalNum = trainType.getEconomyClass();

                int first = getRestTicketNumber(info.getDepartureTime(), tempUnit.getTripId(),
                        tempUnit.getStartStation(), tempUnit.getEndStation(), SeatClass.FIRSTCLASS.getCode(), firstClassTotalNum, tempUnit.getStopStations(), headers);

                int second = getRestTicketNumber(info.getDepartureTime(), tempUnit.getTripId(),
                        tempUnit.getStartStation(), tempUnit.getEndStation(), SeatClass.SECONDCLASS.getCode(), secondClassTotalNum, tempUnit.getStopStations(), headers);
                newUnit.setNumberOfRestTicketFirstClass(first);
                newUnit.setNumberOfRestTicketSecondClass(second);
                lists.add(newUnit);
            }
            return new Response<>(1, success, lists);
        } else {
            TravelPlanServiceImpl.logger.warn("[getMinStation][Get min stations trip warn][Route Plan Result Units: {}]","No Content");
            return new Response<>(0, cannotFind, null);
        }
    }

    private int getRestTicketNumber(String travelDate, String trainNumber, String startStationName, String endStationName, int seatType, int totalNum, List<String> stations, HttpHeaders headers) {
        logger.info("[function name:{}][travelDate:{}, trainNumber:{}, startStationName:{}, endStationName:{}, seatType:{}, totalNum:{}, stations:{}, headers:{}]","getRestTicketNumber",travelDate, trainNumber, startStationName, endStationName, seatType, totalNum, (stations != null ? stations.toString(): null), (headers != null ? headers.toString(): null));
        Seat seatRequest = new Seat();

        seatRequest.setDestStation(startStationName);
        seatRequest.setStartStation(endStationName);
        seatRequest.setTrainNumber(trainNumber);
        seatRequest.setTravelDate(travelDate);
        seatRequest.setSeatType(seatType);
        seatRequest.setStations(stations);
        seatRequest.setTotalNum(totalNum);
        HttpEntity requestEntity = new HttpEntity(seatRequest, null);
        String seat_service_url = getServiceUrl("ts-seat-service");
        ResponseEntity<Response<Integer>> re = restTemplate.exchange(
                seat_service_url + "/api/v1/seatservice/seats/left_tickets",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<Response<Integer>>() {
                });
        logger.info("[status code:{}, url:{} and type:{}]",re.getStatusCode(),
                seat_service_url + "/api/v1/seatservice/seats/left_tickets","POST");

        return re.getBody().getData();
    }

    private ArrayList<RoutePlanResultUnit> getRoutePlanResultCheapest(RoutePlanInfo info, HttpHeaders headers) {
        logger.info("[function name:{}][info:{}, headers:{}]","getRoutePlanResultCheapest",(info != null ? info.toString(): null), (headers != null ? headers.toString(): null));
        HttpEntity requestEntity = new HttpEntity(info, null);
        String route_plan_service_url = getServiceUrl("ts-route-plan-service");
        ResponseEntity<Response<ArrayList<RoutePlanResultUnit>>> re = restTemplate.exchange(
                route_plan_service_url + "/api/v1/routeplanservice/routePlan/cheapestRoute",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<Response<ArrayList<RoutePlanResultUnit>>>() {
                });
        logger.info("[status code:{}, url:{} and type:{}]",re.getStatusCode(),
                route_plan_service_url + "/api/v1/routeplanservice/routePlan/cheapestRoute","POST");
        return re.getBody().getData();
    }

    private ArrayList<RoutePlanResultUnit> getRoutePlanResultQuickest(RoutePlanInfo info, HttpHeaders headers) {
        logger.info("[function name:{}][info:{}, headers:{}]","getRoutePlanResultQuickest",(info != null ? info.toString(): null), (headers != null ? headers.toString(): null));
        HttpEntity requestEntity = new HttpEntity(info, null);
        String route_plan_service_url = getServiceUrl("ts-route-plan-service");
        ResponseEntity<Response<ArrayList<RoutePlanResultUnit>>> re = restTemplate.exchange(
                route_plan_service_url + "/api/v1/routeplanservice/routePlan/quickestRoute",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<Response<ArrayList<RoutePlanResultUnit>>>() {
                });
        logger.info("[status code:{}, url:{} and type:{}]",re.getStatusCode(),
                route_plan_service_url + "/api/v1/routeplanservice/routePlan/quickestRoute","POST");

        return re.getBody().getData();
    }

    private ArrayList<RoutePlanResultUnit> getRoutePlanResultMinStation(RoutePlanInfo info, HttpHeaders headers) {
        logger.info("[function name:{}][info:{}, headers:{}]","getRoutePlanResultMinStation",(info != null ? info.toString(): null), (headers != null ? headers.toString(): null));
        HttpEntity requestEntity = new HttpEntity(info, null);
        String route_plan_service_url = getServiceUrl("ts-route-plan-service");
        ResponseEntity<Response<ArrayList<RoutePlanResultUnit>>> re = restTemplate.exchange(
                route_plan_service_url + "/api/v1/routeplanservice/routePlan/minStopStations",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<Response<ArrayList<RoutePlanResultUnit>>>() {
                });
        logger.info("[status code:{}, url:{} and type:{}]",re.getStatusCode(),
                route_plan_service_url + "/api/v1/routeplanservice/routePlan/minStopStations","POST");
        return re.getBody().getData();
    }

    private List<TripResponse> tripsFromHighSpeed(TripInfo info, HttpHeaders headers) {
        logger.info("[function name:{}][info:{}, headers:{}]","tripsFromHighSpeed",(info != null ? info.toString(): null), (headers != null ? headers.toString(): null));
        HttpEntity requestEntity = new HttpEntity(info, null);
        String travel_service_url=getServiceUrl("ts-travel-service");
        ResponseEntity<Response<List<TripResponse>>> re = restTemplate.exchange(
                travel_service_url + "/api/v1/travelservice/trips/left",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<Response<List<TripResponse>>>() {
                });
        logger.info("[status code:{}, url:{} and type:{}]",re.getStatusCode(),
                travel_service_url + "/api/v1/travelservice/trips/left","POST");
        return re.getBody().getData();
    }

    private ArrayList<TripResponse> tripsFromNormal(TripInfo info, HttpHeaders headers) {
        logger.info("[function name:{}][info:{}, headers:{}]","tripsFromNormal",(info != null ? info.toString(): null), (headers != null ? headers.toString(): null));

        HttpEntity requestEntity = new HttpEntity(info, null);
        String travel2_service_url=getServiceUrl("ts-travel2-service");
        ResponseEntity<Response<ArrayList<TripResponse>>> re = restTemplate.exchange(
                travel2_service_url + "/api/v1/travel2service/trips/left",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<Response<ArrayList<TripResponse>>>() {
                });
        logger.info("[status code:{}, url:{} and type:{}]",re.getStatusCode(),
                travel2_service_url + "/api/v1/travel2service/trips/left","POST");

        return re.getBody().getData();
    }

    public TrainType queryTrainTypeByName(String trainTypeName, HttpHeaders headers) {
        logger.info("[function name:{}][trainTypeName:{}, headers:{}]","queryTrainTypeByName",trainTypeName, (headers != null ? headers.toString(): null));
        HttpEntity requestEntity = new HttpEntity(null);
        String train_service_url=getServiceUrl("ts-train-service");
        ResponseEntity<Response> re = restTemplate.exchange(
                train_service_url + "/api/v1/trainservice/trains/byName/" + trainTypeName,
                HttpMethod.GET,
                requestEntity,
                Response.class);
        logger.info("[status code:{}, url:{} and type:{}]",re.getStatusCode(),
                train_service_url + "/api/v1/trainservice/trains/byName/" + trainTypeName,"GET");
        Response  response = re.getBody();

        return JsonUtils.conveterObject(response.getData(), TrainType.class);
    }

}
