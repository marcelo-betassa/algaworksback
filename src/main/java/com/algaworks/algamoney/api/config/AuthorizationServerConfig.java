package com.algaworks.algamoney.api.config;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import com.algaworks.algamoney.api.config.token.CustomTokeEnhancer;

@Profile(value = "oauth-security")
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.inMemory()
			.withClient("angular")
			.secret("$2a$10$yOIeWvHdIeNEPA6OVXRUVOkaTZsmhqyfqqmyH53rO1duIpqRy133a") //@ngul@r0
			
			.scopes("read", "write")
			.authorizedGrantTypes("password" , "refresh_token")
			.accessTokenValiditySeconds(1800)
			.refreshTokenValiditySeconds(86400)
		.and()
			.withClient("mobile")
			.secret("$2a$10$XABkCmiqVzr5B6nKjf93VOGMQSlSSgZWqXEOt4wLpR5qSHmcPv6RG")
			.scopes("read")
			.authorizedGrantTypes("password" , "refresh_token")
			.accessTokenValiditySeconds(1800)
			.refreshTokenValiditySeconds(86400);
	}
	
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		
		TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
		tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer(), accessTokenConverter()));
		
		
//		endpoints
//		.tokenStore(tokenStore())
//		.accessTokenConverter(this.accessTokenConverter())
//		.reuseRefreshTokens(false)
//		.userDetailsService(this.userDetailsService)
//		.authenticationManager(this.authenticationManager);
		
		endpoints
			.tokenStore(tokenStore())
			.tokenEnhancer(tokenEnhancerChain)
			.reuseRefreshTokens(false)
			.authenticationManager(this.authenticationManager);
	}
	

	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		JwtAccessTokenConverter accessTokenConverter = new JwtAccessTokenConverter();
		accessTokenConverter.setSigningKey("spring-angular");
		return accessTokenConverter;
	}

	@Bean
	public TokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
	}
	
	public TokenEnhancer tokenEnhancer() {
		return new CustomTokeEnhancer();
	}
	
}