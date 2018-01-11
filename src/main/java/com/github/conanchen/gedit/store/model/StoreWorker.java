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
public class StoreWorker {
    /**
     * 用户id
     */
    @Id
    @Column(columnDefinition = "char(32)")
    private String workerId;

    @Column(columnDefinition = "tinyint(1)")
    private Boolean active;

    @Column(columnDefinition = "char(32)")
    private String storeId;

    @Column(columnDefinition = "datetime")
    private Date createdDate;

    @Column(columnDefinition = "datetime")
    private Date updatedDate;
}
