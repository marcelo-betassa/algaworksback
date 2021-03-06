package com.algaworks.algamoney.api.repository.lancamento;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.algaworks.algamoney.api.dto.LancamentoEstatisticaCategoria;
import com.algaworks.algamoney.api.dto.LancamentoEstatisticaDia;
import com.algaworks.algamoney.api.dto.LancamentoEstatisticaPessoa;
import com.algaworks.algamoney.api.model.Categoria_;
import com.algaworks.algamoney.api.model.Lancamento;
import com.algaworks.algamoney.api.model.Lancamento_;
import com.algaworks.algamoney.api.model.Pessoa_;
import com.algaworks.algamoney.api.repository.filter.LancamentoFilter;
import com.algaworks.algamoney.api.repository.projection.ResumoLancamento;

public class LancamentoRepositoryImpl implements LancamentoRepositoryQuery {

	@PersistenceContext
	private EntityManager entityManager;
	
	@Override
	public Page<Lancamento> filtrar(LancamentoFilter lancamentoFilter, Pageable pageable) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Lancamento> criteriaQuery = criteriaBuilder.createQuery(Lancamento.class);
		
		Root<Lancamento> root = criteriaQuery.from(Lancamento.class);
		
		Predicate[] predicates = criarRestricoes(lancamentoFilter , criteriaBuilder , root);
		criteriaQuery.where(predicates);
		
		TypedQuery<Lancamento> query = entityManager.createQuery(criteriaQuery);
		adicionarRestricoesDePaginacao(query , pageable);
		return new PageImpl<>(query.getResultList(), pageable, total(lancamentoFilter));
	}

	@Override
	public Page<ResumoLancamento> resumirLancamento(LancamentoFilter lancamentoFilter, Pageable pageable) {

		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ResumoLancamento> criteria = builder.createQuery(ResumoLancamento.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		
		criteria.select(builder.construct(ResumoLancamento.class
				, root.get(Lancamento_.CODIGO) , root.get(Lancamento_.DESCRICAO)
				, root.get(Lancamento_.DATA_VENCIMENTO) , root.get(Lancamento_.DATA_PAGAMENTO)
				, root.get(Lancamento_.VALOR) , root.get(Lancamento_.TIPO_LANCAMENTO)
				, root.get(Lancamento_.CATEGORIA).get(Categoria_.NOME)
				, root.get(Lancamento_.PESSOA).get(Pessoa_.NOME)));
		
		Predicate[] predicates = criarRestricoes(lancamentoFilter , builder , root);
		criteria.where(predicates);
		
		TypedQuery<ResumoLancamento> query = entityManager.createQuery(criteria);
		adicionarRestricoesDePaginacao(query , pageable);
		return new PageImpl<>(query.getResultList(), pageable, total(lancamentoFilter));
		
	}


	private Predicate[] criarRestricoes(LancamentoFilter lancamentoFilter, CriteriaBuilder criteriaBuilder,
			Root<Lancamento> root) {
		
		List<Predicate> predicates = new ArrayList<>();
		
		if (!StringUtils.isEmpty(lancamentoFilter.getDescricao())) {
			predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(Lancamento_.DESCRICAO)), "%"+lancamentoFilter.getDescricao().toLowerCase()+"%"));		
		}
		
		if (lancamentoFilter.getDataVencimentoDe() != null) {
			predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(Lancamento_.DATA_VENCIMENTO), lancamentoFilter.getDataVencimentoDe()));
			
		}

		if (lancamentoFilter.getDataVencimentoAte() != null) {
			predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(Lancamento_.DATA_VENCIMENTO), lancamentoFilter.getDataVencimentoAte()));
			
		}

		return predicates.toArray(new Predicate[predicates.size()]);
	}

	
	private Long total(LancamentoFilter lancamentoFilter) {

		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);
		criteria.select(builder.count(root));
		
		return entityManager.createQuery(criteria).getSingleResult();
	}

	private void adicionarRestricoesDePaginacao(TypedQuery<?> query, Pageable pageable) {

		int paginaAtual = pageable.getPageNumber();
		int totalRegistrosPorPagina = pageable.getPageSize();
		int primeiroRegistroDaPagina = paginaAtual * totalRegistrosPorPagina;
		query.setFirstResult(primeiroRegistroDaPagina);
		query.setMaxResults(totalRegistrosPorPagina);
		
	}

	@Override
	public List<LancamentoEstatisticaCategoria> porCategoria(LocalDate mesReferencia) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<LancamentoEstatisticaCategoria> criteriaQuery = criteriaBuilder
				.createQuery(LancamentoEstatisticaCategoria.class);
		Root<Lancamento> root = criteriaQuery.from(Lancamento.class);
		criteriaQuery.select(criteriaBuilder.construct(LancamentoEstatisticaCategoria.class,
				root.get(Lancamento_.categoria), criteriaBuilder.sum(root.get(Lancamento_.valor))));

		LocalDate primeiroDia = mesReferencia.withDayOfMonth(1);
		LocalDate ultimoDia = mesReferencia.withDayOfMonth(mesReferencia.lengthOfMonth());

		criteriaQuery.where(criteriaBuilder.greaterThanOrEqualTo(root.get(Lancamento_.DATA_VENCIMENTO), primeiroDia),
				criteriaBuilder.lessThanOrEqualTo(root.get(Lancamento_.DATA_VENCIMENTO), ultimoDia));

		criteriaQuery.groupBy(root.get(Lancamento_.CATEGORIA));

		TypedQuery<LancamentoEstatisticaCategoria> typedQuery = entityManager.createQuery(criteriaQuery);

		return typedQuery.getResultList();
	}

	@Override
	public List<LancamentoEstatisticaDia> porDia(LocalDate mesReferencia) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<LancamentoEstatisticaDia> criteriaQuery = criteriaBuilder
				.createQuery(LancamentoEstatisticaDia.class);
		Root<Lancamento> root = criteriaQuery.from(Lancamento.class);
		criteriaQuery.select(criteriaBuilder.construct(LancamentoEstatisticaDia.class,
				root.get(Lancamento_.TIPO_LANCAMENTO), root.get(Lancamento_.DATA_VENCIMENTO), 
				criteriaBuilder.sum(root.get(Lancamento_.valor))));

		LocalDate primeiroDia = mesReferencia.withDayOfMonth(1);
		LocalDate ultimoDia = mesReferencia.withDayOfMonth(mesReferencia.lengthOfMonth());
	
		criteriaQuery.where(criteriaBuilder.greaterThanOrEqualTo(root.get(Lancamento_.DATA_VENCIMENTO), primeiroDia),
				criteriaBuilder.lessThanOrEqualTo(root.get(Lancamento_.DATA_VENCIMENTO), ultimoDia));

		criteriaQuery.groupBy(root.get(Lancamento_.TIPO_LANCAMENTO), root.get(Lancamento_.DATA_VENCIMENTO));

		TypedQuery<LancamentoEstatisticaDia> typedQuery = entityManager.createQuery(criteriaQuery);

		return typedQuery.getResultList();
		
	}

	@Override
	public List<LancamentoEstatisticaPessoa> porPessoa(LocalDate inicio, LocalDate fim) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<LancamentoEstatisticaPessoa> criteriaQuery = criteriaBuilder
				.createQuery(LancamentoEstatisticaPessoa.class);
		Root<Lancamento> root = criteriaQuery.from(Lancamento.class);
		criteriaQuery.select(criteriaBuilder.construct(LancamentoEstatisticaPessoa.class,
				root.get(Lancamento_.TIPO_LANCAMENTO), root.get(Lancamento_.PESSOA), 
				criteriaBuilder.sum(root.get(Lancamento_.valor))));
	
		criteriaQuery.where(criteriaBuilder.greaterThanOrEqualTo(root.get(Lancamento_.DATA_VENCIMENTO), inicio),
				criteriaBuilder.lessThanOrEqualTo(root.get(Lancamento_.DATA_VENCIMENTO), fim));

		criteriaQuery.groupBy(root.get(Lancamento_.TIPO_LANCAMENTO), root.get(Lancamento_.PESSOA));

		TypedQuery<LancamentoEstatisticaPessoa> typedQuery = entityManager.createQuery(criteriaQuery);

		return typedQuery.getResultList();
	}


}
