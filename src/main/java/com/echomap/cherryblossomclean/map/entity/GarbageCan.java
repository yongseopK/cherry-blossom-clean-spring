package com.echomap.cherryblossomclean.map.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table
@Builder
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GarbageCan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String district;
    private String loadName;
    private String location;
    private String point;
    private String type;
    private double latitude;
    private double longitude;
}
