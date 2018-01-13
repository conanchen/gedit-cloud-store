package com.github.conanchen.gedit.store.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class StoreMember {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @Column(columnDefinition = "char(32)")
    private String uuid;


    @Column(columnDefinition = "char(32)")
    private String memberId;

    @Column(columnDefinition = "char(32)")
    private String storeId;

    @Column(columnDefinition = "varchar(16)")
    private String memberType;


    @Column(columnDefinition = "tinyint(1)")
    private Boolean active;

    @Column(columnDefinition = "datetime")
    private Date createdDate;

    @Column(columnDefinition = "datetime")
    private Date updatedDate;
}
