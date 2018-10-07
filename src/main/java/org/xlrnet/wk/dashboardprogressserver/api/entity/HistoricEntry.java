package org.xlrnet.wk.dashboardprogressserver.api.entity;

import com.j256.simplecsv.common.CsvColumn;
import lombok.Data;
import org.xlrnet.wk.dashboardprogressserver.common.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name = "historic_progress")
public class HistoricEntry extends AbstractEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(name = "request_time")
    @CsvColumn(columnName = "EpochSeconds")
    private long epochSeconds;

    @Column(name = "initiate")
    @CsvColumn(columnName = "Initiate")
    private int initiate;

    @Column(name = "apprentice1")
    @CsvColumn(columnName = "Apprentice1")
    private int apprentice1;

    @Column(name = "apprentice2")
    @CsvColumn(columnName = "Apprentice2")
    private int apprentice2;

    @Column(name = "apprentice3")
    @CsvColumn(columnName = "Apprentice3")
    private int apprentice3;

    @Column(name = "apprentice4")
    @CsvColumn(columnName = "Apprentice4")
    private int apprentice4;

    @Column(name = "guru1")
    @CsvColumn(columnName = "Guru1")
    private int guru1;

    @Column(name = "guru2")
    @CsvColumn(columnName = "Guru2")
    private int guru2;

    @Column(name = "master")
    @CsvColumn(columnName = "Master")
    private int master;

    @Column(name = "enlightened")
    @CsvColumn(columnName = "Enlightened")
    private int enlightened;

    @Column(name = "burned")
    @CsvColumn(columnName = "Burned")
    private int burned;

}
