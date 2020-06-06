package com.algaworks.algamoney.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.algaworks.algamoney.api.model.Cidade;
import com.algaworks.algamoney.api.repository.CidadeRepository;

@RestController
@RequestMapping("/cidades")
public class CidadeController {

	@Autowired
	private CidadeRepository cidadeRepository;
	
	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public List<Cidade> pesquisarCidade(@RequestParam Long codigoEstado) {
		return cidadeRepository.findByEstadoCodigo(codigoEstado);
	}
	
}
