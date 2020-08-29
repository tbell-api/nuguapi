package kr.co.tbell.nuguapi.domain.network;

import kr.co.tbell.nuguapi.domain.model.Timetable;
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
public class ResponseParam {

	private String type;
	
	private String value;
}
