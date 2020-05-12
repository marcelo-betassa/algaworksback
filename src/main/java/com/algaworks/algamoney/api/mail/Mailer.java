package com.algaworks.algamoney.api.mail;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.algaworks.algamoney.api.model.Lancamento;
import com.algaworks.algamoney.api.model.Usuario;

@Component
public class Mailer {

	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private TemplateEngine thymeleaf;
	
//	@Autowired
//	private LancamentoRepository repo;
	
//	@EventListener
//	public void teste(ApplicationReadyEvent event) {
//		String template ="mail/aviso-lancamnetos-vencidos";
//		List<Lancamento> lancamentos = repo.findAll();
//		
//		Map<String, Object> variaveis = new HashMap<>();
//		variaveis.put("lancamentos", lancamentos);
//		
//		this.enviarEmail("mhbeta@gmail.com", Arrays.asList("mhbeta@gmail.com"),"Testando" , template , variaveis);
//		System.out.println("Terminado o envio de email.");
//	}
	
	public void avisarSobreLancamentosVencidos(List<Lancamento> vencidos, List<Usuario> destinatarios) {
		Map<String, Object> variaveis = new HashMap<>();
		variaveis.put("lancamentos", vencidos);
		
		List<String> emails = destinatarios.stream().map(u -> u.getEmail()).collect(Collectors.toList());
		
		this.enviarEmail("mhbeta@gmail.com", emails, "Lançamentos Vencidos", "mail/aviso-lancamnetos-vencidos", variaveis);
	}
	
	public void enviarEmail(String remetente, List<String> destinatarios, String assunto, String template, Map<String, Object> variaveis) {
		Context context = new Context(new Locale("pt", "BR"));
		
		variaveis.entrySet().forEach(e -> context.setVariable(e.getKey(), e.getValue()));
		
		String mensagem = thymeleaf.process(template, context);
		this.enviarEmail(remetente, destinatarios, assunto, mensagem);
	}
	
	public void enviarEmail(String remetente, List<String> destinatarios, String assunto, String mensagem) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper mimeHelper = new MimeMessageHelper(mimeMessage, "UTF-8");
			mimeHelper.setFrom(remetente);
			mimeHelper.setTo(destinatarios.toArray(new String[destinatarios.size()]));
			mimeHelper.setSubject(assunto);
			mimeHelper.setText(mensagem, true);
			mimeHelper.setPriority(1);
			mailSender.send(mimeMessage);
		} catch (MessagingException e) {
			e.printStackTrace();
			throw new RuntimeException("Problemas com envio de email ", e);
		}
	}
}
