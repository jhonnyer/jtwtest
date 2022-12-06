package com.security.jwt.security.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.security.jwt.security.IJWTService;
import com.security.jwt.security.JWTServiceImpl;


public class JWTAuthorizationFilter extends BasicAuthenticationFilter{
	
	@Autowired
	private IJWTService jwtService;

	public JWTAuthorizationFilter(AuthenticationManager authenticationManager, IJWTService jwtService) {
		super(authenticationManager);
		this.jwtService=jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		String header = request.getHeader(JWTServiceImpl.HEADER_STRING);
		if(!requiresAuthentication(header)) {
			chain.doFilter(request, response); //continua con la ejecucion del filtro si es verdadero y se sale del filtro con return
			return;
		}
		
		UsernamePasswordAuthenticationToken authentication = null;
		
		if(jwtService.validate(header)) {

			authentication=new UsernamePasswordAuthenticationToken(jwtService.getUsername(header), null, jwtService.getRoles(header)); //null corresponde al campo de credenciales que no se esta utilizando
			
			System.out.println(authentication);
			System.out.println(authentication.getCredentials());
			System.out.println(authentication.getName());
			System.out.println(authentication.getAuthorities());
			System.out.println(authentication.getPrincipal());
			System.out.println(authentication.getDetails());
		}
		
		SecurityContextHolder.getContext().setAuthentication(authentication); //permite autenticar al usuario dentro del request de la peticion
		chain.doFilter(request, response);  //continuamos con la peticion		
	}
	
	protected boolean requiresAuthentication(String header) {
		if (header==null || !header.startsWith(JWTServiceImpl.TOKEN_PREFIX)) {
			return false;
		}
		return true;
	}
	

}
