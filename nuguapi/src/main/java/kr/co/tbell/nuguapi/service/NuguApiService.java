package kr.co.tbell.nuguapi.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import kr.co.tbell.nuguapi.domain.model.Station;
import kr.co.tbell.nuguapi.domain.model.StationTimetable;
import kr.co.tbell.nuguapi.domain.model.Timetable;
import kr.co.tbell.nuguapi.domain.network.request.NuguApiRequest;
import kr.co.tbell.nuguapi.domain.network.response.NuguApiResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NuguApiService {
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private ObjectMapper objectMapper;

	@Value("${seoul.open.api.endpoint}")
	private String endPoint;

	@Value("${seoul.open.api.servicekey}")
	private String serviceKey;
	
	@Value("${seoul.open.api.stationinfo}")
	private String stationInfo;
	
	@Value("${seoul.open.api.timetable}")
	private String timetable;

	
	public String getData(NuguApiRequest request) {

		String result = "";
		String reqJson = "";
		Station reqStation = new Station();
		NuguApiResponse response = new NuguApiResponse();
		
		try {
			reqJson = objectMapper.writeValueAsString(request);
			
			JsonNode rootNode = objectMapper.readTree(reqJson);
			reqStation.setStationName(rootNode.get("action").get("parameters").get("stationName").get("value").textValue());
			reqStation.setInOut(rootNode.get("action").get("parameters").get("inOut").get("value").textValue());
			reqStation.setLineNum(rootNode.get("action").get("parameters").get("lineNum").get("value").textValue());
			reqStation.setStationCd(getStation(reqStation));
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		String inOut = getInOutCode(reqStation.getInOut());
		List<Timetable> timetableList = getTimeTable(reqStation, inOut);
		
		StationTimetable stationTimetable = new StationTimetable();
		stationTimetable.setStationName(reqStation.getStationName());
		stationTimetable.setLineNum(reqStation.getLineNum());
		stationTimetable.setLineInOut(reqStation.getInOut());
		stationTimetable.setFirstTime(timetableList.get(0).getLeftTime());
		stationTimetable.setSecondTime(timetableList.get(1).getLeftTime());
		stationTimetable.setThirdTime(timetableList.get(2).getLeftTime());
		stationTimetable.setFourthTime(timetableList.get(3).getLeftTime());
		stationTimetable.setFifthTime(timetableList.get(4).getLeftTime());
		
		response.setResultCode("OK");
		response.setVersion("2.0");
		response.setOutput(stationTimetable);
		
		try {
			result = objectMapper.writeValueAsString(response);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	private List<Timetable> getTimeTable(Station station, String inOut) {

        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
        List<Timetable> timetableList = new ArrayList<Timetable>();

		// 시간구하기
		LocalDateTime localDateTime = LocalDateTime.now().withNano(0);
		
		// 요일구하기
		LocalDate localDate = LocalDate.now();
		int weeknum = localDate.getDayOfWeek().getValue();
		String weekday = "";
		if (weeknum >= 6) {
			weekday = "2";
		} else if (weeknum < 6) {
			weekday = "1";
		}
		
		// URL 생성 -> (station.getStationCd(), 넣어야함)
		String path = "/" + serviceKey + "/json/" + timetable + "/1/500/"  + station.getStationCd() + "/" + weekday + "/" + inOut;
		
		String uri =  UriComponentsBuilder.newInstance()
						.scheme("http")
						.host(endPoint)
						.path(path)
						.build()
						.toString();
		
		String timetable = sendApiRequest(uri);

		// 시간표 데이터 파싱
		try {
			JsonNode rootNode = objectMapper.readTree(timetable);
			ArrayNode arrayNode = (ArrayNode) rootNode.get("SearchSTNTimeTableByIDService").get("row");
			int count = 0;
			int index = 0;
			int arraySize = arrayNode.size();
			if (arrayNode.isArray()) {
				for (JsonNode json : arrayNode) {
					index++;
					
					if (index == arraySize) {
						if (count < 4) {
							for(int i=0; i<5-count; i++) {
								Timetable time = new Timetable();
								time.setLeftTime("열차 없음");
								time.setSubwayEName("열차 없음");
								timetableList.add(time);
							}
						}
					}
					
					// 5개를 얻기 위한 카운트
					if (count > 5) {
						break;
					}
					
					// 24:00:00 표기를 00:00:00으로 변경하고 Day 하루 증가
					LocalTime leftTime = LocalTime.parse(json.get("LEFTTIME").textValue(), timeFormat);
					LocalDateTime leftDateTime;
					if (leftTime.getHour() == 24) {
						LocalTime nextDayTime = LocalTime.now().withHour(0);
						leftDateTime = LocalDateTime.now().plusDays(1).with(nextDayTime);
					} else {
						leftDateTime = LocalDateTime.now().with(leftTime);
					}
					
					// 기준 시간 보다 이후인 시간표를 얻고 카운트 증가
					if (localDateTime.isBefore(leftDateTime) && json.get("LINE_NUM").textValue().equals(station.getLineNum())) {
						count++;
						Timetable time = new Timetable();
						time.setLeftTime(convertTime(json.get("LEFTTIME").textValue()));
						time.setSubwayEName(json.get("SUBWAYENAME").textValue());
						timetableList.add(time);
					}
				}
			} else {
				log.info("검색 결과가 없습니다.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		log.info(timetableList.toString());
		return timetableList;
	}
	
	
	private String getStation(Station station) {
		
		String result = "";
		String path = "/" + serviceKey + "/json/" + stationInfo + "/1/100/%20/"  + station.getStationName();
		
		String uri =  UriComponentsBuilder.newInstance()
						.scheme("http")
						.host(endPoint)
						.path(path)
						.build()
						.toString();
		
		String stationResult = sendApiRequest(uri);

		// 역정보 데이터 파싱하여 코드 가져오기
		try {
			JsonNode rootNode = objectMapper.readTree(stationResult);
			ArrayNode arrayNode = (ArrayNode) rootNode.get("SearchSTNBySubwayLineInfo").get("row");

			if (arrayNode.isArray()) {
				for (JsonNode json : arrayNode) {
					if (json.get("STATION_NM").textValue().equals(station.getStationName()) && json.get("LINE_NUM").textValue().equals(station.getLineNum())) {
						result = json.get("STATION_CD").textValue();
					}
				}
			} else {
				log.info("검색 결과가 없습니다.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	
	private String sendApiRequest(String uri) {
		String result = "";
		
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		headers.set(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
		
		HttpEntity<?> httpEntity = new HttpEntity<>(headers);
		
		ResponseEntity<String> exchangeResult = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class);
		result = exchangeResult.getBody();
		
		return result;
	}

	
	private String getInOutCode(String inOut) {
		
		String inOutGubun = "";
		
		if (inOut.equals("상행")) {
			inOutGubun = "1";
		} else if(inOut.equals("하행")) {
			inOutGubun = "2";
		} else if(inOut.equals("내선")) {
			inOutGubun = "1";
		} else if(inOut.equals("외선")) {
			inOutGubun = "2";
		}
		
		return inOutGubun;
	}
	
	
	private String convertTime(String time) {
		
		String result = "";
		
		if (!time.equals("열차 없음")) {
			result = time.substring(0, 2) + "시" + time.substring(3, 5) + "분";
		} 
		
		return result;
	}
	
}
