package com.github.conanchen.gedit.store.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
public class StoreMember {
    @Id
    @Column(columnDefinition = "char(32)")
    private String memberId;

    @Column(columnDefinition = "tinyint(1)")
    private Boolean active;
    @ManyToOne
    @JoinColumn(name = "store_profile_id")
    private StoreProfile storeProfile;

    @Column(columnDefinition = "varchar(16)")
    private String memberType;

    @Column(columnDefinition = "datetime")
    private Date createDate;

    @Column(columnDefinition = "datetime")
    private Date updateDate;
}
