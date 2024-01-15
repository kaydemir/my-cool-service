package com.swisscom.mycoolservice.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

import com.swisscom.mycoolservice.properties.ApplicationProperties.User;
import com.swisscom.mycoolservice.servicesimpl.ConfigurationService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private ConfigurationService configurationService;

    /**
     * This method checks the access information of users.
     * */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .httpBasic()
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/users").access("@opaAuthorizationService.checkAuthorization('POST')")
                .and()
                .csrf().disable()
                .formLogin().disable();
    }

    /**
     * This method provide ignore to swagger configuration.
     * */
    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/v2/api-docs",
                "/configuration/ui",
                "/swagger-resources/**",
                "/configuration/security",
                "/swagger-ui.html",
                "/webjars/**");
    }

    /**
     * This method allows users to login the system.
     * */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> builder = auth.inMemoryAuthentication();
        Map<String, User> users = configurationService.getUsers();
        for (Map.Entry<String, User> userEntry : users.entrySet()) {
            String userName = userEntry.getKey();
            String password = userEntry.getValue().getPassword();
            String[] authorities = userEntry.getValue().getConfig().getRoles();
            if (userEntry.getValue().getConfig().getRoles() == null) {
                authorities = new String[]{};
                userEntry.getValue().getConfig().setRoles(authorities);
            }
            builder.withUser(userName).password(password).authorities(authorities);
        }

    }

}
