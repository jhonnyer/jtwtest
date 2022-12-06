package com.security.jwt.controllers;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;

import com.security.jwt.entity.Cliente;
import com.security.jwt.interfaceDao.IClienteService;
import com.security.jwt.interfaceDao.IUploadFileService;

@RestController
@RequestMapping(value="/cliente")
@SessionAttributes("cliente")
public class ClienteController {
	
	@Autowired
	private IClienteService clienteService;
	
	@Autowired
	private IUploadFileService uploadFileService;
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@GetMapping(value="/uploads/{filename:.+}")
	public ResponseEntity<Resource> verFoto(@PathVariable String filename) {

		Resource recurso = null;
		try {
			recurso = uploadFileService.load(filename);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +  recurso.getFilename() +"\"")
				.body(recurso);
	}

	@GetMapping(value = "/ver/{id}")
	public ResponseEntity<Cliente> ver(@PathVariable(value = "id") Long id) {

		Cliente cliente = clienteService.findOne(id);
		if (cliente == null) {
			return new ResponseEntity<Cliente> (new Cliente(), HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Cliente> (cliente, HttpStatus.ACCEPTED);
	}

	@GetMapping(value = "/listar")
	@Secured("ROLE_ADMIN")
	public Page<Cliente> listar(@RequestParam(name="page", defaultValue="0") int page) {
		Pageable pageRequest = PageRequest.of(page, 4);
		Page<Cliente> clientes = clienteService.findAll(pageRequest);
		return clientes;
	}

	@GetMapping(value = "/form/{id}")
	public ResponseEntity<Cliente> editar(@PathVariable(value = "id") Long id) {
		Cliente cliente = null;
		if (id > 0) {
			cliente = clienteService.findOne(id);
			if (cliente == null) {
				return new ResponseEntity<Cliente> (new Cliente(), HttpStatus.NOT_FOUND);
			}
		} else {
			return  new ResponseEntity<Cliente> (new Cliente(), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<Cliente> (cliente, HttpStatus.CREATED);
	}

	@PostMapping(value = "/form")
	public ResponseEntity<Cliente> guardar(@Valid Cliente cliente, BindingResult result,
			@RequestParam("file") MultipartFile foto, SessionStatus status) {
		
		if (result.hasErrors()) {
			return new ResponseEntity<Cliente> (new Cliente(), HttpStatus.NOT_FOUND);
		}
		
		if (!foto.isEmpty()) {

			if (cliente.getId() != null && cliente.getId() > 0 && cliente.getFoto() != null
					&& cliente.getFoto().length() > 0) {
				uploadFileService.delete(cliente.getFoto());
			}
			
			String uniqueFilename = null;
			try {
				uniqueFilename = uploadFileService.copy(foto);
			} catch (IOException e) {
				e.printStackTrace();
			}
			cliente.setFoto(uniqueFilename);
		}
		
		String mensajeFlash = (cliente.getId() != null) ? "Cliente editado con éxito!" : "Cliente creado con éxito!";
		clienteService.save(cliente);
		status.setComplete();
		log.info(mensajeFlash);
		return new ResponseEntity<Cliente> (cliente, HttpStatus.CREATED);
	}

	@GetMapping(value = "/eliminar/{id}")
	public void eliminar(@PathVariable(value = "id") Long id) {
		if (id > 0) {
			Cliente cliente = clienteService.findOne(id);
			clienteService.delete(id);
			
			if(uploadFileService.delete(cliente.getFoto())) {
					log.info("Foto " + cliente.getFoto() + " eliminada con exito!");
			}else {
				log.info("Error, imagen " + cliente.getFoto() + " no puede ser eliminada");
			}
			
		}
	}
}