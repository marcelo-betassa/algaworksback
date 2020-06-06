package com.algaworks.algamoney.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.algaworks.algamoney.api.model.Estado;
import com.algaworks.algamoney.api.repository.EstadoRepository;

@RestController
@RequestMapping("/estados")
public class EstadoController {

	@Autowired
	private EstadoRepository estadoRepository;
	

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public List<Estado> listarEstados() {
		return estadoRepository.findAll();
	}
	
}
