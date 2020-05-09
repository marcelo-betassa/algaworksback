package com.algaworks.algamoney.api.model;

public enum TipoLancamento {

	DESPESA("Despesa"), 
	RECEITA("Receita");

	private final String descricao;

	private TipoLancamento(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}

}
