package kr.co.tbell.nuguapi.domain.network.response;

import kr.co.tbell.nuguapi.domain.model.StationTimetable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Accessors(chain = true)
public class NuguApiResponse {

	private String version;
	
	private String resultCode;
	
	private StationTimetable output; 
}
