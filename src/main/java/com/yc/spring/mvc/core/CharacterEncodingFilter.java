package com.yc.spring.mvc.core;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("all")
@WebFilter( value = "/*" , filterName="CharacterEncodingFilter" , initParams = {@WebInitParam(name = "encoding" , value = "utf-8")})
public class CharacterEncodingFilter implements Filter {

	private String encoding = "utf-8" ;
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest)request ;
		HttpServletResponse resp = (HttpServletResponse)response ;
		
		req.setCharacterEncoding(encoding);
		resp.setCharacterEncoding(encoding);
		
		chain.doFilter(req, resp);
	}

	public void destroy() {

	}

	public void init(FilterConfig config) throws ServletException {
		String temp = config.getInitParameter("encoding");
		
		if ( temp == null || "".equals(temp) ) {
			return  ;
		}
		
		encoding = temp ;
	}

}