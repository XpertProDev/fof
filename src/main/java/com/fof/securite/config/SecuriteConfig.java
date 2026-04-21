package com.fof.securite.config;

import com.fof.securite.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableMethodSecurity
@Configuration
public class SecuriteConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            // Fichiers publics servis depuis resources/static (URLs renvoyées en photoUrl, etc.)
            .requestMatchers(HttpMethod.GET, "/clientUpload/**", "/userUpload/**", "/employeUpload/**", "/entrepriseUpload/**", "/employePieceUpload/**").permitAll()
            .anyRequest().authenticated()
        )
        .exceptionHandling(eh -> eh
            .authenticationEntryPoint((req, res, ex) -> {
              res.setStatus(401);
              res.setContentType("application/json");
              res.getWriter().write("{\"message\":\"Non authentifié\"}");
            })
            .accessDeniedHandler((req, res, ex) -> {
              res.setStatus(403);
              res.setContentType("application/json");
              res.getWriter().write("{\"message\":\"Accès refusé\"}");
            })
        )
        .addFilterBefore(new FiltreJwt(jwtService), UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService(com.fof.securite.service.UtilisateurDetailsService utilisateurDetailsService) {
    return utilisateurDetailsService;
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOriginPattern("http://localhost:4200");
    config.addAllowedHeader(CorsConfiguration.ALL);
    config.addAllowedMethod(CorsConfiguration.ALL);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  static class FiltreJwt extends OncePerRequestFilter {
    private final JwtService jwtService;

    FiltreJwt(JwtService jwtService) {
      this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
      String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
      if (auth != null && auth.startsWith("Bearer ")) {
        String jwt = auth.substring(7);
        try {
          Long utilisateurId = jwtService.lireUtilisateurId(jwt);
          Set<String> permissions = jwtService.lirePermissions(jwt);
          List<GrantedAuthority> authorities = permissions.stream()
              .map(p -> (GrantedAuthority) new SimpleGrantedAuthority("PERM_" + p))
              .toList();

          var authentication = new UsernamePasswordAuthenticationToken(utilisateurId, null, authorities);
          SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
        }
      }
      filterChain.doFilter(request, response);
    }
  }
}

