package com.fof.reservation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class AeroportInfo {

  @Column(nullable = false, length = 120)
  private String ville;

  @Column(nullable = false, length = 120)
  private String aeroport;

  @Column(length = 20)
  private String terminal;

  @Column(nullable = false)
  private LocalDateTime dateHeure;
}
