package kr.co.tbell.nuguapi.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import kr.co.tbell.nuguapi.common.util.JsonUtil;
import kr.co.tbell.nuguapi.domain.model.Station;
import kr.co.tbell.nuguapi.domain.model.Timetable;
import kr.co.tbell.nuguapi.domain.network.request.NuguApiRequest;
import kr.co.tbell.nuguapi.service.NuguApiService;

@RestController
@RequestMapping(value="/api")
public class NuguApiController {

	@Autowired
	private NuguApiService nuguApiService;
	
	@RequestMapping(value="/getTimetable", method=RequestMethod.POST)
	public String getTimetable(@RequestBody String request) {
		
//		JsonUtil.parseRequest(request);
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
