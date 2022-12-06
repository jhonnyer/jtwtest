package com.security.jwt.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JWTServiceImpl implements IJWTService {
	
	public static final String SECRET=Base64Utils.encodeToString("4qhq8LrEBfYcaRHxhdb9zURb2rf8e7Ud".getBytes());
	public static final long EXPIRACION_DATE=2_592_000L; //tiempo de vida util del token  - > #dias*24horas*3600segundos;  en este caso 30 dias-
	public static final String TOKEN_PREFIX="Bearer ";
	public static final String HEADER_STRING="Authorization";

	@SuppressWarnings("deprecation")
	@Override
	public String create(Authentication auth) throws IOException {
		String username = ((User) auth.getPrincipal()).getUsername();

		Collection<? extends GrantedAuthority> roles = auth.getAuthorities();

		Claims claims = Jwts.claims();
		claims.put("authorities", new ObjectMapper().writeValueAsString(roles));

		return  Jwts.builder().setClaims(claims).setSubject(username)
				.signWith(SignatureAlgorithm.HS256, SECRET.getBytes())
				.setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + EXPIRACION_DATE))
				.compact();
	}

	@Override
	public boolean validate(String token) {
		try {
			getClaims(token);
			return true;
		}catch(JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public Claims getClaims(String token) {
		return Jwts.parser()
				.setSigningKey(SECRET.getBytes()) //obtenemos la llave con la key firmada
				.parseClaimsJws(resolve(token))
				.getBody();  //obtenemos el token
	}

	@Override
	public String getUsername(String token) {
		return getClaims(token).getSubject();  //Reutilizamos el claims para obtener el username del token
	}

	@Override
	public Collection<? extends GrantedAuthority> getRoles(String token) throws IOException {
		Object roles=getClaims(token).get("authorities");
		Collection<? extends GrantedAuthority> authorities = Arrays.asList(new ObjectMapper()
				.addMixIn(SimpleGrantedAuthority.class, SimpleGrantedAuthorityMixin.class)  //para añadirle los roles al token de autenticacion
				.readValue(roles.toString().getBytes(), SimpleGrantedAuthority[].class));
		return authorities;
	}

	@Override
	public String resolve(String token) {
		if(token!=null && token.startsWith(TOKEN_PREFIX)) {
			return token.replace(TOKEN_PREFIX, "");
		}
		return null;
	}

}
