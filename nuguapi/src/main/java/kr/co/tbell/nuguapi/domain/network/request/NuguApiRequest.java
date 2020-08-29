package kr.co.tbell.nuguapi.domain.network.request;

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
public class NuguApiRequest {
	
	private String version;
	
	private Object action;
	
	private Object event;
	
	private Object context;
}
