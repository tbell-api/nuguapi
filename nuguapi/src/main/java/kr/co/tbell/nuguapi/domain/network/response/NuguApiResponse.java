package kr.co.tbell.nuguapi.domain.network.response;

import kr.co.tbell.nuguapi.domain.model.StationTimetable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NuguApiResponse {

	private String version;
	
	private String resultCode;
	
	private StationTimetable output; 
}
