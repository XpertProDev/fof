package com.fof.shared.log;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JournalRequetesFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger("HTTP");

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    long debut = System.currentTimeMillis();
    String methode = request.getMethod();
    String uri = request.getRequestURI();

    try {
      filterChain.doFilter(request, response);
    } finally {
      int statut = response.getStatus();
      long duree = System.currentTimeMillis() - debut;
      LOGGER.info("{} {} -> {} ({} ms)", methode, uri, statut, duree);
    }
  }
}

