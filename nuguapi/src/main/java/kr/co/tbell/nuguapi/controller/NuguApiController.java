package kr.co.tbell.nuguapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import kr.co.tbell.nuguapi.domain.network.request.NuguApiRequest;
import kr.co.tbell.nuguapi.service.NuguApiService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value="/api")
public class NuguApiController {

	@Autowired
	private NuguApiService nuguApiService;
	
	@RequestMapping(value="/timetable", method=RequestMethod.POST)
	public String getTimetable(@RequestBody String request) {
		
//		JsonUtil.parseRequest(request);
		log.info(request);
		NuguApiRequest testRequest = new NuguApiRequest();
//		
		String result = nuguApiService.getData(testRequest);
		
//		Station station = new Station();
//		List<Timetable> timeList =  nuguApiService.getTimeTable(station, "1");
		
		return result;
	}
	
	@RequestMapping(value="/health", method=RequestMethod.GET)
	public String health() {
		
		return "200 ok";
	}
}
