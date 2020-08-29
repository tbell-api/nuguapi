package kr.co.tbell.nuguapi.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
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
		Station startStation = getStation("마곡", "05호선");
		String inOut = getInOut("상행");
		List<Timetable> timetableList = getTimeTable(startStation, inOut);
		NuguApiResponse response = new NuguApiResponse();
		
		
		StationTimetable stationTimetable = new StationTimetable();
		stationTimetable.setStationName(startStation.getStationNm());
		stationTimetable.setLineNum(startStation.getLineNum());
		stationTimetable.setLineInOut(inOut);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		log.info(result);
		
		return result;
	}
	
	private List<Timetable> getTimeTable(Station station, String inOut) {

        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
        List<Timetable> timeTableList = new ArrayList<Timetable>();

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
		String path = "/" + serviceKey + "/json/" + timetable + "/1/500/"  + station.getStationCd() + "/" + inOut + "/" + weekday;
		log.info(path);
		
		String uri =  UriComponentsBuilder.newInstance()
						.scheme("http")
						.host(endPoint)
						.path(path)
						.build()
						.toString();
		
		String timetable = sendApiRequest(uri);
		log.info(timetable);

		// 시간 기준 5개 시간표 잘라서 List에 담기
		// 1. 현재 시간이 16시 30분이라면 가장 근접한 타임 찾기 - LocalTime에 비교 함수
		// 2. 그 시간대부터 다음 5개의 시간표 
		// List<TimeTable>로 만들어서 return
		try {
			JsonNode rootNode = objectMapper.readTree(timetable);
			ArrayNode arrayNode = (ArrayNode) rootNode.get("SearchSTNTimeTableByIDService").get("row");
			int count = 0;
			if (arrayNode.isArray()) {
				for (JsonNode json : arrayNode) {
					// 5개를 얻기 위한 카운트
					if (count > 5) {
						break;
					}
					
					// 24:00:00 표기를 00:00:00으로 변경하고 Day 하루 증가
					LocalTime leftTime = LocalTime.parse(json.get("LEFTTIME").textValue(), timeFormat);
					LocalDateTime leftDateTime;
					if (leftTime.get(ChronoField.CLOCK_HOUR_OF_DAY) == 24) {
						LocalTime nextDayTime = LocalTime.now().withHour(0);
						leftDateTime = LocalDateTime.now().plusDays(1).with(nextDayTime);
						log.info(leftDateTime.toString());
					} else {
						leftDateTime = LocalDateTime.now().with(leftTime);
						log.info(leftDateTime.toString());
					}
					
					// 기준 시간 보다 이후인 시간표를 얻고 카운트 증가
					if (localDateTime.isBefore(leftDateTime) && json.get("LINE_NUM").textValue().equals(station.getLineNum())) {
						log.info(localDateTime.toString());
						log.info(leftTime.toString());
						log.info(count + "번 시간표");
						count++;
						Timetable time = new Timetable();
						time.setLeftTime(json.get("LEFTTIME").textValue());
						time.setSubwayEName(json.get("SUBWAYENAME").textValue());
						timeTableList.add(time);
					}
				}
			} else {
				log.info("검색 결과가 없습니다.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		log.info(timeTableList.toString());
		return timeTableList;
	}
	
	private String getInOut(String inOut) {
		
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
	
	
	
	private Station getStation(String stationName, String lineNum) {
		
		Station station = new Station();
		String path = "/" + serviceKey + "/json/" + stationInfo + "/1/100/%20/"  + stationName;
		log.info(path);
		
		String uri =  UriComponentsBuilder.newInstance()
						.scheme("http")
						.host(endPoint)
						.path(path)
						.build()
						.toString();
		
		String stationResult = sendApiRequest(uri);
		log.info(stationResult);

		try {
			JsonNode rootNode = objectMapper.readTree(stationResult);
			ArrayNode arrayNode = (ArrayNode) rootNode.get("SearchSTNBySubwayLineInfo").get("row");
			// issue: 역이름이 같고, 호선이 같은 역을 찾아내야함
			if (arrayNode.isArray()) {
				for (JsonNode json : arrayNode) {
					if (json.get("STATION_NM").textValue().equals(stationName) && json.get("LINE_NUM").textValue().equals(lineNum)) {
						station.setStationNm(json.get("STATION_NM").textValue());
						station.setLineNum(json.get("LINE_NUM").textValue());
						station.setStationCd(json.get("STATION_CD").textValue());
					}
				}
			} else {
				log.info("검색 결과가 없습니다.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		log.info(station.toString());
		return station;
	}
	
	
	
	private String getTimetable(String stationName, String inOut) {
		
		return null;
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
	
}
