package kr.co.tbell.nuguapi.domain.network.request;

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
public class NuguApiRequest {
	
	private String version;
	
	private String actionName;
	
	private String parameters;

	private String eventType;
	
	private String session;
	
	private String device;
	
	private String supportedInterfaces;
}
