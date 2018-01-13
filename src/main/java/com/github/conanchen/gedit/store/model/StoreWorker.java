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
public class StoreWorker {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @Column(columnDefinition = "char(32)")
    private String uuid;


    @Column(columnDefinition = "char(32)")
    private String workerId;

    @Column(columnDefinition = "char(32)")
    private String storeId;

    @Column(columnDefinition = "tinyint(1)")
    private Boolean active;

    @Column(columnDefinition = "datetime")
    private Date createdDate;

    @Column(columnDefinition = "datetime")
    private Date updatedDate;
}
