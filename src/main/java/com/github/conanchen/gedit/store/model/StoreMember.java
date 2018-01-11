package com.github.conanchen.gedit.store.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class StoreMember {
    /**
     * 店铺/商户id
     */
    @Id
    @Column(columnDefinition = "char(32)")
    private String memberId;

    @Column(columnDefinition = "tinyint(1)")
    private Boolean active;

    @Column(columnDefinition = "char(32)")
    private String storeId;

    @Column(columnDefinition = "varchar(16)")
    private String memberType;

    @Column(columnDefinition = "datetime")
    private Date createdDate;

    @Column(columnDefinition = "datetime")
    private Date updatedDate;
}
