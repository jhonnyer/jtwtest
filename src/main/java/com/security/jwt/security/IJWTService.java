package com.security.jwt.security;

import java.io.IOException;
import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import io.jsonwebtoken.Claims;

public interface IJWTService {
	public String create(Authentication auth) throws IOException;   //Metodo utilizado para crear el token
	public boolean validate(String token);   //Metodo para validar el token
	public Claims getClaims(String token);   //Metodo para obtener los claims
	public String getUsername(String token); //metodo para obtener el username y los roles a partir del token
	public Collection<? extends GrantedAuthority> getRoles(String token) throws IOException;   //Metodo para obtener los roles
	public String resolve(String token);  //Metodo para resolver el token
}
