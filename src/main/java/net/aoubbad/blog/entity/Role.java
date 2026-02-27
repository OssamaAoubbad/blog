package net.aoubbad.blog.entity;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.aoubbad.blog.entity.enums.RoleName;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString(exclude = "users")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    @EqualsAndHashCode.Include
    private RoleName name;


    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();


    public Role() {}

    public Role(RoleName name) {
        this.name = name;
    }


}
