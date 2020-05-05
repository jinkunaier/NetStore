package com.kingsoft.netstore.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	private Logger LOG = LoggerFactory.getLogger(getClass());

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 把web路径映射出去，前端程序以后会打包到当前路径下面
		LOG.info("/**映射路径：" + ResourceUtils.CLASSPATH_URL_PREFIX + "/web/");
		registry.addResourceHandler("/**")
				.addResourceLocations(ResourceUtils.CLASSPATH_URL_PREFIX + "/web/");
	}

	@Bean
	public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
		RestTemplate template = new RestTemplate(factory);
		return template;
	}

	@Bean
	public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setReadTimeout(60000);// 单位为ms
		factory.setConnectTimeout(30000);// 单位为ms
		return factory;
	}
}
