package com.github.conanchen.gedit.store.model;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class StoreProfile {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(
            name = "uuid",
            strategy = "com.github.conanchen.gedit.store.utils.database.CustomUUIDGenerator"
    )
    @Column(columnDefinition = "char(32)")
    private String uuid;

    @Column(columnDefinition = "varchar(64)")
    @NonNull
    private String name;

    @Column(columnDefinition = "char(32)")
    private String ownerUuid;

    @Column(columnDefinition = "varchar(32)")
    private String introducerUuid;

    @Column(columnDefinition = "varchar(255)")
    private String logo;

    @Column(columnDefinition = "decimal(10,6)")
    private Double lat;

    @Column(columnDefinition = "decimal(10,6)")
    private Double lon;

    @Column(columnDefinition = "varchar(255)")
    private String descr;

    //地区id
    @Column(columnDefinition = "varchar(6)")
    private String districtUuid;

    @Column(columnDefinition = "varchar(512)")
    private String detailAddress;

    @Column(columnDefinition = "varchar(16)")
    private String type;

    /**
     * 赠送积分比例
     */
    @Column(columnDefinition = "decimal(2,2)")
    private Double pointsRate;

    @Column(columnDefinition = "varchar(4096)")
    private String photos;

    @Column(columnDefinition = "varchar(64)")
    private String tels;


    @Column(columnDefinition = "tinyint(1)")
    private Boolean active;

    @Column(columnDefinition = "varchar(64)")
    private String keyword;

    @Column(columnDefinition = "varchar(16)")
    private String amapAdCode;

    @Column(columnDefinition = "varchar(64)")
    private String amapAoiName;

    @Column(columnDefinition = "varchar(64)")
    private String amapBuildingId;

    @Column(columnDefinition = "varchar(64)")
    private String amapStreet;

    @Column(columnDefinition = "varchar(64)")
    private String amapStreetNum;

    @Column(columnDefinition = "varchar(64)")
    private String amapDistrict;

    @Column(columnDefinition = "varchar(64)")
    private String amapCityCode;

    @Column(columnDefinition = "varchar(64)")
    private String amapCity;

    @Column(columnDefinition = "varchar(64)")
    private String amapProvince;

    @Column(columnDefinition = "varchar(64)")
    private String amapCountry;

    @Column(columnDefinition = "datetime")
    private Date createdDate;

    @Column(columnDefinition = "datetime")
    private Date updatedDate;


}