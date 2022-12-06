package com.security.jwt.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.security.jwt.entity.Usuario;
import com.security.jwt.security.IJWTService;

@RestController
public class LoginController {
	@Autowired
	public AuthenticationManager autManager;
	
	@Autowired
	private IJWTService jwtService;
	
	@PostMapping("/auth/login")
	public ResponseEntity<Map<String, Object>> login(@RequestBody @Valid Usuario request){
		try {
			Authentication authenticacion= autManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(),request.getPassword()));
			User user = (User) authenticacion.getPrincipal();
			String token=jwtService.create(authenticacion);
//			AuthResponse response= new AuthResponse(user.getUsername(),token);
			Map<String, Object> map=new HashMap<>();
			map.put("token", token);
			map.put("usuario", user);
			return new ResponseEntity<Map<String, Object>> (map,HttpStatus.OK);
		}catch(BadCredentialsException ex) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}
	
}
