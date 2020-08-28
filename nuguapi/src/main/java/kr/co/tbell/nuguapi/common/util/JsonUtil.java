package kr.co.tbell.nuguapi.common.util;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.co.tbell.nuguapi.domain.network.request.NuguApiRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtil {
	
	private JsonUtil() {}
	
	public static NuguApiRequest parseRequest(String json) {
		
		ObjectMapper mapper = new ObjectMapper();
		
		NuguApiRequest request = new NuguApiRequest();
		Map<String, Object> param = new HashMap<String, Object>();
		
		try {
			param = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		log.info(param.toString());
		
		return null;
	}

	
}
