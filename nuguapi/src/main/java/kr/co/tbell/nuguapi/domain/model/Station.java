package kr.co.tbell.nuguapi.domain.model;

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
public class Station {
	
	private String stationName;
	
	private String lineNum;
	
	private String inOut;
	
	private String stationCd;
}
