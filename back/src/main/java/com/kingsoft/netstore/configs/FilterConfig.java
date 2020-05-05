package com.kingsoft.netstore.configs;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * filter 只有通过FilterRegistrationBean配置才好使
 * 通过@WebFilter配置的，需要有@ServletComponentScan扫描
 * 直接在filter上加@Configuration注解的话urlPatterns不起作用
 * 
 * @author JACK
 *
 */
@Configuration
public class FilterConfig {

	@Bean
	public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
		FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(getCorsFilter());
		registration.addUrlPatterns("/*");
		registration.setName("corsFilter");
		registration.setOrder(20);
		return registration;
	}
	
	@Bean
	public FilterRegistrationBean<LoginFilter> loginFilterRegistration() {
		FilterRegistrationBean<LoginFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(getLoginFilter());
		registration.addUrlPatterns("/*");
		registration.setName("loginFilter");
		registration.setOrder(25);
		return registration;
	}

	@Bean
	public CorsFilter getCorsFilter() {
		return new CorsFilter();
	}
	
	@Bean
	public LoginFilter getLoginFilter() {
		return new LoginFilter();
	}
}
